#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

CCD_URL="${CCD_DATA_STORE_URL:-http://localhost:4452}"
CDAM_URL="${CASE_DOCUMENT_AM_URL:-http://localhost:4455}"
CASE_TYPE_ID="${CASE_TYPE_ID:-TEC_BATCH}"
JURISDICTION_ID="${JURISDICTION_ID:-TEC}"
CLASSIFICATION="${DOCUMENT_CLASSIFICATION:-PUBLIC}"
OUTPUTS_CATEGORY_ID="outputs"

CASE_REFERENCE_RAW="${1:-}"
FILE_ONE="${2:-}"
FILE_TWO="${3:-}"

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

usage() {
  cat <<EOF
Usage: ${0} <batch-case-reference> <output-file-1> <output-file-2>

Complete processing for a TEC batch case:
  - verify the case is a batch (TEC_BATCH)
  - attach the two files as Outputs (shown on Batch details)
  - set Batch validation result to
      "<valid> PCNs valid, <removed> PCNs removed, see exception report"
    where <removed> is a random 5–20% of the batch's Number of PCNs in batch, and
    <valid> is the remainder
  - move the case to PROCESSING_COMPLETE

Optional environment variables:
  CCD_DATA_STORE_URL, CASE_DOCUMENT_AM_URL, DOCUMENT_CLASSIFICATION, SEED
EOF
}

if [[ -z "${CASE_REFERENCE_RAW}" || -z "${FILE_ONE}" || -z "${FILE_TWO}" ]]; then
  usage >&2
  exit 1
fi

CASE_REFERENCE="$(printf '%s' "${CASE_REFERENCE_RAW}" | tr -d '-')"
if [[ ! "${CASE_REFERENCE}" =~ ^[0-9]+$ ]]; then
  echo "Case reference must contain digits (hyphens optional): '${CASE_REFERENCE_RAW}'" >&2
  exit 1
fi

for file_path in "${FILE_ONE}" "${FILE_TWO}"; do
  if [[ ! -f "${file_path}" ]]; then
    echo "File not found: ${file_path}" >&2
    exit 1
  fi
done

# Resolve to absolute paths so curl --form can open them reliably.
FILE_ONE="$(cd -- "$(dirname -- "${FILE_ONE}")" && pwd)/$(basename -- "${FILE_ONE}")"
FILE_TWO="$(cd -- "$(dirname -- "${FILE_TWO}")" && pwd)/$(basename -- "${FILE_TWO}")"

# Ensure the local dm-store stub is reachable (CDAM proxies uploads to :4506).
if ! curl --silent --fail --connect-timeout 1 "${DM_STORE_URL:-http://localhost:4506}/health" >/dev/null 2>&1; then
  echo "Local dm-store not reachable; starting ./bin/start-local-dm-store.sh..." >&2
  "${SCRIPT_DIR}/start-local-dm-store.sh"
fi

user_token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"
upload_service_token="$("${SCRIPT_DIR}/get-local-s2s-token.sh" xui_webapp)"
ccd_service_token="$("${SCRIPT_DIR}/get-local-s2s-token.sh" tec_api)"

echo "Fetching batch case ${CASE_REFERENCE} from CCD..." >&2
case_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 60 \
    --request GET "${CCD_URL}/cases/${CASE_REFERENCE}" \
    --header "Authorization: Bearer ${user_token}" \
    --header "ServiceAuthorization: ${ccd_service_token}" \
    --header 'experimental: true'
} 2>&1)" || {
  echo "Failed to fetch case ${CASE_REFERENCE} from ${CCD_URL}" >&2
  echo "${case_response}" >&2
  exit 1
}

case_type="$(jq --raw-output '.case_type_id // .case_type // empty' <<<"${case_response}")"
if [[ "${case_type}" != "TEC_BATCH" ]]; then
  echo "Case ${CASE_REFERENCE} is not a batch case (case type: '${case_type:-unknown}', expected TEC_BATCH)" >&2
  exit 1
fi

pcn_count="$(jq --raw-output '.data.pcnCount // empty' <<<"${case_response}")"
if [[ -z "${pcn_count}" || ! "${pcn_count}" =~ ^[0-9]+$ || "${pcn_count}" -lt 1 ]]; then
  echo "Batch case ${CASE_REFERENCE} has no usable Number of PCNs in batch (pcnCount)" >&2
  echo "${case_response}" | jq '.data' >&2 || true
  exit 1
fi

current_state="$(jq --raw-output '.state // empty' <<<"${case_response}")"
if [[ -z "${current_state}" ]]; then
  echo "CCD response for case ${CASE_REFERENCE} did not contain state" >&2
  echo "${case_response}" >&2
  exit 1
fi

if [[ -n "${SEED:-}" ]]; then
  RANDOM="${SEED}"
fi

removed_percent=$((5 + RANDOM % 16)) # 5–20 inclusive
removed=$((pcn_count * removed_percent / 100))
if (( removed < 1 )); then
  removed=1
fi
if (( removed >= pcn_count )); then
  removed=$((pcn_count - 1))
fi
if (( removed < 1 )); then
  removed=0
fi
valid=$((pcn_count - removed))
validation_display="${valid} PCNs valid, ${removed} PCNs removed, see exception report"

echo "Batch has ${pcn_count} PCNs → validation result: ${validation_display}" >&2

to_cdam_document_url() {
  local url="$1"
  local document_id

  if [[ "${url}" == *"/cases/documents/"* ]]; then
    printf '%s\n' "${url}"
    return 0
  fi

  document_id="$(sed -E 's|.*/documents/([0-9a-fA-F-]{36}).*|\1|' <<<"${url}")"
  if [[ -z "${document_id}" || "${document_id}" == "${url}" ]]; then
    echo "Unable to derive Case Document AM URL from: ${url}" >&2
    exit 1
  fi

  if [[ "${url}" == *"/binary" ]]; then
    printf '%s\n' "${CDAM_URL}/cases/documents/${document_id}/binary"
  else
    printf '%s\n' "${CDAM_URL}/cases/documents/${document_id}"
  fi
}

upload_and_attach_output() {
  local file_path="$1"
  local filename
  filename="$(basename -- "${file_path}")"

  echo "Uploading ${filename} to Case Document AM..." >&2
  local upload_response
  upload_response="$({
    curl --silent --show-error --fail-with-body \
      --connect-timeout 5 \
      --max-time 120 \
      --request POST "${CDAM_URL}/cases/documents" \
      --header "Authorization: Bearer ${user_token}" \
      --header "ServiceAuthorization: ${upload_service_token}" \
      --form "classification=${CLASSIFICATION}" \
      --form "caseTypeId=${CASE_TYPE_ID}" \
      --form "jurisdictionId=${JURISDICTION_ID}" \
      --form "files=@${file_path}"
  } 2>&1)" || {
    echo "Failed to upload document to ${CDAM_URL}/cases/documents" >&2
    echo "${upload_response}" >&2
    exit 1
  }

  local document_url document_binary_url document_hash uploaded_filename
  document_url="$(jq --raw-output '.documents[0]._links.self.href // empty' <<<"${upload_response}")"
  document_binary_url="$(jq --raw-output '.documents[0]._links.binary.href // empty' <<<"${upload_response}")"
  document_hash="$(jq --raw-output '.documents[0].hashToken // empty' <<<"${upload_response}")"
  uploaded_filename="$(jq --raw-output '.documents[0].originalDocumentName // empty' <<<"${upload_response}")"

  if [[ -z "${document_url}" || -z "${document_binary_url}" || -z "${document_hash}" ]]; then
    echo "Unexpected Case Document AM response; expected documents[0] with links and hashToken" >&2
    echo "${upload_response}" >&2
    exit 1
  fi

  document_url="$(to_cdam_document_url "${document_url}")"
  document_binary_url="$(to_cdam_document_url "${document_binary_url}")"
  if [[ -n "${uploaded_filename}" ]]; then
    filename="${uploaded_filename}"
  fi

  echo "Attaching ${filename} to Outputs via attachBatchDocument..." >&2
  submit_ccd_event "attachBatchDocument" "Attach batch document" "$(jq --null-input --compact-output \
    --arg documentUrl "${document_url}" \
    --arg documentBinaryUrl "${document_binary_url}" \
    --arg documentFilename "${filename}" \
    --arg documentHash "${document_hash}" \
    --arg categoryId "${OUTPUTS_CATEGORY_ID}" \
    '{
      batchFileDocument: {
        document_url: $documentUrl,
        document_binary_url: $documentBinaryUrl,
        document_filename: $documentFilename,
        document_hash: $documentHash,
        category_id: $categoryId
      }
    }')" >/dev/null
}

submit_ccd_event() {
  local event_id="$1"
  local summary="$2"
  # Avoid ${3:-{}} — bash treats the first } as end of expansion and appends a stray }.
  local data_json="{}"
  if [[ -n "${3:-}" ]]; then
    data_json="$3"
  fi

  local event_trigger_url start_response event_token submit_body submit_response
  event_trigger_url="${CCD_URL}/cases/${CASE_REFERENCE}/event-triggers/${event_id}"

  start_response="$({
    curl --silent --show-error --fail-with-body \
      --connect-timeout 5 \
      --max-time 120 \
      --request GET "${event_trigger_url}" \
      --header "Authorization: Bearer ${user_token}" \
      --header "ServiceAuthorization: ${ccd_service_token}" \
      --header 'experimental: true'
  } 2>&1)" || {
    echo "Failed to start ${event_id} for case ${CASE_REFERENCE}" >&2
    echo "${start_response}" >&2
    exit 1
  }

  event_token="$(jq --raw-output '.token // empty' <<<"${start_response}")"
  if [[ -z "${event_token}" ]]; then
    echo "CCD start-event response for ${event_id} did not contain a token" >&2
    echo "${start_response}" >&2
    exit 1
  fi

  submit_body="$(jq --null-input --compact-output \
    --arg eventId "${event_id}" \
    --arg eventToken "${event_token}" \
    --arg summary "${summary}" \
    --arg data "${data_json}" \
    '{
      event: {
        id: $eventId,
        summary: $summary,
        description: $summary
      },
      data: ($data | fromjson),
      event_token: $eventToken
    }')"

  submit_response="$({
    curl --silent --show-error --fail-with-body \
      --connect-timeout 5 \
      --max-time 120 \
      --request POST "${CCD_URL}/cases/${CASE_REFERENCE}/events" \
      --header "Authorization: Bearer ${user_token}" \
      --header "ServiceAuthorization: ${ccd_service_token}" \
      --header 'Content-Type: application/json' \
      --header 'experimental: true' \
      --data "${submit_body}"
  } 2>&1)" || {
    echo "Failed to submit ${event_id} for case ${CASE_REFERENCE}" >&2
    echo "${submit_response}" >&2
    exit 1
  }

  printf '%s\n' "${submit_response}"
}

upload_and_attach_output "${FILE_ONE}"
upload_and_attach_output "${FILE_TWO}"

if [[ "${current_state}" == "QUEUED_FOR_PROCESSING" ]]; then
  echo "Starting batch processing (QUEUED_FOR_PROCESSING → PROCESSING_STARTED)..." >&2
  submit_ccd_event "startBatchProcessing" "Batch processing started" '{}' >/dev/null
  current_state="PROCESSING_STARTED"
fi

if [[ "${current_state}" == "PROCESSING_STARTED" || "${current_state}" == "PROCESSING_COMPLETE" ]]; then
  echo "Completing batch processing and recording validation result..." >&2
  complete_response="$(submit_ccd_event "completeBatchProcessing" "Batch processing complete" \
    "$(jq --null-input --compact-output \
      --arg validationDisplay "${validation_display}" \
      '{ batchValidationResultDisplay: $validationDisplay }')")"
else
  echo "Unexpected batch state '${current_state}'; expected QUEUED_FOR_PROCESSING, PROCESSING_STARTED, or PROCESSING_COMPLETE" >&2
  exit 1
fi

echo "${complete_response}" | jq .

final_state="$(jq --raw-output '.state // empty' <<<"${complete_response}")"
echo >&2
echo "Batch ${CASE_REFERENCE} is now ${final_state:-PROCESSING_COMPLETE}." >&2
echo "Validation result: ${validation_display}" >&2
echo "Attached outputs: $(basename -- "${FILE_ONE}"), $(basename -- "${FILE_TWO}")" >&2

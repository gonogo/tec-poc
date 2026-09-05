#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TEC_API_URL="${TEC_API_URL:-http://localhost:4013}"

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

batch_seed=$(($(date +%s) ^ $$ ^ RANDOM))
batch_number="$(printf '%06d' "$(((batch_seed * 37) % 1000000))")"
authority_code="${AUTHORITY_CODE:-AB}"
batch_identifier="${BATCH_IDENTIFIER:-R${authority_code}${batch_number}}"
pcn_count="${PCN_COUNT:-$((200 + batch_seed % 1801))}"
operation="${OPERATION:-registration}"
received_via="${RECEIVED_VIA:-upload}"
local_authority="${LOCAL_AUTHORITY:-westminster}"
target_state="${TARGET_STATE:-QUEUED_FOR_PROCESSING}"
received_at="${RECEIVED_AT:-$(date -u +"%Y-%m-%dT%H:%M:%S")}"

documents_json='[]'
if [[ "${target_state}" == "PROCESSING_COMPLETE" || "${ATTACH_SAMPLE_DOCUMENTS:-}" == "true" ]]; then
  # CCD Document fields require a valid UUID in the URL path.
  new_uuid() {
    if command -v uuidgen >/dev/null 2>&1; then
      uuidgen | tr '[:upper:]' '[:lower:]'
    else
      python3 -c 'import uuid; print(uuid.uuid4())'
    fi
  }
  input_doc_id="$(new_uuid)"
  exception_doc_id="$(new_uuid)"
  certificate_doc_id="$(new_uuid)"
  cdam_base="${CASE_DOCUMENT_AM_URL:-http://localhost:4455}"
  documents_json="$(jq --null-input --compact-output \
    --arg cdamBase "${cdam_base}" \
    --arg inputDocId "${input_doc_id}" \
    --arg exceptionDocId "${exception_doc_id}" \
    --arg certificateDocId "${certificate_doc_id}" \
    --arg operation "${operation}" \
    '[
      {
        categoryId: "inputs",
        documentUrl: ($cdamBase + "/cases/documents/" + $inputDocId),
        documentBinaryUrl: ($cdamBase + "/cases/documents/" + $inputDocId + "/binary"),
        filename: ($operation + "-batch-input.csv")
      },
      {
        categoryId: "outputs",
        documentUrl: ($cdamBase + "/cases/documents/" + $exceptionDocId),
        documentBinaryUrl: ($cdamBase + "/cases/documents/" + $exceptionDocId + "/binary"),
        filename: ($operation + "-exception-report.csv")
      },
      {
        categoryId: "outputs",
        documentUrl: ($cdamBase + "/cases/documents/" + $certificateDocId),
        documentBinaryUrl: ($cdamBase + "/cases/documents/" + $certificateDocId + "/binary"),
        filename: ($operation + "-certificate.pdf")
      }
    ]')"
fi

batch_data="$(jq --null-input --compact-output \
  --arg batchIdentifier "${batch_identifier}" \
  --argjson pcnCount "${pcn_count}" \
  --arg operation "${operation}" \
  --arg receivedVia "${received_via}" \
  --arg receivedAt "${received_at}" \
  --arg localAuthority "${local_authority}" \
  --arg targetState "${target_state}" \
  --argjson documents "${documents_json}" \
  '{
    batchIdentifier: $batchIdentifier,
    pcnCount: $pcnCount,
    operation: $operation,
    receivedVia: $receivedVia,
    receivedAt: $receivedAt,
    localAuthority: $localAuthority,
    batchValidationResult: "batchValid",
    targetState: $targetState,
    documents: $documents
  }')"

token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"

response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${TEC_API_URL}/batches" \
    --header "Authorization: Bearer ${token}" \
    --header 'Content-Type: application/json' \
    --data "${batch_data}"
} 2>&1)" || {
  echo "Failed to create a TEC batch through ${TEC_API_URL}/batches" >&2
  echo "${response}" >&2
  exit 1
}

jq . <<<"${response}"

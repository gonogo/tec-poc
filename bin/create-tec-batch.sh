#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TEC_API_URL="${TEC_API_URL:-http://localhost:4013}"

# Batch type FixedList codes (BatchOperation JSON names).
BATCH_TYPES=(
  registration
  warrantAuthRequests
  warrantReissueRequests
  outOfTimeDecisions
  changeOfAddress
  caseClosureRequests
  transferRequest
)

usage() {
  cat <<EOF
Usage: ${0} <local-authority> [batch-type]
       ${0} -h|--help

Create a TEC Batch case through the local API (${TEC_API_URL}/batches).

Arguments:
  <local-authority>  FixedList code for the submitting local authority.
                     Examples: westminster, manchesterCityCouncil
  [batch-type]       FixedList code for the batch type / operation
                     (default: registration, or OPERATION if set).
                     One of: ${BATCH_TYPES[*]}
  -h, --help         Show this help and exit

If LOCAL_AUTHORITY is set in the environment and no argument is passed,
that value is used as the local authority.

Optional environment variables:
  TEC_API_URL, LOCAL_AUTHORITY, AUTHORITY_CODE, FILE_IDENTIFIER,
  BATCH_IDENTIFIER, PCN_COUNT, OPERATION, RECEIVED_VIA, RECEIVED_AT,
  SUBMITTER_EMAIL, TARGET_STATE, ATTACH_SAMPLE_DOCUMENTS, CASE_DOCUMENT_AM_URL

Examples:
  ${0} westminster
  ${0} westminster warrantAuthRequests
  ${0} manchesterCityCouncil registration
  LOCAL_AUTHORITY=manchesterCityCouncil ${0}
  OPERATION=changeOfAddress ${0} westminster
EOF
}

is_valid_batch_type() {
  local candidate="$1"
  local known
  for known in "${BATCH_TYPES[@]}"; do
    if [[ "${candidate}" == "${known}" ]]; then
      return 0
    fi
  done
  return 1
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

# Require at least one positional arg, unless LOCAL_AUTHORITY is set (mirrors create-tec-case.sh).
if [[ $# -eq 0 ]]; then
  if [[ -n "${LOCAL_AUTHORITY:-}" ]]; then
    local_authority="${LOCAL_AUTHORITY}"
  else
    usage >&2
    exit 1
  fi
else
  local_authority="${1}"
fi

# Positional batch-type wins over OPERATION env; default registration.
operation="${2:-${OPERATION:-registration}}"
if ! is_valid_batch_type "${operation}"; then
  echo "Unknown batch type: ${operation}" >&2
  echo "Expected one of: ${BATCH_TYPES[*]}" >&2
  exit 1
fi

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

batch_seed=$(($(date +%s) ^ $$ ^ RANDOM))
batch_number="$(printf '%06d' "$(((batch_seed * 37) % 1000000))")"
file_number="$(printf '%05d' "$(((batch_seed * 37) % 100000))")"
authority_code="${AUTHORITY_CODE:-AB}"
batch_identifier="${BATCH_IDENTIFIER:-R${authority_code}${batch_number}}"
file_identifier="${FILE_IDENTIFIER:-R${authority_code}${file_number}}"
pcn_count="${PCN_COUNT:-$((200 + batch_seed % 1801))}"
received_via="${RECEIVED_VIA:-upload}"
target_state="${TARGET_STATE:-QUEUED_FOR_PROCESSING}"
received_at="${RECEIVED_AT:-$(date -u +"%Y-%m-%dT%H:%M:%S")}"
submitter_email="${SUBMITTER_EMAIL:-la.submitter@example.com}"

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
  --arg fileIdentifier "${file_identifier}" \
  --arg batchIdentifier "${batch_identifier}" \
  --argjson pcnCount "${pcn_count}" \
  --arg operation "${operation}" \
  --arg receivedVia "${received_via}" \
  --arg receivedAt "${received_at}" \
  --arg localAuthority "${local_authority}" \
  --arg submitterEmail "${submitter_email}" \
  --arg targetState "${target_state}" \
  --argjson documents "${documents_json}" \
  '{
    fileIdentifier: $fileIdentifier,
    batchIdentifier: $batchIdentifier,
    pcnCount: $pcnCount,
    operation: $operation,
    receivedVia: $receivedVia,
    receivedAt: $receivedAt,
    localAuthority: $localAuthority,
    submitterEmail: $submitterEmail,
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

#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

# Batch type FixedList codes (BatchOperation JSON names) — keep in sync with create-tec-batch.sh.
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
Usage: ${0} <count> [batch-type]
       ${0} -h|--help

Create multiple TEC Batch cases by calling create-tec-batch.sh repeatedly.

Arguments:
  <count>       Number of batches to create (positive integer).
  [batch-type]  FixedList code applied to every batch. If omitted, batch types
                rotate through the sample list (or OPERATION if set).
                One of: ${BATCH_TYPES[*]}
  -h, --help    Show this help and exit

Optional environment variables (passed through to create-tec-batch.sh):
  LOCAL_AUTHORITY  If set, every batch uses this authority; otherwise authorities rotate.
  OPERATION        Fixed batch type when [batch-type] is omitted (skips rotation).
  TEC_API_URL, AUTHORITY_CODE, FILE_IDENTIFIER, BATCH_IDENTIFIER, PCN_COUNT,
  RECEIVED_VIA, RECEIVED_AT, SUBMITTER_EMAIL, TARGET_STATE,
  ATTACH_SAMPLE_DOCUMENTS, CASE_DOCUMENT_AM_URL

Examples:
  ${0} 6
  ${0} 3 registration
  ${0} 4 warrantAuthRequests
  LOCAL_AUTHORITY=manchesterCityCouncil ${0} 2
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

if [[ $# -eq 0 ]]; then
  usage >&2
  exit 1
fi

COUNT="${1}"
if ! [[ "${COUNT}" =~ ^[0-9]+$ ]] || [[ "${COUNT}" -lt 1 ]]; then
  echo "Invalid count: ${COUNT} (expected a positive integer)" >&2
  usage >&2
  exit 1
fi

FIXED_BATCH_TYPE="${2:-}"
if [[ -n "${FIXED_BATCH_TYPE}" ]] && ! is_valid_batch_type "${FIXED_BATCH_TYPE}"; then
  echo "Unknown batch type: ${FIXED_BATCH_TYPE}" >&2
  echo "Expected one of: ${BATCH_TYPES[*]}" >&2
  exit 1
fi

AUTHORITY_CODES=(TE AB WM LE BR CK)
OPERATIONS=(registration warrantAuthRequests warrantReissueRequests outOfTimeDecisions changeOfAddress caseClosureRequests transferRequest)
RECEIVED_VIA=(email upload)
TARGET_STATES=(QUEUED_FOR_PROCESSING PROCESSING_STARTED PROCESSING_COMPLETE PROCESSING_FAILED)
LOCAL_AUTHORITIES=(westminster manchesterCityCouncil birminghamCityCouncil leedsCityCouncil bristolCityCouncil)

for ((index = 0; index < COUNT; index++)); do
  export AUTHORITY_CODE="${AUTHORITY_CODES[$((index % ${#AUTHORITY_CODES[@]}))]}"
  if [[ -n "${FIXED_BATCH_TYPE}" ]]; then
    operation="${FIXED_BATCH_TYPE}"
  elif [[ -n "${OPERATION:-}" ]]; then
    operation="${OPERATION}"
  else
    operation="${OPERATIONS[$((index % ${#OPERATIONS[@]}))]}"
  fi
  export RECEIVED_VIA="${RECEIVED_VIA[$((index % ${#RECEIVED_VIA[@]}))]}"
  export TARGET_STATE="${TARGET_STATES[$((index % ${#TARGET_STATES[@]}))]}"
  export PCN_COUNT="$((200 + (index * 173) % 1801))"
  # Unique per run (fixed index math collided on re-runs and caused tec_batch_identifier_uk errors).
  export BATCH_IDENTIFIER="R${AUTHORITY_CODE}$(printf '%06d' "$((($(date +%s) + RANDOM + index * 97) % 1000000))")"

  # Prefer caller LOCAL_AUTHORITY; otherwise rotate through the sample list.
  local_authority="${LOCAL_AUTHORITY:-${LOCAL_AUTHORITIES[$((index % ${#LOCAL_AUTHORITIES[@]}))]}}"

  echo "Creating batch $((index + 1))/${COUNT}: ${BATCH_IDENTIFIER} (${TARGET_STATE}, ${operation}, ${local_authority})"
  "${SCRIPT_DIR}/create-tec-batch.sh" "${local_authority}" "${operation}"
done

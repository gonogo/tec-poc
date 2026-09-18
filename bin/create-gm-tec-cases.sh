#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CASES_PER_AUTHORITY="${CASES_PER_AUTHORITY:-10}"
# Optional: positional arg or BATCH_CASE_REFERENCE env — link each new PCN to a TEC_BATCH case.
if [[ -n "${1:-}" ]]; then
  export BATCH_CASE_REFERENCE="${1}"
fi

if ! [[ "${CASES_PER_AUTHORITY}" =~ ^[0-9]+$ ]] || [[ "${CASES_PER_AUTHORITY}" -lt 1 ]]; then
  echo "Usage: CASES_PER_AUTHORITY=n ${0} [batch-case-reference]" >&2
  echo "CASES_PER_AUTHORITY must be a positive integer (default: 10)." >&2
  echo "Optional batch-case-reference (or BATCH_CASE_REFERENCE) links each PCN to that TEC_BATCH case." >&2
  exit 1
fi

# Greater Manchester authorities — keep in sync with GreaterManchesterLocalAuthorities.java
LOCAL_AUTHORITIES=(
  boltonBoroughCouncil
  buryBoroughCouncil
  manchesterCityCouncil
  oldhamBoroughCouncil
  rochdaleBoroughCouncil
  salfordCityCouncil
  stockportBoroughCouncil
  tamesideBoroughCouncil
  traffordBoroughCouncil
  wiganBoroughCouncil
)

total=$((${#LOCAL_AUTHORITIES[@]} * CASES_PER_AUTHORITY))
created=0

echo "Creating ${CASES_PER_AUTHORITY} TEC cases for each of ${#LOCAL_AUTHORITIES[@]} Greater Manchester authorities (${total} total)..." >&2
if [[ -n "${BATCH_CASE_REFERENCE:-}" ]]; then
  echo "Each created PCN will be linked to batch case ${BATCH_CASE_REFERENCE}." >&2
fi

for local_authority in "${LOCAL_AUTHORITIES[@]}"; do
  for ((index = 1; index <= CASES_PER_AUTHORITY; index++)); do
    export LOCAL_AUTHORITY="${local_authority}"
    response="$("${SCRIPT_DIR}/create-tec-case.sh")" || {
      echo "Failed creating case ${index}/${CASES_PER_AUTHORITY} for ${local_authority}" >&2
      exit 1
    }
    created=$((created + 1))
    case_reference="$(jq --raw-output '.caseReference // empty' <<<"${response}" 2>/dev/null || true)"
    echo "Created ${created}/${total}: ${local_authority}${case_reference:+ (${case_reference})}" >&2
  done
done

echo "Created ${created} TEC cases across Greater Manchester authorities." >&2

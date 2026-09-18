#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CASES_PER_AUTHORITY="${CASES_PER_AUTHORITY:-10}"

usage() {
  cat <<EOF
Usage: ${0} <batch-case-reference|->
       ${0} -h|--help

Create ${CASES_PER_AUTHORITY} TEC PCN cases for each Greater Manchester local
authority (via create-tec-case.sh).

Arguments:
  <batch-case-reference>  Optional CCD case reference of a TEC_BATCH case to link
                          each PCN to after create (hyphens optional). Use '-' to
                          create without linking.
  -h, --help              Show this help and exit

If BATCH_CASE_REFERENCE is set in the environment and no argument is passed,
that value is used as the batch case reference.

Optional environment variables:
  CASES_PER_AUTHORITY (default: 10), BATCH_CASE_REFERENCE

Examples:
  ${0} -                                    # create unlinked PCNs
  CASES_PER_AUTHORITY=2 ${0} -              # 2 per authority, unlinked
  ${0} 1234-5678-9012-3456                  # create and link each to batch
  BATCH_CASE_REFERENCE=1234567890123456 ${0}
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ $# -eq 0 ]]; then
  if [[ -z "${BATCH_CASE_REFERENCE:-}" ]]; then
    usage >&2
    exit 1
  fi
elif [[ "${1}" == "-" ]]; then
  unset BATCH_CASE_REFERENCE
else
  export BATCH_CASE_REFERENCE="${1}"
fi

if ! [[ "${CASES_PER_AUTHORITY}" =~ ^[0-9]+$ ]] || [[ "${CASES_PER_AUTHORITY}" -lt 1 ]]; then
  echo "CASES_PER_AUTHORITY must be a positive integer (default: 10)." >&2
  usage >&2
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

# Pass '-' when unlinked so create-tec-case.sh does not treat no-args as help.
create_arg="${BATCH_CASE_REFERENCE:--}"

for local_authority in "${LOCAL_AUTHORITIES[@]}"; do
  for ((index = 1; index <= CASES_PER_AUTHORITY; index++)); do
    export LOCAL_AUTHORITY="${local_authority}"
    response="$("${SCRIPT_DIR}/create-tec-case.sh" "${create_arg}")" || {
      echo "Failed creating case ${index}/${CASES_PER_AUTHORITY} for ${local_authority}" >&2
      exit 1
    }
    created=$((created + 1))
    case_reference="$(jq --raw-output '.caseReference // empty' <<<"${response}" 2>/dev/null || true)"
    echo "Created ${created}/${total}: ${local_authority}${case_reference:+ (${case_reference})}" >&2
  done
done

echo "Created ${created} TEC cases across Greater Manchester authorities." >&2

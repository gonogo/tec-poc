#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

CCD_URL="${CCD_DATA_STORE_URL:-http://localhost:4452}"
CASE_REFERENCE_RAW="${1:-}"

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

usage() {
  cat <<EOF
Usage: ${0} <batch-case-reference>

Fetch a CCD case and verify it exists and is a TEC_BATCH case.
Hyphens in the case reference are optional.

Optional environment variables:
  CCD_DATA_STORE_URL
EOF
}

if [[ -z "${CASE_REFERENCE_RAW}" || "${CASE_REFERENCE_RAW}" == "-h" || "${CASE_REFERENCE_RAW}" == "--help" ]]; then
  usage >&2
  exit 1
fi

CASE_REFERENCE="$(printf '%s' "${CASE_REFERENCE_RAW}" | tr -d '-')"
if [[ ! "${CASE_REFERENCE}" =~ ^[0-9]+$ ]]; then
  echo "Case reference must contain digits (hyphens optional): '${CASE_REFERENCE_RAW}'" >&2
  exit 1
fi

user_token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"
service_token="$("${SCRIPT_DIR}/get-local-s2s-token.sh" tec_api)"

case_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 60 \
    --request GET "${CCD_URL}/cases/${CASE_REFERENCE}" \
    --header "Authorization: Bearer ${user_token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'experimental: true'
} 2>&1)" || {
  echo "Batch case ${CASE_REFERENCE} does not exist (or could not be fetched from ${CCD_URL})" >&2
  echo "${case_response}" >&2
  exit 1
}

case_type="$(jq --raw-output '.case_type_id // .case_type // empty' <<<"${case_response}")"
if [[ "${case_type}" != "TEC_BATCH" ]]; then
  echo "Case ${CASE_REFERENCE} is not a batch case (case type: '${case_type:-unknown}', expected TEC_BATCH)" >&2
  exit 1
fi

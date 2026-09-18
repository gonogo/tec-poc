#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TEC_API_URL="${TEC_API_URL:-http://localhost:4013}"

usage() {
  cat <<EOF
Usage: ${0} <batch-case-reference|->
       ${0} -h|--help

Create a TEC PCN case through the local API (${TEC_API_URL}/pcn-cases).

Arguments:
  <batch-case-reference>  Optional CCD case reference of a TEC_BATCH case to link
                          after create (hyphens optional). Use '-' to create
                          without linking.
  -h, --help              Show this help and exit

If BATCH_CASE_REFERENCE is set in the environment and no argument is passed,
that value is used as the batch case reference.

Optional environment variables:
  TEC_API_URL, BATCH_CASE_REFERENCE, AMOUNT_DUE, FILE_IDENTIFIER,
  BATCH_IDENTIFIER, PENALTY_CHARGE_NUMBER, LOCAL_AUTHORITY

Examples:
  ${0} -                                    # create unlinked PCN
  ${0} 1234-5678-9012-3456                  # create and link to batch
  BATCH_CASE_REFERENCE=1234567890123456 ${0}
  LOCAL_AUTHORITY=manchesterCityCouncil ${0} -
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

# Optional: positional arg or BATCH_CASE_REFERENCE env — link the new PCN to a TEC_BATCH case.
# Pass '-' to create without linking. With no args and no env, show help.
if [[ $# -eq 0 ]]; then
  if [[ -n "${BATCH_CASE_REFERENCE:-}" ]]; then
    BATCH_CASE_REFERENCE_RAW="${BATCH_CASE_REFERENCE}"
  else
    usage >&2
    exit 1
  fi
elif [[ "${1}" == "-" ]]; then
  BATCH_CASE_REFERENCE_RAW=""
else
  BATCH_CASE_REFERENCE_RAW="${1}"
fi

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

case_seed=$(($(date +%s) ^ $$ ^ RANDOM))
file_number="$(printf '%05d' "$((case_seed % 100000))")"
batch_number="$(printf '%06d' "$(((case_seed * 37) % 1000000))")"
pcn_number="$(printf '%07d' "$(((case_seed * 101) % 10000000))")"
authority_code="AB"
pcn_check_character="A"
pcn_registration_suffix="0"
default_penalty_charge_number="${authority_code}${pcn_number}${pcn_check_character}${pcn_registration_suffix}"

file_identifier="${FILE_IDENTIFIER:-R${authority_code}${file_number}}"
batch_identifier="${BATCH_IDENTIFIER:-R${authority_code}${batch_number}}"
penalty_charge_number="${PENALTY_CHARGE_NUMBER:-${default_penalty_charge_number}}"
amount_due="${AMOUNT_DUE:-12345}"

case_data="$(jq --null-input --compact-output \
  --arg fileIdentifier "${file_identifier}" \
  --arg batchIdentifier "${batch_identifier}" \
  --arg penaltyChargeNumber "${penalty_charge_number}" \
  --arg localAuthority "${LOCAL_AUTHORITY:-westminster}" \
  --argjson amountDue "${amount_due}" \
  '{
    fileIdentifier: $fileIdentifier,
    batchIdentifier: $batchIdentifier,
    penaltyChargeNumber: $penaltyChargeNumber,
    localAuthority: $localAuthority,
    respondentDetails1: "ALEX EXAMPLE",
    respondentDetails2: "1 EXAMPLE STREET",
    respondentDetails3: "LONDON",
    respondentDetails4: "SW1A 1AA",
    vehicleRegistrationNumber: "AB12CDE",
    natureOfOffence: "01",
    dateChargeCertificateServed: "260824",
    amountDue: $amountDue
  }')"

token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"

response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${TEC_API_URL}/pcn-cases" \
    --header "Authorization: Bearer ${token}" \
    --header 'Content-Type: application/json' \
    --data "${case_data}"
} 2>&1)" || {
  echo "Failed to create a TEC case through ${TEC_API_URL}/pcn-cases" >&2
  echo "${response}" >&2
  exit 1
}

jq . <<<"${response}"

if [[ -n "${BATCH_CASE_REFERENCE_RAW}" ]]; then
  case_reference="$(jq --raw-output '.caseReference // empty' <<<"${response}")"
  if [[ -z "${case_reference}" ]]; then
    echo "Created case response did not include caseReference; cannot link to batch" >&2
    exit 1
  fi
  echo "Linking PCN case ${case_reference} to batch case ${BATCH_CASE_REFERENCE_RAW}..." >&2
  "${SCRIPT_DIR}/link-pcn-to-batch.sh" "${case_reference}" "${BATCH_CASE_REFERENCE_RAW}" >/dev/null
fi

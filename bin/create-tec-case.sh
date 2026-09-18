#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TEC_API_URL="${TEC_API_URL:-http://localhost:4013}"
# Optional: positional arg or BATCH_CASE_REFERENCE env — link the new PCN to a TEC_BATCH case.
BATCH_CASE_REFERENCE_RAW="${1:-${BATCH_CASE_REFERENCE:-}}"

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

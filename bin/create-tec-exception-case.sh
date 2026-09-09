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

case_seed=$(($(date +%s) ^ $$ ^ RANDOM))
pcn_number="$(printf '%07d' "$(((case_seed * 101) % 10000000))")"
authority_code="AB"
pcn_check_character="A"
pcn_registration_suffix="0"
default_penalty_charge_number="${authority_code}${pcn_number}${pcn_check_character}${pcn_registration_suffix}"

penalty_charge_number="${PENALTY_CHARGE_NUMBER:-${default_penalty_charge_number}}"

case_data="$(jq --null-input --compact-output \
  --arg penaltyChargeNumber "${penalty_charge_number}" \
  '{
    penaltyChargeNumber: $penaltyChargeNumber
  }')"

token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"

response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${TEC_API_URL}/exception-cases" \
    --header "Authorization: Bearer ${token}" \
    --header 'Content-Type: application/json' \
    --data "${case_data}"
} 2>&1)" || {
  echo "Failed to create a TEC exception case through ${TEC_API_URL}/exception-cases" >&2
  echo "${response}" >&2
  exit 1
}

jq . <<<"${response}"

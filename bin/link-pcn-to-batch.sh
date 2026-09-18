#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

CCD_URL="${CCD_DATA_STORE_URL:-http://localhost:4452}"
EVENT_ID="${EVENT_ID:-linkBatchCase}"
PCN_CASE_REFERENCE_RAW="${1:-}"
BATCH_CASE_REFERENCE_RAW="${2:-}"

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

usage() {
  cat <<EOF
Usage: ${0} <pcn-case-reference> <batch-case-reference>

Link a TEC PCN case to a TEC Batch case via the CCD CaseLink field (batchCase).

Hyphens in either case reference are optional.

Optional environment variables:
  CCD_DATA_STORE_URL, EVENT_ID
EOF
}

if [[ -z "${PCN_CASE_REFERENCE_RAW}" || -z "${BATCH_CASE_REFERENCE_RAW}" ]]; then
  usage >&2
  exit 1
fi

normalise_case_reference() {
  local raw="$1"
  local digits
  digits="$(printf '%s' "${raw}" | tr -d '-')"
  if [[ ! "${digits}" =~ ^[0-9]+$ ]]; then
    echo "Case reference must contain digits (hyphens optional): '${raw}'" >&2
    exit 1
  fi
  printf '%s\n' "${digits}"
}

PCN_CASE_REFERENCE="$(normalise_case_reference "${PCN_CASE_REFERENCE_RAW}")"
BATCH_CASE_REFERENCE="$(normalise_case_reference "${BATCH_CASE_REFERENCE_RAW}")"

user_token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"
service_token="$("${SCRIPT_DIR}/get-local-s2s-token.sh" tec_api)"

echo "Linking PCN case ${PCN_CASE_REFERENCE} to batch case ${BATCH_CASE_REFERENCE}..." >&2

event_trigger_url="${CCD_URL}/cases/${PCN_CASE_REFERENCE}/event-triggers/${EVENT_ID}"

start_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request GET "${event_trigger_url}" \
    --header "Authorization: Bearer ${user_token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'experimental: true'
} 2>&1)" || {
  echo "Failed to start ${EVENT_ID} for case ${PCN_CASE_REFERENCE}" >&2
  echo "${start_response}" >&2
  exit 1
}

event_token="$(jq --raw-output '.token // empty' <<<"${start_response}")"
if [[ -z "${event_token}" ]]; then
  echo "CCD start-event response did not contain a token" >&2
  echo "${start_response}" >&2
  exit 1
fi

submit_body="$(jq --null-input --compact-output \
  --arg eventId "${EVENT_ID}" \
  --arg eventToken "${event_token}" \
  --arg batchCaseReference "${BATCH_CASE_REFERENCE}" \
  '{
    event: {
      id: $eventId,
      summary: "Link batch case",
      description: "Link PCN case to batch case"
    },
    data: {
      batchCase: {
        CaseReference: $batchCaseReference,
        CaseType: "TEC_BATCH"
      }
    },
    event_token: $eventToken
  }')"

submit_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${CCD_URL}/cases/${PCN_CASE_REFERENCE}/events" \
    --header "Authorization: Bearer ${user_token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'Content-Type: application/json' \
    --header 'experimental: true' \
    --data "${submit_body}"
} 2>&1)" || {
  echo "Failed to submit ${EVENT_ID} for case ${PCN_CASE_REFERENCE}" >&2
  echo "${submit_response}" >&2
  exit 1
}

jq . <<<"${submit_response}"

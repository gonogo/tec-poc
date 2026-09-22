#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

CCD_URL="${CCD_DATA_STORE_URL:-http://localhost:4452}"
EVENT_ID="${EVENT_ID:-applyWarrantAuthorisation}"
CASE_REFERENCE_RAW="${1:-}"

DATE_OF_ISSUE="${DATE_OF_ISSUE:-$(date +%Y-%m-%d)}"
DATE_OF_EXPIRY="${DATE_OF_EXPIRY:-$(date -v+1y +%Y-%m-%d 2>/dev/null || date -d '+1 year' +%Y-%m-%d)}"
STATUS="${STATUS:-active}"

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

usage() {
  cat <<EOF
Usage: ${0} <case-reference>

Apply a warrant authorisation to a TEC PCN case via the applyWarrantAuthorisation
event. Case details then shows a Warrant authorisations section with the new
entry (date of issue, date of expiry, status).

Optional environment variables:
  DATE_OF_ISSUE   (default: today, YYYY-MM-DD)
  DATE_OF_EXPIRY  (default: today + 1 year, YYYY-MM-DD)
  STATUS          active | expired | cancelled (default: active)
  CCD_DATA_STORE_URL, EVENT_ID
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

case "${STATUS}" in
  active|expired|cancelled) ;;
  Active|Expired|Cancelled)
    STATUS="$(printf '%s' "${STATUS}" | tr '[:upper:]' '[:lower:]')"
    ;;
  *)
    echo "Unknown STATUS '${STATUS}'. Use active, expired, or cancelled." >&2
    exit 1
    ;;
esac

if [[ ! "${DATE_OF_ISSUE}" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
  echo "DATE_OF_ISSUE must be YYYY-MM-DD: '${DATE_OF_ISSUE}'" >&2
  exit 1
fi
if [[ ! "${DATE_OF_EXPIRY}" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
  echo "DATE_OF_EXPIRY must be YYYY-MM-DD: '${DATE_OF_EXPIRY}'" >&2
  exit 1
fi

user_token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"
service_token="$("${SCRIPT_DIR}/get-local-s2s-token.sh" tec_api)"

event_trigger_url="${CCD_URL}/cases/${CASE_REFERENCE}/event-triggers/${EVENT_ID}"

start_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request GET "${event_trigger_url}" \
    --header "Authorization: Bearer ${user_token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'experimental: true'
} 2>&1)" || {
  echo "Failed to start ${EVENT_ID} for case ${CASE_REFERENCE}" >&2
  echo "${start_response}" >&2
  exit 1
}

event_token="$(jq --raw-output '.token // empty' <<<"${start_response}")"
if [[ -z "${event_token}" ]]; then
  echo "CCD start-event response did not contain a token" >&2
  echo "${start_response}" >&2
  exit 1
fi

submit_url="${CCD_URL}/cases/${CASE_REFERENCE}/events"
submit_body="$(jq --null-input --compact-output \
  --arg eventId "${EVENT_ID}" \
  --arg eventToken "${event_token}" \
  --arg dateOfIssue "${DATE_OF_ISSUE}" \
  --arg dateOfExpiry "${DATE_OF_EXPIRY}" \
  --arg status "${STATUS}" \
  '{
    event: {
      id: $eventId,
      summary: "Apply warrant authorisation",
      description: "Apply warrant authorisation"
    },
    data: {
      warrantAuthorisation: {
        dateOfIssue: $dateOfIssue,
        dateOfExpiry: $dateOfExpiry,
        status: $status
      }
    },
    event_token: $eventToken
  }')"

submit_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${submit_url}" \
    --header "Authorization: Bearer ${user_token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'Content-Type: application/json' \
    --header 'experimental: true' \
    --data "${submit_body}"
} 2>&1)" || {
  echo "Failed to submit ${EVENT_ID} for case ${CASE_REFERENCE}" >&2
  echo "${submit_response}" >&2
  exit 1
}

jq . <<<"${submit_response}"

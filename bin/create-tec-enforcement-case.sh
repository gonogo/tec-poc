#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
TEC_API_URL="${TEC_API_URL:-http://localhost:4013}"
CCD_URL="${CCD_DATA_STORE_URL:-http://localhost:4452}"
TE10_PDF="${TE10_PDF:-${REPO_ROOT}/fixtures/TE10.pdf}"

usage() {
  cat <<EOF
Usage: ${0} <pcn-case-reference> [pcn-case-reference...]
       ${0} -h|--help

Create a TEC Enforcement case, link the given existing TEC PCN case references,
and attach TE10.pdf to Case File View.

Fails if any PCN case reference does not exist.

Optional environment variables:
  TEC_API_URL, CCD_DATA_STORE_URL, LOCAL_AUTHORITY, SUBMITTER_EMAIL,
  RECEIVED_VIA, TE10_PDF
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

if [[ "$#" -lt 1 ]]; then
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

pcn_refs=()
for raw in "$@"; do
  pcn_refs+=("$(normalise_case_reference "${raw}")")
done

local_authority="${LOCAL_AUTHORITY:-westminster}"
submitter_email="${SUBMITTER_EMAIL:-la.submitter@example.com}"
received_via="${RECEIVED_VIA:-email}"

if [[ ! -f "${TE10_PDF}" ]]; then
  echo "TE10.pdf not found: ${TE10_PDF}" >&2
  echo "Set TE10_PDF or place the file at fixtures/TE10.pdf" >&2
  exit 1
fi

case_data="$(jq --null-input --compact-output \
  --arg localAuthority "${local_authority}" \
  --arg submitterEmail "${submitter_email}" \
  --arg receivedVia "${received_via}" \
  '{
    localAuthority: $localAuthority,
    submitterEmail: $submitterEmail,
    receivedVia: $receivedVia
  }')"

token="$("${SCRIPT_DIR}/get-local-idam-token.sh")"
service_token="$("${SCRIPT_DIR}/get-local-s2s-token.sh" tec_api)"

echo "Creating TEC Enforcement case..." >&2

create_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${TEC_API_URL}/enforcement-cases" \
    --header "Authorization: Bearer ${token}" \
    --header 'Content-Type: application/json' \
    --data "${case_data}"
} 2>&1)" || {
  echo "Failed to create a TEC enforcement case through ${TEC_API_URL}/enforcement-cases" >&2
  echo "${create_response}" >&2
  exit 1
}

enforcement_ref="$(jq --raw-output '.caseReference // empty' <<<"${create_response}")"
if [[ -z "${enforcement_ref}" ]]; then
  echo "Create response did not contain caseReference" >&2
  echo "${create_response}" >&2
  exit 1
fi

echo "Created enforcement case ${enforcement_ref}; linking ${#pcn_refs[@]} PCN case(s)..." >&2

case_links_json="$(jq --null-input --compact-output \
  --argjson refs "$(printf '%s\n' "${pcn_refs[@]}" | jq -R . | jq -s .)" \
  '[ $refs[] | { id: null, value: { CaseReference: ., CaseType: "TEC" } } ]')"

event_trigger_url="${CCD_URL}/cases/${enforcement_ref}/event-triggers/linkPcnCases"

start_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request GET "${event_trigger_url}" \
    --header "Authorization: Bearer ${token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'experimental: true'
} 2>&1)" || {
  echo "Failed to start linkPcnCases for enforcement case ${enforcement_ref}" >&2
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
  --arg eventToken "${event_token}" \
  --argjson caseLinks "${case_links_json}" \
  '{
    event: {
      id: "linkPcnCases",
      summary: "Link PCN cases",
      description: "Link PCN cases to enforcement case"
    },
    data: {
      caseLinks: $caseLinks
    },
    event_token: $eventToken
  }')"

link_response="$({
  curl --silent --show-error --fail-with-body \
    --connect-timeout 5 \
    --max-time 120 \
    --request POST "${CCD_URL}/cases/${enforcement_ref}/events" \
    --header "Authorization: Bearer ${token}" \
    --header "ServiceAuthorization: ${service_token}" \
    --header 'Content-Type: application/json' \
    --header 'experimental: true' \
    --data "${submit_body}"
} 2>&1)" || {
  echo "Failed to link PCN cases to enforcement case ${enforcement_ref}" >&2
  echo "${link_response}" >&2
  exit 1
}

echo "Recording linkEnforcementCase on each PCN for History..." >&2
for pcn_ref in "${pcn_refs[@]}"; do
  pcn_event_trigger_url="${CCD_URL}/cases/${pcn_ref}/event-triggers/linkEnforcementCase"

  pcn_start_response="$({
    curl --silent --show-error --fail-with-body \
      --connect-timeout 5 \
      --max-time 120 \
      --request GET "${pcn_event_trigger_url}" \
      --header "Authorization: Bearer ${token}" \
      --header "ServiceAuthorization: ${service_token}" \
      --header 'experimental: true'
  } 2>&1)" || {
    echo "Failed to start linkEnforcementCase for PCN case ${pcn_ref}" >&2
    echo "${pcn_start_response}" >&2
    exit 1
  }

  pcn_event_token="$(jq --raw-output '.token // empty' <<<"${pcn_start_response}")"
  if [[ -z "${pcn_event_token}" ]]; then
    echo "CCD start-event response did not contain a token for PCN ${pcn_ref}" >&2
    echo "${pcn_start_response}" >&2
    exit 1
  fi

  pcn_submit_body="$(jq --null-input --compact-output \
    --arg eventToken "${pcn_event_token}" \
    --arg enforcementRef "${enforcement_ref}" \
    '{
      event: {
        id: "linkEnforcementCase",
        summary: "Link enforcement case",
        description: "Link PCN case to enforcement case"
      },
      data: {
        enforcementCase: {
          CaseReference: $enforcementRef,
          CaseType: "TEC_ENFORCEMENT"
        }
      },
      event_token: $eventToken
    }')"

  pcn_link_response="$({
    curl --silent --show-error --fail-with-body \
      --connect-timeout 5 \
      --max-time 120 \
      --request POST "${CCD_URL}/cases/${pcn_ref}/events" \
      --header "Authorization: Bearer ${token}" \
      --header "ServiceAuthorization: ${service_token}" \
      --header 'Content-Type: application/json' \
      --header 'experimental: true' \
      --data "${pcn_submit_body}"
  } 2>&1)" || {
    echo "Failed to submit linkEnforcementCase for PCN case ${pcn_ref}" >&2
    echo "${pcn_link_response}" >&2
    exit 1
  }
done

echo "Attaching TE10.pdf to enforcement case ${enforcement_ref}..." >&2
CASE_TYPE_ID=TEC_ENFORCEMENT EVENT_ID=attachCaseFileDocument \
  "${SCRIPT_DIR}/attach-case-file-document.sh" "${enforcement_ref}" flat "${TE10_PDF}" >/dev/null

jq --null-input \
  --argjson create "${create_response}" \
  --arg enforcementCaseReference "${enforcement_ref}" \
  --argjson pcnCaseReferences "$(printf '%s\n' "${pcn_refs[@]}" | jq -R . | jq -s .)" \
  '{
    caseReference: ($create.caseReference // ($enforcementCaseReference | tonumber)),
    state: ($create.state // "OPEN"),
    pcnCaseReferences: ($pcnCaseReferences | map(tonumber)),
    document: "TE10.pdf"
  }'

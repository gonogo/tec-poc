#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
readonly PARTIAL_PATH="${REPO_ROOT}/design_docs/source/partials/_local_demo_case_links.html.erb"
readonly JSON_PATH="${SCRIPT_DIR}/.demo-catalogue.json"
readonly TEMPLATES_DIR="${SCRIPT_DIR}/templates"

TEC_API_URL="${TEC_API_URL:-http://localhost:4013}"
EXUI_BASE_URL="${EXUI_BASE_URL:-http://localhost:3000}"
DESIGN_DOCS_URL="${DESIGN_DOCS_URL:-http://localhost:4567/local-demo-cases.html}"
SKIP_CLEAR="${SKIP_CLEAR:-false}"
REGISTRATION_PCN_COUNT="${REGISTRATION_PCN_COUNT:-3}"

# Accumulated catalogue entries as a JSON array string.
CATALOGUE_JSON='[]'

usage() {
  cat <<EOF
Usage: ${0} [OPTIONS]
       ${0} -h|--help

Clear local TEC cases, seed a curated demo catalogue using existing bin/
scripts, and write design-docs case links.

Options:
  --skip-clear   Do not run clear-tec-cases.sh (re-seed on top of existing data)
  -h, --help     Show this help and exit

Optional environment variables:
  TEC_API_URL (default: http://localhost:4013)
  EXUI_BASE_URL (default: http://localhost:3000)
  DESIGN_DOCS_URL (default: http://localhost:4567/local-demo-cases.html)
  SKIP_CLEAR (default: false)
  REGISTRATION_PCN_COUNT (default: 3) — PCNs linked to the registration batch demo

See tech docs: Local demo catalogue seed.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-clear)
      SKIP_CLEAR=true
      ;;
    -h | --help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

for command in curl jq; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

require_stack() {
  if ! curl --silent --show-error --fail --connect-timeout 3 --max-time 10 \
    "${TEC_API_URL}/health" >/dev/null 2>&1; then
    echo "TEC API not reachable at ${TEC_API_URL}/health." >&2
    echo "Start the stack with ./gradlew bootWithCCD and try again." >&2
    exit 1
  fi
}

unique_batch_identifier() {
  # CreateBatchRequest: ^R[A-Z]{2,3}[0-9]{6}$ (authority prefix AB).
  printf 'RAB%06d' "$((($(date +%s) + RANDOM + $$) % 1000000))"
}

# Capture JSON stdout from a create script; print caseReference on stdout.
# Returns non-zero on failure (safe to use inside `if`).
create_ref_from() {
  local response
  if ! response="$("$@")"; then
    return 1
  fi
  local ref
  ref="$(jq --raw-output '.caseReference // empty' <<<"${response}")"
  if [[ -z "${ref}" ]]; then
    echo "Create script did not return caseReference:" >&2
    echo "${response}" >&2
    return 1
  fi
  printf '%s\n' "${ref}"
}

require_ref() {
  local ref
  if ! ref="$(create_ref_from "$@")"; then
    exit 1
  fi
  printf '%s\n' "${ref}"
}

# Run a mutate script that prints CCD/event JSON; print .state (or fallback).
state_from_json_cmd() {
  local fallback="$1"
  shift
  local response state
  response="$("$@")" || return 1
  state="$(jq --raw-output '.state // empty' <<<"${response}")"
  if [[ -z "${state}" ]]; then
    printf '%s\n' "${fallback}"
  else
    printf '%s\n' "${state}"
  fi
}

attach_pcn_doc() {
  local case_reference="$1"
  local folder="$2"
  local file_path="$3"
  echo "  attaching $(basename -- "${file_path}") → ${folder} on PCN ${case_reference}..." >&2
  "${SCRIPT_DIR}/attach-case-file-document.sh" \
    "${case_reference}" "${folder}" "${file_path}" >/dev/null
}

attach_batch_doc() {
  local case_reference="$1"
  local folder="$2"
  local file_path="$3"
  echo "  attaching $(basename -- "${file_path}") → ${folder} on batch ${case_reference}..." >&2
  CASE_TYPE_ID=TEC_BATCH EVENT_ID=attachBatchDocument \
    "${SCRIPT_DIR}/attach-case-file-document.sh" \
    "${case_reference}" "${folder}" "${file_path}" >/dev/null
}

# Every seeded batch gets Batch file.xlsx under Inputs; transferRequest also gets TE10.png.
attach_standard_batch_inputs() {
  local batch_ref="$1"
  local batch_type="${2:-}"
  attach_batch_doc "${batch_ref}" inputs "${TEMPLATES_DIR}/Batch file.xlsx"
  if [[ "${batch_type}" == "transferRequest" ]]; then
    attach_batch_doc "${batch_ref}" inputs "${TEMPLATES_DIR}/TE10.png"
  fi
}

gen_application() {
  local case_reference="$1"
  local timing="$2"
  local form="$3"
  echo "  generating ${timing} ${form} application on ${case_reference}..." >&2
  "${SCRIPT_DIR}/generate-application.sh" \
    "${case_reference}" "${timing}" "${form}" >/dev/null
}

gen_time_extension() {
  local case_reference="$1"
  local form="$2"
  echo "  generating ${form} time extension on ${case_reference}..." >&2
  "${SCRIPT_DIR}/generate-time-extension.sh" \
    "${case_reference}" "${form}" >/dev/null
}

record_entry() {
  local id="$1"
  local title="$2"
  local case_type="$3"
  local state="$4"
  local case_reference="$5"
  local explanation="$6"
  local url="${EXUI_BASE_URL}/cases/case-details/${case_reference}"

  CATALOGUE_JSON="$(jq \
    --arg id "${id}" \
    --arg title "${title}" \
    --arg caseType "${case_type}" \
    --arg state "${state}" \
    --arg caseReference "${case_reference}" \
    --arg url "${url}" \
    --arg explanation "${explanation}" \
    '. + [{
      id: $id,
      title: $title,
      caseType: $caseType,
      state: $state,
      caseReference: $caseReference,
      url: $url,
      explanation: $explanation
    }]' <<<"${CATALOGUE_JSON}")"

  echo "  recorded ${id}: ${case_type} ${case_reference} (${state})" >&2
}

html_escape() {
  # Minimal escape for table cell text.
  local s="$1"
  s="${s//&/&amp;}"
  s="${s//</&lt;}"
  s="${s//>/&gt;}"
  s="${s//\"/&quot;}"
  printf '%s' "${s}"
}

write_partial() {
  local generated_at
  generated_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  mkdir -p "$(dirname "${PARTIAL_PATH}")"

  {
    cat <<EOF
<%#
  Generated by bin/seed-demo-catalogue.sh at ${generated_at}.
  Do not edit by hand — re-run the seed script. This file is gitignored.
%>
<table class="govuk-table">
  <caption class="govuk-table__caption govuk-table__caption--m">Seeded local demo cases</caption>
  <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th scope="col" class="govuk-table__header">Demo</th>
      <th scope="col" class="govuk-table__header">Type</th>
      <th scope="col" class="govuk-table__header">State</th>
      <th scope="col" class="govuk-table__header">Case</th>
      <th scope="col" class="govuk-table__header">What to look at</th>
    </tr>
  </thead>
  <tbody class="govuk-table__body">
EOF

    local count
    count="$(jq 'length' <<<"${CATALOGUE_JSON}")"
    local i
    for ((i = 0; i < count; i++)); do
      local id title case_type state case_reference url explanation
      id="$(jq --raw-output --argjson i "${i}" '.[$i].id' <<<"${CATALOGUE_JSON}")"
      title="$(jq --raw-output --argjson i "${i}" '.[$i].title' <<<"${CATALOGUE_JSON}")"
      case_type="$(jq --raw-output --argjson i "${i}" '.[$i].caseType' <<<"${CATALOGUE_JSON}")"
      state="$(jq --raw-output --argjson i "${i}" '.[$i].state' <<<"${CATALOGUE_JSON}")"
      case_reference="$(jq --raw-output --argjson i "${i}" '.[$i].caseReference' <<<"${CATALOGUE_JSON}")"
      url="$(jq --raw-output --argjson i "${i}" '.[$i].url' <<<"${CATALOGUE_JSON}")"
      explanation="$(jq --raw-output --argjson i "${i}" '.[$i].explanation' <<<"${CATALOGUE_JSON}")"

      cat <<EOF
    <tr class="govuk-table__row" id="demo-$(html_escape "${id}")">
      <th scope="row" class="govuk-table__header">$(html_escape "${title}")</th>
      <td class="govuk-table__cell"><code>$(html_escape "${case_type}")</code></td>
      <td class="govuk-table__cell"><code>$(html_escape "${state}")</code></td>
      <td class="govuk-table__cell"><a class="govuk-link" href="$(html_escape "${url}")">${case_reference}</a></td>
      <td class="govuk-table__cell">$(html_escape "${explanation}")</td>
    </tr>
EOF
    done

    cat <<EOF
  </tbody>
</table>
<p class="govuk-body-s">Generated ${generated_at}. Open links while signed in to Manage Cases on ${EXUI_BASE_URL}.</p>
EOF
  } >"${PARTIAL_PATH}"
}

write_json() {
  jq --null-input \
    --arg generatedAt "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" \
    --argjson entries "${CATALOGUE_JSON}" \
    '{ generatedAt: $generatedAt, entries: $entries }' \
    >"${JSON_PATH}"
}

seed_catalogue() {
  local ref batch_ref state

  echo "Seeding pcn-pending-case-issued..." >&2
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" -)"
  record_entry \
    "pcn-pending-case-issued" \
    "PCN — pending case issued" \
    "TEC" \
    "PENDING_CASE_ISSUED" \
    "${ref}" \
    "Fresh registration with payment still pending. Check Tasks and Case details for the default create state. Case File View is empty."

  echo "Seeding pcn-case-issued..." >&2
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" -)"
  state="$(state_from_json_cmd CASE_ISSUED "${SCRIPT_DIR}/transition-to-case-issued.sh" "${ref}")"
  gen_application "${ref}" "in time" TE9
  record_entry \
    "pcn-case-issued" \
    "PCN — case issued" \
    "TEC" \
    "${state}" \
    "${ref}" \
    "After registration payment succeeded. Case File View → Applications has an in-time TE9."

  echo "Seeding pcn-awaiting-la-oot..." >&2
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" -)"
  gen_application "${ref}" "out of time" TE9
  gen_time_extension "${ref}" TE7
  state="$(state_from_json_cmd AWAITING_LA_OOT_RESPONSE \
    "${SCRIPT_DIR}/set-case-state.sh" "${ref}" AWAITING_LA_OOT_RESPONSE)"
  record_entry \
    "pcn-awaiting-la-oot" \
    "PCN — awaiting LA OOT response" \
    "TEC" \
    "${state}" \
    "${ref}" \
    "Prototype out-of-time / LA response state. Case File View → Applications has out-of-time TE9 and TE7."

  echo "Seeding pcn-warrant-issued..." >&2
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" -)"
  state="$(
    STATUS=active state_from_json_cmd WARRANT_AUTHORISATION_ISSUED \
      "${SCRIPT_DIR}/apply-warrant-authorisation.sh" "${ref}"
  )"
  record_entry \
    "pcn-warrant-issued" \
    "PCN — warrant authorisation issued" \
    "TEC" \
    "${state}" \
    "${ref}" \
    "Warrant authorisations section on Case details with an active warrant; state is Warrant Authorisation Issued."

  echo "Seeding pcn-warrant-expired..." >&2
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" -)"
  state="$(
    STATUS=expired state_from_json_cmd WARRANT_AUTHORISATION_EXPIRED \
      "${SCRIPT_DIR}/apply-warrant-authorisation.sh" "${ref}"
  )"
  record_entry \
    "pcn-warrant-expired" \
    "PCN — warrant authorisation expired" \
    "TEC" \
    "${state}" \
    "${ref}" \
    "Same warrant UI with an expired status for comparison with the active warrant case."

  echo "Seeding pcn-refer-enforcement..." >&2
  export BATCH_IDENTIFIER
  BATCH_IDENTIFIER="$(unique_batch_identifier)"
  batch_ref="$(require_ref "${SCRIPT_DIR}/create-tec-batch.sh" westminster transferRequest)"
  attach_standard_batch_inputs "${batch_ref}" transferRequest
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" "${batch_ref}")"
  record_entry \
    "pcn-refer-enforcement" \
    "PCN — refer for enforcement" \
    "TEC" \
    "REFER_FOR_ENFORCEMENT" \
    "${ref}" \
    "Linked to a transfer-request batch (Case File View Inputs: Batch file.xlsx and TE10.png). State is Refer for Enforcement; check Linked Cases."

  echo "Seeding pcn-closed..." >&2
  BATCH_IDENTIFIER="$(unique_batch_identifier)"
  batch_ref="$(require_ref "${SCRIPT_DIR}/create-tec-batch.sh" westminster caseClosureRequests)"
  attach_standard_batch_inputs "${batch_ref}" caseClosureRequests
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-case.sh" "${batch_ref}")"
  record_entry \
    "pcn-closed" \
    "PCN — closed" \
    "TEC" \
    "CLOSED" \
    "${ref}" \
    "Linked to a case-closure-requests batch (Inputs: Batch file.xlsx). State is Closed; Linked Cases shows the closure batch."

  echo "Seeding batch-registration-with-pcns (${REGISTRATION_PCN_COUNT} PCNs)..." >&2
  BATCH_IDENTIFIER="$(unique_batch_identifier)"
  batch_ref="$(require_ref "${SCRIPT_DIR}/create-tec-batch.sh" westminster registration)"
  attach_standard_batch_inputs "${batch_ref}" registration
  CASE_COUNT="${REGISTRATION_PCN_COUNT}" \
    "${SCRIPT_DIR}/create-tec-cases.sh" "${batch_ref}" >/dev/null
  record_entry \
    "batch-registration-with-pcns" \
    "Batch — registration with linked PCNs" \
    "TEC_BATCH" \
    "QUEUED_FOR_PROCESSING" \
    "${batch_ref}" \
    "Registration batch with ${REGISTRATION_PCN_COUNT} linked PCNs. Case File View Inputs has Batch file.xlsx; check Linked Cases."

  echo "Seeding batch-queued..." >&2
  BATCH_IDENTIFIER="$(unique_batch_identifier)"
  batch_ref="$(
    TARGET_STATE=QUEUED_FOR_PROCESSING \
      require_ref "${SCRIPT_DIR}/create-tec-batch.sh" westminster warrantAuthRequests
  )"
  attach_standard_batch_inputs "${batch_ref}" warrantAuthRequests
  record_entry \
    "batch-queued" \
    "Batch — queued for processing" \
    "TEC_BATCH" \
    "QUEUED_FOR_PROCESSING" \
    "${batch_ref}" \
    "Warrant-auth batch still queued. Case File View Inputs has Batch file.xlsx."

  echo "Seeding batch-processing-complete..." >&2
  BATCH_IDENTIFIER="$(unique_batch_identifier)"
  batch_ref="$(
    TARGET_STATE=PROCESSING_COMPLETE \
      require_ref "${SCRIPT_DIR}/create-tec-batch.sh" westminster warrantAuthRequests
  )"
  attach_standard_batch_inputs "${batch_ref}" warrantAuthRequests
  attach_batch_doc "${batch_ref}" outputs "${TEMPLATES_DIR}/PE3.pdf"
  record_entry \
    "batch-processing-complete" \
    "Batch — processing complete" \
    "TEC_BATCH" \
    "PROCESSING_COMPLETE" \
    "${batch_ref}" \
    "Warrant-auth batch marked processing complete. Inputs: Batch file.xlsx; Outputs: PE3.pdf."

  echo "Seeding batch-processing-failed..." >&2
  BATCH_IDENTIFIER="$(unique_batch_identifier)"
  batch_ref="$(
    TARGET_STATE=PROCESSING_FAILED \
      require_ref "${SCRIPT_DIR}/create-tec-batch.sh" westminster warrantAuthRequests
  )"
  attach_standard_batch_inputs "${batch_ref}" warrantAuthRequests
  record_entry \
    "batch-processing-failed" \
    "Batch — processing failed" \
    "TEC_BATCH" \
    "PROCESSING_FAILED" \
    "${batch_ref}" \
    "Warrant-auth batch that failed during processing. Case File View Inputs has Batch file.xlsx."

  echo "Seeding exception-pending-review..." >&2
  ref="$(require_ref "${SCRIPT_DIR}/create-tec-exception-case.sh")"
  record_entry \
    "exception-pending-review" \
    "Exception — pending review" \
    "TEC_EXCEPTION" \
    "EXCEPTION_PENDING_REVIEW" \
    "${ref}" \
    "Exception case for review Tasks (no Case File View attach event yet). Use Case list filtered to TEC Exception."
}

main() {
  require_stack

  if [[ "${SKIP_CLEAR}" == "true" ]]; then
    echo "Skipping clear (--skip-clear)." >&2
  else
    echo "Clearing existing TEC cases..." >&2
    "${SCRIPT_DIR}/clear-tec-cases.sh" --yes
  fi

  echo "Seeding demo catalogue..." >&2
  seed_catalogue

  write_partial
  write_json

  local count
  count="$(jq 'length' <<<"${CATALOGUE_JSON}")"
  echo >&2
  echo "Seeded ${count} catalogue entries." >&2
  echo "Wrote ${PARTIAL_PATH}" >&2
  echo "Wrote ${JSON_PATH}" >&2
  echo "Design docs: ${DESIGN_DOCS_URL}" >&2
  echo "Sign in to Manage Cases at ${EXUI_BASE_URL} as tec-demo@test.com / password." >&2
}

main

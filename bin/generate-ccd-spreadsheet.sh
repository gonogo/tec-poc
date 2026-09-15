#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

CASE_TYPE="${CCD_CASE_TYPE:-TEC_BATCH_DATAFILE}"
CALLBACK_URL="${CASE_API_URL:-http://localhost:4013}"
ALLOW_BEAN_DEFINITION_OVERRIDING="${SPRING_MAIN_ALLOW_BEAN_DEFINITION_OVERRIDING:-true}"
PROCESSOR_IMAGE="${CCD_DEFINITION_PROCESSOR_IMAGE:-hmctspublic.azurecr.io/ccd/definition-processor:latest}"
PROCESSOR_PLATFORM="${CCD_DEFINITION_PROCESSOR_PLATFORM:-linux/amd64}"
OUTPUT_FILE="${CCD_SPREADSHEET_OUTPUT:-${PROJECT_DIR}/build/ccd-spreadsheet/${CASE_TYPE}.xlsx}"
DEFINITION_DIR="${PROJECT_DIR}/build/ccd-definition/${CASE_TYPE}"

for command in docker find; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running or is not accessible." >&2
  exit 1
fi

echo "Generating CCD JSON for ${CASE_TYPE}..."
(
  cd "${PROJECT_DIR}"
  CASE_API_URL="${CALLBACK_URL}" \
    SPRING_MAIN_ALLOW_BEAN_DEFINITION_OVERRIDING="${ALLOW_BEAN_DEFINITION_OVERRIDING}" \
    SPRING_FLYWAY_ENABLED=false \
    SPRING_JPA_HIBERNATE_DDL_AUTO=none \
    SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect \
    SERVER_PORT=0 \
    ./gradlew clean generateCCDConfig
)

if [[ ! -d "${DEFINITION_DIR}" ]]; then
  echo "Expected CCD definition directory was not generated: ${DEFINITION_DIR}" >&2
  exit 1
fi

json_count="$(find "${DEFINITION_DIR}" -type f -name '*.json' | wc -l | tr -d ' ')"
if [[ "${json_count}" -eq 0 ]]; then
  echo "No JSON definition files were generated under ${DEFINITION_DIR}" >&2
  exit 1
fi

if command -v jq >/dev/null 2>&1; then
  echo "Validating ${json_count} generated JSON files..."
  find "${DEFINITION_DIR}" -type f -name '*.json' -exec jq empty {} +
else
  echo "jq is not installed; skipping JSON syntax validation."
fi

output_dir="$(dirname -- "${OUTPUT_FILE}")"
output_name="$(basename -- "${OUTPUT_FILE}")"
mkdir -p "${output_dir}"
output_dir="$(cd -- "${output_dir}" && pwd)"

temporary_name=".${output_name}.tmp.$$"
temporary_file="${output_dir}/${temporary_name}"
trap 'rm -f -- "${temporary_file}"' EXIT

echo "Creating ${OUTPUT_FILE} with ${PROCESSOR_IMAGE}..."
docker run --rm \
  --platform "${PROCESSOR_PLATFORM}" \
  --user "$(id -u):$(id -g)" \
  --env HOME=/tmp \
  --env XDG_CACHE_HOME=/tmp/.cache \
  --volume "${DEFINITION_DIR}:/tmp/ccd-definition:ro" \
  --volume "${output_dir}:/tmp/output" \
  "${PROCESSOR_IMAGE}" \
  json2xlsx \
  -D /tmp/ccd-definition \
  -o "/tmp/output/${temporary_name}"

if [[ ! -s "${temporary_file}" ]]; then
  echo "The definition processor did not create a non-empty XLSX file." >&2
  exit 1
fi

mv -- "${temporary_file}" "${OUTPUT_FILE}"
trap - EXIT

echo "Created CCD spreadsheet: ${OUTPUT_FILE}"

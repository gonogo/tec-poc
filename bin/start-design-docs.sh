#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
readonly DOCS_DIR="${REPO_ROOT}/design_docs"
readonly PID_FILE="${SCRIPT_DIR}/.design-docs.pid"
readonly LOG_FILE="${SCRIPT_DIR}/.design-docs.log"
readonly PORT="${DESIGN_DOCS_PORT:-4567}"

if [[ ! -d "${DOCS_DIR}" ]]; then
  echo "Design docs directory not found at ${DOCS_DIR}; skipping." >&2
  exit 0
fi

if ! command -v ruby >/dev/null 2>&1 || ! command -v bundle >/dev/null 2>&1; then
  echo "Skipping design docs server: Ruby 3.3 and Bundler are required." >&2
  echo "Install Ruby 3.3 (see design_docs/.ruby-version), then: cd design_docs && bundle install" >&2
  echo "Or start later with: ${SCRIPT_DIR}/start-design-docs.sh" >&2
  exit 0
fi

if [[ -f "${PID_FILE}" ]]; then
  existing_pid="$(<"${PID_FILE}")"
  if kill -0 "${existing_pid}" 2>/dev/null; then
    echo "Design docs server already running (pid ${existing_pid}) on port ${PORT}"
    exit 0
  fi
  rm -f "${PID_FILE}"
fi

if command -v lsof >/dev/null 2>&1; then
  if lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port ${PORT} is already in use; assuming design docs are available at http://localhost:${PORT}"
    exit 0
  fi
fi

(
  cd "${DOCS_DIR}"
  if ! bundle check >/dev/null 2>&1; then
    echo "Installing design docs Ruby gems (first run)..."
    bundle install
  fi
)

# Detach from the parent shell so IDE/session cleanup does not kill Middleman.
nohup bash -c "cd \"${DOCS_DIR}\" && exec bundle exec middleman server -p ${PORT}" \
  >"${LOG_FILE}" 2>&1 < /dev/null &
echo $! >"${PID_FILE}"
sleep 0.5

for _ in {1..60}; do
  if curl --silent --fail --connect-timeout 1 "http://localhost:${PORT}/" >/dev/null 2>&1; then
    echo "Design docs server started on http://localhost:${PORT} (pid $(<"${PID_FILE}"))"
    echo "Logs: ${LOG_FILE}"
    exit 0
  fi
  if ! kill -0 "$(<"${PID_FILE}")" 2>/dev/null; then
    echo "Failed to start design docs server; see ${LOG_FILE}" >&2
    rm -f "${PID_FILE}"
    exit 1
  fi
  sleep 1
done

echo "Design docs server did not become ready in time; see ${LOG_FILE}" >&2
exit 1

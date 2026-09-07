#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly PROXY_SCRIPT="${SCRIPT_DIR}/xui-manage-batches-proxy.py"
readonly PID_FILE="${SCRIPT_DIR}/.xui-manage-batches-proxy.pid"
readonly LOG_FILE="${SCRIPT_DIR}/.xui-manage-batches-proxy.log"
readonly PORT="${XUI_NAV_PROXY_PORT:-3000}"
readonly UPSTREAM_PORT="${XUI_NAV_PROXY_UPSTREAM_PORT:-3002}"

# Always target the CFTLib XUI port unless the caller explicitly overrides upstream.
if [[ -z "${XUI_NAV_PROXY_UPSTREAM:-}" ]]; then
  export XUI_NAV_PROXY_UPSTREAM="http://127.0.0.1:${UPSTREAM_PORT}"
fi
export XUI_NAV_PROXY_PORT="${PORT}"
export XUI_NAV_PROXY_CREATE_BATCH_PATH="${XUI_NAV_PROXY_CREATE_BATCH_PATH:-${XUI_NAV_PROXY_BATCHES_PATH:-/cases/case-create/TEC/TEC_BATCH/uploadBatch}}"
export XUI_NAV_PROXY_TEC_ROLE_KEY="${XUI_NAV_PROXY_TEC_ROLE_KEY:-caseworker-tec}"
export XUI_NAV_PROXY_HOST="${XUI_NAV_PROXY_HOST:-127.0.0.1}"

if ! command -v python3 >/dev/null 2>&1; then
  echo "Required command not found: python3" >&2
  exit 1
fi

if [[ -f "${PID_FILE}" ]]; then
  existing_pid="$(<"${PID_FILE}")"
  if kill -0 "${existing_pid}" 2>/dev/null; then
    echo "XUI Create batch nav proxy already running (pid ${existing_pid}) on port ${PORT}"
    exit 0
  fi
  rm -f "${PID_FILE}"
fi

if command -v lsof >/dev/null 2>&1; then
  if lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port ${PORT} is already in use; not starting the Create batch nav proxy." >&2
    echo "If a previous Manage Cases container still binds ${PORT}, recreate it with XUI_PORT=${UPSTREAM_PORT}" >&2
    lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN >&2 || true
    exit 1
  fi
fi

nohup python3 "${PROXY_SCRIPT}" >"${LOG_FILE}" 2>&1 < /dev/null &
echo $! >"${PID_FILE}"
sleep 0.2

if ! kill -0 "$(<"${PID_FILE}")" 2>/dev/null; then
  echo "Failed to start XUI Create batch nav proxy; see ${LOG_FILE}" >&2
  rm -f "${PID_FILE}"
  exit 1
fi

echo "XUI Create batch nav proxy started on http://localhost:${PORT} (pid $(<"${PID_FILE}"))"
echo "Upstream Manage Cases: ${XUI_NAV_PROXY_UPSTREAM}"
echo "Logs: ${LOG_FILE}"

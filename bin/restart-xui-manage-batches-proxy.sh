#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly PID_FILE="${SCRIPT_DIR}/.xui-manage-batches-proxy.pid"
readonly PORT="${XUI_NAV_PROXY_PORT:-3000}"

echo "Stopping any Manage batches nav proxy on port ${PORT}..."

if [[ -f "${PID_FILE}" ]]; then
  pid="$(<"${PID_FILE}")"
  if [[ -n "${pid}" ]]; then
    kill -9 "${pid}" 2>/dev/null || true
  fi
  rm -f "${PID_FILE}"
fi

if command -v lsof >/dev/null 2>&1; then
  while read -r pid; do
    [[ -n "${pid}" ]] || continue
    echo "Killing pid ${pid} listening on ${PORT}"
    kill -9 "${pid}" 2>/dev/null || true
  done < <(lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN -t 2>/dev/null || true)
fi

# Drop polluted test overrides so we always bind :3000 -> :3002 unless the caller sets them.
unset XUI_NAV_PROXY_UPSTREAM XUI_NAV_PROXY_UPSTREAM_PORT XUI_NAV_PROXY_HOST || true
export XUI_NAV_PROXY_PORT="${PORT}"

sleep 0.3
"${SCRIPT_DIR}/start-xui-manage-batches-proxy.sh"

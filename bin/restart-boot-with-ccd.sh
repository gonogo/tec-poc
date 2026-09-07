#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_NAME="$(basename "${BASH_SOURCE[0]}")"
readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

usage() {
  cat <<EOF
Usage: ${SCRIPT_NAME} [OPTIONS]

Stop the local bootWithCCD / CFTLib stack, then start it again with
./gradlew bootWithCCD.

Any options are forwarded to bin/stop-boot-with-ccd.sh.

Options:
  --all-docker    Stop all running Docker containers, not just CFTLib ones
  -h, --help      Show this help message
EOF
}

for arg in "$@"; do
  case "${arg}" in
    -h | --help)
      usage
      exit 0
      ;;
  esac
done

echo "Stopping bootWithCCD stack..."
"${SCRIPT_DIR}/stop-boot-with-ccd.sh" "$@"

echo
echo "Starting bootWithCCD..."
cd -- "${REPO_ROOT}"
exec ./gradlew bootWithCCD

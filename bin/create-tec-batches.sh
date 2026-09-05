#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
COUNT="${1:-6}"

if ! [[ "${COUNT}" =~ ^[0-9]+$ ]] || [[ "${COUNT}" -lt 1 ]]; then
  echo "Usage: ${0} [count]" >&2
  exit 1
fi

AUTHORITY_CODES=(TE AB WM LE BR CK)
OPERATIONS=(registration warrantAuthRequests warrantReissueRequests outOfTimeDecisions changeOfAddress caseClosureRequests)
RECEIVED_VIA=(email upload)
TARGET_STATES=(QUEUED_FOR_PROCESSING PROCESSING_STARTED PROCESSING_COMPLETE)
LOCAL_AUTHORITIES=(westminster manchesterCityCouncil birminghamCityCouncil leedsCityCouncil bristolCityCouncil)

for ((index = 0; index < COUNT; index++)); do
  export AUTHORITY_CODE="${AUTHORITY_CODES[$((index % ${#AUTHORITY_CODES[@]}))]}"
  export OPERATION="${OPERATIONS[$((index % ${#OPERATIONS[@]}))]}"
  export RECEIVED_VIA="${RECEIVED_VIA[$((index % ${#RECEIVED_VIA[@]}))]}"
  export TARGET_STATE="${TARGET_STATES[$((index % ${#TARGET_STATES[@]}))]}"
  export LOCAL_AUTHORITY="${LOCAL_AUTHORITIES[$((index % ${#LOCAL_AUTHORITIES[@]}))]}"
  export PCN_COUNT="$((200 + (index * 173) % 1801))"
  # Unique per run (fixed index math collided on re-runs and caused tec_batch_identifier_uk errors).
  export BATCH_IDENTIFIER="R${AUTHORITY_CODE}$(printf '%06d' "$((($(date +%s) + RANDOM + index * 97) % 1000000))")"

  echo "Creating batch $((index + 1))/${COUNT}: ${BATCH_IDENTIFIER} (${TARGET_STATE}, ${OPERATION})"
  "${SCRIPT_DIR}/create-tec-batch.sh"
done

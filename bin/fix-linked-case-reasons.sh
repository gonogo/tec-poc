#!/usr/bin/env bash
# Reload local pieces that make ExUI Linked Cases "Reasons for case link" show.
#
# Root causes this addresses:
# 1. CaseView must emit Reason=CLRC007 + OtherDescription=… (needs Java restart)
# 2. ExUI must load CaseLinkingReasonCode LOV (nav proxy stub; needs proxy restart)
#
# Usage: ./bin/fix-linked-case-reasons.sh

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

echo "1/2 Restarting ExUI nav proxy (CaseLinkingReasonCode LOV stub)..."
"${SCRIPT_DIR}/restart-xui-manage-batches-proxy.sh"

echo
echo "2/2 Restart TEC / CFTLib so BatchCaseView picks up CLRC007 reasons:"
echo "    ./bin/restart-boot-with-ccd.sh"
echo
echo "Then hard-refresh Manage Cases and open a batch or PCN Linked Cases tab."
echo "Expected reason text: Other - Linked as part of a batch of registrations"
echo
echo "Quick API checks after restart:"
echo "  curl -s http://localhost:3000/api/commondata/lov/categories/CaseLinkingReasonCode | jq '.list_of_values|length'"
echo "  # batch caseLinks[0].ReasonForLink should be Reason=CLRC007 + OtherDescription=…"

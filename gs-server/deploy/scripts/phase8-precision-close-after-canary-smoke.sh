#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new"
CLOSE_SCRIPT="${ROOT}/gs-server/deploy/scripts/phase8-precision-close-after-canary.sh"
MATRIX_SCRIPT="${ROOT}/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh"
TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT

mkdir -p "${TMP}/docs/phase8/precision" "${TMP}/docs" "${TMP}/support"
cp "${ROOT}/gs-server/deploy/config/phase8-precision-policy.json" "${TMP}/policy.json"
cp "${ROOT}/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json" "${TMP}/checklist.json"

cat > "${TMP}/docs/phase8/precision/phase8-precision-nonprod-canary-evidence-20990101-000000.md" <<'EVID'
# Synthetic Phase 8 Non-Prod Precision Canary Evidence Pack

```text
status=READY
gs_container=refactor-gs-1
log_dir=/tmp/fake
precision_dual_calc_log_lines=7
policy_file=/tmp/policy.json
matrix_report=/tmp/preclose-matrix.md
matrix_blocking_count=1
matrix_remaining_blockers=nonprod_canary_runtime: pending
canary_flags_hint=-Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3
```
EVID

"${CLOSE_SCRIPT}" \
  --policy-file "${TMP}/policy.json" \
  --checklist-file "${TMP}/checklist.json" \
  --evidence-dir "${TMP}/docs/phase8/precision" \
  --matrix-script "${MATRIX_SCRIPT}" \
  --matrix-out-dir "${TMP}/docs/phase8/precision" \
  --sync-policy-copy false \
  --sync-dashboard false \
  --docs-dir "${TMP}/docs" \
  --doc-number 999 \
  > "${TMP}/close.out"

closure_doc="$(rg -n '^closure_doc=' "${TMP}/close.out" | sed -E 's/^[0-9]+:closure_doc=//')"
matrix_report="$(rg -n '^matrix_report=' "${TMP}/close.out" | sed -E 's/^[0-9]+:matrix_report=//')"
phase8_ready="$(rg -n '^phase8_ready_to_close=' "${TMP}/close.out" | sed -E 's/^[0-9]+:phase8_ready_to_close=//')"
blocking="$(rg -n '^blocking_categories=' "${TMP}/close.out" | sed -E 's/^[0-9]+:blocking_categories=//')"

[[ -f "${closure_doc}" ]]
[[ -f "${matrix_report}" ]]
[[ "${phase8_ready}" == "yes" ]]
[[ "${blocking}" == "0" ]]

node - "${TMP}/policy.json" "${TMP}/checklist.json" <<'NODE'
const fs = require('fs');
const [policyFile, checklistFile] = process.argv.slice(2);
const p = JSON.parse(fs.readFileSync(policyFile, 'utf8'));
const v = p.verificationCategories.find(x => x.category === 'nonprod_canary_runtime');
if (!v) throw new Error('policy category missing');
if (v.blocking !== false) throw new Error('policy blocker not cleared');
if (!String(v.status).includes('cleared')) throw new Error('policy status not cleared');
const c = JSON.parse(fs.readFileSync(checklistFile, 'utf8'));
const item = c.sections.flatMap(s => s.items || []).find(i => i.id === 'pu-precision-audit');
if (!item) throw new Error('checklist item missing');
if (item.status !== 'done') throw new Error('checklist item not done');
if (!String(item.evidence).includes('phase8-precision-runtime-canary-phase-closure')) throw new Error('checklist evidence not updated');
console.log('ok');
NODE

echo "summary pass=1 fail=0"

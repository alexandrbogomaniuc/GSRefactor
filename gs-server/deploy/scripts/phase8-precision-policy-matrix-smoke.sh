#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic smoke for Phase 8 precision policy + matrix generator.
USAGE
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" ]]; then
  usage
  exit 0
fi
if [[ $# -gt 0 ]]; then
  echo "Unknown argument: $1" >&2
  usage
  exit 1
fi

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
TMPD="$(mktemp -d)"
trap 'rm -rf "${TMPD}"' EXIT

POLICY="${ROOT}/gs-server/deploy/config/phase8-precision-policy.json"
MATRIX_OUT="${TMPD}/matrix"
mkdir -p "${MATRIX_OUT}"

node -e '
const fs=require("fs");
const p=JSON.parse(fs.readFileSync(process.argv[1],"utf8"));
if(p.schemaVersion!==1) throw new Error("schemaVersion");
if(!Array.isArray(p.currencyPolicies)||p.currencyPolicies.length<3) throw new Error("currencyPolicies");
if(!p.currencyPolicies.some(c=>c.minorUnitScale===3)) throw new Error("scale3 missing");
if(!Array.isArray(p.verificationCategories)||p.verificationCategories.length===0) throw new Error("verificationCategories");
if(!p.verificationCategories.some(v=>v.category==="nonprod_canary_runtime")) throw new Error("nonprod_canary_runtime missing");
const blockingCount = p.verificationCategories.filter(v=>v.blocking===true).length;
console.log("PASS policy schema/basic contents blockingCount="+blockingCount);
' "${POLICY}"

GEN_OUT="$(${ROOT}/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh --policy-file "${POLICY}" --out-dir "${MATRIX_OUT}")"
echo "${GEN_OUT}"
REPORT="${GEN_OUT#report=}"

rg -q '^# Phase 8 Precision Verification Matrix' "${REPORT}" && echo 'PASS matrix header'
rg -q '^\| KWD \| 3 \|' "${REPORT}" && echo 'PASS scale3 currency row'
rg -q '^\| wallet_contract_and_rounding \|' "${REPORT}" && echo 'PASS wallet blocking category row'
if rg -q '^- phase8ReadyToClose: no$' "${REPORT}"; then
  echo 'PASS closure gate summary (pre-close state)'
elif rg -q '^- phase8ReadyToClose: yes$' "${REPORT}" && rg -q '^- blockingCategories: 0$' "${REPORT}"; then
  echo 'PASS closure gate summary (closed state)'
else
  echo 'FAIL: unexpected closure gate summary state' >&2
  exit 1
fi

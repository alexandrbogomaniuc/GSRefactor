#!/usr/bin/env bash
set -euo pipefail

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

ROOT="/Users/alexb/Documents/Dev/Dev_new"
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
if(!Array.isArray(p.verificationCategories)||!p.verificationCategories.some(v=>v.blocking===true)) throw new Error("blocking category missing");
console.log("PASS policy schema/basic contents");
' "${POLICY}"

GEN_OUT="$(${ROOT}/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh --policy-file "${POLICY}" --out-dir "${MATRIX_OUT}")"
echo "${GEN_OUT}"
REPORT="${GEN_OUT#report=}"

rg -q '^# Phase 8 Precision Verification Matrix' "${REPORT}" && echo 'PASS matrix header'
rg -q '^\| KWD \| 3 \|' "${REPORT}" && echo 'PASS scale3 currency row'
rg -q '^\| wallet_contract_and_rounding \|' "${REPORT}" && echo 'PASS wallet blocking category row'
rg -q '^- phase8ReadyToClose: no$' "${REPORT}" && echo 'PASS closure gate summary'

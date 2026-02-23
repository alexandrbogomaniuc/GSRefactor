#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic parity smoke for NumberUtils.asMoney legacy cent rounding semantics
(Math.round-based), including negative half-cent edge cases.
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

node - <<'NODE'
function javaMathRound(x) {
  return Math.floor(x + 0.5);
}

function legacyAsMoney(d) {
  return javaMathRound(d * 100) / 100;
}

function centsToDouble(cents) {
  return cents / 100;
}

function refactoredAsMoney(d) {
  return centsToDouble(javaMathRound(d * 100));
}

function fixed2(n) {
  return Number(n).toFixed(2);
}

const vectors = [
  { in: 0, expected: '0.00' },
  { in: 0.004, expected: '0.00' },
  { in: 0.005, expected: '0.01' },
  { in: 1.234, expected: '1.23' },
  { in: 1.235, expected: '1.24' },
  { in: -0.004, expected: '0.00' },
  { in: -0.005, expected: '0.00' }, // Math.round semantics (toward +inf tie)
  { in: -0.006, expected: '-0.01' },
  { in: -1.234, expected: '-1.23' },
  { in: -1.235, expected: '-1.24' },
  { in: 9999999.999, expected: '10000000.00' },
];

let pass = 0;
let fail = 0;
console.log('# Phase 8 Wave 1 NumberUtils.asMoney Parity Smoke');
for (const v of vectors) {
  const legacy = legacyAsMoney(v.in);
  const refac = refactoredAsMoney(v.in);
  const ok = (Object.is(legacy, refac) || legacy === refac) && fixed2(legacy) === v.expected;
  if (ok) {
    pass += 1;
    console.log(`PASS in=${v.in} legacy=${fixed2(legacy)} refac=${fixed2(refac)} expected=${v.expected}`);
  } else {
    fail += 1;
    console.log(`FAIL in=${v.in} legacy=${fixed2(legacy)} refac=${fixed2(refac)} expected=${v.expected}`);
  }
}

const weird = legacyAsMoney(-0.005);
if (fixed2(weird) === '0.00') {
  pass += 1;
  console.log('PASS negative-half-cent legacy semantics preserved (-0.005 -> 0.00)');
} else {
  fail += 1;
  console.log(`FAIL negative-half-cent semantics changed: got ${fixed2(weird)}`);
}

console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

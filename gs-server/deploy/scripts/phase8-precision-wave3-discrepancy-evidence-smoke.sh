#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime smoke for Phase 8 Wave 3 discrepancy evidence scaffolding.
Validates counter increment, throttled snapshot emission rules, and snapshot message shape
used by disabled-by-default parity hooks in GS settings/coin-rule paths.
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
function normalizeLogEvery(raw, fallback = 1000) {
  if (raw == null) return fallback;
  const s = String(raw).trim();
  if (!s) return fallback;
  const n = Number(s);
  if (!Number.isInteger(n) || n <= 0) return fallback;
  return n;
}

function shouldLog(checkCount, logEvery, mismatch) {
  return !!mismatch || checkCount === 1 || (checkCount % logEvery === 0);
}

function buildDynamicSnapshot(metric, checkCount, mismatchCount, bankId, gameId, legacy, generalized) {
  return `phase8-precision-dual-calc metric=${metric} checkCount=${checkCount} mismatchCount=${mismatchCount} bankId=${bankId} gameId=${gameId} legacy=${legacy} generalized=${generalized}`;
}

function buildHelperSnapshot(metric, checkCount, mismatchCount, templateMaxCredits, legacy, generalized) {
  return `phase8-precision-dual-calc metric=${metric} checkCount=${checkCount} mismatchCount=${mismatchCount} templateMaxCredits=${templateMaxCredits} legacy=${legacy} generalized=${generalized}`;
}

const checks = [
  ['default logEvery fallback', () => normalizeLogEvery(undefined) === 1000],
  ['blank logEvery fallback', () => normalizeLogEvery('  ') === 1000],
  ['invalid logEvery fallback', () => normalizeLogEvery('abc') === 1000],
  ['non-positive logEvery fallback', () => normalizeLogEvery('0') === 1000 && normalizeLogEvery('-5') === 1000],
  ['valid logEvery accepted', () => normalizeLogEvery('250') === 250],
  ['logs first check', () => shouldLog(1, 1000, false) === true],
  ['skips middle check', () => shouldLog(2, 1000, false) === false],
  ['logs interval check', () => shouldLog(1000, 1000, false) === true],
  ['logs mismatch immediately', () => shouldLog(2, 1000, true) === true],
  ['dynamic snapshot shape includes ids and values', () => {
    const s = buildDynamicSnapshot('baseBetMinorUnitsScale2', 10, 1, '6275', 'dragonestone', 3000, 3000);
    return s.includes('metric=baseBetMinorUnitsScale2') && s.includes('checkCount=10') && s.includes('mismatchCount=1') && s.includes('bankId=6275') && s.includes('gameId=dragonestone') && s.includes('legacy=3000') && s.includes('generalized=3000');
  }],
  ['helper snapshot shape includes template credits', () => {
    const s = buildHelperSnapshot('templateMaxBetScale2', 20, 0, 25, 2500, 2500);
    return s.includes('metric=templateMaxBetScale2') && s.includes('templateMaxCredits=25') && s.includes('legacy=2500') && s.includes('generalized=2500');
  }],
  ['counter progression example', () => {
    const logEvery = 3;
    let checksCount = 0;
    let mismatchCount = 0;
    const logged = [];
    [true, true, true, false].forEach((matches) => {
      checksCount += 1;
      const mismatch = !matches;
      if (mismatch) mismatchCount += 1;
      if (shouldLog(checksCount, logEvery, mismatch)) logged.push(`${checksCount}:${mismatchCount}`);
    });
    return mismatchCount === 1 && logged.join(',') === '1:0,3:0,4:1';
  }],
];

let pass = 0;
let fail = 0;
console.log('# Phase 8 Wave 3 Discrepancy Evidence Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) { pass += 1; console.log(`PASS ${name}`); }
  else { fail += 1; console.log(`FAIL ${name}`); }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

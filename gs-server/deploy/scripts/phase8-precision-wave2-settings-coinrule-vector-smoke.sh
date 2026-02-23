#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime vector smoke for Phase 8 Wave 2 (game settings / coin-rule assumptions),
covering line-based base-bet normalization and nearest-coin selection under scale 2 and scale 3.
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
function pow10(scale) {
  return 10n ** BigInt(scale);
}

function parseDecimalToMinorUnits(input, scale) {
  if (!/^-?\d+(?:\.\d+)?$/.test(input)) throw new Error(`invalid decimal: ${input}`);
  const neg = input.startsWith('-');
  const raw = neg ? input.slice(1) : input;
  const [whole, frac = ''] = raw.split('.');
  if (frac.length > scale) throw new Error(`too many fractional digits: ${input}`);
  const units = BigInt(whole) * pow10(scale) + BigInt((frac + '0'.repeat(scale)).slice(0, scale) || '0');
  return neg ? -units : units;
}

function baseBetMinorUnits(defaultLines, scale, centsPerLineLegacyBase = 1) {
  // Legacy code normalizes with *100 using line count (scale=2). This generalized helper is the Phase 8 target.
  return BigInt(defaultLines) * BigInt(centsPerLineLegacyBase) * pow10(scale);
}

function coinValueToMinorUnits(coinValueStr, scale) {
  return parseDecimalToMinorUnits(coinValueStr, scale);
}

function totalBetMinorUnits(coinValueStr, defaultLines, scale) {
  const lineBaseMinor = baseBetMinorUnits(defaultLines, scale, 1); // one unit per line in normalized model
  const coinMinor = coinValueToMinorUnits(coinValueStr, scale);
  return (coinMinor * lineBaseMinor) / pow10(scale);
}

function pickNearestCoinIndex(coinValues, defaultLines, targetBetStr, scale) {
  const targetMinor = parseDecimalToMinorUnits(targetBetStr, scale);
  let best = 0;
  let bestDelta = null;
  for (let i = 0; i < coinValues.length; i++) {
    const betMinor = totalBetMinorUnits(coinValues[i], defaultLines, scale);
    const delta = betMinor >= targetMinor ? betMinor - targetMinor : targetMinor - betMinor;
    if (bestDelta === null || delta < bestDelta) {
      best = i;
      bestDelta = delta;
    }
  }
  return best;
}

function fmtMinor(units, scale) {
  const neg = units < 0n;
  const abs = neg ? -units : units;
  const base = pow10(scale);
  const whole = abs / base;
  const frac = (abs % base).toString().padStart(scale, '0');
  return `${neg ? '-' : ''}${whole}.${frac}`;
}

const checks = [
  ['legacy scale2 baseBet(30) = 30.00', () => fmtMinor(baseBetMinorUnits(30, 2), 2) === '30.00'],
  ['target scale3 baseBet(30) = 30.000', () => fmtMinor(baseBetMinorUnits(30, 3), 3) === '30.000'],
  ['30 lines * 0.01 (scale2) = 0.30', () => fmtMinor(totalBetMinorUnits('0.01', 30, 2), 2) === '0.30'],
  ['30 lines * 0.001 (scale3) = 0.030', () => fmtMinor(totalBetMinorUnits('0.001', 30, 3), 3) === '0.030'],
  ['nearest coin legacy target 0.30 picks 0.01', () => pickNearestCoinIndex(['0.01','0.02','0.05'], 30, '0.30', 2) === 0],
  ['nearest coin scale3 target 0.030 picks 0.001', () => pickNearestCoinIndex(['0.001','0.002','0.005'], 30, '0.030', 3) === 0],
  ['nearest coin scale3 target 0.060 picks 0.002', () => pickNearestCoinIndex(['0.001','0.002','0.005'], 30, '0.060', 3) === 1],
  ['nearest coin tie keeps first (stable)', () => pickNearestCoinIndex(['0.001','0.003'], 10, '0.020', 3) === 0],
  ['reject over-precision for scale2', () => {
    try { parseDecimalToMinorUnits('0.001', 2); return false; } catch (e) { return /too many fractional digits/.test(String(e.message)); }
  }],
  ['reject malformed target', () => {
    try { pickNearestCoinIndex(['0.01'], 30, '1.', 2); return false; } catch (e) { return /invalid decimal/.test(String(e.message)); }
  }],
];

let pass = 0;
let fail = 0;
console.log('# Phase 8 Wave 2 Settings/Coin-Rule Vector Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) { pass++; console.log(`PASS ${name}`); }
  else { fail++; console.log(`FAIL ${name}`); }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

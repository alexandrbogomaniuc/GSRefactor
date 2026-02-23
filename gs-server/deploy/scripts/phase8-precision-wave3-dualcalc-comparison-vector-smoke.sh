#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime vector smoke for Phase 8 Wave 3 (dual-calculation comparison scaffold).
Validates that the new generalized precision helpers preserve legacy scale=2 behavior and
provides deterministic delta visibility for future scale=3 enablement (comparison only).
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
function pow10(scale) { return 10n ** BigInt(scale); }

function parseDecimalToMinorUnits(input, scale) {
  if (!/^-?\d+(?:\.\d+)?$/.test(input)) throw new Error(`invalid decimal: ${input}`);
  const neg = input.startsWith('-');
  const raw = neg ? input.slice(1) : input;
  const [whole, frac = ''] = raw.split('.');
  if (frac.length > scale) throw new Error(`too many fractional digits: ${input}`);
  const minor = BigInt(whole) * pow10(scale) + BigInt((frac + '0'.repeat(scale)).slice(0, scale) || '0');
  return neg ? -minor : minor;
}

function fmtMinor(units, scale) {
  const neg = units < 0n;
  const abs = neg ? -units : units;
  const base = pow10(scale);
  const whole = abs / base;
  const frac = (abs % base).toString().padStart(scale, '0');
  return `${neg ? '-' : ''}${whole}.${frac}`;
}

// Legacy GS pattern: currency scale 2 hardcoded in settings/coin-rule normalization.
function legacyCurrencyMultiplier() { return 100n; }
function legacyBaseBetMinorUnits(defaultLines) { return BigInt(defaultLines) * legacyCurrencyMultiplier(); }
function legacyTemplateMaxBetMinorUnits(maxBetCurrencyStr) { return parseDecimalToMinorUnits(maxBetCurrencyStr, 2); }
function legacyTotalBetMinorUnits(coinValueStr, defaultLines) {
  return (parseDecimalToMinorUnits(coinValueStr, 2) * legacyBaseBetMinorUnits(defaultLines)) / legacyCurrencyMultiplier();
}

// Generalized Phase 8 helper path (not a behavior switch by itself).
function generalizedCurrencyMultiplier(scale) { return pow10(scale); }
function generalizedBaseBetMinorUnits(defaultLines, scale) { return BigInt(defaultLines) * generalizedCurrencyMultiplier(scale); }
function generalizedTemplateMaxBetMinorUnits(maxBetCurrencyStr, scale) { return parseDecimalToMinorUnits(maxBetCurrencyStr, scale); }
function generalizedTotalBetMinorUnits(coinValueStr, defaultLines, scale) {
  return (parseDecimalToMinorUnits(coinValueStr, scale) * generalizedBaseBetMinorUnits(defaultLines, scale)) / generalizedCurrencyMultiplier(scale);
}

function chooseNearestCoinIndex(coinValues, defaultLines, targetBetStr, scale, totalBetFn) {
  const targetMinor = parseDecimalToMinorUnits(targetBetStr, scale);
  let best = 0;
  let bestDelta = null;
  for (let i = 0; i < coinValues.length; i++) {
    const betMinor = totalBetFn(coinValues[i], defaultLines, scale);
    const delta = betMinor >= targetMinor ? betMinor - targetMinor : targetMinor - betMinor;
    if (bestDelta === null || delta < bestDelta) {
      bestDelta = delta;
      best = i;
    }
  }
  return best;
}

const checks = [];
function add(name, fn) { checks.push([name, fn]); }

// Scale=2 parity checks (legacy vs generalized path)
add('scale2 multiplier parity', () => legacyCurrencyMultiplier() === generalizedCurrencyMultiplier(2));
add('scale2 base bet parity (30 lines)', () => legacyBaseBetMinorUnits(30) === generalizedBaseBetMinorUnits(30, 2));
add('scale2 base bet parity (243 lines)', () => legacyBaseBetMinorUnits(243) === generalizedBaseBetMinorUnits(243, 2));
add('scale2 template max parity 1234.56', () => legacyTemplateMaxBetMinorUnits('1234.56') === generalizedTemplateMaxBetMinorUnits('1234.56', 2));
add('scale2 total bet parity 30 lines * 0.01', () => legacyTotalBetMinorUnits('0.01', 30) === generalizedTotalBetMinorUnits('0.01', 30, 2));
add('scale2 total bet parity 25 lines * 0.05', () => legacyTotalBetMinorUnits('0.05', 25) === generalizedTotalBetMinorUnits('0.05', 25, 2));
add('scale2 nearest coin parity target 0.30', () => {
  const legacyIdx = chooseNearestCoinIndex(['0.01','0.02','0.05'], 30, '0.30', 2, (c,l) => legacyTotalBetMinorUnits(c,l));
  const genIdx = chooseNearestCoinIndex(['0.01','0.02','0.05'], 30, '0.30', 2, generalizedTotalBetMinorUnits);
  return legacyIdx === genIdx && genIdx === 0;
});

// Scale=3 deterministic comparison-only checks (future enablement visibility)
add('scale3 generalized supports 0.001 * 30 = 0.030', () => fmtMinor(generalizedTotalBetMinorUnits('0.001', 30, 3), 3) === '0.030');
add('scale3 generalized template max 1234.567 parses', () => fmtMinor(generalizedTemplateMaxBetMinorUnits('1234.567', 3), 3) === '1234.567');
add('scale3 nearest coin target 0.060 picks 0.002', () => chooseNearestCoinIndex(['0.001','0.002','0.005'], 30, '0.060', 3, generalizedTotalBetMinorUnits) === 1);

// Delta visibility checks (legacy coercion vs future precision path)
add('delta visibility: legacy cannot parse 0.001 while scale3 can', () => {
  let legacyRejected = false;
  try { legacyTotalBetMinorUnits('0.001', 30); } catch (e) { legacyRejected = /too many fractional digits/.test(String(e.message)); }
  return legacyRejected && fmtMinor(generalizedTotalBetMinorUnits('0.001', 30, 3), 3) === '0.030';
});
add('delta visibility: malformed target still rejected', () => {
  try { chooseNearestCoinIndex(['0.01'], 30, '1.', 2, generalizedTotalBetMinorUnits); return false; } catch (e) { return /invalid decimal/.test(String(e.message)); }
});

let pass = 0;
let fail = 0;
console.log('# Phase 8 Wave 3 Dual-Calculation Comparison Vector Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) {
    pass++;
    console.log(`PASS ${name}`);
  } else {
    fail++;
    console.log(`FAIL ${name}`);
  }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

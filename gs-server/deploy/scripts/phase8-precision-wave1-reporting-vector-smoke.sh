#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime vector smoke for Phase 8 Wave 1 (reporting/display)
cent conversions and 2-decimal rounding boundaries.
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
function parseDecimalToMilli(input) {
  if (typeof input !== 'string' || !/^-?\d+(?:\.\d+)?$/.test(input)) {
    throw new Error(`invalid decimal: ${input}`);
  }
  const neg = input.startsWith('-');
  const raw = neg ? input.slice(1) : input;
  const [whole, frac = ''] = raw.split('.');
  if (frac.length > 3) throw new Error(`too many fractional digits: ${input}`);
  const milli = BigInt(whole) * 1000n + BigInt((frac + '000').slice(0, 3));
  return neg ? -milli : milli;
}

function roundMilliToCents(milli, mode) {
  const neg = milli < 0n;
  let abs = neg ? -milli : milli;
  let cents = abs / 10n;
  const rem = abs % 10n;
  if (mode === 'HALF_UP' && rem >= 5n) cents += 1n;
  if (mode === 'DOWN') {
    // truncate
  }
  if (mode !== 'HALF_UP' && mode !== 'DOWN') throw new Error(`unsupported mode: ${mode}`);
  return neg ? -cents : cents;
}

function formatCents(cents) {
  const neg = cents < 0n;
  const abs = neg ? -cents : cents;
  const whole = abs / 100n;
  const frac = (abs % 100n).toString().padStart(2, '0');
  return `${neg ? '-' : ''}${whole.toString()}.${frac}`;
}

function scoreToCentsHalfUp(scoreStr) {
  return roundMilliToCents(parseDecimalToMilli(scoreStr), 'HALF_UP');
}

const checks = [
  ['format 1 cent -> 0.01', () => formatCents(1n) === '0.01'],
  ['format 100 cents -> 1.00', () => formatCents(100n) === '1.00'],
  ['format 12345 cents -> 123.45', () => formatCents(12345n) === '123.45'],
  ['score 12.345 -> 1235 cents (HALF_UP)', () => scoreToCentsHalfUp('12.345') === 1235n],
  ['score 12.344 -> 1234 cents (HALF_UP)', () => scoreToCentsHalfUp('12.344') === 1234n],
  ['score 0.005 -> 1 cent (HALF_UP)', () => scoreToCentsHalfUp('0.005') === 1n],
  ['score 0.004 -> 0 cent (HALF_UP)', () => scoreToCentsHalfUp('0.004') === 0n],
  ['milli 19 -> 0.01 (DOWN)', () => formatCents(roundMilliToCents(19n, 'DOWN')) === '0.01'],
  ['milli 19 -> 0.02 (HALF_UP)', () => formatCents(roundMilliToCents(19n, 'HALF_UP')) === '0.02'],
  ['negative rounding preserved', () => formatCents(scoreToCentsHalfUp('-0.015')) === '-0.02'],
  ['reject malformed score', () => {
    try { scoreToCentsHalfUp('1.'); return false; } catch (e) { return /invalid decimal/.test(String(e.message)); }
  }],
  ['reject >3 decimals', () => {
    try { scoreToCentsHalfUp('0.0009'); return false; } catch (e) { return /too many fractional digits/.test(String(e.message)); }
  }],
];

let pass = 0, fail = 0;
console.log('# Phase 8 Wave 1 Reporting/Display Precision Vector Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) { pass++; console.log(`PASS ${name}`); }
  else { fail++; console.log(`FAIL ${name}`); }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

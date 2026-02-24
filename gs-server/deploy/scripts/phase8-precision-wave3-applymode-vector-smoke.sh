#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime vector smoke for Phase 8 Wave 3 scale-ready apply-mode scaffolding.
Validates disabled-by-default behavior, minor-unit scale property parsing, and scale-aware
settings/coin-rule calculations (apply-mode only when explicitly enabled).
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
function parseBool(raw) { return String(raw).toLowerCase() === 'true'; }

const LEGACY_SCALE = 2;

function resolveMinorUnitScale(raw) {
  if (raw == null || String(raw).trim() === '') return LEGACY_SCALE;
  const n = Number.parseInt(String(raw).trim(), 10);
  if (!Number.isInteger(n) || n < 0 || n > 9) return LEGACY_SCALE;
  return n;
}

function resolveApplyEnabled(raw) {
  return parseBool(raw);
}

function templateMaxBetMinorUnits(templateMaxCredits, applyEnabled, scaleRaw) {
  const scale = resolveMinorUnitScale(scaleRaw);
  const activeScale = applyEnabled ? scale : LEGACY_SCALE;
  return Number(templateMaxCredits) * Number(10 ** activeScale);
}

function parseDecimalToMinorUnits(input, scale) {
  const text = String(input);
  if (!/^-?\d+(?:\.\d+)?$/.test(text)) throw new Error(`invalid decimal: ${text}`);
  const neg = text.startsWith('-');
  const raw = neg ? text.slice(1) : text;
  const [whole, frac = ''] = raw.split('.');
  if (frac.length > scale) throw new Error(`too many fractional digits: ${text}`);
  const units = BigInt(whole) * pow10(scale) + BigInt((frac + '0'.repeat(scale)).slice(0, scale) || '0');
  return neg ? -units : units;
}

function baseBetMinorUnits(defaultLines, applyEnabled, scaleRaw) {
  const scale = resolveMinorUnitScale(scaleRaw);
  const activeScale = applyEnabled ? scale : LEGACY_SCALE;
  return BigInt(defaultLines) * pow10(activeScale);
}

function totalBetMinorUnits(coinValue, defaultLines, applyEnabled, scaleRaw) {
  const scale = resolveMinorUnitScale(scaleRaw);
  const activeScale = applyEnabled ? scale : LEGACY_SCALE;
  const base = baseBetMinorUnits(defaultLines, applyEnabled, scaleRaw);
  const coinMinor = parseDecimalToMinorUnits(coinValue, activeScale);
  return (coinMinor * base) / pow10(activeScale);
}

function fmtMinor(units, scale) {
  const base = pow10(scale);
  const whole = units / base;
  const frac = (units % base).toString().padStart(scale, '0');
  return `${whole}.${frac}`;
}

const checks = [
  ['apply mode disabled by default', () => resolveApplyEnabled(undefined) === false],
  ['empty scale defaults to legacy scale2', () => resolveMinorUnitScale('') === 2],
  ['invalid scale falls back to legacy scale2', () => resolveMinorUnitScale('abc') === 2 && resolveMinorUnitScale('12') === 2],
  ['enabled scale3 selected when configured', () => resolveApplyEnabled('true') && resolveMinorUnitScale('3') === 3],
  ['template max uses legacy scale2 when apply disabled', () => templateMaxBetMinorUnits('1234.567', false, '3') === 123456.7],
  ['template max uses scale3 when apply enabled', () => templateMaxBetMinorUnits('1234.567', true, '3') === 1234567],
  ['base bet uses scale2 when apply disabled', () => baseBetMinorUnits(30, false, '3') === 3000n],
  ['base bet uses scale3 when apply enabled', () => baseBetMinorUnits(30, true, '3') === 30000n],
  ['30 lines * 0.001 with apply enabled scale3 = 0.030', () => fmtMinor(totalBetMinorUnits('0.001', 30, true, '3'), 3) === '0.030'],
  ['30 lines * 0.001 with apply disabled rejects legacy scale2 precision', () => {
    try { totalBetMinorUnits('0.001', 30, false, '3'); return false; } catch (e) { return /too many fractional digits/.test(String(e.message)); }
  }],
  ['scale0 is supported in resolver', () => resolveMinorUnitScale('0') === 0],
  ['apply enabled with invalid scale still legacy scale2', () => baseBetMinorUnits(7, true, 'bad') === 700n],
];

let pass = 0;
let fail = 0;
console.log('# Phase 8 Wave 3 Apply-Mode Vector Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) { pass++; console.log(`PASS ${name}`); }
  else { fail++; console.log(`FAIL ${name}`); }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

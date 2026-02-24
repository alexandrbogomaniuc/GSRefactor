#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime vector smoke for Phase 8 wallet contract and rounding handling.
Validates minor-unit roundtrip, fixed-scale canonical decimal formatting, and JSON HMAC sensitivity.
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
const crypto = require('crypto');

function pow10(scale) { return 10n ** BigInt(scale); }
function parseMinor(input, scale) {
  const s = String(input);
  if (!/^-?\d+(?:\.\d+)?$/.test(s)) throw new Error(`invalid:${s}`);
  const neg = s.startsWith('-');
  const raw = neg ? s.slice(1) : s;
  const [w, f=''] = raw.split('.');
  if (f.length > scale) throw new Error(`overprecision:${s}`);
  const units = BigInt(w) * pow10(scale) + BigInt((f + '0'.repeat(scale)).slice(0, scale) || '0');
  return neg ? -units : units;
}
function fmtMinor(units, scale) {
  const neg = units < 0n;
  const abs = neg ? -units : units;
  const base = pow10(scale);
  const whole = abs / base;
  const frac = (abs % base).toString().padStart(scale, '0');
  return `${neg ? '-' : ''}${whole}.${frac}`;
}
function hmacHex(secret, text) {
  return crypto.createHmac('sha256', secret).update(text, 'utf8').digest('hex');
}
function canonicalWalletBody(payload, scaleMap) {
  const out = {};
  for (const [k, v] of Object.entries(payload)) {
    if (Object.prototype.hasOwnProperty.call(scaleMap, k)) {
      out[k] = fmtMinor(parseMinor(v, scaleMap[k]), scaleMap[k]);
    } else {
      out[k] = v;
    }
  }
  return JSON.stringify(out);
}

const checks = [
  ['scale2 roundtrip 12.34', () => fmtMinor(parseMinor('12.34', 2), 2) === '12.34'],
  ['scale3 roundtrip 12.345', () => fmtMinor(parseMinor('12.345', 3), 3) === '12.345'],
  ['scale3 rejects 4dp', () => { try { parseMinor('0.0001', 3); return false; } catch (e) { return String(e.message).startsWith('overprecision:'); } }],
  ['canonical wallet body fixes trailing zeros for scale3', () => canonicalWalletBody({bankId:6275, amount:'0.03', balance:'12.5'}, {amount:3,balance:3}) === '{"bankId":6275,"amount":"0.030","balance":"12.500"}'],
  ['canonical wallet body preserves strings/ids', () => canonicalWalletBody({operationId:'op-1', amount:'1', currency:'KWD'}, {amount:3}) === '{"operationId":"op-1","amount":"1.000","currency":"KWD"}'],
  ['HMAC changes when decimal formatting changes', () => {
    const secret = 'redacted-test-secret';
    const a = hmacHex(secret, '{"amount":"0.03"}');
    const b = hmacHex(secret, '{"amount":"0.030"}');
    return a !== b;
  }],
  ['HMAC stable for same canonical body', () => {
    const secret = 'redacted-test-secret';
    const body = canonicalWalletBody({bankId:6275, amount:'0.03'}, {amount:3});
    return hmacHex(secret, body) === hmacHex(secret, body);
  }],
  ['minor-unit sum exact scale3 (wallet settle)', () => fmtMinor(parseMinor('10.001',3) - parseMinor('0.001',3) + parseMinor('0.500',3), 3) === '10.500'],
  ['legacy scale2 rejects thousandth wallet amount', () => { try { canonicalWalletBody({amount:'0.001'}, {amount:2}); return false; } catch (e) { return String(e.message).startsWith('overprecision:'); } }],
  ['negative adjustment canonical formatting', () => canonicalWalletBody({adjustment:'-0.125'}, {adjustment:3}) === '{"adjustment":"-0.125"}'],
];

let pass = 0, fail = 0;
console.log('# Phase 8 Wallet Contract/Rounding Precision Vector Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) { pass++; console.log(`PASS ${name}`); }
  else { fail++; console.log(`FAIL ${name}`); }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic non-runtime vector smoke for Phase 8 history/reporting/export precision handling.
Validates integer-minor-unit aggregation and fixed-scale export formatting for scale2 and scale3.
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
function sumAmounts(list, scale) {
  return list.reduce((acc, x) => acc + parseMinor(x, scale), 0n);
}
function csvRow(fields) {
  return fields.map(v => String(v).includes(',') ? `"${String(v).replace(/"/g,'""')}"` : String(v)).join(',');
}
function exportHistoryRow(event, scale) {
  return csvRow([
    event.bankId,
    event.userId,
    event.currency,
    fmtMinor(parseMinor(event.wager, scale), scale),
    fmtMinor(parseMinor(event.win, scale), scale),
    fmtMinor(parseMinor(event.balanceAfter, scale), scale)
  ]);
}

const checks = [
  ['scale2 sum exact 0.10+0.20+0.30=0.60', () => fmtMinor(sumAmounts(['0.10','0.20','0.30'], 2), 2) === '0.60'],
  ['scale3 sum exact 0.001+0.002+0.003=0.006', () => fmtMinor(sumAmounts(['0.001','0.002','0.003'], 3), 3) === '0.006'],
  ['scale3 mixed sum exact 1.234+2.000-0.001=3.233', () => fmtMinor(sumAmounts(['1.234','2.000','-0.001'], 3), 3) === '3.233'],
  ['scale2 export row keeps fixed 2dp', () => exportHistoryRow({bankId:6275,userId:'u1',currency:'USD',wager:'0.3',win:'1',balanceAfter:'12.5'}, 2) === '6275,u1,USD,0.30,1.00,12.50'],
  ['scale3 export row keeps fixed 3dp', () => exportHistoryRow({bankId:6275,userId:'u1',currency:'KWD',wager:'0.03',win:'1.234',balanceAfter:'12.5'}, 3) === '6275,u1,KWD,0.030,1.234,12.500'],
  ['scale3 csv quoting works', () => exportHistoryRow({bankId:6275,userId:'u,1',currency:'KWD',wager:'0.03',win:'0',balanceAfter:'0'}, 3).startsWith('6275,"u,1",KWD,0.030,0.000,0.000')],
  ['legacy scale2 rejects 3dp export input', () => { try { exportHistoryRow({bankId:1,userId:'u',currency:'USD',wager:'0.001',win:'0',balanceAfter:'0'}, 2); return false; } catch (e) { return String(e.message).startsWith('overprecision:'); } }],
  ['invalid amount rejected', () => { try { sumAmounts(['1.2.3'], 2); return false; } catch (e) { return String(e.message).startsWith('invalid:'); } }],
  ['scale3 thousandth wager+win total exact', () => fmtMinor(sumAmounts(['0.001','0.009'], 3), 3) === '0.010'],
  ['negative sign formatting preserved', () => fmtMinor(parseMinor('-0.125', 3), 3) === '-0.125'],
];

let pass = 0, fail = 0;
console.log('# Phase 8 History/Reporting Export Precision Vector Smoke');
for (const [name, fn] of checks) {
  let ok = false;
  try { ok = !!fn(); } catch (_) { ok = false; }
  if (ok) { pass++; console.log(`PASS ${name}`); }
  else { fail++; console.log(`FAIL ${name}`); }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail) process.exit(1);
NODE

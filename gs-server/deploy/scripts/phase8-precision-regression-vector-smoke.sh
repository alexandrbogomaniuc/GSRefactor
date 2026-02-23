#!/usr/bin/env bash
set -euo pipefail

SCALE=3

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --scale N       Decimal scale for test vectors (default: ${SCALE})
  -h, --help      Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --scale)
      SCALE="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

node - "$SCALE" <<'NODE'
const scale = Number(process.argv[2]);
if (!Number.isInteger(scale) || scale < 0 || scale > 9) {
  console.error(`Invalid scale: ${process.argv[2]}`);
  process.exit(2);
}

function pow10(n) {
  return BigInt('1' + '0'.repeat(n));
}

function parseDecimalToUnits(input, scale) {
  if (typeof input !== 'string' || !/^-?\d+(?:\.\d+)?$/.test(input)) {
    throw new Error(`invalid decimal format: ${input}`);
  }
  const neg = input.startsWith('-');
  const raw = neg ? input.slice(1) : input;
  const [intPart, fracPart = ''] = raw.split('.');
  if (fracPart.length > scale) {
    throw new Error(`too many fractional digits for scale=${scale}: ${input}`);
  }
  const units = BigInt(intPart) * pow10(scale) + BigInt((fracPart + '0'.repeat(scale)).slice(0, scale) || '0');
  return neg ? -units : units;
}

function formatUnits(units, scale) {
  const neg = units < 0n;
  const abs = neg ? -units : units;
  const base = pow10(scale);
  const whole = abs / base;
  const frac = (abs % base).toString().padStart(scale, '0');
  return `${neg ? '-' : ''}${whole.toString()}${scale > 0 ? '.' + frac : ''}`;
}

function mulDecimalByInt(decimalString, multiplier, scale) {
  const units = parseDecimalToUnits(decimalString, scale);
  return units * BigInt(multiplier);
}

const checks = [
  { name: 'parse 0.001 -> 1 unit', fn: () => parseDecimalToUnits('0.001', scale) === 1n },
  { name: 'parse 0.01 -> 10 units', fn: () => parseDecimalToUnits('0.01', scale) === 10n },
  { name: 'parse 0.3 -> 300 units', fn: () => parseDecimalToUnits('0.3', scale) === 300n },
  { name: 'format 300 units -> 0.300', fn: () => formatUnits(300n, scale) === '0.300' },
  { name: '30 lines * 0.001 = 0.030', fn: () => formatUnits(mulDecimalByInt('0.001', 30, scale), scale) === '0.030' },
  { name: '30 lines * 0.01 = 0.300', fn: () => formatUnits(mulDecimalByInt('0.01', 30, scale), scale) === '0.300' },
  { name: '25 lines * 0.004 = 0.100', fn: () => formatUnits(mulDecimalByInt('0.004', 25, scale), scale) === '0.100' },
  { name: 'sum preserves exact thousandths', fn: () => (parseDecimalToUnits('0.001', scale) + parseDecimalToUnits('0.002', scale) + parseDecimalToUnits('0.007', scale)) === 10n },
  { name: 'reject > scale precision (0.0009)', fn: () => {
      try { parseDecimalToUnits('0.0009', scale); return false; }
      catch (e) { return /too many fractional digits/.test(String(e.message)); }
    }
  },
  { name: 'reject malformed decimal', fn: () => {
      try { parseDecimalToUnits('1.', scale); return false; }
      catch (e) { return /invalid decimal format/.test(String(e.message)); }
    }
  },
];

let pass = 0;
let fail = 0;
console.log(`# Phase 8 Precision Regression Vector Smoke (scale=${scale})`);
for (const c of checks) {
  let ok = false;
  try { ok = Boolean(c.fn()); } catch (e) { ok = false; }
  if (ok) {
    pass += 1;
    console.log(`PASS ${c.name}`);
  } else {
    fail += 1;
    console.log(`FAIL ${c.name}`);
  }
}
console.log(`summary pass=${pass} fail=${fail}`);
if (fail > 0) process.exit(1);
NODE

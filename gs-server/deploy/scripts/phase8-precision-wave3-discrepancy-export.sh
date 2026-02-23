#!/usr/bin/env bash
set -euo pipefail

LOG_FILES=()
OUT_FILE=""
PRETTY="true"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Parse Phase 8 Wave 3 discrepancy snapshot log lines emitted by GS parity hooks
(phase8-precision-dual-calc ...) and export a structured JSON summary.

Options:
  --log-file FILE      Input log file (repeatable). If omitted, reads stdin.
  --out-file FILE      Write JSON output to file (default: stdout)
  --pretty BOOL        true|false pretty JSON (default: true)
  -h, --help           Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --log-file)
      LOG_FILES+=("$2"); shift 2 ;;
    --out-file)
      OUT_FILE="$2"; shift 2 ;;
    --pretty)
      PRETTY="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ "${PRETTY}" != "true" && "${PRETTY}" != "false" ]]; then
  echo "--pretty must be true or false" >&2
  exit 1
fi

TMP_IN="$(mktemp)"
trap 'rm -f "${TMP_IN}"' EXIT

if [[ ${#LOG_FILES[@]} -eq 0 ]]; then
  cat > "${TMP_IN}"
else
  for f in "${LOG_FILES[@]}"; do
    if [[ ! -f "${f}" ]]; then
      echo "Missing log file: ${f}" >&2
      exit 1
    fi
    cat "${f}" >> "${TMP_IN}"
    printf '\n' >> "${TMP_IN}"
  done
fi

node - "${TMP_IN}" "${OUT_FILE}" "${PRETTY}" "${#LOG_FILES[@]}" "${LOG_FILES[@]:-}" <<'NODE'
const fs = require('fs');

const argv = process.argv.slice(2);
const tmpIn = argv[0];
const outFile = argv[1] || '';
const pretty = argv[2] === 'true';
const fileCount = Number(argv[3] || '0');
const sourceFiles = fileCount > 0 ? argv.slice(4, 4 + fileCount) : ['stdin'];
const text = fs.readFileSync(tmpIn, 'utf8');
const lines = text.split(/\r?\n/);

function parseSnapshotLine(line) {
  const marker = 'phase8-precision-dual-calc ';
  const idx = line.indexOf(marker);
  if (idx < 0) return null;
  const tail = line.slice(idx + marker.length).trim();
  if (!tail) return null;
  const parts = tail.split(/\s+/);
  const kv = {};
  for (const part of parts) {
    const eq = part.indexOf('=');
    if (eq <= 0) continue;
    const key = part.slice(0, eq);
    const value = part.slice(eq + 1);
    kv[key] = value;
  }
  if (!kv.metric) return null;
  return kv;
}

function toNumMaybe(v) {
  if (v == null) return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

const entries = [];
for (const line of lines) {
  const parsed = parseSnapshotLine(line);
  if (parsed) entries.push(parsed);
}

const metrics = {};
for (const e of entries) {
  const key = e.metric;
  const m = metrics[key] || {
    metric: key,
    snapshots: 0,
    maxCheckCount: 0,
    maxMismatchCount: 0,
    lastCheckCount: null,
    lastMismatchCount: null,
    lastBankId: null,
    lastGameId: null,
    lastTemplateMaxCredits: null,
    lastLegacy: null,
    lastGeneralized: null,
    mismatchEventsObserved: 0
  };
  m.snapshots += 1;
  const checkCount = toNumMaybe(e.checkCount);
  const mismatchCount = toNumMaybe(e.mismatchCount);
  const legacy = toNumMaybe(e.legacy);
  const generalized = toNumMaybe(e.generalized);
  if (checkCount != null) {
    m.maxCheckCount = Math.max(m.maxCheckCount, checkCount);
    m.lastCheckCount = checkCount;
  }
  if (mismatchCount != null) {
    m.maxMismatchCount = Math.max(m.maxMismatchCount, mismatchCount);
    m.lastMismatchCount = mismatchCount;
    if (mismatchCount > 0) m.mismatchEventsObserved += 1;
  }
  if (e.bankId != null) m.lastBankId = e.bankId;
  if (e.gameId != null) m.lastGameId = e.gameId;
  if (e.templateMaxCredits != null) m.lastTemplateMaxCredits = e.templateMaxCredits;
  if (legacy != null) m.lastLegacy = legacy;
  if (generalized != null) m.lastGeneralized = generalized;
  metrics[key] = m;
}

const out = {
  generatedAtUtc: new Date().toISOString(),
  parser: 'phase8-precision-wave3-discrepancy-export',
  sourceFiles,
  totalSnapshotLines: entries.length,
  metricCount: Object.keys(metrics).length,
  metrics
};

const json = JSON.stringify(out, null, pretty ? 2 : 0);
if (outFile) {
  fs.writeFileSync(outFile, json + '\n', 'utf8');
  console.log(`out=${outFile}`);
}
console.log(`summary totalSnapshotLines=${out.totalSnapshotLines} metricCount=${out.metricCount}`);
if (!outFile) {
  process.stdout.write(json + '\n');
}
NODE

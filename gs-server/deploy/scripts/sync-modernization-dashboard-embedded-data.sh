#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support"
HTML_FILE="${ROOT}/modernizationProgress.html"
CHECKLIST_JSON="${ROOT}/data/modernization-checklist.json"
OUTBOX_JSON="${ROOT}/data/session-outbox-health.json"
READINESS_REPORT_GLOB="/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/program-deploy-readiness-status-*.md"
READINESS_REPORT=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --html FILE        Default: ${HTML_FILE}
  --checklist FILE   Default: ${CHECKLIST_JSON}
  --outbox FILE      Default: ${OUTBOX_JSON}
  --readiness FILE   Default: latest from ${READINESS_REPORT_GLOB}
  -h, --help         Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --html)
      HTML_FILE="$2"; shift 2 ;;
    --checklist)
      CHECKLIST_JSON="$2"; shift 2 ;;
    --outbox)
      OUTBOX_JSON="$2"; shift 2 ;;
    --readiness)
      READINESS_REPORT="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ -z "${READINESS_REPORT}" ]]; then
  READINESS_REPORT="$(ls -1t ${READINESS_REPORT_GLOB} 2>/dev/null | head -n 1 || true)"
fi

for f in "${HTML_FILE}" "${CHECKLIST_JSON}" "${OUTBOX_JSON}"; do
  if [[ ! -f "${f}" ]]; then
    echo "Missing file: ${f}" >&2
    exit 1
  fi
done

if [[ -n "${READINESS_REPORT}" && ! -f "${READINESS_REPORT}" ]]; then
  echo "Missing readiness report: ${READINESS_REPORT}" >&2
  exit 1
fi

tmp_file="$(mktemp)"
trap 'rm -f "${tmp_file}"' EXIT

node - "${HTML_FILE}" "${CHECKLIST_JSON}" "${OUTBOX_JSON}" "${READINESS_REPORT}" "${tmp_file}" <<'NODE'
const fs = require('fs');
const crypto = require('crypto');

const [htmlFile, checklistFile, outboxFile, readinessFile, outFile] = process.argv.slice(2);
const html = fs.readFileSync(htmlFile, 'utf8');
const checklist = JSON.parse(fs.readFileSync(checklistFile, 'utf8'));
const outbox = JSON.parse(fs.readFileSync(outboxFile, 'utf8'));
const syncedAt = new Date().toISOString();

function enrichEmbedded(obj) {
  const clone = JSON.parse(JSON.stringify(obj));
  const fingerprint = crypto.createHash('sha1')
    .update(JSON.stringify(obj))
    .digest('hex')
    .slice(0, 12);
  clone.__embeddedSyncedAtUtc = syncedAt;
  clone.__embeddedFingerprint = fingerprint;
  return clone;
}

function replaceScriptJson(source, scriptId, obj) {
  const pattern = new RegExp(`(<script id="${scriptId}" type="application/json">)([\\s\\S]*?)(</script>)`);
  if (!pattern.test(source)) {
    throw new Error(`script tag not found: ${scriptId}`);
  }
  const pretty = '\n' + JSON.stringify(obj, null, 2) + '\n  ';
  return source.replace(pattern, `$1${pretty}$3`);
}

function parseReadinessMarkdown(text, sourcePath) {
  if (!text) {
    return {
      status: 'UNKNOWN',
      reason: 'readiness_report_missing',
      source: sourcePath || 'none'
    };
  }
  const lines = text.split(/\r?\n/);
  const meta = {};
  const blockers = [];
  for (const line of lines) {
    let m = line.match(/^- Checklist completion:\s*(.+)$/);
    if (m) { meta.checklistCompletion = m[1].trim(); continue; }
    m = line.match(/^- overall_status:\s*(.+)$/);
    if (m) { meta.overallStatus = m[1].trim(); continue; }
    m = line.match(/^- phase4_protocol_status:\s*(.+)$/);
    if (m) { meta.phase4ProtocolStatus = m[1].trim(); continue; }
    m = line.match(/^- phase5_6_extraction_status:\s*(.+)$/);
    if (m) { meta.phase56ExtractionStatus = m[1].trim(); continue; }
    m = line.match(/^- phase7_cassandra_status:\s*(.+)$/);
    if (m) { meta.phase7CassandraStatus = m[1].trim(); continue; }
    m = line.match(/^- security_hardening_status:\s*(.+)$/);
    if (m) { meta.securityHardeningStatus = m[1].trim(); continue; }
    m = line.match(/^\|\s*([^|]+?)\s*\|\s*([A-Z]+)\s*\|\s*([^|]+?)\s*\|$/);
    if (m && m[1].trim() !== 'Blocker' && m[1].trim() !== 'blocker') {
      blockers.push({
        id: m[1].trim(),
        severity: m[2].trim(),
        state: m[3].trim()
      });
    }
  }
  if (!meta.phase7CassandraStatus) {
    const cassandraBlocker = blockers.find((b) => /cassandra/i.test(b.id));
    if (cassandraBlocker) {
      meta.phase7CassandraStatus = cassandraBlocker.state;
    }
  }
  return {
    status: meta.overallStatus || 'UNKNOWN',
    checklistCompletion: meta.checklistCompletion || null,
    phase4ProtocolStatus: meta.phase4ProtocolStatus || null,
    phase56ExtractionStatus: meta.phase56ExtractionStatus || null,
    phase7CassandraStatus: meta.phase7CassandraStatus || null,
    securityHardeningStatus: meta.securityHardeningStatus || null,
    blockers,
    source: sourcePath || 'none'
  };
}

let next = html;
const embeddedChecklist = enrichEmbedded(checklist);
const embeddedOutbox = enrichEmbedded(outbox);
const readinessText = readinessFile ? fs.readFileSync(readinessFile, 'utf8') : '';
const embeddedReadiness = enrichEmbedded(parseReadinessMarkdown(readinessText, readinessFile || 'none'));
next = replaceScriptJson(next, 'embedded-checklist', embeddedChecklist);
next = replaceScriptJson(next, 'embedded-outbox-health', embeddedOutbox);
next = replaceScriptJson(next, 'embedded-deploy-readiness', embeddedReadiness);

fs.writeFileSync(outFile, next, 'utf8');

const count = (m) => (m.sections || []).reduce((a, s) => a + (s.items || []).length, 0);
const done = (m) => (m.sections || []).reduce((a, s) => a + (s.items || []).filter(i => i.status === 'done').length, 0);
console.log(`embedded-checklist synced: ${done(checklist)}/${count(checklist)} updatedAt=${checklist.updatedAt || '-'} embeddedSyncedAt=${embeddedChecklist.__embeddedSyncedAtUtc} fp=${embeddedChecklist.__embeddedFingerprint}`);
console.log(`embedded-outbox-health synced: updatedAt=${outbox.updatedAt || '-'} embeddedSyncedAt=${embeddedOutbox.__embeddedSyncedAtUtc} fp=${embeddedOutbox.__embeddedFingerprint}`);
console.log(`embedded-deploy-readiness synced: status=${embeddedReadiness.status || '-'} source=${embeddedReadiness.source || '-'} embeddedSyncedAt=${embeddedReadiness.__embeddedSyncedAtUtc} fp=${embeddedReadiness.__embeddedFingerprint}`);
NODE

mv "${tmp_file}" "${HTML_FILE}"
trap - EXIT

echo "Updated: ${HTML_FILE}"

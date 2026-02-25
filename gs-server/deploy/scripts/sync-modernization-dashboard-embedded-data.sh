#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
SUPPORT_ROOT="${REPO_ROOT}/gs-server/game-server/web-gs/src/main/webapp/support"
DOCS_ROOT="${REPO_ROOT}/docs"

HTML_FILE="${SUPPORT_ROOT}/modernizationProgress.html"
CHECKLIST_JSON="${SUPPORT_ROOT}/data/modernization-checklist.json"
OUTBOX_JSON="${SUPPORT_ROOT}/data/session-outbox-health.json"
AUDIT_REQ_JSON="${SUPPORT_ROOT}/data/audit-requirements-status.json"
AUDIT_SCOPE_JSON="${SUPPORT_ROOT}/data/audit-scope-summary.json"
READINESS_REPORT_GLOB="${DOCS_ROOT}/release-readiness/program-deploy-readiness-status-*.md"
READINESS_REPORT=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --repo-root DIR     Default: ${REPO_ROOT}
  --html FILE        Default: ${HTML_FILE}
  --checklist FILE   Default: ${CHECKLIST_JSON}
  --outbox FILE      Default: ${OUTBOX_JSON}
  --audit-req FILE   Default: ${AUDIT_REQ_JSON}
  --audit-scope FILE Default: ${AUDIT_SCOPE_JSON}
  --readiness FILE   Default: latest from ${READINESS_REPORT_GLOB}
  -h, --help         Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo-root)
      REPO_ROOT="$2"
      SUPPORT_ROOT="${REPO_ROOT}/gs-server/game-server/web-gs/src/main/webapp/support"
      DOCS_ROOT="${REPO_ROOT}/docs"
      HTML_FILE="${SUPPORT_ROOT}/modernizationProgress.html"
      CHECKLIST_JSON="${SUPPORT_ROOT}/data/modernization-checklist.json"
      OUTBOX_JSON="${SUPPORT_ROOT}/data/session-outbox-health.json"
      AUDIT_REQ_JSON="${SUPPORT_ROOT}/data/audit-requirements-status.json"
      AUDIT_SCOPE_JSON="${SUPPORT_ROOT}/data/audit-scope-summary.json"
      READINESS_REPORT_GLOB="${DOCS_ROOT}/release-readiness/program-deploy-readiness-status-*.md"
      shift 2 ;;
    --html)
      HTML_FILE="$2"; shift 2 ;;
    --checklist)
      CHECKLIST_JSON="$2"; shift 2 ;;
    --outbox)
      OUTBOX_JSON="$2"; shift 2 ;;
    --audit-req)
      AUDIT_REQ_JSON="$2"; shift 2 ;;
    --audit-scope)
      AUDIT_SCOPE_JSON="$2"; shift 2 ;;
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

for optional_json in "${AUDIT_REQ_JSON}" "${AUDIT_SCOPE_JSON}"; do
  if [[ ! -f "${optional_json}" ]]; then
    echo "Warning: optional dashboard data file not found, will embed fallback object: ${optional_json}" >&2
  fi
done

tmp_file="$(mktemp)"
trap 'rm -f "${tmp_file}"' EXIT

node - "${HTML_FILE}" "${CHECKLIST_JSON}" "${OUTBOX_JSON}" "${READINESS_REPORT}" "${AUDIT_REQ_JSON}" "${AUDIT_SCOPE_JSON}" "${tmp_file}" <<'NODE'
const fs = require('fs');
const crypto = require('crypto');

const [htmlFile, checklistFile, outboxFile, readinessFile, auditReqFile, auditScopeFile, outFile] = process.argv.slice(2);
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

function hasScriptTag(source, scriptId) {
  const pattern = new RegExp(`(<script id="${scriptId}" type="application/json">)([\\s\\S]*?)(</script>)`);
  return pattern.test(source);
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
    m = line.match(/^- legacy_mixed_topology_status:\s*(.+)$/);
    if (m) { meta.legacyMixedTopologyStatus = m[1].trim(); continue; }
    m = line.match(/^- phase7_cassandra_rehearsal_no_go:\s*(.+)$/);
    if (m) { meta.phase7CassandraRehearsalNoGo = m[1].trim(); continue; }
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
    phase7CassandraRehearsalNoGo: meta.phase7CassandraRehearsalNoGo || null,
    legacyMixedTopologyStatus: meta.legacyMixedTopologyStatus || null,
    securityHardeningStatus: meta.securityHardeningStatus || null,
    blockers,
    source: sourcePath || 'none'
  };
}

function loadOptionalJson(path, fallbackObj) {
  if (!path || !fs.existsSync(path)) {
    return fallbackObj;
  }
  try {
    return JSON.parse(fs.readFileSync(path, 'utf8'));
  } catch (err) {
    return { ...fallbackObj, reason: `parse_error: ${String(err)}`, source: path };
  }
}

let next = html;
const embeddedChecklist = enrichEmbedded(checklist);
const embeddedOutbox = enrichEmbedded(outbox);
const readinessText = readinessFile ? fs.readFileSync(readinessFile, 'utf8') : '';
const embeddedReadiness = enrichEmbedded(parseReadinessMarkdown(readinessText, readinessFile || 'none'));
const embeddedAuditRequirements = enrichEmbedded(loadOptionalJson(auditReqFile, {
  updatedAt: null,
  reason: 'audit_requirements_status_missing',
  source: auditReqFile || 'none',
  summaryCounts: {},
  requirements: []
}));
const embeddedAuditScope = enrichEmbedded(loadOptionalJson(auditScopeFile, {
  updatedAt: null,
  reason: 'audit_scope_summary_missing',
  source: auditScopeFile || 'none',
  coreScope: {},
  currentCutoverBlockers: [],
  latestEvidenceSources: [],
  whatNeedsApprovalNext: [],
  parallelWorkstreams: [],
  scopeCreepExamples: []
}));
next = replaceScriptJson(next, 'embedded-checklist', embeddedChecklist);
next = replaceScriptJson(next, 'embedded-outbox-health', embeddedOutbox);
next = replaceScriptJson(next, 'embedded-deploy-readiness', embeddedReadiness);
if (hasScriptTag(next, 'embedded-audit-requirements')) {
  next = replaceScriptJson(next, 'embedded-audit-requirements', embeddedAuditRequirements);
}
if (hasScriptTag(next, 'embedded-audit-scope')) {
  next = replaceScriptJson(next, 'embedded-audit-scope', embeddedAuditScope);
}

fs.writeFileSync(outFile, next, 'utf8');

const count = (m) => (m.sections || []).reduce((a, s) => a + (s.items || []).length, 0);
const done = (m) => (m.sections || []).reduce((a, s) => a + (s.items || []).filter(i => i.status === 'done').length, 0);
console.log(`embedded-checklist synced: ${done(checklist)}/${count(checklist)} updatedAt=${checklist.updatedAt || '-'} embeddedSyncedAt=${embeddedChecklist.__embeddedSyncedAtUtc} fp=${embeddedChecklist.__embeddedFingerprint}`);
console.log(`embedded-outbox-health synced: updatedAt=${outbox.updatedAt || '-'} embeddedSyncedAt=${embeddedOutbox.__embeddedSyncedAtUtc} fp=${embeddedOutbox.__embeddedFingerprint}`);
console.log(`embedded-deploy-readiness synced: status=${embeddedReadiness.status || '-'} source=${embeddedReadiness.source || '-'} embeddedSyncedAt=${embeddedReadiness.__embeddedSyncedAtUtc} fp=${embeddedReadiness.__embeddedFingerprint}`);
console.log(`embedded-audit-requirements synced: updatedAt=${embeddedAuditRequirements.updatedAt || '-'} source=${auditReqFile || '-'} embeddedSyncedAt=${embeddedAuditRequirements.__embeddedSyncedAtUtc} fp=${embeddedAuditRequirements.__embeddedFingerprint}`);
console.log(`embedded-audit-scope synced: updatedAt=${embeddedAuditScope.updatedAt || '-'} source=${auditScopeFile || '-'} embeddedSyncedAt=${embeddedAuditScope.__embeddedSyncedAtUtc} fp=${embeddedAuditScope.__embeddedFingerprint}`);
NODE

mv "${tmp_file}" "${HTML_FILE}"
trap - EXIT

echo "Updated: ${HTML_FILE}"

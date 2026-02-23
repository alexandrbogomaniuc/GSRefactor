#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support"
HTML_FILE="${ROOT}/modernizationProgress.html"
CHECKLIST_JSON="${ROOT}/data/modernization-checklist.json"
OUTBOX_JSON="${ROOT}/data/session-outbox-health.json"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --html FILE        Default: ${HTML_FILE}
  --checklist FILE   Default: ${CHECKLIST_JSON}
  --outbox FILE      Default: ${OUTBOX_JSON}
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
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

for f in "${HTML_FILE}" "${CHECKLIST_JSON}" "${OUTBOX_JSON}"; do
  if [[ ! -f "${f}" ]]; then
    echo "Missing file: ${f}" >&2
    exit 1
  fi
done

tmp_file="$(mktemp)"
trap 'rm -f "${tmp_file}"' EXIT

node - "${HTML_FILE}" "${CHECKLIST_JSON}" "${OUTBOX_JSON}" "${tmp_file}" <<'NODE'
const fs = require('fs');

const [htmlFile, checklistFile, outboxFile, outFile] = process.argv.slice(2);
const html = fs.readFileSync(htmlFile, 'utf8');
const checklist = JSON.parse(fs.readFileSync(checklistFile, 'utf8'));
const outbox = JSON.parse(fs.readFileSync(outboxFile, 'utf8'));

function replaceScriptJson(source, scriptId, obj) {
  const pattern = new RegExp(`(<script id="${scriptId}" type="application/json">)([\\s\\S]*?)(</script>)`);
  if (!pattern.test(source)) {
    throw new Error(`script tag not found: ${scriptId}`);
  }
  const pretty = '\n' + JSON.stringify(obj, null, 2) + '\n  ';
  return source.replace(pattern, `$1${pretty}$3`);
}

let next = html;
next = replaceScriptJson(next, 'embedded-checklist', checklist);
next = replaceScriptJson(next, 'embedded-outbox-health', outbox);

fs.writeFileSync(outFile, next, 'utf8');

const count = (m) => (m.sections || []).reduce((a, s) => a + (s.items || []).length, 0);
const done = (m) => (m.sections || []).reduce((a, s) => a + (s.items || []).filter(i => i.status === 'done').length, 0);
console.log(`embedded-checklist synced: ${done(checklist)}/${count(checklist)} updatedAt=${checklist.updatedAt || '-'}`);
console.log(`embedded-outbox-health synced: updatedAt=${outbox.updatedAt || '-'}`);
NODE

mv "${tmp_file}" "${HTML_FILE}"
trap - EXIT

echo "Updated: ${HTML_FILE}"

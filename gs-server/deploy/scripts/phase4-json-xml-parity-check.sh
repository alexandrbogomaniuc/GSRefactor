#!/usr/bin/env bash
set -euo pipefail

BANK_ID="6275"
BASE_URL="http://127.0.0.1:18078"
ENDPOINT="/wallet/reserve"
METHOD="POST"
SESSION_ID="parity-session-6275"
OPERATION_ID="parity-op-001"
ROUND_ID="parity-round-001"
AMOUNT="100"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID         Default: ${BANK_ID}
  --base-url URL       Default: ${BASE_URL}
  --endpoint PATH      Default: ${ENDPOINT}
  --method METHOD      Default: ${METHOD}
  --session-id SID     Default: ${SESSION_ID}
  --operation-id ID    Default: ${OPERATION_ID}
  --round-id ID        Default: ${ROUND_ID}
  --amount VALUE       Default: ${AMOUNT}
  -h, --help           Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --base-url)
      BASE_URL="$2"; shift 2 ;;
    --endpoint)
      ENDPOINT="$2"; shift 2 ;;
    --method)
      METHOD="$2"; shift 2 ;;
    --session-id)
      SESSION_ID="$2"; shift 2 ;;
    --operation-id)
      OPERATION_ID="$2"; shift 2 ;;
    --round-id)
      ROUND_ID="$2"; shift 2 ;;
    --amount)
      AMOUNT="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd node

tmp_dir="$(mktemp -d)"
current_file="${tmp_dir}/current-settings.json"
restore_patch_file="${tmp_dir}/restore-patch.json"
xml_patch_file="${tmp_dir}/xml-patch.json"
json_patch_file="${tmp_dir}/json-patch.json"
normalize_request_file="${tmp_dir}/normalize-request.json"
xml_response_file="${tmp_dir}/xml-response.json"
json_response_file="${tmp_dir}/json-response.json"

restore_settings() {
  if [[ -f "${restore_patch_file}" ]]; then
    curl -sS -X POST "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" \
      -H 'Content-Type: application/json' \
      --data @"${restore_patch_file}" >/dev/null || true
  fi
}

cleanup() {
  restore_settings
  rm -rf "${tmp_dir}"
}
trap cleanup EXIT

http_get_to_file() {
  local url="$1"
  local out_file="$2"
  local code
  code=$(curl -sS -o "${out_file}" -w '%{http_code}' "${url}")
  if [[ ! "${code}" =~ ^2 ]]; then
    echo "HTTP GET failed: ${url} -> ${code}" >&2
    cat "${out_file}" >&2 || true
    exit 2
  fi
}

http_post_file() {
  local url="$1"
  local body_file="$2"
  local out_file="$3"
  local code
  code=$(curl -sS -X POST "${url}" \
    -H 'Content-Type: application/json' \
    --data @"${body_file}" \
    -o "${out_file}" \
    -w '%{http_code}')
  if [[ ! "${code}" =~ ^2 ]]; then
    echo "HTTP POST failed: ${url} -> ${code}" >&2
    cat "${out_file}" >&2 || true
    exit 3
  fi
}

http_get_to_file "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" "${current_file}"

node - <<'NODE' "${current_file}" "${restore_patch_file}"
const fs = require('fs');
const current = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
if (!current || !current.settings) {
  throw new Error('current settings payload missing settings field');
}
const s = current.settings;
const patch = {
  performedBy: 'phase4-json-xml-parity-check-restore',
  protocolMode: s.protocolMode,
  jsonSecurity: {
    hash: {
      enabled: !!(s.jsonSecurity && s.jsonSecurity.hash && s.jsonSecurity.hash.enabled),
      headerName: (s.jsonSecurity && s.jsonSecurity.hash && s.jsonSecurity.hash.headerName) || 'Hash',
      enforcementMode: (s.jsonSecurity && s.jsonSecurity.hash && s.jsonSecurity.hash.enforcementMode) || 'OFF',
      secretRef: (s.jsonSecurity && s.jsonSecurity.hash && s.jsonSecurity.hash.secretRef) || null,
      exemptEndpoints: (s.jsonSecurity && s.jsonSecurity.hash && s.jsonSecurity.hash.exemptEndpoints) || [],
      getHashRules: (s.jsonSecurity && s.jsonSecurity.hash && s.jsonSecurity.hash.getHashRules) || {}
    },
    replay: {
      enabled: !!(s.jsonSecurity && s.jsonSecurity.replay && s.jsonSecurity.replay.enabled),
      windowSeconds: (s.jsonSecurity && s.jsonSecurity.replay && s.jsonSecurity.replay.windowSeconds) || 300,
      nonceTtlSeconds: (s.jsonSecurity && s.jsonSecurity.replay && s.jsonSecurity.replay.nonceTtlSeconds) || 300
    }
  }
};
fs.writeFileSync(process.argv[3], JSON.stringify(patch));
NODE

cat > "${xml_patch_file}" <<JSON
{"performedBy":"phase4-json-xml-parity-check","protocolMode":"XML","jsonSecurity":{"hash":{"enabled":false,"enforcementMode":"OFF"},"replay":{"enabled":false}}}
JSON

cat > "${json_patch_file}" <<JSON
{"performedBy":"phase4-json-xml-parity-check","protocolMode":"JSON","jsonSecurity":{"hash":{"enabled":false,"enforcementMode":"OFF"},"replay":{"enabled":false}}}
JSON

cat > "${normalize_request_file}" <<JSON
{"bankId":"${BANK_ID}","method":"${METHOD}","endpoint":"${ENDPOINT}","sessionId":"${SESSION_ID}","operationId":"${OPERATION_ID}","payload":{"roundId":"${ROUND_ID}","amount":${AMOUNT}},"query":{}}
JSON

http_post_file "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" "${xml_patch_file}" "${tmp_dir}/xml-mode-set.json"
http_post_file "${BASE_URL}/api/v1/protocol/requests/normalize" "${normalize_request_file}" "${xml_response_file}"

http_post_file "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" "${json_patch_file}" "${tmp_dir}/json-mode-set.json"
http_post_file "${BASE_URL}/api/v1/protocol/requests/normalize" "${normalize_request_file}" "${json_response_file}"

node - <<'NODE' "${xml_response_file}" "${json_response_file}" "${BANK_ID}"
const fs = require('fs');
const xmlResp = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const jsonResp = JSON.parse(fs.readFileSync(process.argv[3], 'utf8'));
const bankId = process.argv[4];

function stable(value) {
  if (Array.isArray(value)) return value.map(stable);
  if (value && typeof value === 'object') {
    const out = {};
    Object.keys(value).sort().forEach((k) => { out[k] = stable(value[k]); });
    return out;
  }
  return value;
}

if (!xmlResp.canonicalRequest || !jsonResp.canonicalRequest) {
  throw new Error('Missing canonicalRequest in one of responses');
}
if (!xmlResp.allowed || !jsonResp.allowed) {
  throw new Error('One mode rejected normalize request');
}

const xmlCanonical = { ...xmlResp.canonicalRequest };
const jsonCanonical = { ...jsonResp.canonicalRequest };
delete xmlCanonical.protocolMode;
delete jsonCanonical.protocolMode;
delete xmlCanonical.receivedAt;
delete jsonCanonical.receivedAt;

const left = JSON.stringify(stable(xmlCanonical));
const right = JSON.stringify(stable(jsonCanonical));
if (left !== right) {
  throw new Error('Canonical mismatch between XML and JSON modes\nXML=' + left + '\nJSON=' + right);
}

if (xmlResp.canonicalRequest.protocolMode !== 'XML' || jsonResp.canonicalRequest.protocolMode !== 'JSON') {
  throw new Error('Protocol mode markers are not as expected');
}

console.log('PARITY_OK bankId=' + bankId + ' endpoint=' + xmlResp.canonicalRequest.endpoint);
NODE

echo "JSON/XML parity check passed for bank ${BANK_ID} (${METHOD} ${ENDPOINT})"

#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

WORK_DIR=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --work-dir DIR   Optional temp work dir (default: auto mktemp)
  -h, --help       Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --work-dir)
      WORK_DIR="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ -z "${WORK_DIR}" ]]; then
  WORK_DIR="$(mktemp -d)"
  trap 'rm -rf "${WORK_DIR}"' EXIT
else
  mkdir -p "${WORK_DIR}"
fi

STORE_JS="${REPO_ROOT}/gs-server/refactor-services/protocol-adapter/src/store.js"

STORE_FILE="${WORK_DIR}/protocol-security-smoke.store.json" \
PROTOCOL_ADAPTER_DEFAULT_MODE="JSON" \
PROTOCOL_ADAPTER_BANK_MODES="6275:JSON" \
PROTOCOL_ADAPTER_JSON_HASH_ENABLED="true" \
PROTOCOL_ADAPTER_JSON_HASH_HEADER="Hash" \
PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE="SHADOW" \
PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS="/api/v1/account/info,/api/v1/balance,/api/v1/bonus/cash/check,/api/v1/bonus/frb/check,/api/v1/viewers,/api/v1/feeds" \
PROTOCOL_ADAPTER_JSON_GET_HASH_RULES="/api/v1/bonus/cash/history:bankId+userId+timeZone" \
PROTOCOL_ADAPTER_JSON_REPLAY_ENABLED="false" \
PROTOCOL_ADAPTER_JSON_HMAC_SECRETS="6275:SecretKey001" \
node - "${STORE_JS}" <<'NODE'
const crypto = require('crypto');
const storePath = process.argv[2];
const store = require(storePath);

function assert(cond, msg) {
  if (!cond) throw new Error(msg);
}

function hmac(value) {
  return crypto.createHmac('sha256', 'SecretKey001').update(value, 'utf8').digest('hex');
}

// POST request hash check (rawBody must be used as-is).
const postRaw = '{"bankId":"6275","bonusId":123456}';
let result = store.evaluateRequest({
  bankId: '6275',
  method: 'POST',
  endpoint: '/api/bonus/cash/cancel',
  headers: { Hash: hmac(postRaw) },
  payload: { bankId: '6275', bonusId: 123456 },
  rawBody: postRaw
});
assert(result.ok, 'POST hash smoke evaluateRequest should return ok');
assert(result.result.allowed === true, 'POST hash smoke should allow in SHADOW');
assert(result.result.security.hash.required === true, 'POST hash should be required');
assert(result.result.security.hash.verified === true, 'POST hash should verify');
assert(result.result.security.hash.hashInput === postRaw, 'POST hash input must match rawBody exactly');
console.log('PASS protocol hash smoke (POST rawBody HMAC)');

// GET request hash rule (bankId+userId+timeZone).
const getInput = '6275dragonestone0';
result = store.evaluateRequest({
  bankId: '6275',
  method: 'GET',
  endpoint: '/api/v1/bonus/cash/history',
  headers: { Hash: hmac(getInput) },
  query: { bankId: '6275', userId: 'dragonestone', timeZone: '0' }
});
assert(result.ok, 'GET hash smoke evaluateRequest should return ok');
assert(result.result.security.hash.required === true, 'GET hash should be required');
assert(result.result.security.hash.verified === true, 'GET hash should verify');
assert(result.result.security.hash.hashInput === getInput, 'GET hash input should follow configured field order');
console.log('PASS protocol hash smoke (GET hash rule)');

// Exempt endpoint should not require hash.
result = store.evaluateRequest({
  bankId: '6275',
  method: 'GET',
  endpoint: '/api/v1/balance',
  headers: {},
  query: { bankId: '6275' }
});
assert(result.ok, 'Exempt endpoint evaluateRequest should return ok');
assert(result.result.allowed === true, 'Exempt endpoint should be allowed');
assert(result.result.security.hash.required === false, 'Exempt endpoint should not require hash');
assert(result.result.security.hash.reason === 'hash_exempt_endpoint', 'Exempt endpoint reason mismatch');
console.log('PASS protocol hash smoke (exempt endpoint)');
NODE

STORE_FILE="${WORK_DIR}/protocol-security-enforce.store.json" \
PROTOCOL_ADAPTER_DEFAULT_MODE="JSON" \
PROTOCOL_ADAPTER_BANK_MODES="6275:JSON" \
PROTOCOL_ADAPTER_JSON_HASH_ENABLED="true" \
PROTOCOL_ADAPTER_JSON_HASH_HEADER="Hash" \
PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE="ENFORCE" \
PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS="" \
PROTOCOL_ADAPTER_JSON_REPLAY_ENABLED="false" \
PROTOCOL_ADAPTER_JSON_HMAC_SECRETS="6275:SecretKey001" \
node - "${STORE_JS}" <<'NODE'
const store = require(process.argv[2]);
function assert(cond, msg) { if (!cond) throw new Error(msg); }

const result = store.evaluateRequest({
  bankId: '6275',
  method: 'POST',
  endpoint: '/api/bonus/cash/cancel',
  headers: {},
  payload: { bankId: '6275', bonusId: 123456 },
  rawBody: '{"bankId":"6275","bonusId":123456}'
});
assert(result.ok, 'ENFORCE missing hash request should return protocol result');
assert(result.result.allowed === false, 'ENFORCE missing hash should block');
assert(result.result.httpStatus === 401, 'ENFORCE missing hash should return 401');
assert(result.result.security.hash.reason === 'missing_hash_header', 'ENFORCE missing hash reason mismatch');
console.log('PASS protocol hash smoke (ENFORCE missing hash blocked)');
NODE

STORE_FILE="${WORK_DIR}/protocol-security-replay.store.json" \
PROTOCOL_ADAPTER_DEFAULT_MODE="JSON" \
PROTOCOL_ADAPTER_BANK_MODES="6275:JSON" \
PROTOCOL_ADAPTER_JSON_HASH_ENABLED="true" \
PROTOCOL_ADAPTER_JSON_HASH_HEADER="Hash" \
PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE="ENFORCE" \
PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS="" \
PROTOCOL_ADAPTER_JSON_REPLAY_ENABLED="true" \
PROTOCOL_ADAPTER_JSON_REPLAY_WINDOW_SECONDS="300" \
PROTOCOL_ADAPTER_JSON_NONCE_TTL_SECONDS="300" \
PROTOCOL_ADAPTER_JSON_HMAC_SECRETS="6275:SecretKey001" \
node - "${STORE_JS}" <<'NODE'
const crypto = require('crypto');
const store = require(process.argv[2]);
function assert(cond, msg) { if (!cond) throw new Error(msg); }
function hmac(value) {
  return crypto.createHmac('sha256', 'SecretKey001').update(value, 'utf8').digest('hex');
}

const rawBody = '{"bankId":"6275","bonusId":86584571}';
const headers = {
  Hash: hmac(rawBody),
  'X-Timestamp': String(Math.floor(Date.now() / 1000)),
  'X-Nonce': 'nonce-smoke-001'
};

let first = store.evaluateRequest({
  bankId: '6275',
  method: 'POST',
  endpoint: '/api/bonus/cash/cancel',
  headers,
  payload: { bankId: '6275', bonusId: 86584571 },
  rawBody
});
assert(first.ok && first.result.allowed === true, 'First replay-protected request should pass');
assert(first.result.security.replay.checked === true, 'Replay should be checked');
assert(first.result.security.replay.reason === 'ok', 'Replay first request should be ok');

let second = store.evaluateRequest({
  bankId: '6275',
  method: 'POST',
  endpoint: '/api/bonus/cash/cancel',
  headers,
  payload: { bankId: '6275', bonusId: 86584571 },
  rawBody
});
assert(second.ok, 'Second replay-protected request should return protocol result');
assert(second.result.allowed === false, 'Reused nonce should be blocked in ENFORCE mode');
assert(second.result.httpStatus === 409, 'Reused nonce should return 409');
assert(second.result.security.replay.reason === 'nonce_reused', 'Replay reason should be nonce_reused');
console.log('PASS protocol replay smoke (nonce reuse blocked)');
NODE

echo "PASS: phase4 protocol security logic smoke suite"

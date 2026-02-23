#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
BASE_URL="$(cluster_hosts_http_url PROTOCOL_ADAPTER_EXTERNAL_HOST PROTOCOL_ADAPTER_EXTERNAL_PORT 127.0.0.1 18078)"
HMAC_SECRET=""
REQUIRE_SECRET="false"
PROBE_ENFORCEMENT_MODE="ENFORCE"
PROBE_TIMESTAMP="$(date +%s)"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID              Default: ${BANK_ID}
  --base-url URL            Default: ${BASE_URL}
  --hmac-secret VALUE       Optional non-prod test secret for local/runtime validation
  --require-secret BOOL     true|false (default: ${REQUIRE_SECRET})
  --enforcement-mode MODE   SHADOW|ENFORCE (default: ${PROBE_ENFORCEMENT_MODE})
  --timestamp UNIX_SEC      Optional fixed timestamp for probe requests (default: now)
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --base-url)
      BASE_URL="$2"; shift 2 ;;
    --hmac-secret)
      HMAC_SECRET="$2"; shift 2 ;;
    --require-secret)
      REQUIRE_SECRET="$2"; shift 2 ;;
    --enforcement-mode)
      PROBE_ENFORCEMENT_MODE="$2"; shift 2 ;;
    --timestamp)
      PROBE_TIMESTAMP="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

PROBE_ENFORCEMENT_MODE="$(printf '%s' "${PROBE_ENFORCEMENT_MODE}" | tr '[:lower:]' '[:upper:]')"
if [[ "${PROBE_ENFORCEMENT_MODE}" != "SHADOW" && "${PROBE_ENFORCEMENT_MODE}" != "ENFORCE" ]]; then
  echo "Invalid --enforcement-mode: ${PROBE_ENFORCEMENT_MODE}" >&2
  exit 1
fi
if [[ "${REQUIRE_SECRET}" != "true" && "${REQUIRE_SECRET}" != "false" ]]; then
  echo "Invalid --require-secret: ${REQUIRE_SECRET}" >&2
  exit 1
fi

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd node
require_cmd grep

tmp_dir="$(mktemp -d)"
current_file="${tmp_dir}/current-settings.json"
restore_patch_file="${tmp_dir}/restore-patch.json"
set_patch_file="${tmp_dir}/set-patch.json"
post_ok_file="${tmp_dir}/post-ok.json"
post_replay_file="${tmp_dir}/post-replay.json"
get_exempt_file="${tmp_dir}/get-exempt.json"
get_rule_file="${tmp_dir}/get-rule.json"
trap 'rm -rf "${tmp_dir}"' EXIT

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

http_post_json_file() {
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

restore_settings() {
  if [[ -f "${restore_patch_file}" ]]; then
    curl -sS -X POST "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" \
      -H 'Content-Type: application/json' \
      --data @"${restore_patch_file}" >/dev/null || true
  fi
}
trap 'restore_settings; rm -rf "${tmp_dir}"' EXIT

http_get_to_file "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" "${current_file}"

node - <<'NODE' "${current_file}" "${restore_patch_file}" "${set_patch_file}" "${PROBE_ENFORCEMENT_MODE}"
const fs = require('fs');
const current = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const restoreFile = process.argv[3];
const setFile = process.argv[4];
const enforcementMode = process.argv[5];
if (!current || !current.settings) throw new Error('current settings missing');
const s = current.settings;
const restorePatch = {
  performedBy: 'phase4-protocol-json-security-canary-restore',
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
const setPatch = {
  performedBy: 'phase4-protocol-json-security-canary',
  protocolMode: 'JSON',
  jsonSecurity: {
    hash: {
      enabled: true,
      headerName: 'Hash',
      enforcementMode,
      exemptEndpoints: ['/api/v1/account/info', '/api/v1/balance', '/api/v1/bonus/cash/check', '/api/v1/bonus/frb/check', '/api/v1/viewers', '/api/v1/feeds'],
      getHashRules: {
        '/api/v1/bonus/cash/history': ['bankId', 'userId', 'timeZone']
      }
    },
    replay: {
      enabled: true,
      windowSeconds: 300,
      nonceTtlSeconds: 300
    }
  }
};
fs.writeFileSync(restoreFile, JSON.stringify(restorePatch));
fs.writeFileSync(setFile, JSON.stringify(setPatch));
NODE

http_post_json_file "${BASE_URL}/api/v1/protocol/banks/${BANK_ID}/settings" "${set_patch_file}" "${tmp_dir}/set-result.json"

build_hash() {
  local input="$1"
  if [[ -z "${HMAC_SECRET}" ]]; then
    echo ""
    return 0
  fi
  node - <<'NODE' "${HMAC_SECRET}" "${input}"
const crypto = require('crypto');
const secret = process.argv[2];
const input = process.argv[3];
process.stdout.write(crypto.createHmac('sha256', secret).update(input, 'utf8').digest('hex'));
NODE
}

normalize_to_file() {
  local body_file="$1"
  local out_file="$2"
  local code
  code=$(curl -sS -X POST "${BASE_URL}/api/v1/protocol/requests/normalize" \
    -H 'Content-Type: application/json' \
    --data @"${body_file}" \
    -o "${out_file}" \
    -w '%{http_code}')
  if [[ ! "${code}" =~ ^2 ]]; then
    echo "HTTP POST normalize failed: ${code}" >&2
    cat "${out_file}" >&2 || true
    exit 4
  fi
}

post_raw='{"bankId":"6275","bonusId":86584571}'
post_raw_json_escaped='{\"bankId\":\"6275\",\"bonusId\":86584571}'
post_hash="$(build_hash "${post_raw}")"
cat > "${tmp_dir}/post-ok.req.json" <<JSON
{"bankId":"${BANK_ID}","method":"POST","endpoint":"/api/bonus/cash/cancel","headers":{"Hash":"${post_hash}","X-Timestamp":"${PROBE_TIMESTAMP}","X-Nonce":"nonce-phase4-security-1"},"payload":{"bankId":"${BANK_ID}","bonusId":86584571},"rawBody":"${post_raw_json_escaped}"}
JSON
normalize_to_file "${tmp_dir}/post-ok.req.json" "${post_ok_file}"

post_reason="$(node -e 'const x=require(process.argv[1]); console.log(x.security.hash.reason);' "${post_ok_file}")"
post_allowed="$(node -e 'const x=require(process.argv[1]); console.log(String(x.allowed));' "${post_ok_file}")"

if [[ "${post_reason}" == "secret_not_available" ]]; then
  echo "Protocol JSON security canary summary"
  echo "  bankId: ${BANK_ID}"
  echo "  enforcementMode: ${PROBE_ENFORCEMENT_MODE}"
  echo "  runtime_secret_available: false"
  echo "  hashReason: secret_not_available"
  if [[ "${REQUIRE_SECRET}" == "true" ]]; then
    echo "FAIL: runtime protocol-adapter does not have HMAC secret configured for bank ${BANK_ID}" >&2
    exit 5
  fi
  echo "SKIP: runtime HMAC secret unavailable; local logic smoke already validates hash/replay semantics."
  exit 0
fi

if [[ "${post_allowed}" != "true" || "${post_reason}" != "ok" ]]; then
  echo "FAIL: initial POST hash/replay validation did not pass" >&2
  cat "${post_ok_file}" >&2
  exit 6
fi

cat > "${tmp_dir}/post-replay.req.json" <<JSON
{"bankId":"${BANK_ID}","method":"POST","endpoint":"/api/bonus/cash/cancel","headers":{"Hash":"${post_hash}","X-Timestamp":"${PROBE_TIMESTAMP}","X-Nonce":"nonce-phase4-security-1"},"payload":{"bankId":"${BANK_ID}","bonusId":86584571},"rawBody":"${post_raw_json_escaped}"}
JSON
normalize_to_file "${tmp_dir}/post-replay.req.json" "${post_replay_file}"

replay_reason="$(node -e 'const x=require(process.argv[1]); console.log(x.security.replay.reason);' "${post_replay_file}")"
replay_allowed="$(node -e 'const x=require(process.argv[1]); console.log(String(x.allowed));' "${post_replay_file}")"
replay_status="$(node -e 'const x=require(process.argv[1]); console.log(String(x.httpStatus));' "${post_replay_file}")"
if [[ "${PROBE_ENFORCEMENT_MODE}" == "ENFORCE" ]]; then
  [[ "${replay_allowed}" == "false" && "${replay_status}" == "409" && "${replay_reason}" == "nonce_reused" ]] || {
    echo "FAIL: replay nonce reuse behavior mismatch in ENFORCE mode" >&2
    cat "${post_replay_file}" >&2
    exit 7
  }
fi

cat > "${tmp_dir}/get-exempt.req.json" <<JSON
{"bankId":"${BANK_ID}","method":"GET","endpoint":"/api/v1/balance","headers":{},"query":{"bankId":"${BANK_ID}"}}
JSON
normalize_to_file "${tmp_dir}/get-exempt.req.json" "${get_exempt_file}"
exempt_hash_reason="$(node -e 'const x=require(process.argv[1]); console.log(x.security.hash.reason);' "${get_exempt_file}")"
[[ "${exempt_hash_reason}" == "hash_exempt_endpoint" ]] || {
  echo "FAIL: exempt endpoint hash reason mismatch" >&2
  cat "${get_exempt_file}" >&2
  exit 8
}

get_hash_input="${BANK_ID}dragonestone0"
get_hash="$(build_hash "${get_hash_input}")"
cat > "${tmp_dir}/get-rule.req.json" <<JSON
{"bankId":"${BANK_ID}","method":"GET","endpoint":"/api/v1/bonus/cash/history","headers":{"Hash":"${get_hash}","X-Timestamp":"$((PROBE_TIMESTAMP+1))","X-Nonce":"nonce-phase4-security-2"},"query":{"bankId":"${BANK_ID}","userId":"dragonestone","timeZone":"0"}}
JSON
normalize_to_file "${tmp_dir}/get-rule.req.json" "${get_rule_file}"
get_rule_reason="$(node -e 'const x=require(process.argv[1]); console.log(x.security.hash.reason);' "${get_rule_file}")"
get_rule_input="$(node -e 'const x=require(process.argv[1]); console.log(x.security.hash.hashInput);' "${get_rule_file}")"
[[ "${get_rule_reason}" == "ok" && "${get_rule_input}" == "${get_hash_input}" ]] || {
  echo "FAIL: GET hash rule validation mismatch" >&2
  cat "${get_rule_file}" >&2
  exit 9
}

echo "Protocol JSON security canary summary"
echo "  bankId: ${BANK_ID}"
echo "  enforcementMode: ${PROBE_ENFORCEMENT_MODE}"
echo "  runtime_secret_available: true"
echo "  post_hash: ok"
echo "  replay_nonce_reuse: ${replay_reason}"
echo "  get_exempt: ${exempt_hash_reason}"
echo "  get_hash_rule: ${get_rule_reason}"
echo "PASS: protocol JSON hash/replay runtime canary verified."

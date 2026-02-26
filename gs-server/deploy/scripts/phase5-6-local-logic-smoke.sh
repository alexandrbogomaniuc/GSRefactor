#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
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

bonus_store="${WORK_DIR}/bonus-store.json"
history_store="${WORK_DIR}/history-store.json"
mp_store="${WORK_DIR}/mp-store.json"

BONUS_STORE="${bonus_store}" REPO_ROOT="${ROOT}" node <<'NODE'
const assert = (c, m) => { if (!c) throw new Error(m); };
const path = require('path');
process.env.STORE_FILE = process.env.BONUS_STORE;
const store = require(process.env.REPO_ROOT + '/gs-server/refactor-services/bonus-frb-service/src/store');

const a = store.checkFrb({ bankId: '6275', accountId: 'acc1', frbId: 'frb1' });
assert(a.ok && a.frb.status === 'AVAILABLE', 'initial FRB should be AVAILABLE');
const c1 = store.consumeFrb({ bankId: '6275', accountId: 'acc1', frbId: 'frb1', operationId: 'op-consume-1' });
assert(c1.ok && c1.frb.status === 'CONSUMED' && c1.idempotent === false, 'consume should set CONSUMED');
const c1dup = store.consumeFrb({ bankId: '6275', accountId: 'acc1', frbId: 'frb1', operationId: 'op-consume-1' });
assert(c1dup.ok && c1dup.idempotent === true, 'consume should be idempotent');
const r1 = store.releaseFrb({ bankId: '6275', accountId: 'acc1', frbId: 'frb1', operationId: 'op-release-1' });
assert(r1.ok && r1.frb.status === 'AVAILABLE', 'release should restore AVAILABLE');
const listed = store.listFrb('6275', 'acc1');
assert(Array.isArray(listed.frbRecords) && listed.frbRecords.length >= 1, 'listFrb should return records');
console.log('PASS bonus-frb store smoke');
NODE

HISTORY_STORE="${history_store}" REPO_ROOT="${ROOT}" node <<'NODE'
const assert = (c, m) => { if (!c) throw new Error(m); };
process.env.STORE_FILE = process.env.HISTORY_STORE;
const store = require(process.env.REPO_ROOT + '/gs-server/refactor-services/history-service/src/store');

const a1 = store.appendRecord({ bankId: '6275', sessionId: 's1', operationId: 'hist-op-1', eventType: 'wager', payload: { amount: 100 } });
assert(a1.ok && a1.idempotent === false, 'appendRecord should create record');
const a1dup = store.appendRecord({ bankId: '6275', sessionId: 's1', operationId: 'hist-op-1', eventType: 'wager', payload: { amount: 100 } });
assert(a1dup.ok && a1dup.idempotent === true, 'appendRecord should be idempotent by operationId');
const list = store.listRecords('6275', 's1', 'wager');
assert(Array.isArray(list.records) && list.records.some(r => r.operationId === 'hist-op-1'), 'listRecords should return appended record');
console.log('PASS history store smoke');
NODE

MP_STORE="${mp_store}" REPO_ROOT="${ROOT}" node <<'NODE'
const assert = (c, m) => { if (!c) throw new Error(m); };
process.env.STORE_FILE = process.env.MP_STORE;
const store = require(process.env.REPO_ROOT + '/gs-server/refactor-services/multiplayer-service/src/store');

const s1 = store.upsertSession({ bankId: '7777', sessionId: 'mp1', playerId: 'p1', operationType: 'session_sync', operationId: 'mp-op-1', status: 'SYNCED' });
assert(s1.ok && s1.session.status === 'SYNCED' && s1.idempotent === false, 'session_sync should create session');
const s1dup = store.upsertSession({ bankId: '7777', sessionId: 'mp1', playerId: 'p1', operationType: 'session_sync', operationId: 'mp-op-1', status: 'SYNCED' });
assert(s1dup.ok && s1dup.idempotent === true, 'session_sync should be idempotent');
const sitIn = store.upsertSession({ bankId: '7777', sessionId: 'mp1', playerId: 'p1', roomId: 'roomA', operationType: 'room_sit_in', operationId: 'mp-op-2', status: 'SIT_IN' });
assert(sitIn.ok && sitIn.session.roomId === 'roomA', 'room_sit_in should set roomId');
const list = store.listSessions('7777', 'mp1');
assert(Array.isArray(list.sessions) && list.sessions.length === 1, 'listSessions should return session');
console.log('PASS multiplayer store smoke');
NODE

REPO_ROOT="${ROOT}" node <<'NODE'
const assert = (c, m) => { if (!c) throw new Error(m); };
const policy = require(process.env.REPO_ROOT + '/gs-server/refactor-services/multiplayer-service/src/policy');
const bankFlags = policy.parseBankFlags('6275:false,7777:true');

let d = policy.routeDecision({ bankId: '6275', isMultiplayer: false, routeEnabled: true, canaryBanks: ['6275','7777'], bankFlags });
assert(d.routeToMultiplayerService === false && d.reason === 'non_multiplayer_game', 'non-MP request should bypass');

 d = policy.routeDecision({ bankId: '6275', isMultiplayer: true, routeEnabled: true, canaryBanks: ['6275','7777'], bankFlags });
assert(d.routeToMultiplayerService === false && d.reason === 'bank_multiplayer_disabled', 'bank-disabled MP request should bypass');

 d = policy.routeDecision({ bankId: '7777', isMultiplayer: true, routeEnabled: true, canaryBanks: ['6275','7777'], bankFlags });
assert(d.routeToMultiplayerService === true && d.reason === 'eligible', 'eligible MP request should route');

 d = policy.routeDecision({ bankId: '8888', isMultiplayer: true, routeEnabled: true, canaryBanks: ['6275','7777'], bankFlags });
assert(d.routeToMultiplayerService === false && d.reason === 'bank_not_in_canary', 'non-canary bank should not route');

console.log('PASS multiplayer policy smoke');
NODE

echo "PASS: phase5/6 local logic smoke suite"

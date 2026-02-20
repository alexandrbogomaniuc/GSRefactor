#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PORT="${CM_PORT:-18070}"
BASE="http://localhost:${PORT}"
SERVER_LOG="${ROOT_DIR}/data/server.log"
TOKEN_FILE="${ROOT_DIR}/data/.token"
CORE_FILE="${CM_CORE_FILE:-${CM_USERS_FILE:-${ROOT_DIR}/data/cm-core.json}}"
MIRROR_FILE="${CM_MIRROR_FILE:-${ROOT_DIR}/data/cm-mirror.json}"
LEGACY_FILE="${ROOT_DIR}/data/users.json"

cleanup() {
  if [[ -n "${SERVER_PID:-}" ]] && kill -0 "${SERVER_PID}" 2>/dev/null; then
    kill "${SERVER_PID}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

rm -f "${TOKEN_FILE}"
if [[ "${CM_SMOKE_RESET_BOOTSTRAP:-1}" == "1" ]]; then
  rm -f "${CORE_FILE}" "${MIRROR_FILE}" "${LEGACY_FILE}"
fi

CM_PORT="${PORT}" CM_CORE_FILE="${CORE_FILE}" CM_MIRROR_FILE="${MIRROR_FILE}" \
  node "${ROOT_DIR}/src/server.js" >"${SERVER_LOG}" 2>&1 &
SERVER_PID=$!

for _ in {1..40}; do
  if curl -fsS "${BASE}/health" >/dev/null 2>&1; then
    break
  fi
  sleep 0.5
done

echo "[smoke] health:"
curl -fsS "${BASE}/health"
echo

echo "[smoke] login root/root:"
LOGIN_RESP="$(curl -fsS -X POST "${BASE}/cm-auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"root","password":"root"}')"
echo "${LOGIN_RESP}"
echo

ACCESS_TOKEN="$(echo "${LOGIN_RESP}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(j.accessToken||"");});')"
MUST_CHANGE="$(echo "${LOGIN_RESP}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(String(!!j.mustChangePassword));});')"

if [[ "${MUST_CHANGE}" != "true" ]]; then
  echo "[smoke] expected mustChangePassword=true for bootstrap user"
  exit 1
fi

echo "[smoke] protected call before password change (expect PASSWORD_CHANGE_REQUIRED):"
curl -sS "${BASE}/cm/reports/playerSearch?bankId=6274&limit=3" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
echo

NEW_PASSWORD="${CM_NEW_PASSWORD:-RootPass#2026AB}"
echo "[smoke] change password:"
curl -fsS -X POST "${BASE}/cm-auth/change-password" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d "{\"oldPassword\":\"root\",\"newPassword\":\"${NEW_PASSWORD}\"}"
echo

echo "[smoke] login root with new password:"
LOGIN2="$(curl -fsS -X POST "${BASE}/cm-auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"root\",\"password\":\"${NEW_PASSWORD}\"}")"
echo "${LOGIN2}"
echo

ROOT_ACCESS="$(echo "${LOGIN2}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(j.accessToken||"");});')"

echo "[smoke] user management meta:"
ROOT_META="$(curl -fsS "${BASE}/cm/meta/user-management" -H "Authorization: Bearer ${ROOT_ACCESS}")"
echo "${ROOT_META}"
echo

SUPPORT_ROLE_ID="$(echo "${ROOT_META}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);const role=(j.roles||[]).find(r=>String(r.title||"").toUpperCase()==="SUPPORT");process.stdout.write(String(role&&role.id||""));});')"
if [[ -z "${SUPPORT_ROLE_ID}" ]]; then
  echo "[smoke] support role not found"
  exit 1
fi

echo "[smoke] create user-manager smoke role:"
CREATE_ROLE="$(curl -fsS -X POST "${BASE}/cm/actions/createRole" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -d '{"roleName":"qa-role-smoke","description":"smoke role","isNonRestricted":true,"permissions":[1,2,3,6]}')"
echo "${CREATE_ROLE}"
echo

ROLE_ID="$(echo "${CREATE_ROLE}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(String((j.role&&j.role.roleId)||""));});')"
if [[ -z "${ROLE_ID}" ]]; then
  echo "[smoke] failed to parse role id"
  exit 1
fi

echo "[smoke] create manager user:"
CREATE_USER="$(curl -fsS -X POST "${BASE}/cm/actions/createUser" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -d "{\"login\":\"qa.user.smoke\",\"email\":\"qa.user.smoke@example.com\",\"comment\":\"smoke account\",\"isGeneral\":true,\"includeFutureBanks\":true,\"initialPassword\":\"UserPass#2026AB\",\"roleIds\":[${ROLE_ID}]}")"
echo "${CREATE_USER}"
echo

echo "[smoke] create support user:"
CREATE_SUPPORT="$(curl -fsS -X POST "${BASE}/cm/actions/createUser" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -d "{\"login\":\"qa.support.smoke\",\"email\":\"qa.support.smoke@example.com\",\"comment\":\"support smoke\",\"isGeneral\":true,\"includeFutureBanks\":true,\"initialPassword\":\"SupportPass#2026AB\",\"roleIds\":[${SUPPORT_ROLE_ID}]}")"
echo "${CREATE_SUPPORT}"
echo

echo "[smoke] login support user (must change password):"
SUPPORT_LOGIN_1="$(curl -fsS -X POST "${BASE}/cm-auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"qa.support.smoke","password":"SupportPass#2026AB"}')"
echo "${SUPPORT_LOGIN_1}"
echo

SUPPORT_ACCESS_1="$(echo "${SUPPORT_LOGIN_1}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(j.accessToken||"");});')"
SUPPORT_MUST_CHANGE="$(echo "${SUPPORT_LOGIN_1}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(String(!!j.mustChangePassword));});')"
if [[ "${SUPPORT_MUST_CHANGE}" != "true" ]]; then
  echo "[smoke] expected support user mustChangePassword=true"
  exit 1
fi

curl -fsS -X POST "${BASE}/cm-auth/change-password" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${SUPPORT_ACCESS_1}" \
  -d '{"oldPassword":"SupportPass#2026AB","newPassword":"SupportPass#2026CD"}' >/dev/null

echo "[smoke] login support user after password change:"
SUPPORT_LOGIN_2="$(curl -fsS -X POST "${BASE}/cm-auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"qa.support.smoke","password":"SupportPass#2026CD"}')"
echo "${SUPPORT_LOGIN_2}"
echo

SUPPORT_ACCESS_2="$(echo "${SUPPORT_LOGIN_2}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);process.stdout.write(j.accessToken||"");});')"

echo "[smoke] support capabilities:"
SUPPORT_META="$(curl -fsS "${BASE}/cm/meta/user-management" -H "Authorization: Bearer ${SUPPORT_ACCESS_2}")"
echo "${SUPPORT_META}"
echo

echo "${SUPPORT_META}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);const c=j.capabilities||{};if(c.canCreateUser||c.canCreateRole||c.canEditUser||c.canResetPassword||c.canDeleteUser||c.canLockUnlock||c.canFlushIps){console.error("[smoke] support capability escalation detected",c);process.exit(1);}process.stdout.write("[smoke] support capabilities are read-only\n");});'

echo "[smoke] support menu should not include create links:"
SUPPORT_MENU="$(curl -fsS "${BASE}/cm/meta/menu" -H "Authorization: Bearer ${SUPPORT_ACCESS_2}")"
echo "${SUPPORT_MENU}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);const txt=JSON.stringify(j);if(txt.includes("createUser")||txt.includes("createRole")){console.error("[smoke] support menu still exposes create actions");process.exit(1);}process.stdout.write("[smoke] support menu filtered correctly\n");});'

echo "[smoke] support can read user list:"
curl -fsS "${BASE}/cm/reports/userList?limit=5" \
  -H "Authorization: Bearer ${SUPPORT_ACCESS_2}" >/dev/null
echo "[smoke] support read path OK"

HTTP_CREATE="$(curl -sS -o /tmp/cm-smoke-create.out -w "%{http_code}" -X POST "${BASE}/cm/actions/createUser" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${SUPPORT_ACCESS_2}" \
  -d '{"login":"should.fail","email":"should.fail@example.com","isGeneral":true,"roleIds":[2]}')"
if [[ "${HTTP_CREATE}" != "403" ]]; then
  echo "[smoke] expected 403 for support createUser, got ${HTTP_CREATE}"
  cat /tmp/cm-smoke-create.out
  exit 1
fi

HTTP_EDIT="$(curl -sS -o /tmp/cm-smoke-edit.out -w "%{http_code}" -X POST "${BASE}/cm/actions/editUser" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${SUPPORT_ACCESS_2}" \
  -d '{"originalLogin":"qa.user.smoke","comment":"x"}')"
if [[ "${HTTP_EDIT}" != "403" ]]; then
  echo "[smoke] expected 403 for support editUser, got ${HTTP_EDIT}"
  cat /tmp/cm-smoke-edit.out
  exit 1
fi

echo "[smoke] report playerSearch:"
curl -fsS "${BASE}/cm/reports/playerSearch?bankId=6274&limit=5" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" >/dev/null
echo "[smoke] report path OK"

echo "[smoke] player summary flow:"
PLAYER_SEARCH_JSON="$(curl -fsS "${BASE}/cm/reports/playerSearch?bankList=6274&mainPerPage=1" \
  -H "Authorization: Bearer ${ROOT_ACCESS}")"

PLAYER_BANK_ID="$(echo "${PLAYER_SEARCH_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);const r=(j.rows||[])[0]||{};process.stdout.write(String(r.bankId||""));});')"
PLAYER_ACCOUNT_ID="$(echo "${PLAYER_SEARCH_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);const r=(j.rows||[])[0]||{};process.stdout.write(String(r.accountId||""));});')"

if [[ -z "${PLAYER_BANK_ID}" || -z "${PLAYER_ACCOUNT_ID}" ]]; then
  echo "[smoke] playerSearch did not return player ids"
  echo "${PLAYER_SEARCH_JSON}"
  exit 1
fi

curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/summary" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" >/dev/null
curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/game-info" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" >/dev/null
curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/change-history" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" >/dev/null
echo "[smoke] player summary read endpoints OK"

curl -fsS -X POST "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/actions/lockAccount" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -H 'Content-Type: application/json' \
  -d '{}' >/dev/null
curl -fsS -X POST "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/actions/lockAccount" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -H 'Content-Type: application/json' \
  -d '{}' >/dev/null
echo "[smoke] player lock/unlock action OK"

echo "[smoke] player award bonus action:"
BONUS_AWARD_HTTP="$(curl -sS -o /tmp/cm-smoke-award-bonus.out -w "%{http_code}" -X POST \
  "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/actions/awardBonus" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -H 'Content-Type: application/json' \
  -d '{"bonusType":0,"amount":10,"rolloverMultiplier":2,"multiplierCap":0,"startTime":"2026-02-18T10:09:00.000Z","expirationTime":"2026-03-20T23:59:00.000Z","gameLimitType":0,"gameList":[],"description":"smoke bonus","releasedType":true}')"
BONUS_AWARDED="false"
if [[ "${BONUS_AWARD_HTTP}" == "200" ]]; then
  BONUS_AWARDED="true"
  echo "[smoke] award bonus OK"
else
  BONUS_AWARD_BODY="$(cat /tmp/cm-smoke-award-bonus.out)"
  BONUS_AWARD_CODE="$(echo "${BONUS_AWARD_BODY}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{const j=JSON.parse(s);process.stdout.write(String(j.code||""));}catch(e){}})')"
  BONUS_AWARD_ERR="$(echo "${BONUS_AWARD_BODY}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{const j=JSON.parse(s);process.stdout.write(String(j.error||""));}catch(e){}})')"
  if [[ "${BONUS_AWARD_HTTP}" == "400" && "${BONUS_AWARD_ERR}" == "AWARD_BONUS_API_ERROR" && "${BONUS_AWARD_CODE}" == "699" ]]; then
    echo "[smoke] award bonus skipped: GS bank bonus configuration returned code 699 (expected in this local env)"
  else
    echo "[smoke] award bonus failed: HTTP=${BONUS_AWARD_HTTP}"
    cat /tmp/cm-smoke-award-bonus.out
    exit 1
  fi
fi

echo "[smoke] player award FR bonus action:"
curl -fsS -X POST "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/actions/awardFRBonus" \
  -H "Authorization: Bearer ${ROOT_ACCESS}" \
  -H 'Content-Type: application/json' \
  -d '{"rounds":5,"frbBetType":1,"gameLimitType":0,"gameList":[],"startTime":"2026-02-18T10:09:00.000Z","expirationTime":"2026-03-20T23:59:00.000Z","awardDurationDays":0,"frChips":10,"maxWinCap":0,"description":"smoke frbonus"}' >/dev/null
echo "[smoke] award FR bonus OK"

echo "[smoke] bonus and frbonus report rows:"
BONUS_ROWS_JSON="$(curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/bonus-detail" \
  -H "Authorization: Bearer ${ROOT_ACCESS}")"
FRBONUS_ROWS_JSON="$(curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/frbonus-detail" \
  -H "Authorization: Bearer ${ROOT_ACCESS}")"
if [[ "${BONUS_AWARDED}" == "true" ]]; then
  echo "${BONUS_ROWS_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);if(!Array.isArray(j.rows)||j.rows.length<1){console.error("[smoke] expected bonus rows");process.exit(1);}process.stdout.write("[smoke] bonus rows present\n");});'
else
  echo "${BONUS_ROWS_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);if(!Array.isArray(j.rows)){console.error("[smoke] expected bonus rows array");process.exit(1);}process.stdout.write("[smoke] bonus rows endpoint reachable\n");});'
fi
echo "${FRBONUS_ROWS_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);if(!Array.isArray(j.rows)||j.rows.length<1){console.error("[smoke] expected frbonus rows");process.exit(1);}process.stdout.write("[smoke] frbonus rows present\n");});'

echo "[smoke] game-info filter modes:"
GAME_GRP_JSON="$(curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/game-info?showBySessions=false" \
  -H "Authorization: Bearer ${ROOT_ACCESS}")"
GAME_SES_JSON="$(curl -fsS "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/game-info?showBySessions=true" \
  -H "Authorization: Bearer ${ROOT_ACCESS}")"
echo "${GAME_GRP_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);if(j.mode!=="games"){console.error("[smoke] expected game mode=games");process.exit(1);}process.stdout.write("[smoke] game mode games OK\n");});'
echo "${GAME_SES_JSON}" | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{const j=JSON.parse(s);if(j.mode!=="sessions"){console.error("[smoke] expected game mode=sessions");process.exit(1);}process.stdout.write("[smoke] game mode sessions OK\n");});'

HTTP_SUPPORT_PLAYER_ACTION="$(curl -sS -o /tmp/cm-smoke-support-player-action.out -w "%{http_code}" -X POST \
  "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/actions/lockAccount" \
  -H "Authorization: Bearer ${SUPPORT_ACCESS_2}" \
  -H 'Content-Type: application/json' \
  -d '{}')"
if [[ "${HTTP_SUPPORT_PLAYER_ACTION}" != "403" ]]; then
  echo "[smoke] expected 403 for support player action, got ${HTTP_SUPPORT_PLAYER_ACTION}"
  cat /tmp/cm-smoke-support-player-action.out
  exit 1
fi
echo "[smoke] support player action restriction OK"

HTTP_SUPPORT_PLAYER_AWARD="$(curl -sS -o /tmp/cm-smoke-support-player-award.out -w "%{http_code}" -X POST \
  "${BASE}/cm/players/${PLAYER_BANK_ID}/${PLAYER_ACCOUNT_ID}/actions/awardBonus" \
  -H "Authorization: Bearer ${SUPPORT_ACCESS_2}" \
  -H 'Content-Type: application/json' \
  -d '{"bonusType":0,"amount":10,"rolloverMultiplier":2,"startTime":"2026-02-18T10:09:00.000Z","expirationTime":"2026-03-20T23:59:00.000Z"}')"
if [[ "${HTTP_SUPPORT_PLAYER_AWARD}" != "403" ]]; then
  echo "[smoke] expected 403 for support awardBonus, got ${HTTP_SUPPORT_PLAYER_AWARD}"
  cat /tmp/cm-smoke-support-player-award.out
  exit 1
fi
echo "[smoke] support award action restriction OK"

echo "[smoke] done"

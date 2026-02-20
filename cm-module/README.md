# CM Module Prototype

Runnable Casino Manager phase-1 module with a dedicated Docker container.

## Implemented Scope
- Auth bootstrap:
  - default login `root` / `root`
  - forced password change on first login
  - lockout policy + token-protected endpoints
- User management (functional, persisted in CM core DB `data/cm-core.json`):
  - `User List` with filters
  - `Create User`
  - `Role List`
  - `Create Role`
  - user details (`Common`, `Sessions`, `IPs`)
  - 2FA toggle
  - object change history
  - per-role RBAC with menu filtering (support is read-only)
- Cassandra-backed operational reports:
  - `playerSearch` (provider-style columns + filters, clickable player rows)
  - `player summary info` + `view/actions` endpoints
  - `bankList`
  - `transactions`
  - `gameSessionSearch`
  - `walletOperationAlerts`
- Red CM-style web UI with menu-driven panels.
- Mirror DB snapshot store (`data/cm-mirror.json`) for report copy/sync baseline.

## Requirements
- Node.js 18+
- Docker running
- Cassandra container available as `gp3-c1-1`

## Run Locally
```bash
cd /Users/alexb/Documents/Dev/cm-module
npm start
```

Server URL:
- `http://localhost:7070`

## Run As Dedicated Container
```bash
cd /Users/alexb/Documents/Dev/cm-module
docker compose up -d --build
```

Container URL:
- `http://localhost:18070`

Container details:
- container name: `cm-module`
- host port: `18070`
- internal port: `7070`
- mounted volume: `/Users/alexb/Documents/Dev/cm-module/data:/app/data`

Health:
```bash
curl -sS http://localhost:18070/health
```

Stop:
```bash
cd /Users/alexb/Documents/Dev/cm-module
docker compose down
```

## Reset To Default `root/root`
```bash
cd /Users/alexb/Documents/Dev/cm-module
bash scripts/reset-bootstrap.sh
docker compose restart
```

## Smoke Test
```bash
cd /Users/alexb/Documents/Dev/cm-module
CM_PORT=18071 npm run smoke
```

Smoke test verifies:
1. bootstrap login `root/root`
2. password-change gate
3. password update + re-login
4. create role
5. create user
6. support user is read-only (capabilities + menu + forbidden write actions)
7. user list + role list + detail endpoints
8. created user first-login password-change requirement
9. live Cassandra report endpoints
10. player summary endpoints + lock/unlock action
11. support role denied from player write actions

## Key Endpoints

Auth:
- `POST /cm-auth/login`
- `POST /cm-auth/change-password`
- `POST /cm-auth/logout`

Meta:
- `GET /cm/meta/menu`
- `GET /cm/meta/reports`
- `GET /cm/meta/user-management`

User management:
- `GET /cm/reports/userList`
- `POST /cm/actions/createUser`
- `GET /cm/reports/roleList`
- `POST /cm/actions/createRole`
- `POST /cm/actions/switch2fa`
- `GET /cm/users/:login/common-info`
- `GET /cm/users/:login/session-info`
- `GET /cm/users/:login/ips-info`
- `GET /cm/history/object-change`

Operational reports:
- `GET /cm/reports/playerSearch?bankId=6274&limit=20`
- `GET /cm/players/:bankId/:accountId/summary`
- `GET /cm/players/:bankId/:accountId/game-info`
- `GET /cm/players/:bankId/:accountId/payment-detail`
- `GET /cm/players/:bankId/:accountId/bonus-detail`
- `GET /cm/players/:bankId/:accountId/frbonus-detail`
- `GET /cm/players/:bankId/:accountId/change-history`
- `POST /cm/players/:bankId/:accountId/actions/lockAccount`
- `POST /cm/players/:bankId/:accountId/actions/makeTester`
- `GET /cm/reports/bankList?limit=50`
- `GET /cm/reports/transactions?extId=bav_game_session_001&limit=20`
- `GET /cm/reports/gameSessionSearch?gameSessionId=<id>`
- `GET /cm/reports/walletOperationAlerts?limit=20`

## Environment Variables
- `CM_PORT` (default `7070`)
- `CM_CASSANDRA_CONTAINER` (default `gp3-c1-1`)
- `CM_CORE_FILE` (default `./data/cm-core.json`)
- `CM_MIRROR_FILE` (default `./data/cm-mirror.json`)

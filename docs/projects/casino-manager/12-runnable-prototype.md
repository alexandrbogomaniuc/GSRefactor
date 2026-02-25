# Runnable Prototype

## Module Location
- `/Users/alexb/Documents/Dev/cm-module`

## What Is Implemented
- Bootstrap auth:
  - default username: `root`
  - default password: `root`
  - forced first-login password change
- User-management functionality (non-placeholder):
  - `User List` (filtering)
  - `Create User`
  - `Role List`
  - `Create Role`
  - user details (`Common`, `Sessions`, `IPs`)
  - 2FA toggle
  - object-change history
- Cassandra-backed report endpoints:
  - `/cm/reports/playerSearch` (provider-style filters/columns)
  - `/cm/players/:bankId/:accountId/summary`
  - `/cm/players/:bankId/:accountId/game-info`
  - `/cm/players/:bankId/:accountId/payment-detail`
  - `/cm/players/:bankId/:accountId/bonus-detail`
  - `/cm/players/:bankId/:accountId/frbonus-detail`
  - `/cm/players/:bankId/:accountId/change-history`
  - `/cm/players/:bankId/:accountId/actions/lockAccount`
  - `/cm/players/:bankId/:accountId/actions/makeTester`
  - `/cm/reports/bankList`
  - `/cm/reports/transactions`
  - `/cm/reports/gameSessionSearch`
  - `/cm/reports/walletOperationAlerts`
- Web UI at `/` in red BetOnline visual direction.

## Run As Dedicated Container
```bash
cd /Users/alexb/Documents/Dev/cm-module
docker compose up -d --build
```

Container URL:
- `http://localhost:18070`

Container name:
- `cm-module`

Health:
```bash
curl -sS http://localhost:18070/health
```

## Reset Back To Default `root/root`
```bash
cd /Users/alexb/Documents/Dev/cm-module
bash scripts/reset-bootstrap.sh
docker compose restart
```

## Verified End-to-End Flow (2026-02-17)
1. Login with `root/root`.
2. Confirm `mustChangePassword=true` and protected endpoint gate before password update.
3. Change password as required.
4. Login with new password and access CM.
5. Create role.
6. Create user.
7. Validate user in `User List` and role in `Role List`.
8. Validate `common-info` / `session-info` / `ips-info` endpoints.
9. Toggle 2FA for created user.
10. Validate created user login requires password change.
11. Run Cassandra-backed reports.

## Latest Verified API Snapshot
- `health`: `ok=true`
- `root/root` first login: success, `mustChangePassword=true`
- pre-password-change user list call: `PASSWORD_CHANGE_REQUIRED`
- role creation: success (`201`)
- user creation: success (`201`)
- user list filter by created login: returned `count=1`
- role list: returned `count=4` (including created role)
- 2FA switch endpoint: success (`status2fa=Enabled`)
- report calls:
  - `playerSearch` returned live row (`bankid=6274`, `extid=bav_game_session_001`, `accountid=40962`)
  - `bankList`/`transactions` executed successfully with current dataset

## Web UI Validation
- URL tested: `http://localhost:18070/`
- Confirmed:
  - login page with BetOnline logo,
  - red themed dashboard,
  - menu navigation to User List/Create User/Role List/Create Role/Player Search,
  - functional user-management actions and tables,
  - functional player search table with clickable rows to summary page,
  - functional summary `View`/`Actions` menus backed by API endpoints.
- Evidence screenshot:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/casino-manager/evidence/cm-ui-red-user-management-20260217.png`

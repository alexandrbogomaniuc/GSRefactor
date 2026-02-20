# CM User Management Smoke - 2026-02-17

## Target
- `http://localhost:18070`

## Verified Chain
1. `POST /cm-auth/login` with `root/root` returns `mustChangePassword=true`.
2. Protected endpoint before password change returns `PASSWORD_CHANGE_REQUIRED`.
3. `POST /cm-auth/change-password` succeeds.
4. Re-login with updated root password succeeds.
5. `POST /cm/actions/createRole` succeeds and returns new `roleId`.
6. `POST /cm/actions/createUser` succeeds and binds created role.
7. `GET /cm/reports/userList?login=<created_login>` returns `count=1`.
8. `GET /cm/reports/roleList` contains created role.
9. `GET /cm/users/:login/common-info` returns scoped user details.
10. `POST /cm/actions/switch2fa` toggles 2FA status to `Enabled`.
11. Created user login returns `mustChangePassword=true`.
12. Cassandra report endpoints still execute successfully:
   - `playerSearch`
   - `bankList`
   - `transactions`

## Evidence
- API execution output captured in terminal run with final marker: `smoke-ok`.
- UI screenshot:
  - `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/cm-ui-red-user-management-20260217.png`

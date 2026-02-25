# Authorization Logic

## Objective
Define phase-1 authentication and authorization for the CM module with default bootstrap credentials:
- username: `root`
- password: `root`

## Security Baseline
- Password hashing: `Argon2id` (never store plaintext).
- Session style: short-lived access token + rotating refresh token.
- Transport: HTTPS only.
- Audit: every login/logout/password-change/admin-action recorded.

## Bootstrap Flow (`root`/`root`)
1. On startup, `cm-auth` checks whether any user exists.
2. If no users exist:
   - create user `root`,
   - hash password `root`,
   - set `mustChangePassword=true`,
   - assign role `SUPER_ADMIN`,
   - write bootstrap audit event.
3. First successful login with `root`/`root`:
   - allow session creation,
   - force immediate password change before any CM page access.

## Login Rules
- Username + password required.
- Failed attempts:
  - max `5` failed attempts in `15` minutes,
  - account lock for `15` minutes after threshold.
- Session TTL:
  - access token: `15` minutes,
  - refresh token: `8` hours, rotated on use.
- Global logout:
  - revoke all active refresh tokens for that user.

## Password Policy
- Min length: `12`.
- Must include: upper, lower, number, special char.
- Disallow reuse of last `5` password hashes.
- Password rotation recommendation: every `90` days (enforced later if needed).

## RBAC Model (Phase-1)
- `SUPER_ADMIN`:
  - full CM access, user/role management, sync controls.
- `READ_ONLY_OPERATOR`:
  - report/dashboard access only.
- `ANALYST`:
  - read reports + export capability, no config changes.

## API Contract (Phase-1)
- `POST /cm-auth/login`
  - input: `username`, `password`
  - output: access token, refresh token, `mustChangePassword`, user roles.
- `POST /cm-auth/refresh`
  - input: refresh token
  - output: new token pair.
- `POST /cm-auth/change-password`
  - input: `oldPassword`, `newPassword`
  - output: success/failure.
- `POST /cm-auth/logout`
  - input: refresh token
  - output: success.

## Cassandra Tables (CM Auth Namespace)
```sql
CREATE TABLE cm_auth.users (
  username text PRIMARY KEY,
  password_hash text,
  role_set set<text>,
  status text,
  must_change_password boolean,
  failed_attempts int,
  locked_until timestamp,
  created_at timestamp,
  updated_at timestamp,
  last_login_at timestamp
);

CREATE TABLE cm_auth.refresh_tokens (
  token_id uuid PRIMARY KEY,
  username text,
  token_hash text,
  issued_at timestamp,
  expires_at timestamp,
  revoked boolean,
  revoked_at timestamp
);

CREATE TABLE cm_auth.audit_log (
  day text,
  ts timestamp,
  event_id uuid,
  username text,
  action text,
  outcome text,
  ip text,
  user_agent text,
  details text,
  PRIMARY KEY (day, ts, event_id)
) WITH CLUSTERING ORDER BY (ts DESC, event_id ASC);
```

## Hardening Notes
- Keep bootstrap enabled only for first initialization.
- After first password change, disable `root/root` login permanently.
- Optional production flag:
  - `CM_ALLOW_DEFAULT_BOOTSTRAP=false` after initial setup.

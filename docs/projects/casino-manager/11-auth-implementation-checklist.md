# Auth Implementation Checklist

## Goal
Ship phase-1 CM auth with secure bootstrap and RBAC.

## Mandatory Functional Items
- [ ] Create `cm_auth` keyspace/tables.
- [ ] Implement startup bootstrap:
  - [ ] if no users exist, create `root` with password `root`,
  - [ ] mark `mustChangePassword=true`,
  - [ ] assign `SUPER_ADMIN`.
- [ ] Implement login endpoint.
- [ ] Implement refresh endpoint (rotating refresh tokens).
- [ ] Implement change-password endpoint.
- [ ] Implement logout endpoint (token revocation).
- [ ] Enforce forced password change before other API access.
- [ ] Implement lockout policy (`5` fails in `15` min, lock `15` min).

## Mandatory Security Items
- [ ] Use `Argon2id` for password hashes.
- [ ] Store only token hashes, never raw refresh tokens.
- [ ] Enforce HTTPS and secure cookie/header strategy.
- [ ] Add request-level audit log for auth actions.
- [ ] Disable default bootstrap after first successful password change.

## RBAC Enforcement
- [ ] Define roles:
  - [ ] `SUPER_ADMIN`
  - [ ] `READ_ONLY_OPERATOR`
  - [ ] `ANALYST`
- [ ] Add middleware guard by route/report.
- [ ] Restrict sync-control APIs to `SUPER_ADMIN`.

## Verification
- [ ] Unit tests:
  - [ ] bootstrap creation path,
  - [ ] login success/failure paths,
  - [ ] lockout behavior,
  - [ ] forced password change gate,
  - [ ] refresh rotation/revocation.
- [ ] Integration tests:
  - [ ] end-to-end login + forced change + access grant.
- [ ] Security checks:
  - [ ] brute-force rate limiting works,
  - [ ] expired/revoked token rejection works.

## Exit Criteria
- [ ] `root/root` only works until first password change.
- [ ] No plaintext passwords or tokens in logs/db.
- [ ] Auth audit rows available for all auth actions.

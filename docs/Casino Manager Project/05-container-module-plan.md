# Dedicated CM Container Plan

## Target
Deploy CM as an independent module/container with clear boundaries from GS/MP runtime.

## Proposed Services
- `cm-web`:
  - UI + API facade for CM pages/actions.
- `cm-auth` (embedded in `cm-web` for phase-1):
  - login/session/role checks and bootstrap admin user management.
- `cm-sync-worker`:
  - hourly differential sync jobs.
- `cm-db`:
  - read-optimized copy DB (or schema namespace) used only by CM.
- `cm-cache` (optional):
  - short TTL cache for heavy reports.

## Interface Boundaries
- CM reads from `cm-db`, not from live transactional stores directly.
- CM write actions (if enabled later) go through explicit service endpoints with audit trail.
- No direct provider-side action calls from production CM without approval gate.

## Deployment Notes
- Separate docker compose profile/module for CM.
- Dedicated env file and secrets mount.
- Independent health checks and restart policy.
- Dedicated logs/metrics namespace for CM.

## First Milestone Deliverable
- Bring up `cm-web + cm-db + cm-sync-worker` in local stack.
- Implement bootstrap authorization:
  - default admin credentials `root` / `root`,
  - forced password change on first login.
- Implement 3 read-only pages end-to-end:
  - `Player Search`
  - `Bank List`
  - `Transactions`

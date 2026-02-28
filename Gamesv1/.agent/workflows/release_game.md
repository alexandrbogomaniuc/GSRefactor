# /release_game - Execute Production Release Workflow

When triggered:
1) Read `docs/RELEASE_PROCESS.md` fully and extract target `gameId`, environment, and version.
2) Generate/check GS registration artifacts:
```bash
npm run config:gen
```
3) Run build + assetpack release build:
```bash
corepack pnpm --filter @games/<gameId> build
corepack pnpm --filter @games/<gameId> exec vite build
```
4) Produce release manifest + SQL registration artifact.
5) Perform deployment steps (DB registration + CDN routing activation).
6) Verify launch URLs in `guest`, `free`, and `real` modes.
7) Validate runtime handshake sequence and reconnect expectations.
8) Output a release summary with pass/fail for every numbered checklist item.

Mandatory validation points:
- `BET_REQUEST -> BET_ACCEPTED -> SETTLE_REQUEST -> SETTLE_ACCEPTED -> balance update`
- Retry/idempotency uses identical `operationId`
- Reconnect yields sync/recovery behavior (WS session sync or ExtGame state echo)

Acceptance:
- Every checklist item in `docs/RELEASE_PROCESS.md` is marked pass/fail with evidence.
- Artifacts exist and map to the same version (`CDN`, `manifest`, `SQL`, `DB`).
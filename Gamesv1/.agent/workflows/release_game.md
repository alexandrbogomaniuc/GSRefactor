# /release_game - Execute Production Release Workflow

When triggered:
1) Read `docs/RELEASE_PROCESS.md` and capture `gameId`, environment, version.
2) Generate/check GS registration artifacts:
```bash
npm run config:gen
```
3) Run build + production asset packaging:
```bash
corepack pnpm --filter @games/<gameId> build
corepack pnpm --filter @games/<gameId> exec vite build
```
4) Produce release metadata + registration SQL artifact.
5) Perform deployment steps (GS registration + CDN/static activation).
6) Verify launch URLs for `guest`, `free`, `real`.
7) Validate canonical HTTP runtime behavior:
- init/enter session load
- transaction request/response flow
- restore on reconnect/reload
8) Output pass/fail release summary for every checklist item.

Mandatory validation points:
- Idempotency keys stable across retries
- requestCounter/sequencing respected
- Client treats GS wallet/session as authoritative
- Artifact versions match (`CDN`, `manifest`, `SQL`, `DB`)

Acceptance:
- Every `docs/RELEASE_PROCESS.md` checklist item has evidence.
- Release artifacts map to the same version and git SHA.

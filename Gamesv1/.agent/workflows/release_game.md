# /release_game - Execute Production Release Workflow

When triggered:
1) Read `docs/RELEASE_PROCESS.md` and capture `gameId`, environment, version.
2) Generate/check GS registration artifacts:
```bash
corepack pnpm run config:gen
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
- `POST /v1/opengame` session load
- `POST /v1/placebet` + `POST /v1/collect` round flow
- restore on reconnect/reload
- browser->GS transport only (no direct slot-engine/browser coupling)
- server-side slot-engine audit data not treated as browser UI state
8) Output pass/fail release summary for every checklist item.

Mandatory validation points:
- Idempotency keys stable across retries
- requestCounter/sequencing respected
- Client treats GS wallet/session as authoritative
- Artifact versions match (`CDN`, `manifest`, `SQL`, `DB`)

Acceptance:
- Every `docs/RELEASE_PROCESS.md` checklist item has evidence.
- Release artifacts map to the same version and git SHA.

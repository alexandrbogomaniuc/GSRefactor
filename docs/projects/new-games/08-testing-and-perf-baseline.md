# Testing And Performance Baseline

Last updated: 2026-02-13

## Scope
This document records the first automated quality gate for `new-games-server`:
- API contract tests
- idempotency tests
- request-counter negative tests
- upstream timeout failure tests
- reconnect state-resume tests
- local smoke performance harness

## Automated Contract Tests
Location:
- `/Users/alexb/Documents/Dev/new-games-server/test/ngs-contract.e2e.test.ts`
- `/Users/alexb/Documents/Dev/new-games-server/test/ngs-failure-reconnect.e2e.test.ts`

Command:
```bash
npm --prefix /Users/alexb/Documents/Dev/new-games-server test
```

Coverage in this suite:
- `opengame -> placebet -> collect -> readhistory` happy path
- duplicate `placebet` idempotency by `clientOperationId`
- duplicate `collect` idempotency by `clientOperationId`
- out-of-order `requestCounter` rejection
- insufficient funds rejection
- `opengame` timeout on GS session validate
- `opengame` stale SID mismatch auto-recovery (retry with GS expected SID)
- `placebet` timeout on GS wallet reserve
- `collect` timeout on GS wallet settle
- reconnect (`opengame` re-entry) preserves session balance and request counter

Latest result (2026-02-13):
- tests: 8
- pass: 8
- fail: 0

## Performance Smoke Harness
Location:
- `/Users/alexb/Documents/Dev/new-games-server/scripts/perf-smoke.ts`

Run with a live NGS process:
```bash
npm --prefix /Users/alexb/Documents/Dev/new-games-server run dev
npm --prefix /Users/alexb/Documents/Dev/new-games-server run perf:smoke
```

Default harness config:
- `sessions=200`
- `roundsPerSession=2`
- `concurrency=40`
- `betAmount=100`

Latest run (2026-02-12):
- rounds: `400`
- duration: `0.18s`
- achieved bets/sec: `2210.19`
- placebet latency: `p50=8.25ms`, `p95=13.04ms`, `p99=17.87ms`
- collect latency: `p50=8.28ms`, `p95=15.56ms`, `p99=17.2ms`

## Notes
- This is a local single-node smoke baseline (not production-like).
- Reconnect/timeout baseline is now covered at API level; network chaos and multi-node reconnect are still pending for milestone M4.
- During setup we fixed a runtime dependency mismatch:
  - `@fastify/cors` downgraded to Fastify 4 compatible line (`^8.5.0`).

## Live Runtime E2E Proof
Latest run (2026-02-12):
- route launch:
  - `GET http://localhost/cwstartgamev2.do?bankId=6274&gameId=00010&mode=real&token=bav_game_session_001&lang=en` -> `302` to NGS client URL with `SID`, `ngsApiUrl`, `gsInternalBaseUrl`
- direct GS-internal validation:
  - `POST http://localhost:81/gs-internal/newgames/v1/session/validate` -> `200`
- full NGS contract chain against real GS-internal base:
  - `POST /v1/opengame` -> `200`
  - `POST /v1/placebet` -> `200`
  - `POST /v1/collect` -> `200`
  - `POST /v1/readhistory` -> `200`
- runtime evidence:
  - GS logs show reserve and settle wallet calls to casino-side `bav/betResult`.
  - GS logs show `NGS_HISTORY_WRITE` events for `BET_PLACED` and `ROUND_COLLECTED`.

## Runtime Automation (2026-02-13)
- Deploy/hot-swap + runtime checks:
  - `cd /Users/alexb/Documents/Dev/new-games-server && npm run runtime:deploy-gs`
- Full launch + API E2E verification:
  - `cd /Users/alexb/Documents/Dev/new-games-server && npm run runtime:e2e`

Latest script validation:
- `RESTART_GS=0 npm run runtime:deploy-gs` -> success (`GET /gs-internal/newgames/v1/session/validate` returned `405` as expected for GET, route launch returned `302`).
- `npm run runtime:e2e` -> success (`GSValidateStatus=200`, `opengame/placebet/collect/readhistory` all `200`).

These scripts reduce manual runtime steps and provide a stable one-command checkpoint for crash recovery.

## Runtime Packaging And Handoff (2026-02-13)
- Added GS runtime artifact build script:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/build-gs-runtime-bundle.sh`
  - output: `artifacts/gs-runtime/newgames-gs-runtime-<timestamp>.tar.gz` + `.sha256`
- Extended runtime deploy to support prebuilt artifact input:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/deploy-gs-runtime.sh`
  - override: `CLASS_BUNDLE=/absolute/path/to/tar.gz`
- Added runtime status snapshot script for ops handoff:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/runtime-status.sh`
- Added npm aliases:
  - `runtime:build-bundle`
  - `runtime:status`
- Added runbook:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/new-games/09-runtime-ops-handoff.md`

## M4 Proof Pack (2026-02-13)
- Added one-command M4 proof automation:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/perf-proof-pack.sh`
  - npm command: `npm --prefix /Users/alexb/Documents/Dev/new-games-server run runtime:proof-pack`
- Proof pack executes and stores:
  - runtime E2E (`runtime:e2e`) output,
  - runtime status (`runtime:status`) output,
  - performance JSON (`perf:smoke`) output,
  - performance stderr output (`perf:smoke` stderr),
  - SLO verdict report in docs evidence folder.
- Latest generated report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/new-games/evidence/m4-proof-pack-20260213-120017.md`
  - result: `PASS`
  - checks:
    - bets/sec >= `100`: `2087.59`
    - placebet p95 <= `250ms`: `14.46ms`
    - collect p95 <= `300ms`: `17.26ms`
    - runtime E2E chain: `PASS`

## M3 Client Parity Baseline (2026-02-13)
- Implemented UI parity blocks in Plinko client:
  - settings (`animation speed`, `sound toggle`),
  - autobet (`rounds`, `interval`, `start/stop`, progress),
  - history feed + explicit server sync (`readhistory`),
  - rules section with payout guidance.
- Updated files:
  - `/Users/alexb/Documents/Dev/new-games-client/index.html`
  - `/Users/alexb/Documents/Dev/new-games-client/src/main.ts`
  - `/Users/alexb/Documents/Dev/new-games-client/src/style.css`
- Verification:
  - `npm --prefix /Users/alexb/Documents/Dev/new-games-client run build` -> success.

## Session-Mismatch Recovery Fix (2026-02-13)
- Root cause:
  - GS validate can reject stale SIDs with `Mismatch sessionId` and expected fresh SID.
- Fix:
  - NGS `opengame` now retries session validate once using the `expected` SID parsed from GS error.
  - file: `/Users/alexb/Documents/Dev/new-games-server/src/index.ts`
- Client UX alignment:
  - session input is synchronized to returned SID after successful open.
  - stale-session failures are surfaced with reload guidance.
  - file: `/Users/alexb/Documents/Dev/new-games-client/src/main.ts`
- Test evidence:
  - new e2e test in `/Users/alexb/Documents/Dev/new-games-server/test/ngs-failure-reconnect.e2e.test.ts` validates stale SID recovery path.

## Browser Gameplay Simulation (2026-02-13)
- Simulated normal play flow from live GS launch URL:
  - launch redirect to client (`cwstartgamev2 -> 302 -> localhost:5174`),
  - manual `Drop Ball`,
  - auto bet `5` rounds,
  - history sync.
- Result:
  - all flows completed without runtime errors.
  - history, balance, counter, and round state updates were coherent in UI and backend.
- Stability rerun:
  - `20x runtime:e2e` loop completed with `TOTAL_FAILS=0`.

## UI Redesign + Legacy Regression (2026-02-13)
- Added BetOnline-themed redesign + branded preloader in new client:
  - `/Users/alexb/Documents/Dev/new-games-client/index.html`
  - `/Users/alexb/Documents/Dev/new-games-client/src/style.css`
  - `/Users/alexb/Documents/Dev/new-games-client/src/main.ts`
- Browser verification:
  - new route (`gameId=00010`) launch + open + manual spin + 3-round auto bet completed.
  - legacy route (`gameId=838`) verified unchanged routing to:
    - `/real/mp/template.jsp?...` and legacy iframe chain (`MAX DUEL`).
- Console observations:
  - new route: no app errors.
  - legacy route: non-blocking warnings/asset noise only; no new routing regressions introduced by client changes.

## Physics + Landing Accuracy Upgrade (2026-02-13)
- Implemented gameplay mechanics/visual upgrades:
  - metallic pins with shadow/highlight volume,
  - physics-style bounce simulation (gravity + peg collision response + rebound damping),
  - symmetric pots and separator walls,
  - final-slot snap and impact explosion effect so landing is always in a valid pocket.
- Configurable falling speed:
  - `Calm/Normal/Fast/Turbo` in client settings.
- Browser validation:
  - new route (`gameId=00010`) verified with manual spin + auto-bet while `Turbo` speed was active.
  - observed: no console errors and stable settle flow.
- Legacy validation:
  - `gameId=838` remains legacy (`template.jsp` -> iframe `MAX DUEL`) after these changes.

## RTP Baseline (2026-02-13)
- Current deterministic paytable target:
  - theoretical RTP: `97.73%`.
- Source of truth:
  - `/Users/alexb/Documents/Dev/new-games-server/src/index.ts` (`deterministicSlot` + `deterministicOutcome`).
- Current slot model:
  - weights: `[1,8,28,56,70,56,28,8,1]`
  - multipliers: `[14,4,1.8,0.45,0.1,0.45,1.8,4,14]`
- Note:
  - this is server-side deterministic prototype math, not yet a certified production math package.

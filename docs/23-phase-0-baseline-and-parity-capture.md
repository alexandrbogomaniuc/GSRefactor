# Phase 0 - Baseline and Parity Capture (Started)

Last updated: 2026-02-19 UTC
Baseline commit: `2fcdd18293654eb97fbefda4ad857e8c1ed6e894`

## 1) Endpoint/Protocol Inventory (Initial Freeze)

### Raw inventory artifacts
- Struts action routes: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/struts-action-paths.txt` (131 paths)
- Spring endpoint annotations: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/spring-endpoint-annotations.txt`

### Critical contract inventory (parity scope)

| Domain | Endpoints / Routes | Contract Mode | Compatibility Rule |
|---|---|---|---|
| Launch | `/cwstartgamev2.do`, `/bsstartgame.do`, `/cwstartgameidfrb.do`, `/restartgame.do` | HTTP query -> redirect/template payload | request keys and launch branching remain stable |
| Game list | `/gamelistExt.do`, `/frbgamelist.do` | HTTP response (legacy format) | no semantic drift in enabled/visible games |
| Wager/settle wallet bridge | `/bscheck.do`, `/bsaward.do`, `/bscancel.do` | XML-centric legacy responses | preserve response codes/messages by scenario |
| FRB | `/frbcheck.do`, `/frbaward.do`, `/frbcancel.do`, `/frbinfo.do`, `/frbhistory.do` | XML-centric legacy responses | preserve FRB state transitions and status codes |
| History | `/vabs/historyByRound.do`, `/vabs/historyByToken.do`, `/cwstarthistory.do` | HTTP redirect/render + lookup behavior | same not-found and success behaviors |
| Support/ops | `/support/*`, `/support/metrics`, `/support/showAPIIssues.do` | HTML + support APIs | preserve operator workflows and health visibility |
| Websocket handoff | launch payload `WEB_SOCKET_URL`, `/webSocket`, `/tournamentWebSocket` | WS URL in launch, websocket upgrade endpoints | preserve WS URL semantics and session correlation |

### Planned protocol-mode extension (required)
- New bank-level setting: `protocolMode = JSON | XML`.
- Internal canonical model remains single; JSON/XML conversion only at boundary adapters.
- Backward compatibility default in migration phases: legacy XML behavior unless bank override is explicitly enabled.

## 2) Behavior Parity Test Matrix (v1)

| Test ID | Flow | Scenario | Legacy Baseline Expectation | Modernization Gate |
|---|---|---|---|---|
| P0-LA-01 | Launch | Real-money launch happy path | 2xx + valid redirect/template payload + SID | exact contract parity |
| P0-LA-02 | Launch | Invalid token/auth params | legacy error code/body unchanged | exact error parity |
| P0-LA-03 | Launch | Maintenance mode gate | same blocking behavior/page | exact gate parity |
| P0-WA-01 | Wager | Debit success | wallet operation transitions to expected completed state | idempotent op ID present |
| P0-WA-02 | Wager | Duplicate retry | no double debit | idempotent replay-safe behavior |
| P0-SE-01 | Settle | Credit success | final round/session state consistent | outbox event produced once |
| P0-SE-02 | Settle | Upstream timeout/retry | no duplicate credit, deterministic final state | idempotent replay-safe behavior |
| P0-FRB-01 | FRB | FRB check + award + cancel sequence | same eligibility and state transitions | exact FRB parity |
| P0-HI-01 | History | Existing round/token lookup | same redirect/render content semantics | exact data parity |
| P0-HI-02 | History | Missing round/token | same not-found behavior | exact error parity |
| P0-RC-01 | Reconnect | active session reconnect | same session ownership checks | strict SID parity |
| P0-RC-02 | Reconnect | session mismatch/tamper | same rejection behavior | strict mismatch parity |

## 3) Golden Flows (v1)

### GF-1 Launch
1. Request `cwstartgamev2` with `bankId`, `gameId`, `mode`, `token`, `lang`.
2. Auth/token validation.
3. Session open and launch branch (single-player/multiplayer/FRB/new-games).
4. Redirect/render payload includes SID + required params.

Required invariants:
- launch contract fields unchanged,
- same routing branch for same inputs,
- trace fields attached (`traceId`, `sessionId`, `bankId`, `gameId`).

### GF-2 Wager
1. Active SID submits bet/debit path.
2. Wallet reserve/debit operation created.
3. Session/round state persisted.

Required invariants:
- no duplicate debit,
- operation correlation via `operationId`,
- auditable transition state.

### GF-3 Settle
1. Round result submitted.
2. Wallet credit/settle path executed.
3. Final session/round state persisted and history event emitted.

Required invariants:
- exactly-once financial effect,
- deterministic close semantics,
- audit log continuity.

### GF-4 History
1. Query by round OR token.
2. Resolve to session history view/data.

Required invariants:
- same found/not-found semantics,
- stable response/redirect contract.

### GF-5 FRB
1. Check FRB eligibility.
2. Apply award/cancel operations by FRB flow.
3. Validate FRB state transitions and restrictions.

Required invariants:
- no FRB state corruption,
- same business outcomes under same input.

### GF-6 Reconnect
1. Reconnect attempt with existing session context.
2. Validate ownership and session continuity.

Required invariants:
- strict mismatch rejection,
- stable reconnect behavior for valid session.

## 4) Rollback and Parity Safeguards for Phase 0
- Legacy handlers remain traffic owner while parity assets are built.
- No schema or protocol-breaking change in Phase 0.
- Any instrumentation additions must be non-invasive and flag-controlled.

## 5) Next Execution Items (Immediate)
1. Implement replay harness skeleton for `P0-LA-01`, `P0-WA-01`, `P0-SE-01` in dry-run mode.
2. Attach sample fixtures for one canary bank/game pair.
3. Wire matrix execution results (`pass/fail`, evidence link) into this document.

## 6) Replay Harness Skeleton (Implemented)
- Script: `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase0-parity-harness.sh`
- Fixture template: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-fixture.env.example`
- Dry-run evidence: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-parity-20260220-093037.md`

### Initial execution status
| Test ID | Status | Notes |
|---|---|---|
| P0-LA-01 | DRY_RUN | launch command generated with canary bank/game fixture (`6274/838`) |
| P0-WA-01 | DRY_RUN | endpoint command generated; requires wallet fixture values for run mode |
| P0-SE-01 | DRY_RUN | endpoint command generated; requires wallet fixture values for run mode |

### First run-mode probe (refactor stack not started)
- Evidence: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-parity-20260220-093311.md`
- Observed:
  - `P0-LA-01`: `FAIL_EXEC` (cannot connect to `localhost:18080`),
  - `P0-WA-01`: `SKIPPED_MISSING_FIXTURE`,
  - `P0-SE-01`: `SKIPPED_MISSING_FIXTURE`.

### Run-mode after isolated refactor stack startup
- Evidence: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-parity-20260220-094035.md`
- Observed:
  - `P0-LA-01`: `PASS_HTTP (200)` with body artifact `P0-LA-01-20260220-094035.body.txt` (HTTP-only check, now superseded),
  - `P0-WA-01`: `SKIPPED_MISSING_FIXTURE`,
  - `P0-SE-01`: `SKIPPED_MISSING_FIXTURE`.

### Run-mode after contract-level checks (corrected)
- Evidence: `/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution/phase0-parity-20260220-094449.md`
- Observed:
  - `P0-LA-01`: `FAIL_CONTRACT (200)`; response body contains legacy error page text `Bank is incorrect` (`P0-LA-01-20260220-094449.body.txt`),
  - `P0-WA-01`: `SKIPPED_MISSING_FIXTURE`,
  - `P0-SE-01`: `SKIPPED_MISSING_FIXTURE`.

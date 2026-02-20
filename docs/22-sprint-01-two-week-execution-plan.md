# Sprint 01 (2 Weeks) - Execution Plan

Window: 2026-02-19 to 2026-03-04 (UTC)
Goal: complete Phase 0 baseline/parity capture and establish migration governance + observability minimums without breaking legacy behavior.

## Sprint Tasks and Acceptance Criteria

| Priority | Task | Owner | Acceptance Criteria |
|---|---|---|---|
| P0 | Bootstrap verification package | Platform | branch `Codex` verified, backup tag exists, source and `Dev_new` commit hashes match, evidence recorded in docs |
| P0 | Endpoint/protocol inventory freeze | Compatibility | full `.do` Struts action inventory captured, Spring endpoints captured, protocol classification for launch/wager/settle/history/FRB/support complete |
| P0 | Parity matrix v1 | Compatibility + QA | matrix includes launch/wager/settle/history/FRB/reconnect scenarios, expected status/body invariants, and pass/fail slots for legacy vs modern path |
| P0 | Golden flow definitions | Architecture + QA | deterministic request/response flows documented for launch, wager, settle, history, FRB, reconnect with required correlation keys |
| P0 | Replay harness skeleton | QA | CLI harness scaffolding created with stable fixtures for top banks/games; dry-run executes without mutating production data |
| P1 | Observability contract baseline | Platform | `traceId`, `sessionId`, `bankId`, `gameId`, `operationId`, `configVersion` fields defined in logging contract and mapped to current hot paths |
| P1 | Error taxonomy draft | Platform + Support | top error classes mapped to actionable buckets (`validation`, `upstream`, `state`, `dependency`, `unknown`) with runbook links |
| P1 | CI quality gates baseline | DevEx | lint/test/smoke gates defined; branch policy requires green checks for parity-related changes |
| P1 | Bank canary policy draft | Architecture + Ops | canary bank selection criteria, success thresholds, rollback trigger conditions documented |
| P2 | Backlog slicing for Phase 3/4 | Architecture | Config Service and protocol adapter epics split into implementable stories with dependencies and risk tags |

## Sprint Exit Criteria
1. Phase 0 artifacts are versioned and reviewable in `Dev_new/docs`.
2. At least one deterministic golden flow test script is runnable in dry-run mode.
3. No endpoint contract changes are introduced in legacy serving path.
4. Rollback triggers and rollback owner are explicit for all P0/P1 tasks.

## Out of Scope (Sprint 01)
- no production cutover,
- no Cassandra version upgrade,
- no package-wide rename waves,
- no direct deprecation of legacy endpoints.

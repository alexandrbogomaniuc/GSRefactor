# Milestones

Last updated: 2026-02-13

## Progress Snapshot (2026-02-13)
- M1: completed in local runtime (`00010` route + GS internal API v1 + wallet/history path).
- M2: completed for backend vertical slice (deterministic flow + e2e/idempotency/reconnect tests).
- M3: completed for client parity baseline (settings/history/rules/autobet UX + runtime integration).
- M4: completed for local load/latency proof (`runtime:proof-pack` reproducible PASS reports).

## Launch Targets (Current)
- 200 concurrent players.
- 100 bets/second sustained.
- No mandatory compliance feature gates in phase 1.

## M0: Foundation Definition
- Finalize architecture boundaries between GS and New Games Runtime.
- Freeze API contract v1 for Plinko (`opengame/placebet/collect/readhistory`).
- Define idempotency and retry semantics for wallet-impacting operations.
- Output:
  - contract doc,
  - sequence diagrams,
  - acceptance checklist.

## M1: GS Integration Layer
- Implement internal GS endpoints/services used by New Games Runtime.
- Reuse existing GS lock/session and wallet flow primitives.
- Add tracing IDs across GS and New Games Runtime for each round command.
- Output:
  - integration working in local env with mock client.

## M2: Plinko Backend Vertical Slice
- Implement Plinko round engine and state transitions.
- Support rows/risk combinations and payout table variants.
- Support request counters, replay protection, and collect flow.
- Output:
  - deterministic test suite,
  - e2e API tests.

## M3: Plinko Client Vertical Slice (BETONLINE Direction)
- Implement modern PixiJS client with BETONLINE-aligned look and feel.
- Implement settings/history/rules/autobet UX.
- Integrate with New Games Runtime + GS-backed wallet results.
- Output:
  - playable end-to-end build in dev stack.

## M4: Performance And Stability
- Load test at 100 bets/sec and 200 concurrent players.
- Validate p95/p99 for `placebet` and `collect`.
- Add failure-mode tests: reconnect, duplicate requests, delayed responses.
- Output:
  - performance report,
  - production hardening tasks.

## M5: Beta Release
- Controlled rollout on selected bank configuration.
- Run operational playbook with support/monitoring.
- Track defect burn-down and readiness metrics.
- Output:
  - go/no-go review packet.

## Proposed Acceptance SLOs (Initial Draft)
- `placebet` success rate: >= 99.9% (excluding invalid client requests).
- `placebet` p95 latency: <= 250 ms under target load.
- `collect` p95 latency: <= 300 ms under target load.
- Duplicate charge rate: 0 (idempotency required).

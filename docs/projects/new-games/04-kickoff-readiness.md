# Kickoff Readiness Checklist

Last updated: 2026-02-12

This is the minimum required to start implementation safely and fast.

## A) Must-Have Decisions (Project-Level)
- [x] GS is core platform.
- [x] GS performs all casino-side wallet calls.
- [x] First game is Plinko.
- [x] Initial load target: 200 concurrent players, 100 bets/second.
- [x] Phase-1 compliance gates are out of scope.

## B) Repositories And Structure
- [x] Dedicated docs folder created: `docs/projects/new-games/`.
- [x] Create implementation workspace for new services and client:
  - suggested:
    - `/Users/alexb/Documents/Dev/new-games-server/`
    - `/Users/alexb/Documents/Dev/new-games-client/`
- [ ] Create `codex/new-games-kickoff` branch for first implementation cycle.

## C) Runtime And Tooling Baseline
- [ ] Confirm local stack health before coding:
  - GS initialized and reachable.
  - MP/static/cassandra/kafka/zookeeper healthy.
- [ ] Toolchain baseline:
  - Java 8 + Maven for GS work.
  - Node.js 20 LTS for new client/server JS stack.
  - Docker Compose for integrated local test runs.

## D) Integration Contract (Blocking For Build Start)
- [x] Freeze v1 internal contract between New Games runtime and GS:
  - session validation,
  - wallet reserve/debit,
  - wallet settle/credit,
  - history write/read,
  - idempotency + requestCounter rules.
- [x] Define unified error envelope and retry policy.
- [x] Define tracing fields (requestId, sessionId, roundId, operationId).

## E) Product And UX Inputs
- [ ] BETONLINE brand input pack (official):
  - logos,
  - color palette,
  - typography rules,
  - usage constraints.
- [x] Added temporary placeholder brand foundation and assets for prototype.
- [ ] Confirm allowed asset reuse scope from reference providers.
- [ ] Define “parity vs custom” for Plinko UX:
  - payout math parity level,
  - custom visual style boundaries.

## F) Testing Gates To Start Development
- [x] Contract tests for `opengame/placebet/collect/readhistory`.
- [x] Idempotency tests (duplicate placebet/collect).
- [x] Failure tests (timeout, reconnect, out-of-order requestCounter).
- [x] Performance smoke test harness for 100 bets/sec.

Evidence:
- test suite: `/Users/alexb/Documents/Dev/new-games-server/test/ngs-contract.e2e.test.ts`
- failure/reconnect suite: `/Users/alexb/Documents/Dev/new-games-server/test/ngs-failure-reconnect.e2e.test.ts`
- perf harness: `/Users/alexb/Documents/Dev/new-games-server/scripts/perf-smoke.ts`
- report: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/new-games/08-testing-and-perf-baseline.md`
- note: timeout coverage now depends on `GS_INTERNAL_TIMEOUT_MS` in NGS runtime.

## G) Operations Baseline
- [ ] Define first dashboard metrics:
  - bet request rate, success rate, p95/p99 latency,
  - wallet call latency/error rate,
  - duplicate operation count.
- [ ] Define alert thresholds for launch beta.

## We Can Start Immediately If
1. You approve creating `new-games-server` and `new-games-client` directories in this workspace.
2. We proceed with a draft GS-internal API contract document as the next artifact.

## Day-1 Execution Plan (Recommended)
1. Create project skeletons (`new-games-server`, `new-games-client`).
2. Write `05-gs-internal-api-contract-v1.md`.
3. Implement mock endpoints and end-to-end local “happy path”.
4. Wire Plinko request flow (`opengame -> placebet -> collect`) with deterministic test math.

# ADR-001: Redis for Deterministic Math Runtime State (GS Modernization)

Status: Accepted (v1)  
Date: 2026-02-20 UTC  
Scope: `/Users/alexb/Documents/Dev/Dev_new` GS modernization only

## Context
- GS modernization requires strict financial idempotency, auditability, and reconnect recovery.
- Game math/outcome behavior must be deterministic: same `seed + state + input` => same outcome.
- Keeping runtime state only in-memory risks loss on restart and weak reconnect behavior.

## Decision
Use Redis as a fast ephemeral state layer for gameplay/session recovery and idempotency acceleration, while keeping durable financial truth in persistent stores.

Redis is approved for:
1. `session:{sessionId}:stateBlob` (TTL): compact deterministic state snapshot for reconnect.
2. `session:{sessionId}:lastSeq` (TTL): replay/reorder anchor for WS traffic.
3. `op:{operationId}:result` (TTL): cached idempotent response for duplicate bet/settle retries.

Redis is not approved for:
1. canonical wallet ledger,
2. final financial settlement source-of-truth,
3. long-term audit storage.

## Rationale
1. Improves reconnect latency and resilience for both current and future third-party games.
2. Reduces duplicate downstream calls under retry storms.
3. Preserves bank-grade safety because durable stores/outbox remain canonical.

## Guardrails
1. Every state blob includes `configVersion`, `updatedAt`, and deterministic context (`seed`, `roundId`, or equivalent).
2. Redis outage must degrade to durable-read recovery path with no money loss.
3. Idempotency outcome must be reproducible from durable event/ledger history.
4. TTL policy is bank-configurable and defaults conservatively.

## Rollback
1. Feature flag `redisStateCacheEnabled=false` routes all reads to durable recovery path.
2. No schema change required for rollback.
3. Existing protocol and integration contracts remain unchanged.

## Consequences
- Positive: lower reconnect MTTR, cleaner replay handling, better horizontal scalability.
- Tradeoff: added infrastructure dependency and cache invalidation complexity.
- Mitigation: observability + fallback mode + deterministic rehydration tests.

## Validation Plan
1. Determinism test: replay same `seed + state + input` against cache-hit and cache-miss paths; outcome must match.
2. Failure test: kill Redis during active sessions; verify no duplicate financial effects and successful durable recovery.
3. Replay test: duplicate operation IDs return identical responses with and without Redis.

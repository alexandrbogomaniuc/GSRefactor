# GS Modernization Roadmap v1

Last updated: 2026-02-19 UTC
Baseline commit: `2fcdd18293654eb97fbefda4ad857e8c1ed6e894`

## Guardrails
- Backward compatibility is mandatory for Casino Side, MP/client, New Games.
- No big-bang rewrite; strangler extraction with compatibility facade.
- Every phase has bank-gated rollout and rollback switch.
- Financial operations remain idempotent and auditable.

## Prioritized Milestones

| Milestone | Phase | Target Window | Key Outputs | Dependencies | Rollback Gate |
|---|---|---|---|---|---|
| M0 | Phase 0: Baseline + parity capture | Week 1-3 | endpoint/protocol inventory, parity matrix, golden flows, replay harness skeleton | none | legacy-only traffic default |
| M1 | Phase 1: Repo/governance | Week 1-2 (parallel) | branch policy, CI quality gates, baseline tags, backup runbook | M0 baseline commit fixed | frozen baseline branch/tag kept deployable |
| M2 | Phase 2: Observability foundation | Week 2-5 | correlation standard (`traceId/sessionId/bankId/gameId/operationId/configVersion`), dashboards, error taxonomy | M0 | tracing disabled by flag falls back to legacy logs |
| M3 | Phase 3: Config platform modernization | Week 4-10 | versioned config model, effective-value resolver, publish workflow | M0, M2 | read path toggles back to legacy cache layer |
| M4 | Phase 4: Protocol adapter layer | Week 6-10 | canonical model, JSON/XML adapters, per-bank `protocolMode` routing | M0, M3 | per-bank toggle to legacy serializer |
| M5 | Phase 5: Core extraction (Session + Gameplay + Wallet adapter) | Week 9-18 | extracted services behind facade, idempotent transitions, wallet resilience | M2, M3, M4 | route-level fallback to monolith handlers |
| M6 | Phase 6: Multiplayer extraction | Week 14-20 | standalone multiplayer service, bank `isMultiplayer` routing | M5 | `isMultiplayer=false` bypass + legacy MP fallback |
| M7 | Phase 7: Cassandra upgrade | Week 16-22 | rehearsal report, compatibility validation, cutover/rollback runbook | M2, M5 | dual cluster + rollback to previous Cassandra cluster |
| M8 | Phase 8: Precision modernization | Week 20-24 | decimal audit, hardcoded min removal, `0.001` verification matrix | M5, M7 | per-currency precision policy toggle to legacy min |
| M9 | Phase 9: Branding/namespace replacement | Week 22-28 | wave-based ABS rename, compatibility mapping, regression proofs | M5 stable | compatibility aliases + staged rename revert |

## Dependency Backbone
1. M0 unlocks all migration work by freezing parity truth.
2. M2 must land before service extractions to avoid blind cutovers.
3. M3 and M4 are prerequisites for safe bank-by-bank canaries.
4. M5 is prerequisite for multiplayer split (M6), precision (M8), and large-scale rename (M9).
5. Cassandra upgrade (M7) only after parity + observability + idempotent core paths are in place.

## Runtime State Guidance
- Add Redis as fast ephemeral state for reconnect/idempotency caches (`stateBlob`, `lastSeq`, `operation replay`).
- Keep financial truth in durable ledger/outbox stores; Redis is optimization only.

## Risk Register

| ID | Risk | Severity | Impact | Mitigation | Early Signal |
|---|---|---|---|---|---|
| R1 | Hidden legacy behavior regression | Critical | protocol breaks for existing banks | parity matrix + replay contracts before each cutover | canary mismatch in response/body/error code |
| R2 | Dual-run complexity during strangler phases | High | hard-to-debug incident patterns | strict routing flags, one-way ownership by endpoint, rollback rehearsals | mixed ownership for same endpoint in logs |
| R3 | Wallet idempotency gaps | Critical | financial inconsistency | operationId + idempotency key + outbox + reconciliation checks | duplicate debit/credit attempts |
| R4 | Config propagation drift | High | wrong limits/coins/FRB behavior per node | config version stamping + lag SLO + invalidation telemetry | same request differs by node |
| R5 | Cassandra upgrade incompatibility | High | runtime failures/latency spikes | rehearsal with production-like data + driver compatibility matrix | read/write anomalies in rehearsal |
| R6 | JSON/XML adapter divergence | High | bank-specific contract break | canonical model + parity assertions for both adapters | same business op differs by mode |
| R7 | Multiplayer split regressions | Medium | non-MP banks impacted by MP code paths | `isMultiplayer` capability gate + bypass tests | MP classes touched for non-MP bank |
| R8 | Precision/rounding inconsistencies at 0.001 | High | settlement/report mismatch | decimal policy audit, currency-specific tests, wallet/reporting parity checks | mismatch between wallet and GS totals |
| R9 | Namespace rename side effects | Medium | reflection/import/config failures | controlled rename waves + compatibility map + static checks | class-loading/config key not found |

## Canary Order
1. Canary wave A: internal test bank(s) with full observability.
2. Canary wave B: low-volume production banks.
3. Global rollout only after parity suite + canary stability window pass.

# Requirements from user

Last updated: 2026-02-19 (UTC)  
Prepared for: modernization and refactoring planning  
Style: non-technical, decision-friendly

## 1. Goal of this document
This document restates your requirements and gives architecture advice for each one:
- what is good,
- what is risky,
- how to implement safely.

---

## 2. Summary of your requirements and my point of view

### Requirement 1. Keep backward compatibility
Your requirement:
- GS must keep current protocol behavior with Casino Side, MP/client, and New Games.

Advice:
- This is correct and must be a hard rule.
- We should use a compatibility facade so old endpoints and message formats still work while internals are rebuilt.

Risk:
- Hidden legacy behaviors may break if not captured in tests.

Recommendation:
1. Create contract tests for all important request/response protocols before refactor.
2. Keep old route signatures unchanged until final deprecation phase.

---

### Requirement 2. Bank-level JSON/XML communication setting
Your requirement:
- Per bank setting decides whether GS communicates with Casino Side in JSON or XML.

Advice:
- Good requirement; implement via protocol adapters.
- Internally keep one canonical model, then convert to JSON/XML only at integration boundary.

Risk:
- Duplicating business logic in two serialization paths.

Recommendation:
1. Add `protocolMode` setting per bank (`JSON` or `XML`).
2. Build `JsonAdapter` and `XmlAdapter` that map to the same internal objects.
3. Add parity tests to ensure JSON and XML output carry the same business meaning.

---

### Requirement 3. Rename legacy brand/package words to ABS
Your requirement:
- Replace words like `betsoft`, `nucleus`, `maxquest`, `mq`, `dgphoenix`, `com.dgphoenix`, etc. with `abs`/`ABS`.

Advice:
- Valid strategic branding goal, but this is high-risk if done as one big replacement.
- Package renaming affects imports, reflection, config keys, serialized names, and DB references.

Risk:
- Runtime breakage and hard-to-debug class loading/config errors.

Recommendation:
1. Do this in controlled waves, not global replace.
2. Start with UI text and docs, then config keys, then internal package refactor.
3. Keep compatibility aliases where required during transition.
4. Build a rename map and checklist before editing code.

---

### Requirement 4. Microservices + Kafka control
Your requirement:
- Move to microservices architecture and use Kafka for control/events.

Advice:
- Strong direction and aligns with modernization goals.
- Use phased strangler migration, not immediate split.

Risk:
- Too-early service splitting increases complexity and latency.

Recommendation:
1. Start with service boundaries: Config, Session, Gameplay, Bonus/FRB, History.
2. Keep compatibility facade in front.
3. Use Kafka for event propagation and cache invalidation, with idempotent consumers.

---

### Requirement 5. Backup old code, create new DEV_new and new git repo
Your requirement:
- Push current project to git, clone into `DEV_new`, and continue modernization in new repository.

Advice:
- Excellent governance practice.

Risk:
- History loss if copied incorrectly, or confusion between old/new remotes.

Recommendation:
1. Freeze and tag current stable baseline.
2. Mirror repository to backup remote.
3. Create `DEV_new` working directory from tagged baseline (exact path: `/Users/alexb/Documents/Dev/Dev_new`).
4. Verify `DEV_new` clone uses the same commit hash as source before any new changes.
5. Use separate remotes and branch strategy from day 1.

---

### Requirement 6. Review every file and upgrade technologies
Your requirement:
- File-by-file review and upgrades of technologies/versions.

Advice:
- Correct objective, but must be prioritized by risk and value.

Risk:
- Big-bang upgrades can delay core refactor and create unstable build chains.

Recommendation:
1. Build SBOM/dependency inventory first.
2. Group upgrades into:
   - critical security/runtime,
   - platform/tooling,
   - optional optimizations.
3. Upgrade with compatibility tests per group.

---

### Requirement 7. Upgrade Cassandra to latest while keeping schema/tables
Your requirement:
- First upgrade should be Cassandra to latest version, preserving current schema and tables.

Advice:
- Good target, but must be done with migration rehearsal and data compatibility checks.

Risk:
- Driver/protocol incompatibility, query behavior changes, compaction/repair differences.

Recommendation:
1. Snapshot current schema and representative data.
2. Run full migration rehearsal in staging with production-like dataset.
3. Validate read/write behavior and performance before production cutover.
4. Upgrade DB driver/client code together with Cassandra rollout.

---

### Requirement 8. User-friendly web config UI (clusters/banks/games/currencies/promos)
Your requirement:
- Friendly modular web access for GS configuration.

Advice:
- This is a major productivity win and should be first-class product, not only admin pages.

Risk:
- Without validation workflow, easy to publish dangerous config.

Recommendation:
1. Build module-based UI:
   - Clusters,
   - Banks,
   - Games,
   - Currencies,
   - Promotions.
2. Add draft -> validate -> approve -> publish -> rollback workflow.
3. Include diff and impact preview before publish.

---

### Requirement 9. Prepare GS for new modules (extra promo tools, etc.)
Your requirement:
- Easy future connectivity for additional modules.

Advice:
- Correct; introduce plugin/module contracts now.

Risk:
- Ad-hoc integrations create long-term coupling.

Recommendation:
1. Define module API contracts (events, commands, data contracts).
2. Use module registry and feature flags.
3. Enforce versioned interfaces for new modules.

---

### Requirement 10. Move multiplayer logic to separate microservice with `isMultiplayer` bank flag
Your requirement:
- Multiplayer (lobbies/rooms/sit-in) should be separated and optional per bank.

Advice:
- Very good fit for your product direction (mainly non-multiplayer).

Risk:
- Session and wallet consistency during cross-service calls.

Recommendation:
1. Split multiplayer orchestration into dedicated service.
2. Add bank capability flag `isMultiplayer`.
3. If `false`, bypass multiplayer routes completely.
4. Keep shared session/wallet contracts centralized.

---

### Requirement 11. Reduce minimum operational amount to 0.001
Your requirement:
- Remove effective restrictions that keep minimum around `0.01` / `0.3` due to lines and hardcoded defaults.

Advice:
- Possible, but this is a cross-cutting precision project.

Risk:
- Rounding mismatches across GS, wallet, clients, games, reports, and DB schemas.

Recommendation:
1. Run full precision audit:
   - decimal scales,
   - currency precision,
   - min/max bet formulas,
   - per-line and total bet calculations.
2. Remove hardcoded minimum assumptions in game logic and config validators.
3. Add per-currency precision policy and exhaustive tests for 0.001 paths.

---

## 3. Recommended implementation order (high level)
To reduce risk and keep momentum, this order is best:
1. Baseline capture + compatibility contract tests.
2. Repository split/governance (`DEV_new`, tagging, branch rules).
3. Config service + user-friendly modular admin UI.
4. Protocol adapter layer (JSON/XML by bank).
5. Multiplayer service extraction behind `isMultiplayer`.
6. Cassandra upgrade with rehearsal and controlled cutover.
7. Precision/min-bet modernization (`0.001`) with end-to-end testing.
8. Branding/package rename waves (`ABS`) with compatibility map.

Why this order:
- It protects existing behavior first, then introduces safer architecture foundations, then handles higher-risk transformations.

---

## 4. Non-negotiable safeguards
These should be mandatory in your project:
1. No production cutover without parity tests.
2. Every major migration must have rollback.
3. Financial operations must be idempotent and auditable.
4. All config changes must be versioned and reversible.
5. Canary rollout by banks before global rollout.

---

## 5. Proposed first step for next week
Practical first-week plan:
1. Lock baseline and create compatibility test matrix.
2. Finalize service boundaries and migration phases.
3. Build dependency/version inventory and Cassandra readiness checklist.
4. Define admin UI modules and publish workflow.
5. Start protocol adapter design (JSON/XML switching by bank).

Expected output at end of week:
- approved architecture scope,
- prioritized migration backlog,
- risk register,
- execution roadmap for implementation phase.

---

This document captures your requirements and recommended execution approach in a way that supports safe, staged modernization.

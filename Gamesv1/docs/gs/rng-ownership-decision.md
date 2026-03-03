# RNG Ownership Decision for New Slots on GS

- Status: Accepted (Phase 1)
- Date: 2026-02-28
- Related:
  - `docs/adr/0001-new-slots-target-architecture.md`
  - `docs/gs/math-package-spec.md`
  - `docs/gs/internal-slot-runtime-contract.md`

## 1) Context

Constraints:
- GS remains authoritative for session, wallet, DB persistence, idempotency/requestCounter, and unfinished-round restore.
- Multiplayer is out of scope.
- Launch-time math injection in production is forbidden.
- Runtime should support reusable slot backend capabilities across many future titles.

## 2) Options Evaluated

### Option A: RNG inside GS

Description:
- GS generates random values and passes them into slot-engine logic.

Pros:
- Maximum central control in GS.
- Straightforward authority narrative.

Cons:
- Adds game-logic coupling to GS core.
- Slows reusable engine evolution across titles.
- Expands GS blast radius for RNG algorithm updates.

### Option B: RNG inside internal Slot Engine Host

Description:
- Slot-engine host generates RNG server-side during deterministic round evaluation.
- Host is private/internal and callable only by GS.

Pros:
- Aligns with reusable common slot runtime model.
- Keeps RNG close to game-math execution path.
- Easier per-game algorithm governance via versioned math packages.
- Preserves GS authority because GS controls call timing, financial commits, and persisted outcomes.

Cons:
- Requires explicit audit surface across GS <-> engine boundary.
- Requires strict contracting and replay tooling.

### Option C: External RNG service

Description:
- Separate RNG service supplies entropy/outcome random streams.

Pros:
- Potentially useful for strict regulated remote-RNG jurisdictions.
- Independent certification surface.

Cons:
- Extra dependency and latency.
- Higher operational complexity and failure handling overhead.
- More complex certification/change-management coordination.

## 3) Decision

### Phase 1 Recommendation (Strong)

**Use Option B: RNG inside internal Slot Engine Host.**

Rationale:
- Best fit for reusable slot backend functionality across future games.
- Maintains GS authority boundaries where required (session/wallet/persistence/restore/routing).
- Avoids public exposure or browser involvement in randomness.
- Works naturally with immutable math-package release governance.

### Optional Future Path

**Option C can be introduced for selected jurisdictions only** if regulation requires externally certified RNG infrastructure. This is an opt-in market path, not default platform architecture.

## 4) Authority and Control Model

Even with RNG in engine host:
- GS remains authoritative for:
  - whether a round may execute,
  - request sequencing and idempotency,
  - wallet reserve/settle commit decisions,
  - math package/version and RTP model selection,
  - what is persisted as authoritative outcome/state.
- Engine host remains authoritative only for:
  - loading and executing only pre-registered, verified versions selected by GS,
  - deterministic game logic execution including RNG draws.

Engine host must reject unregistered or non-verified package/version/model execution requests.

## 5) Audit, Certification, and Change Management Impact

### 5.1 Auditability

For each round, persist or derivably link:
- `sessionId`
- `roundId`
- `requestCounter`
- `idempotencyKey` / `clientOperationId`
- `mathPackageVersion`
- `rtpModelId`
- `rngAlgorithmVersion`
- `rngTraceRef` (reference/hash, not raw secret seed in logs)

### 5.2 Certification

Certification artifacts should map to:
- math package version,
- RNG algorithm version,
- replay test corpus and pass evidence,
- release approval chain.

No certification-relevant RNG behavior may be changed outside release pipeline.

### 5.3 Change Management

Any RNG behavior change requires:
1. New versioned artifact (`mathPackageVersion` and/or `rngAlgorithmVersion`).
2. Regression and replay proof.
3. Controlled rollout and rollback plan.
4. Documented approval.

## 6) Security and Compliance Guardrails

- Browser-provided randomness is ignored.
- No launch parameter may alter RNG algorithm, seed policy, or model in production.
- RNG runtime must not access GS DB directly.
- Raw secret seed material must not be stored in plaintext operational logs.

## 7) Rejected Defaults

- Do not make external RNG a baseline requirement in Phase 1.
- Do not centralize RNG in GS core if it blocks reusable engine evolution.

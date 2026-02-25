# Phase 9 Tooling Reuse Map (for Runtime Naming Cleanup Subproject)

Date (UTC): 2026-02-25 18:27:26 UTC

## Plain-English Summary
Good news:
- the project already has strong Phase 9 rename tooling for safe, review-first cleanup waves.

Important limitation:
- this tooling does **not** yet automate runtime-safe renames for `com.dgphoenix` or `MQ*`.

That means we should reuse the tooling for governance, inventory, and approvals, then add only the missing runtime-safe pieces.

## What already exists (and should be reused)

### 1. Compatibility policy manifest (safe vs risky mappings)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json`

Key facts already encoded:
- `com.dgphoenix` -> review-only, wrapper-required, later wave (`W3`)
- `mq` -> review-only (context-sensitive / collision risk)

Why this is useful:
- The project already has an executable policy for “do not auto-rename these.”

### 2. Wave/blocklist safety registry
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-wave-status-blocklist.json`

Why this matters:
- It already records deferred/blocked runtime-sensitive files and mappings.

### 3. Validation and smoke checks for the Phase 9 toolchain
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-compatibility-map-validate.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-compatibility-map-smoke.sh`
- plus Phase 9 scan/patch-plan/apply smoke scripts

Why this matters:
- We can extend the workflow without breaking the current guardrails.

### 4. Discovery and planning scripts (already useful now)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-legacy-name-inventory.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-diff.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-execution-plan.sh`

What they give us:
- inventory
- candidate grouping
- review-only blocking
- execution planning artifacts

### 5. Patch-plan and guarded apply workflow (W0-proven)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-patch-plan-export.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-w0-approval-artifact-generate.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-wave-status-report.sh`

Why this matters:
- The review/approval/report pipeline is already tested and can be reused.

## What is missing (must be added for runtime cleanup)

### A. `com.dgphoenix` runtime wrapper/delegation support
Current gap:
- No automated wrapper-generation or wrapper-aware migration tooling for runtime class/package names.

Why this matters:
- Many class names are loaded by `Class.forName(...)` or XML mappings.
- Renaming without compatibility wrappers can break runtime startup and bank-config-driven flows.

### B. `MQ*` context-aware rename/alias logic
Current gap:
- Existing scans detect `mq` broadly, but there is no safe token-aware runtime migration tool for:
  - `MQ_*` config keys
  - template payload keys
  - class names beginning with `MQ`

Why this matters:
- `mq` is too broad and collision-prone for automatic replacement.
- `MQ*` includes real runtime keys and behaviors, not just branding text.

### C. Runtime-specific validation workflow for rename waves
Current gap:
- Phase 9 toolchain is strong on text/planning/apply governance, but runtime-sensitive rename waves need:
  - compatibility validation matrix
  - launch/wallet/startup checks tied to each rename batch
  - reflection/class-load failure checks

## Safe Reuse Strategy (recommended)
1. Keep existing Phase 9 manifests and review-only guards as the source of truth.
2. Build a narrow runtime cleanup workflow on top of them (not a separate unrelated toolchain).
3. Treat runtime rename changes as new waves (`RN*`) with explicit evidence and rollback.

## Practical Next Deliverable
Use the current inventory + Phase 9 manifests to produce a **rename-ready shortlist**:
- wrapper-required `com.dgphoenix` references
- `MQ*` keys that need alias strategy
- text-only references that can be delayed

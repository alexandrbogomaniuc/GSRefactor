# Controlled-Wave Execution Plan (Runtime Naming Cleanup)

## Plain-English Goal
Finish the deferred rename cleanup safely, without breaking the running system.

## Important Rule
Do not do a global search/replace for:
- `com.dgphoenix`
- `mq` / `MQ*`

These names are used in runtime config, class loading, and templates.

## Reuse Existing Phase 9 Tooling (do not rebuild from scratch)
Already available and should be reused:
- compatibility map manifest
- review-only guards
- candidate scan + diff + execution plan tools
- patch-plan export
- approval artifacts
- wave status reporting

## What existing tooling can do today
It can:
- inventory and scan legacy names
- block unsafe auto-apply when `reviewOnly` mappings are found
- produce review artifacts and patch plans

It cannot yet:
- safely automate runtime `com.dgphoenix` package/class migration (wrapper-aware)
- safely automate `MQ*` runtime key/token migration with context rules

## Subproject Waves (new follow-on waves)

### RN0: Inventory Freeze and Scope Lock (current step)
Status: In progress (started)

Outputs:
- runtime-sensitive inventory baseline
- tooling capability map (what can be reused, what is missing)
- this execution plan

Exit criteria:
- approved scope and risk rules

### RN1: Rename-Ready Shortlist (analysis only)
Goal:
- Create a small, high-confidence list of runtime-sensitive references grouped by migration strategy.

Required groups:
1. `Wrapper-required package/class references` (`com.dgphoenix`)
2. `Protocol/config key references` (`MQ_*`)
3. `Template payload keys / JS-facing names`
4. `Framework XML class mappings`
5. `Text-only low-risk references`

Exit criteria:
- each item assigned a migration strategy
- no “unknown-risk” items in the shortlist

### RN2: Compatibility Layer Design (no broad rename yet)
Goal:
- Design safe compatibility approach before touching runtime names.

Design outputs:
- package/class wrapper strategy for `com.dgphoenix.*`
- alias strategy for `MQ*` config keys (dual-read / dual-write if needed)
- validation matrix for each strategy

Examples of expected patterns:
- old config value still accepted, new value supported
- wrappers/delegates for old FQCNs during transition
- parser/reader support for both old and new keys where needed

Exit criteria:
- wrapper/alias design approved
- validation plan defined per component

### RN3: Controlled Implementation Wave A (lowest runtime risk)
Goal:
- Implement and validate the smallest runtime-safe compatibility changes first.

Likely candidates:
- readers/parsers that can support both names
- non-breaking aliases
- guarded config interpretation updates

Do not include:
- mass renames of packages/classes
- widespread template key renames

Exit criteria:
- no regression in launch/wallet/core runtime checks
- evidence docs produced

### RN4: Controlled Implementation Wave B (wrapper-based class/package migration)
Goal:
- Introduce compatibility wrappers/delegates for high-impact `com.dgphoenix` runtime references.

Likely areas:
- wallet manager class names from bank config
- processor classes loaded with `Class.forName(...)`
- XML-configured class mappings where wrappers are viable

Exit criteria:
- old names continue to work during migration
- new names can be introduced safely in staged config updates

### RN5: Template/Protocol Key Migration (`MQ*`) with Compatibility Proof
Goal:
- Migrate or alias selected `MQ*` runtime keys/tokens only where behavior is proven safe.

Important:
- Some `MQ*` tokens may need to remain for backward compatibility longer than expected.
- “rename all MQ words” is not a safe requirement by itself.

Exit criteria:
- each changed key has compatibility proof
- support UI / templates / runtime consumers still work

### RN6: Cleanup and Decommission Planning
Goal:
- Document which legacy names can be removed and which must remain for compatibility.

Outputs:
- final retained-legacy list (if any)
- deprecation timeline
- rollback notes

## Validation Requirements (apply to every implementation wave)
- Launch alias smoke (`/startgame`) still passes
- Wallet auth/balance/wager/refund path still passes
- Mixed topology compatibility (if affected)
- Support pages that edit bank properties still function
- No startup failures caused by missing class names / reflection errors

## Proposed First Practical Next Step (recommended)
Create the `RN1 rename-ready shortlist` from the baseline inventory and existing Phase 9 manifests.

This is the correct next step because it:
- narrows the work,
- reduces risk,
- and makes later coding changes reviewable and testable.

## What we are NOT doing in this plan
- No blind mass rename
- No breaking runtime configs to “clean names” quickly
- No removing compatibility until runtime evidence says it is safe

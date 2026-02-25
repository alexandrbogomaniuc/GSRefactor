# Phase 9 Runtime Naming Cleanup Subproject

## Purpose
This subproject covers the remaining runtime-sensitive naming cleanup that was intentionally deferred when Phase 9 closed as a tooling/governance phase.

Examples of deferred runtime-sensitive names:
- `com.dgphoenix.*`
- `MQ*` class names and property keys (for example `MQBCloseGameProcessor`, `MQ_FRB_DEF_CHIPS`)

## Why this is a separate subproject
These names appear in runtime class loading, persisted bank/server config, framework XML mappings, and templates.
Blind text replacement can break startup, wallet flow, launch flow, or multiplayer behavior.

## Files in this subproject
- `00-subproject-charter-and-scope.md`
- `01-runtime-sensitive-inventory-baseline-20260225-182726.md`
- `02-controlled-wave-execution-plan.md`
- `03-phase9-tooling-reuse-map-20260225-182726.md`
- `04-rn1-rename-ready-shortlist-v1.md`
- `05-runtime-class-string-inventory.md`
- `06-runtime-config-template-script-inventory.md`
- `07-safe-rename-execution-plan-with-compatibility-mapping.md`
- `evidence/` (raw grep inventories for code/config/script scans)

## Current status (2026-02-25)
- Inventory baseline: complete
- Existing Phase 9 tooling reuse map: complete
- Controlled execution plan: drafted
- RN1 rename-ready shortlist: complete (v1)
- RN2 Wave A (BankInfo MQ key aliases): complete (`feed2f3f`)
- RN2 Wave B (runtime class-loading package fallback): complete (`1045b5ec`)
- RN3 Wave A (GS reflection compatibility expansion): complete (`62df498e`)
- RN3 Wave B (MP reflection compatibility expansion): complete (`d5776764`)
- RN4 Wave A (class-string config key aliases in `BankInfo`): complete (`ad156b6c`)
- RN5 Wave A/B (GS->MP `MQ_*` dual fields + MP fallback readers + template dual fields): in progress

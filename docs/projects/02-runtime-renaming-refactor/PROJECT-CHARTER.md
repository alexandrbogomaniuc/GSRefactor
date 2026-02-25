# Project 02 Charter: Runtime Renaming Refactor Completion

Last updated: 2026-02-25 UTC
Project code: `RENAME-FINAL`

## Objective
Complete refactoring and renaming of legacy class/package/config naming (including `com.dgphoenix.*` and `MQ*` runtime surfaces) with safe compatibility-first execution and full regression evidence.

## Why this project exists
Legacy names still exist in runtime-sensitive paths (reflection, XML mappings, config values, payload keys, templates). Blind replacement can break launch, wallet, and multiplayer flows. This project finishes the rename safely.

## Scope in
1. Complete inventory of remaining legacy naming in code, configs, templates, scripts.
2. Controlled migration waves with compatibility mapping.
3. Update runtime key contracts (`MQ_*` to target naming) with dual-read/dual-write transition where needed.
4. Update persisted and template config references safely.
5. Remove legacy fallback only after proof.

## Scope out
- New game features.
- Unrelated module redesign.
- Product/UX changes outside naming and compatibility.

## Baseline facts (as of 2026-02-25)
- Existing subproject materials exist in `docs/phase9/runtime-naming-cleanup`.
- Several compatibility waves are already complete, but decommission/closure is unfinished.

## Success criteria
1. All runtime-critical rename surfaces migrated with no functional regression.
2. Compatibility mapping is complete and traceable.
3. End-to-end launch/wallet/multiplayer tests pass after migration waves.
4. Legacy compatibility paths are removed only where proven safe.
5. Clear closure report lists what remains legacy (if anything) with reasons.

## Exit status options
- `SIGN_OFF_READY`: all criteria passed.
- `NO_GO`: runtime regressions or unresolved high-risk rename items.
- `PARTIAL`: migration advanced but cleanup/decommission incomplete.

## Linked plan files
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/WORK-BREAKDOWN-AND-SCHEDULE.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/TEST-STRATEGY.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/DOCUMENTATION-AND-EVIDENCE-CHECKLIST.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/RISKS-ROLLBACK-SIGNOFF.md`

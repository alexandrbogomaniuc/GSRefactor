# Hard-Cut M2 Wave 150A/150B + Wave 151 Report

Date (UTC): 2026-02-27
Wave group: 150A + 150B + 151
Scope: batched-safe parallel migration with bounded integration stabilization.

## Batch Breakdown
- `W150A`: 11 declaration migrations in `sb-utils/common/util/support` + `utils/common/util/system`.
- `W150B`: 10 declaration migrations in `common/client/canex/request/privateroom` + `common-promo/messages/server/notifications/tournament`.
- `W151`: bounded importer rewires across `common`, `common-wallet`, `common-gs`, `web-gs`, `cassandra-cache`, and `sb-utils`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-050718-hardcut-m2-wave150ab-wave151-parallel-batches/`
- Fast gate:
  - `rerun1` failed at step1 (`common`) due stale `IJsonCWClient` import (`GetFriendsResponse` old package).
  - `rerun2` failed at step1 (`common`) due dependency order (`common` compiled before migrated `sb-utils` support declarations).
  - `rerun3` failed at step4 (`common-wallet`) due type-identity mismatch in `CanexCWClient` signatures/imports.
  - `rerun4` PASS `9/9` after bounded import/signature alignment and corrected dependency order.
- Full matrix:
  - `rerun1` PASS `9/9` with wave-specific pre-setup installs (`utils`, `sb-utils`, `common-promo`) before canonical 9 steps.

## Stabilization Notes
- Bounded compatibility fixes retained:
  - `IJsonCWClient`: aligned `friends/onlineplayer/onlinerooms` imports to `com.abs` package lineage.
  - `CanexCWClient`: aligned `friends/onlineplayer/onlinerooms` imports and method return types to `com.abs` lineage to satisfy `IJsonCWClient` contract.
- Dependency-order alignment retained:
  - fast gate now compiles `sb-utils` before `common` for this wave due moved `common.util.support` declarations.
- Runtime-safety guardrail maintained:
  - no global replace; only package-scoped declaration migration and bounded rewires.

## Outcome Metrics
- Scoped declaration migrations retained: `21`.
- Scoped bounded rewires retained: `26`.
- Global tracked declarations/files remaining: `1480` (baseline `2277`, reduced `797`).
- Hard-cut burndown completion: `35.002196%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `29.375274%`
  - Core total (01+02): `64.687637%`
  - Entire portfolio: `82.343819%`
- ETA refresh: ~`60.9h` (~`7.62` workdays).

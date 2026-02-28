# Hard-Cut M2 Wave 246A/246B + Wave 247 Report

Date (UTC): 2026-02-28
Wave group: 246A + 246B + 247
Scope: declaration-first migration of common-gs Kafka bot-config/private-room DTOs with bounded import rewires.

## Batch Breakdown
- `W246A`: `6` declaration migrations retained (`EnableBotServiceRequest`, `GetAllBotConfigInfosRequest`, `GetBotConfigInfo*`, `IsBotServiceEnabledRequest`).
- `W246B`: `6` declaration migrations retained (`RemoveBotConfigInfoRequest`, `UpsertBotConfigInfoRequest`, `BotConfigInfosResponse`, `TournamentEndedDto`, `GetPrivateRoomInfoRequest`, `UpdatePrivateRoomRequest`).
- `W247`: integration validation.

## Stabilization
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - direct FQCN rewires for moved DTO imports/usages.
  - wildcard consumer alignment in `KafkaRequestMultiPlayer` and `BattlegroundService` via explicit `com.abs` imports for migrated classes.
- Pre-existing unrelated local files preserved outside commit scope: `cluster-hosts.properties`, `.tmp-w202-batchA.txt`, `.tmp-w202-batchB.txt`, prior uncommitted evidence folders.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-050920-hardcut-m2-wave246ab-wave247-kafka-dto-botconfig/`
- Fast gate batchA:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from prior checkpoint:
  - `com.dgphoenix -> com.abs`: `12`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+12`
- Global tracked declarations/files remaining: `657` (baseline `2277`, reduced `1620`).
- Hard-cut burndown completion: `71.146245%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `44.957411%`
  - Core total (01+02): `72.478706%`
  - Entire portfolio: `86.239353%`
- ETA refresh: ~`27.3h` (~`3.41` workdays).

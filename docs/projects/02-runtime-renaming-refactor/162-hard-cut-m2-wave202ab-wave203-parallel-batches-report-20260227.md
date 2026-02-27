# Hard-Cut M2 Wave 202A/202B + Wave 203 Report

Date (UTC): 2026-02-27
Wave group: 202A + 202B + 203
Scope: declaration-first migration in `common.promo.messages.server.notifications.prizes`, `common.web.diagnostic`, `configuration.resource.event`, `gs.managers.game.core/history`, `gs.status`, `system.configuration.identification`, and bounded test/support surfaces.

## Batch Breakdown
- `W202A`: 10 declaration migrations in promo prize-notification classes, common diagnostic classes, and resource-event interfaces.
- `W202B`: 11 declaration migrations in game-core/history/status/identification classes and RNG test helpers.
- `W203`: integration rewires and validation.

## Stabilization
- Primary mode executed as `1 explorer + 2 workers + main` with strict non-overlapping ownership.
- Zero-overlap selection validated up-front (`decl overlap=0`, `rewire overlap=0`, cross-overlap=0).
- Bounded rewires retained only in direct importer/FQCN call-sites plus one bounded JSP import fix:
  - `vabs/html5template.jspf` import updated to `com.abs.casino.gs.managers.game.history.HistoryManager`.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) out of migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-150630-hardcut-m2-wave202ab-wave203-parallel-batches/`
- Fast gate:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, recovery retry executed once)
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, recovery retry executed once)

## Outcome Metrics
- Scoped declaration migrations retained: `21`.
- Scoped bounded rewires retained: `33` (`rewires-batchA-all.txt`, `rewires-batchB-all.txt`, plus bounded JSP import integration fix).
- Global tracked declarations/files remaining: `1103` (baseline `2277`, reduced `1174`).
- Hard-cut burndown completion: `51.559069%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `32.580236%`
  - Core total (01+02): `66.290118%`
  - Entire portfolio: `83.145059%`
- ETA refresh: ~`45.6h` (~`5.70` workdays).

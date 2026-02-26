# Hard-Cut M0 Baseline Lock Report

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M0 - Freeze and baseline lock`
Status: `COMPLETE`

## Purpose
Lock a factual baseline before any hard-cut rename wave so we can:
- prove what changed,
- rollback safely,
- avoid logic regressions.

## Baseline evidence folder
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline`

## Baseline metrics (from evidence)
- `gs_package_com_dgphoenix_count`: 2060
- `gs_package_com_abs_count`: 1
- `mp_package_com_dgphoenix_count`: 217
- `mp_package_com_abs_count`: 0
- `gs_pom_groupid_com_dgphoenix_count`: 57
- `gs_pom_groupid_com_abs_count`: 0
- `mp_pom_groupid_com_dgphoenix_count`: 40
- `mp_pom_groupid_com_abs_count`: 0
- `runtime_classstring_inventory_count`: 1583
- `mp_runtime_token_inventory_count`: 1532
- `gs_log_com_dgphoenix_hits` (latest tail): 100
- `mp_log_com_dgphoenix_hits` (latest tail): 0

## What this means (plain English)
1. The codebase is still mostly in the old namespace (`com.dgphoenix`).
2. Build coordinates are still mostly old namespace too.
3. Runtime configuration and support pages still contain many hardcoded legacy class strings.
4. GS log output confirms old namespace classes are active in runtime.

## Highest-risk rename hotspots (from scan)
Top runtime class-string hotspots:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/SCClusterConfig.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfig.xml`

These are high risk because class-string mistakes here can break startup, routing, wallet, or multiplayer flows.

## Guardrails for next waves
1. No blind global replace.
2. Wave size limit:
   - high-risk runtime/config: max 3 files per wave,
   - mechanical package refactor: max 1 module per wave.
3. Every wave must have:
   - pre-change scan,
   - post-change scan,
   - build + smoke evidence,
   - rollback point.

## M1 immediate execution order
1. Prepare build-coordinate transition for GS parent/module poms.
2. Validate dependency graph with no runtime code change yet.
3. Commit M1 prep as isolated wave before any package declaration rename.

## Evidence index
- counts: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/baseline-counts.txt`
- GS package hits: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/gs-package-com-dgphoenix.txt`
- MP package hits: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/mp-package-com-dgphoenix.txt`
- GS pom groupIds: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/gs-pom-groupid-com-dgphoenix.txt`
- MP pom groupIds: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/mp-pom-groupid-com-dgphoenix.txt`
- runtime class strings: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/runtime-classstring-inventory.txt`
- MP runtime tokens: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/mp-runtime-token-inventory.txt`
- GS log snapshot: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/refactor-gs-1-log-tail.txt`
- GS selected log lines: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/refactor-gs-1-log-selected.txt`
- MP log snapshot: `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/refactor-mp-1-log-tail.txt`


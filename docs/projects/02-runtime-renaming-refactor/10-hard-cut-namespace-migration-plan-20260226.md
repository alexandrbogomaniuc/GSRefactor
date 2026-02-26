# Project 02 Replan: Hard-Cut Namespace Migration

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Status: `REPLANNED_FOR_HARD_CUT`

## Why this replan exists
The previous Project 02 closure completed a compatibility-first backlog.
It did **not** complete a hard-cut package migration.

Current fact:
- GS still compiles and runs mostly under `com.dgphoenix.*`.
- Restarting GS will continue to show `com.dgphoenix` in stack traces until package/class/build coordinates are actually migrated.

## Hard-cut target (definition of done)
This replan is complete only when all items below are true:

1. Runtime code packages are migrated from `com.dgphoenix.*` to `com.abs.*`.
2. Maven coordinates are migrated from `com.dgphoenix.casino*` to `com.abs.casino*` where applicable.
3. Runtime class-string configs/XML/JSP bindings are migrated and validated.
4. Compatibility fallback code is removed (or reduced to approved short-term bridge only).
5. New GS runtime logs (after clean restart) do not print `com.dgphoenix` from active code paths.
6. End-to-end launch + wallet + multiplayer tests pass.

## Scope
In scope:
- Java package migration in GS/MP related modules in `Dev_new`.
- Build metadata migration (groupId/artifact references where required).
- Runtime class string migration (config/XML/JSP/support pages).
- Regression test matrix and production-readiness evidence.

Out of scope:
- New game feature work.
- Product UX changes not required for migration.
- Legacy stack startup.

## Execution model
- Branch model: one guarded mini-wave per risk slice.
- Wave size: maximum 3 files for high-risk runtime slices, up to 20 files for mechanical package refactors in one module.
- After each wave: build + targeted smoke + rollback checkpoint.
- No blind global replace.

## Milestones (hard-cut)

## M0 - Freeze and baseline lock
Purpose:
- Freeze feature work and lock a reproducible baseline.

Outputs:
- Baseline scan report:
  - package counts
  - groupId counts
  - runtime class-string inventory
- Baseline runtime evidence from fresh restart and log snapshot.

Exit gate:
- Baseline committed and tagged.

## M1 - Build-coordinate migration plan and parent POM prep
Purpose:
- Make build system ready for package migration without dependency breakage.

Work:
- Prepare parent POM coordinate transition strategy.
- Add temporary relocation/bridge strategy if needed for inter-module compatibility.
- Validate dependency graph resolution.

Exit gate:
- `mvn -q -DskipTests install` succeeds for parent + core dependency chain.

## M2 - Core utility and common modules package migration
Purpose:
- Migrate foundational packages first.

Target examples:
- `sb-utils`
- `common`
- `utils`
- shared annotations/helpers used by many modules

Rules:
- Refactor package declarations and imports module-by-module.
- Keep compilation green at every wave.

Exit gate:
- Unit tests for migrated modules pass.
- No `com.dgphoenix` package declarations remain in migrated modules.

## M3 - Cassandra + wallet + promo module migration
Purpose:
- Migrate runtime data and payment critical paths.

Target examples:
- `common-wallet`
- `cassandra-cache/*`
- `promo/*`

Rules:
- Keep API behavior identical.
- Re-run wallet and persistence tests after each wave.

Exit gate:
- Package/build success for these modules.
- Existing wallet/auth flows still pass smoke tests.

## M4 - Web GS and support/runtime binding migration
Purpose:
- Migrate action classes, servlet mappings, JSP bindings, and support pages.

Work:
- Update Java package references in `web-gs` code.
- Update XML mappings (Struts/Spring/cluster configs).
- Replace hardcoded class strings in JSP/support configs.

Exit gate:
- `web-gs` package build succeeds.
- Support pages and `/startgame` path return expected status.

## M5 - MP-side compatibility alignment
Purpose:
- Ensure GS-to-MP class contracts and runtime keys still work post package migration.

Work:
- Update MP package references where tied to migrated classes.
- Validate launch, session, room, and close-game flows.

Exit gate:
- `mp-server` targeted package build succeeds.
- GS/MP integration smokes pass.

## M6 - Remove legacy fallback bridge
Purpose:
- Complete hard-cut by removing compatibility crutches.

Work:
- Remove `com.abs` <-> `com.dgphoenix` fallback loading in runtime loaders after migration proves stable.
- Remove deprecated alias keys only after two green full runs.

Exit gate:
- No runtime dependency on legacy package aliasing.
- Regression remains green.

## M7 - Full regression, cutover evidence, and sign-off pack
Purpose:
- Provide production-ready evidence.

Required checks:
1. Build/test matrix (existing standard matrix).
2. Runtime smoke matrix:
   - singleplayer launch (bank template)
   - multiplayer launch (bank template)
   - wallet start/bet/settle/close path
3. Log proof from fresh restart:
   - no new `com.dgphoenix` entries in active runtime code paths
4. Static scan proof:
   - zero `^package com.dgphoenix` in active source modules
   - zero `com.dgphoenix.casino` groupId where migration target applies

Exit gate:
- Sign-off report generated with clear PASS/FAIL per criterion.

## Test matrix (minimum)
Build/tests:
- `gs-server/sb-utils`: `mvn test`
- `gs-server/promo/persisters`: `mvn -DskipTests install`
- `gs-server/cassandra-cache/common-persisters`: `mvn -DskipTests install`
- `gs-server/cassandra-cache/cache`: `mvn test`
- `gs-server/game-server/web-gs`: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package`
- `mp-server`: `mvn -pl core-interfaces,core,persistance -am -DskipTests package`

Runtime/smoke:
- onboarding smoke via `refactor-onboard.mjs smoke`
- `/startgame` for multiplayer bank and singleplayer bank
- GS/MP log correlation for session create + close

Static hard-cut checks:
- `rg '^package com\\.dgphoenix' gs-server --glob '!**/target/**'`
- `rg 'com\\.dgphoenix\\.casino' gs-server --glob '**/pom.xml'`
- `rg 'com\\.dgphoenix' gs-server/game-server/web-gs/src/main/webapp/support`

## Rollback policy
- Tag before each milestone (`hardcut-mX-pre`).
- Keep wave commits atomic and reversible.
- If a wave breaks runtime smoke, rollback that wave only and re-scope.

## Documentation and evidence outputs
- Activity log updates:
  - `docs/projects/02-runtime-renaming-refactor/ACTIVITY-LOG.md`
- Milestone evidence folders:
  - `docs/projects/02-runtime-renaming-refactor/evidence/<timestamp>/`
- Final sign-off report:
  - `docs/projects/02-runtime-renaming-refactor/11-hard-cut-signoff-report.md`

## Realistic completion criteria for this replan
Project 02 is considered truly complete only after M7 passes with:
- zero active runtime package usage of `com.dgphoenix`,
- green launch/wallet/mp flows,
- clean sign-off evidence.

# Activity Log

Project: RENAME-FINAL (runtime class/package/config naming refactor)

## 2026-02-25 20:16 UTC
- Created per-project activity log as requested.
- Baseline planning package already present in this folder (`PROJECT-CHARTER.md`, `WORK-BREAKDOWN-AND-SCHEDULE.md`, `TEST-STRATEGY.md`, `DOCUMENTATION-AND-EVIDENCE-CHECKLIST.md`, `RISKS-ROLLBACK-SIGNOFF.md`).
- Status: planning ready, execution waves pending.

## 2026-02-25 20:24-20:26 UTC
- Completed RN5 compatibility wave implementation in code and templates.
- GS->MP payload now writes both `MQ_WEAPONS_MODE` and `ABS_WEAPONS_MODE`.
- Multiplayer launch templates now emit dual `MQ_*` + `ABS_*` keys for help/timer/weapons-saving/autofiring/rooms-sort/client-log settings.
- Support template property editor now includes `ABS_STAKES_RESERVE`, `ABS_STAKES_LIMIT`, and `ABS_AWARD_PLAYER_START_BONUS` options.
- Validation executed and saved under `docs/projects/02-runtime-renaming-refactor/evidence/20260225-202452/`:
  - `BankInfoAliasCompatibilityTest` PASS (15/15)
  - `ReflectionUtilsCompatibilityTest` PASS (3/3)
  - `common-gs` build PASS
  - `web-gs` build PASS
  - `/startgame` runtime smoke returned HTTP 200
- Updated phase9 runtime naming subproject status (`RN5 ... complete`).

## 2026-02-26 06:31 UTC
- Executed RENAME-FINAL Phase 0 refresh and attempted Phase 1 W0 low-risk apply using guarded phase9 tooling.
- Evidence bundle created:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-063100/`
- Key tooling outputs:
  - runtime naming inventory (`20260226-062924`, `20260226-063101`)
  - candidate scans (`20260226-062933`, `20260226-063101`, `20260226-063246`)
  - W0 patch plan (`20260226-062933`)
  - W0 dry-run/apply reports (`20260226-063004`, `20260226-063020`)
- Safety gate result:
  - diff audit found runtime-sensitive startup replacement in docker compose (`com.betsoft...NettyServer` -> `com.abs...NettyServer`) which is unsafe for current MP package layout.
  - all W0 applied runtime/config file edits were rolled back before commit.
- Guardrail update implemented:
  - `gs-server/deploy/config/phase9-abs-compatibility-map.json`
  - W0 automatic apply disabled (`allowsAutomaticApply=false`)
  - brand-token mappings moved to `reviewOnly=true`.
- Post-guardrail validation:
  - map validate PASS (`reviewOnly=9`)
  - candidate scan now reports `Auto-candidate mappings: 0` and review-only hits only.
- Build matrix verification PASS:
  - promo/common install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package
- Outcome:
  - Phase 0 refreshed and documented.
  - Phase 1 now hard-gated to manual curated waves to prevent unsafe runtime replacements.

## 2026-02-26 06:36 UTC
- Produced manual curated execution backlog after enabling safety guardrails.
- New planning artifact:
  - `docs/projects/02-runtime-renaming-refactor/08-manual-curated-wave-backlog-20260226.md`
- Backlog defines M1-M4 waves with explicit file targets, method, and exit checks.
- Execution policy locked:
  - max `3` files per mini-wave,
  - full validation matrix after each wave,
  - dual-key/dual-read compatibility until post-cutover removal stage.
- Project 02 completion estimate updated to `35%` (Phase 0 complete, auto-path guarded, manual waves pending).

## 2026-02-26 06:38 UTC (Mini-Wave M1.1)
- Implemented first manual mini-wave from M1 backlog in support configuration reflection path.
- Changed file:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/configuration/ServerConfigurationAction.java`
- Change detail:
  - replaced direct `Class.forName(className)` resolution with `ReflectionUtils.forNameWithCompatibilityAliases(className)` and reused resolved class object in reflection flow.
- Validation:
  - `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `web-gs`: PASS
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-063800/`
- Outcome:
  - support config actions now participate in package-rename compatibility bridge (`com.abs.*`/`com.dgphoenix.*`).

## 2026-02-26 06:41 UTC (Mini-Wave M1.2)
- Executed manual mini-wave M1.2 on dynamic class-loader hotspots in `sb-utils`.
- Changed files:
  - `sb-utils/src/main/java/com/dgphoenix/casino/common/util/xml/parser/XmlHandlerRegistry.java`
  - `sb-utils/src/main/java/com/dgphoenix/casino/common/util/xml/parser/XmlHandler.java`
  - `sb-utils/src/main/java/com/dgphoenix/casino/common/util/test/api/ClientFactory.java`
- Change detail:
  - replaced direct `Class.forName(...)` calls with `ReflectionUtils.forNameWithCompatibilityAliases(...)`.
- Validation PASS:
  - `sb-utils` tests (`57` tests, 0 failures)
  - promo/common install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-064100/`
- Outcome:
  - runtime string-based instantiation coverage increased for staged package rename compatibility.
- Project 02 completion estimate: `45%`.

## 2026-02-26 06:42 UTC (Mini-Wave M1.3)
- Implemented the next manual runtime-safe rename mini-wave in support configuration form validation.
- Changed file:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/configuration/ServerConfigurationForm.java`
- Change detail:
  - replaced `Class.forName(GameServerConfigTemplate.class.getName())...` with direct class literal access `GameServerConfigTemplate.class...`.
- Validation PASS:
  - `sb-utils` tests (`57` tests)
  - promo/common install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-064238/`
- Outcome:
  - removed one additional reflection hotspot from rename-sensitive runtime path.
  - Project 02 completion estimate updated to `50%`.

## 2026-02-26 06:48 UTC (Mini-Wave M2.1)
- Executed bank-template sanitization wave to remove third-party integration URLs from active refactor profiles.
- Changed files:
  - `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
  - `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- Change detail:
  - replaced `wallet.mqbase.com` endpoints with local stub/noop endpoints (`http://gs:8080/config/stub/...`, `http://gs:8080/empty.jsp`),
  - replaced `FR_BONUS_WIN_URL` external endpoint with local stub endpoint,
  - switched external allow-list/origin/fatal-page values to localhost values,
  - set `MP_LOBBY_WS_URL` to local mapped MP endpoint `127.0.0.1:16300` for local stack usage.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime audit script: `bank-template-audit.mjs` for banks `6275,6276` in multiplayer mode -> PASS (third-party URLs=0, allow-list violations=0).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-064800/`
- Outcome:
  - active local/refactor bank templates no longer depend on external third-party wallet/social endpoints.
  - Project 02 completion estimate updated to `60%`.

## 2026-02-26 06:51 UTC (Mini-Wave M2.2)
- Executed alias-key seeding wave on bank templates for runtime-safe staged rename.
- Changed files:
  - `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
  - `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- Change detail:
  - added `ABS_WPM_CLASS` alongside `WPM_CLASS`,
  - added `ABS_CLOSE_GAME_PROCESSOR` alongside `CLOSE_GAME_PROCESSOR`,
  - added `ABS_START_GAME_PROCESSOR` alongside `START_GAME_PROCESSOR` in mqb profile blocks.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime audit script (`bank-template-audit.mjs`) for banks `6275,6276` in multiplayer mode: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-065116/`
- Outcome:
  - key runtime class-string properties are now dual-keyed (`legacy + ABS`) in active bank templates.
  - Project 02 completion estimate updated to `65%`.

## 2026-02-26 07:01 UTC (Mini-Wave M2.3)
- Executed MQ key alias seeding wave for weapons mode in active bank templates.
- Changed files:
  - `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
  - `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- Change detail:
  - added `ABS_WEAPONS_MODE=LOOT_BOX` next to existing `MQ_WEAPONS_MODE=LOOT_BOX` entries.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-070144/`
- Outcome:
  - dual-key compatibility for weapons mode is now seeded in local/refactor bank templates.
  - Project 02 completion estimate updated to `68%`.

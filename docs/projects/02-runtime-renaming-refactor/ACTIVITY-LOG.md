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

## 2026-02-26 07:05 UTC (Mini-Wave M2.4)
- Executed mqb server-config sanitization wave to remove remaining external `mqbase` domains from active server template config.
- Changed file:
  - `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml`
- Change detail:
  - switched host/domain/gsDomain values from `mqbase` to localhost variants,
  - switched `MQ_CLUSTERS_CONFIG` + `MP_LOBBY_WS_HOST` references to local MP endpoint `127.0.0.1:16300`,
  - replaced support sender email domain token with local value.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
  - direct domain scan of mqb server config: no remaining `mqbase` host/domain tokens.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-070527/`
- Outcome:
  - active mqb server config template now aligns with local/internal endpoint strategy.
  - Project 02 completion estimate updated to `72%`.

## 2026-02-26 07:11 UTC (Mini-Wave M3.1)
- Executed support JSP class-string compatibility wave for staged package rename.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/webapp/support/initGames.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/setIdGeneratorStartValue.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/bankReleaseReport.jsp`
- Change detail:
  - `initGames.jsp`: game controller prefix classifier now accepts both `com.dgphoenix.casino.singlegames.*` and `com.abs.casino.singlegames.*`.
  - `setIdGeneratorStartValue.jsp`: sequencer lookup now checks `com.abs...DBWalletOperation` first and falls back to `com.dgphoenix...DBWalletOperation`.
  - `bankReleaseReport.jsp`: default wallet client recognition now treats both legacy and ABS class names as standard (no false custom-integration warning).
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-071122/`
- Outcome:
  - support workflows now tolerate both legacy and target package naming in runtime-sensitive checks.
  - Project 02 completion estimate updated to `76%`.

## 2026-02-26 07:14 UTC (Mini-Wave M3.2)
- Executed support template-flow class-string compatibility wave for SP game processor resolution.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/webapp/support/templateManager/cloneTemplate.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/games/829_step1_AddGameInfoTemplate.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/games/829_step2_AddGameInfo.jsp`
- Change detail:
  - replaced single hardcoded SP processor class dependency with runtime-compatible resolution:
    - prefer `com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor`
    - fallback `com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor`
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-071412/`
- Outcome:
  - support template-generation flows are now package-rename tolerant for SP processor class references.
  - Project 02 completion estimate updated to `80%`.

## 2026-02-26 07:16 UTC (Mini-Wave M3.3)
- Executed GameBankConfig class-default compatibility wave.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/webapp/support/gameBankConfig/GameClass.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/gameBankConfig/editGameForm.jsp`
- Change detail:
  - added runtime fallback resolution for class defaults in support game configuration flows:
    - SP processor class: `com.abs...` preferred, `com.dgphoenix...` fallback
    - single-game servlet default class: `com.abs...` preferred, `com.dgphoenix...` fallback
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-071656/`
- Outcome:
  - support GameBankConfig defaults now tolerate staged package renaming without manual updates.
  - Project 02 completion estimate updated to `84%`.

## 2026-02-26 07:19 UTC (Mini-Wave M2.5)
- Executed mpstress config compatibility/sanitization wave.
- Changed files:
  - `gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
  - `gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml`
- Change detail:
  - added dual alias keys in both mpstress bank entries:
    - `ABS_CLOSE_GAME_PROCESSOR`
    - `ABS_WPM_CLASS`
    - `ABS_WEAPONS_MODE`
  - replaced active `FR_BONUS_WIN_URL` external endpoint values with local stress stub endpoint.
  - replaced remaining `fromSupportEmail` values using `report-gp3.maxquest.com` with `support@localhost`.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
  - static scan artifacts captured for remaining `maxquest` tokens and alias presence.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-071948/`
- Outcome:
  - mpstress runtime config now follows the same dual-key compatibility pattern and has no active `FR_BONUS_WIN_URL` external dependency.
  - remaining `maxquest` tokens are in descriptive/commented content and can be addressed in a documentation cleanup wave.
  - Project 02 completion estimate updated to `88%`.

## 2026-02-26 07:24 UTC (Mini-Wave M3.4)
- Executed support `jsp:useBean` class-decoupling wave on bank property pages.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/properties/edit/editProperties.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/common/addBank.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/common/subCasinoInfo.jsp`
- Change detail:
  - removed hardcoded `jsp:useBean class="com.dgphoenix..."` for request-scoped forms where the bean is already provided by action flow.
  - replaced `SubcasinoForm` class-coupled access with request attribute fallback (`getId()` via reflection) and a missing-id guard.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-072419/`
- Outcome:
  - reduced hard dependency on legacy package name in support form wiring paths.
  - Project 02 completion estimate updated to `92%`.

## 2026-02-26 07:27 UTC (Mini-Wave M3.5)
- Executed support language-table `jsp:useBean` decoupling wave.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/properties/languageTable.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/properties/edit/languageTable.jsp`
- Change detail:
  - removed hardcoded `class="com.dgphoenix..."` `gameBean` declaration from both pages.
  - replaced `gameBean` value extraction with request/context-driven values (`bankId` resolve fallback + direct iterate `game` usage).
  - added explicit `bankId` missing guard.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-072744/`
- Outcome:
  - language support pages no longer rely on hardcoded legacy bean-helper class binding.
  - Project 02 completion estimate updated to `96%`.

## 2026-02-26 07:30 UTC (Mini-Wave M3.6)
- Executed support history iterate-type decoupling wave.
- Changed file:
  - `gs-server/game-server/web-gs/src/main/webapp/support/supporthistory.jsp`
- Change detail:
  - removed hardcoded Struts iterate `type="com.dgphoenix..."` binding.
  - replaced typed method access (`entry.getHistoryUrl()`) with `bean:define` property extraction (`historyUrl`), preserving behavior and JSPC compatibility.
- Validation PASS:
  - full build/test matrix (sb-utils, promo/common-persisters, cache tests, web-gs package, mp subset package)
  - runtime bank-template audit (`bank-template-audit.mjs`) for banks `6275,6276`: PASS.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-073017/`
- Outcome:
  - eliminated remaining active support JSP hardcoded class binding from iterate tag path.
  - remaining `com.dgphoenix` tokens in support JSP scan are now expected compatibility fallbacks/imports/comments only.
  - Project 02 completion estimate updated to `100%` (actionable runtime renaming backlog complete).

## 2026-02-26 07:33 UTC (Closure Package)
- Added final closure/sign-off artifact:
  - `docs/projects/02-runtime-renaming-refactor/09-runtime-renaming-closure-report-20260226.md`
- Captured:
  - full scope completion statement,
  - validation protocol used across all waves,
  - latest evidence chain,
  - latest commit checkpoints,
  - residual-token policy (intentional compatibility/documentation references).
- Outcome:
  - Project 02 is documented as closed for actionable runtime-renaming scope.

## 2026-02-26 07:43 UTC (Evidence retention cleanup)
- Added previously untracked guarded/failure wave evidence files into tracked audit artifacts.
- Included Phase 9 pre-guardrail scan snapshots and failed-attempt validation logs (`20260226-063100`, `20260226-072309`, `20260226-072929`).
- Commit reference:
  - `27ca4543` — `Add retained phase9 guarded-wave evidence artifacts`

## 2026-02-26 09:13 UTC (Hard-cut Replan)
- User requested a full replan because `com.dgphoenix` still appears in runtime logs and the original goal was full hard-cut rename.
- Verified current state before replanning:
  - source remains mostly `com.dgphoenix` package namespace,
  - compatibility-first closure was complete, but hard-cut namespace migration was not complete.
- Added new execution plan document:
  - `docs/projects/02-runtime-renaming-refactor/10-hard-cut-namespace-migration-plan-20260226.md`
- Plan defines strict hard-cut done criteria:
  - zero active runtime `com.dgphoenix` package usage,
  - migrated build coordinates/runtime class-string bindings,
  - fallback removal,
  - full launch/wallet/mp regression evidence.
- Next step:
  - start M0 baseline lock and milestone-by-milestone execution under this new hard-cut plan.

## 2026-02-26 09:15 UTC (M0 Baseline Lock Completed)
- Executed hard-cut M0 baseline capture before any rename edits.
- Created immutable evidence pack:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-091520-hardcut-m0-baseline/`
- Captured:
  - git head/status,
  - docker runtime snapshot,
  - GS/MP log tails,
  - package declaration inventories (`com.dgphoenix`/`com.abs`) for GS and MP,
  - pom groupId inventories for GS and MP,
  - runtime class-string/token inventories and hotspot rankings.
- M0 summary report created:
  - `docs/projects/02-runtime-renaming-refactor/11-hard-cut-m0-baseline-lock-report-20260226.md`
- Key baseline facts:
  - GS packages: `com.dgphoenix=2060`, `com.abs=1`
  - MP packages: `com.dgphoenix=217`, `com.abs=0`
  - GS pom groupId legacy hits: `57`; MP pom legacy hits: `40`
  - GS runtime class-string inventory: `1583` hits
  - MP runtime token inventory: `1532` hits
  - GS log legacy namespace hits in tail: `100`
- Outcome:
  - M0 gate is complete and reproducible.
  - Next gate is M1 (build-coordinate transition prep) under strict guarded waves.

## 2026-02-26 09:19 UTC (M1 Build-Coordinate Prep Completed)
- Created M1 evidence pack:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-091920-hardcut-m1-coordinate-prep/`
- Mapped legacy coordinate usage in poms:
  - GS hits: `57`
  - MP hits: `40`
- Confirmed MP bridge dependencies still tied to legacy GS coordinate namespace:
  - `com.dgphoenix.casino:gsn-cache-restricted`
  - `com.dgphoenix.casino:utils-restricted`
  - `com.dgphoenix.casino.tools:kryo-validator`
- Build probe note:
  - broad selector command hit duplicate reactor project identity (`gsn-common-gs`), so validation switched to proven per-module matrix.
- Validation matrix status: PASS (all `BUILD SUCCESS`), evidence files captured in pack.
- M1 report added:
  - `docs/projects/02-runtime-renaming-refactor/12-hard-cut-m1-coordinate-prep-report-20260226.md`
- Decision:
  - keep legacy coordinate bridge during package migration waves, postpone coordinate hard-cut to dedicated late wave to avoid MP breakage.

## 2026-02-26 09:22 UTC (M2 Wave 1 - Annotations Package)
- Executed first package migration wave with low-risk scope only:
  - `com.dgphoenix.casino.tools.annotations` -> `com.abs.casino.tools.annotations`
- Changed 17 source/test files + 3 annotation package declarations.
- Created wave evidence pack:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-092251-hardcut-m2-wave1-annotations/`
- Verification outcome:
  - legacy annotation package refs in `gs-server`: `0`
  - new `com.abs` refs: `18`
  - 6 validation commands completed with `BUILD SUCCESS`.
- Note on execution safety:
  - initial parallel run of `annotations install` and `kryo-validator test` caused temporary compile failure due dependency update race.
  - reran sequentially; `kryo-validator` then passed.
- Wave report added:
  - `docs/projects/02-runtime-renaming-refactor/13-hard-cut-m2-wave1-annotations-report-20260226.md`

## 2026-02-26 09:27 UTC (M2 Wave 2 - Kryo Package)
- Executed second hard-cut package wave:
  - `com.dgphoenix.casino.tools.kryo*` -> `com.abs.casino.tools.kryo*`
- Scope: 55 files (kryo-validator module package/import declarations + dependent test imports in common/common-gs/sb-utils).
- Evidence pack:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-092746-hardcut-m2-wave2-kryo/`
- Post-scan result:
  - legacy kryo refs (`com.dgphoenix...tools.kryo`) in GS/MP: `0`
  - target refs (`com.abs...tools.kryo`) in GS/MP: `72`
- Validation:
  - PASS: annotations install, kryo-validator test/install, sb-utils test rerun, promo/common-persisters install, cache test, web-gs package, mp package, runtime smoke.
  - Captured failures for traceability:
    - initial sb-utils/common failures due dependency-order race before `kryo-validator` install,
    - common test rerun NPE in `KryoSerializationTest` (`FeedQueue` path),
    - common-gs rerun compile failure in unchanged `BasicTransactionDataStorageHelper` (`PROTOCOL_VERSION`).
- Outcome:
  - Wave completed with runtime/package validation green and known non-wave baseline issues explicitly documented.
- Report:
  - `docs/projects/02-runtime-renaming-refactor/14-hard-cut-m2-wave2-kryo-report-20260226.md`

## 2026-02-26 09:36 UTC (M2 Wave 3 - Common REST Package)
- Executed next hard-cut package migration wave for `common.rest` family.
- Migrated namespace:
  - `com.dgphoenix.casino.common.rest` -> `com.abs.casino.common.rest`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/rest/CustomRestTemplate.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/rest/AddableHttpRequest.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/rest/CustomResponseErrorHandler.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/CanexCWClient.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/CustomRESTCWClient.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/StandardJsonCWClient.java`
  - `gs-server/common-wallet/src/test/java/com/dgphoenix/casino/payment/wallet/client/v4/CanexCWClientTest.java`
- Post-scan result:
  - legacy refs: `0`
  - abs refs: `7`
- Validation matrix: all 9 commands PASS (including runtime smoke).
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-093419-hardcut-m2-wave3-common-rest`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/15-hard-cut-m2-wave3-common-rest-report-20260226.md`

## 2026-02-26 09:39 UTC (M2 Wave 4 - Promo Icon + Masker)
- Migrated namespaces:
  - `com.dgphoenix.casino.promo.masker` -> `com.abs.casino.promo.masker`
  - `com.dgphoenix.casino.promo.icon` -> `com.abs.casino.promo.icon`
- Updated dependent imports in promo persisters, common-gs tournament handlers, and support JSP icon pages.
- Post-scan result: legacy refs `0`, abs refs `11`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-093723-hardcut-m2-wave4-promo-icon-masker`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/16-hard-cut-m2-wave4-promo-icon-masker-report-20260226.md`

## 2026-02-26 09:46 UTC (M2 Wave 5 - Onlineplayer Attempt Aborted)
- Attempted namespace migration for:
  - `com.dgphoenix.casino.common.client.canex.request.onlineplayer`
- Validation result:
  - initial run: `web-gs` compile mismatch due stale `common-gs` artifact.
  - rerun with `common-gs` install exposed known baseline blocker in `BasicTransactionDataStorageHelper` (`PROTOCOL_VERSION`) plus cross-artifact type mismatch chain.
- Action:
  - rolled back onlineplayer code edits to avoid unstable partial migration.
- Retained evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-093957-hardcut-m2-wave5-onlineplayer`

## 2026-02-26 09:46 UTC (M2 Wave 5 - Analytics Spin Completed)
- Re-scoped Wave 5 to low-fanout family:
  - `com.dgphoenix.casino.common.analytics.spin` -> `com.abs.casino.common.analytics.spin`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/analytics/spin/SpinStatistic.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/analytics/spin/ClientStatistic.java`
- Post-scan result: legacy refs `0`, abs refs `2`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-094413-hardcut-m2-wave5-analytics-spin`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/17-hard-cut-m2-wave5-analytics-spin-report-20260226.md`

## 2026-02-26 09:48 UTC (M2 Wave 6 - Canex Response Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.client.canex.response` -> `com.abs.casino.common.client.canex.response`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/client/canex/response/CanexResponse.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/client/canex/response/CanexJsonResponse.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/client/canex/response/ExtSystem.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/CanexCWClient.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/controller/stub/cw/CanexStubController.java`
- Post-scan result: legacy refs `0`, abs refs `6`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-094646-hardcut-m2-wave6-canex-response`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/18-hard-cut-m2-wave6-canex-response-report-20260226.md`

## 2026-02-26 09:51 UTC (M2 Wave 7 - Promo Win Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.promo.win` -> `com.abs.casino.promo.win`
- Changed files:
  - `gs-server/promo/persisters/src/main/java/com/dgphoenix/casino/promo/win/PromoWin.java`
  - `gs-server/promo/persisters/src/main/java/com/dgphoenix/casino/promo/persisters/CassandraPromoWinPersister.java`
- Post-scan result: legacy refs `0`, abs refs `2`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-094953-hardcut-m2-wave7-promo-win`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/19-hard-cut-m2-wave7-promo-win-report-20260226.md`

## 2026-02-26 09:55 UTC (M2 Wave 8 - Canex Request Root Completed)
- Migrated root request namespace class usage:
  - `com.dgphoenix.casino.common.client.canex.request.CanexRequest`
  - `com.dgphoenix.casino.common.client.canex.request.CanexJsonRequest`
  - `com.dgphoenix.casino.common.client.canex.request.RequestType`
  -> `com.abs.casino.common.client.canex.request.*`
- Updated package declarations for root request package files and dependent imports in canex request subpackages, common-wallet, and web-gs stub controller.
- Post-scan result: legacy refs `0`, abs refs `23`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-095313-hardcut-m2-wave8-canex-request-root`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/20-hard-cut-m2-wave8-canex-request-root-report-20260226.md`

## 2026-02-26 09:58 UTC (M2 Wave 9 - Common Monitoring Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.monitoring.OnlineConcurrentMailNotification` -> `com.abs.casino.common.monitoring.OnlineConcurrentMailNotification`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/monitoring/OnlineConcurrentMailNotification.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraNotificationPersister.java`
- Post-scan result: legacy refs `0`, abs refs `2`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-095618-hardcut-m2-wave9-common-monitoring`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/21-hard-cut-m2-wave9-common-monitoring-report-20260226.md`

## 2026-02-26 10:00 UTC (M2 Wave 10 - DomainWhiteList Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.domain.DomainWhiteList` -> `com.abs.casino.common.cache.data.domain.DomainWhiteList`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/domain/DomainWhiteList.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/DomainWhiteListCache.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraDomainWhiteListPersister.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/EditDomainAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/DomainForManyAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/ReadDomainsAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/DomainWhiteListAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/DomainsByGameAction.java`
- Post-scan result: legacy refs `0`, abs refs `8`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-095853-hardcut-m2-wave10-domain-whitelist`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/22-hard-cut-m2-wave10-domain-whitelist-report-20260226.md`

## 2026-02-26 10:03 UTC (M2 Wave 11 - Common Web JSON Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.json.ZonedDateTimeSerializer` -> `com.abs.casino.common.web.json.ZonedDateTimeSerializer`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/json/ZonedDateTimeSerializer.java`
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100207-hardcut-m2-wave11-common-web-json`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/23-hard-cut-m2-wave11-common-web-json-report-20260226.md`

## 2026-02-26 10:05 UTC (M2 Wave 12 - LanguageType Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.language.LanguageType` -> `com.abs.casino.common.cache.data.language.LanguageType`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/language/LanguageType.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/util/LanguageLabelValueBean.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/history/GameHistoryListForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/gamehistory/GameHistorySupportForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/addbank/LoadDefBankAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/addbank/AcceptInfoFromOtherBankAction.java`
- Post-scan result: legacy refs `0`, abs refs `6`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100426-hardcut-m2-wave12-language-type`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/24-hard-cut-m2-wave12-language-type-report-20260226.md`

## 2026-02-26 10:08 UTC (M2 Wave 13 - Wallet Operation Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.wallet_operation.ExternalTransactionInfo` -> `com.abs.casino.common.cache.data.wallet_operation.ExternalTransactionInfo`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/wallet_operation/ExternalTransactionInfo.java`
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100634-hardcut-m2-wave13-wallet-operation`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/25-hard-cut-m2-wave13-wallet-operation-report-20260226.md`

## 2026-02-26 10:11 UTC (M2 Wave 14 - PeriodicReportInfo Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.report.PeriodicReportInfo` -> `com.abs.casino.common.cache.data.report.PeriodicReportInfo`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/report/PeriodicReportInfo.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/PeriodicReportsCache.java`
- Post-scan result: legacy refs `0`, abs refs `2`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100936-hardcut-m2-wave14-periodic-report`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/26-hard-cut-m2-wave14-periodic-report-report-20260226.md`

## 2026-02-26 10:20 UTC (M2 Wave 15 - MassAwardRestriction Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.bonus.restriction.MassAwardRestriction` -> `com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction`
- Final changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bonus/restriction/MassAwardRestriction.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraMassAwardRestrictionPersister.java`
- Scoped scan result (wave scope only): legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-101415-hardcut-m2-wave15-mass-award-restriction`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/27-hard-cut-m2-wave15-mass-award-restriction-report-20260226.md`
- Execution note:
  - attempted boundary extension into `common-gs`/JSP imports was reverted because of existing baseline compile blocker in `common-gs` (`BasicTransactionDataStorageHelper` `PROTOCOL_VERSION`). Wave was intentionally re-scoped to keep the main validation matrix green.

## 2026-02-26 10:24 UTC (M2 Wave 16 - PaymentMean Family Completed)
- Migrated namespace family:
  - `com.dgphoenix.casino.common.cache.data.payment.transfer.paymentmean.*` -> `com.abs.casino.common.cache.data.payment.transfer.paymentmean.*`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/paymentmean/AbstractPaymentMean.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/paymentmean/CereusPaymentMean.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/paymentmean/IPaymentMean.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/paymentmean/PaymentMeanId.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/paymentmean/PaymentMeanType.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/PaymentTransaction.java`
- Post-scan result: legacy refs `0`, abs refs `7`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-102256-hardcut-m2-wave16-paymentmean`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/28-hard-cut-m2-wave16-paymentmean-report-20260226.md`

## 2026-02-26 10:26 UTC (M2 Wave 17 - Payment Transfer Processor Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.payment.transfer.processor.IPaymentProcessor` -> `com.abs.casino.common.cache.data.payment.transfer.processor.IPaymentProcessor`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/transfer/processor/IPaymentProcessor.java`
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-102516-hardcut-m2-wave17-payment-transfer-processor`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/29-hard-cut-m2-wave17-payment-transfer-processor-report-20260226.md`

## 2026-02-26 10:31 UTC (M2 Wave 18 - HistoryInformerItem Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.HistoryInformerItem` -> `com.abs.casino.common.cache.data.HistoryInformerItem`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/HistoryInformerItem.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHistoryInformerItemPersister.java`
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103007-hardcut-m2-wave18-history-informer-item`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/30-hard-cut-m2-wave18-history-informer-item-report-20260226.md`

## 2026-02-26 10:34 UTC (M2 Wave 19 - Gender Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.account.Gender` -> `com.abs.casino.common.cache.data.account.Gender`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/Gender.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v3/RESTCWClient.java`
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103303-hardcut-m2-wave19-gender`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/31-hard-cut-m2-wave19-gender-report-20260226.md`

## 2026-02-26 10:36 UTC (M2 Wave 20 - Session Browser + Client Info Completed)
- Migrated namespaces:
  - `com.dgphoenix.casino.common.cache.data.session.BrowserInfo` -> `com.abs.casino.common.cache.data.session.BrowserInfo`
  - `com.dgphoenix.casino.common.cache.data.session.GameClientInfo` -> `com.abs.casino.common.cache.data.session.GameClientInfo`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/session/BrowserInfo.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/session/GameClientInfo.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraClientStatisticsPersister.java`
- Post-scan result:
  - BrowserInfo legacy refs `0`, abs refs `1`
  - GameClientInfo legacy refs `0`, abs refs `1`
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103510-hardcut-m2-wave20-session-client-browser`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/32-hard-cut-m2-wave20-session-client-browser-report-20260226.md`

## 2026-02-26 10:41 UTC (M2 Wave 21 - AccountConstants + PlayerAction Completed)
- Initial attempt (aborted): `WalletOperationInfo` migration failed due direct dependency chain to unchanged `WalletOperationStatus`/`WalletOperationType`; changes were rolled back before commit.
- Final Wave 21 scope (completed):
  - `com.dgphoenix.casino.common.cache.data.account.AccountConstants` -> `com.abs.casino.common.cache.data.account.AccountConstants`
  - `com.dgphoenix.casino.common.cache.data.account.PlayerAction` -> `com.abs.casino.common.cache.data.account.PlayerAction`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/AccountConstants.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/PlayerAction.java`
- Post-scan result: legacy declarations `0`, abs declarations `2` (scope files).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103948-hardcut-m2-wave21-account-constants-player-action`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/33-hard-cut-m2-wave21-account-constants-player-action-report-20260226.md`

## 2026-02-26 10:45 UTC (M2 Wave 22 - Isolated Account/Payment Models Completed)
- Migrated namespace declarations:
  - `ExtendedAccountInfo` -> `com.abs.casino.common.cache.data.account.ExtendedAccountInfo`
  - `PlayerGameError` -> `com.abs.casino.common.cache.data.account.PlayerGameError`
  - `PlayerGameState` -> `com.abs.casino.common.cache.data.account.PlayerGameState`
  - `GameSessionInfoContainer` -> `com.abs.casino.common.cache.data.payment.GameSessionInfoContainer`
  - `ListOfLongsContainer` -> `com.abs.casino.common.cache.data.payment.ListOfLongsContainer`
  - `LongValueContainer` -> `com.abs.casino.common.cache.data.payment.LongValueContainer`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/ExtendedAccountInfo.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/PlayerGameError.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/PlayerGameState.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/GameSessionInfoContainer.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/ListOfLongsContainer.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/LongValueContainer.java`
- Post-scan result: legacy declarations `0`, abs declarations `6` (scope files).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-104418-hardcut-m2-wave22-isolated-models`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/34-hard-cut-m2-wave22-isolated-models-report-20260226.md`

## 2026-02-26 10:49 UTC (M2 Wave 23 - Server/Session Constants Completed)
- Initial attempt (aborted): included `BankMiniGameInfo`, which failed due in-package dependencies (`MiniGameInfo`, `BaseGameInfo`). The `BankMiniGameInfo` change was rolled back before finalizing the wave.
- Final Wave 23 scope (completed):
  - `com.dgphoenix.casino.common.cache.data.server.ServerInfoConstants` -> `com.abs.casino.common.cache.data.server.ServerInfoConstants`
  - `com.dgphoenix.casino.common.cache.data.session.SessionLimit` -> `com.abs.casino.common.cache.data.session.SessionLimit`
  - `com.dgphoenix.casino.common.cache.data.session.SessionStatistics` -> `com.abs.casino.common.cache.data.session.SessionStatistics`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/server/ServerInfoConstants.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/session/SessionLimit.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/session/SessionStatistics.java`
- Post-scan result: legacy declarations `0`, abs declarations `3` (scope files).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-104658-hardcut-m2-wave23-server-session-constants`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/35-hard-cut-m2-wave23-server-session-constants-report-20260226.md`

## 2026-02-26 10:53 UTC (M2 Wave 24 - PlayerGameSettingsType Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.bank.PlayerGameSettingsType` -> `com.abs.casino.common.cache.data.bank.PlayerGameSettingsType`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/PlayerGameSettingsType.java`
  - `gs-server/game-server/web-gs/src/main/webapp/support/setup_replaceurl_pm_st.jsp`
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-105209-hardcut-m2-wave24-player-game-settings-type`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/36-hard-cut-m2-wave24-player-game-settings-type-report-20260226.md`

## 2026-02-26 11:02 UTC (M2 Wave 25 - MaxQuestClientLogLevel Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.bank.MaxQuestClientLogLevel` -> `com.abs.casino.common.cache.data.bank.MaxQuestClientLogLevel`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/MaxQuestClientLogLevel.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java`
  - `gs-server/common/src/test/java/com/dgphoenix/casino/common/cache/data/bank/BankInfoAliasCompatibilityTest.java`
- Compatibility correction during wave:
  - Added explicit import for `com.abs...PlayerGameSettingsType` in `BankInfo` because Wave 24 moved that enum out of the local package.
- Post-scan result: legacy refs `0`, abs refs `2`.
- Validation matrix:
  - Required 9 checks PASS.
  - `web-gs` package required an environment-only rerun with `-Dcluster.properties=local/local-machine.properties`.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-105534-hardcut-m2-wave25-maxquest-client-log-level`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/37-hard-cut-m2-wave25-maxquest-client-log-level-report-20260226.md`

## 2026-02-26 11:09 UTC (M2 Wave 26 - IndividualGameSettingsType Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.bank.IndividualGameSettingsType` -> `com.abs.casino.common.cache.data.bank.IndividualGameSettingsType`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/IndividualGameSettingsType.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java`
- Compatibility correction during wave:
  - Added explicit import for `com.abs...IndividualGameSettingsType` in `BankInfo`.
- Post-scan result: legacy refs `0`, abs refs `1`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-110501-hardcut-m2-wave26-individual-game-settings-type`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/38-hard-cut-m2-wave26-individual-game-settings-type-report-20260226.md`

## 2026-02-26 11:14 UTC (M2 Wave 27 - IServerInfoInternalProvider Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.server.IServerInfoInternalProvider` -> `com.abs.casino.common.cache.data.server.IServerInfoInternalProvider`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/server/IServerInfoInternalProvider.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/LoadBalancerCache.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/ServerConfigsCache.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraServerInfoPersister.java`
- Compatibility correction during wave:
  - Added explicit `ServerInfo` import in migrated interface because `ServerInfo` remains in `com.dgphoenix`.
- Post-scan result: legacy refs `0`, abs refs `3`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-111037-hardcut-m2-wave27-server-info-internal-provider`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/39-hard-cut-m2-wave27-server-info-internal-provider-report-20260226.md`

## 2026-02-26 11:19 UTC (M2 Wave 28 - BankConstants Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.bank.BankConstants` -> `com.abs.casino.common.cache.data.bank.BankConstants`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankConstants.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/BankInfoCache.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java`
  - `gs-server/game-server/web-gs/src/main/webapp/support/gameBankConfig/applyGame.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/games/829_step1_AddGameInfoTemplate.jsp`
  - `gs-server/game-server/web-gs/src/main/webapp/support/templateManager/cloneTemplate.jsp`
- Post-scan result: legacy refs `0`, abs refs `5`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-111555-hardcut-m2-wave28-bank-constants`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/40-hard-cut-m2-wave28-bank-constants-report-20260226.md`

## 2026-02-26 11:25 UTC (M2 Wave 29 - SessionConstants Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.session.SessionConstants` -> `com.abs.casino.common.cache.data.session.SessionConstants`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/session/SessionConstants.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/session/SessionInfo.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
- Post-scan result: legacy refs `0`, abs refs `2`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-112208-hardcut-m2-wave29-session-constants`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/41-hard-cut-m2-wave29-session-constants-report-20260226.md`

## 2026-02-26 11:33 UTC (M2 Wave 30 - IFRBonusWinOperation Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.payment.frb.IFRBonusWinOperation` -> `com.abs.casino.common.cache.data.payment.frb.IFRBonusWinOperation`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/frb/IFRBonusWinOperation.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/frb/IFRBonusWin.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/FRBWinOperation.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/client/frb/FRBRESTClient.java`
- Compatibility correction during wave:
  - Added explicit `FRBWinOperationStatus` import in migrated interface because status enum remains in `com.dgphoenix`.
- Post-scan result: legacy refs `0`, abs refs `3`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-112847-hardcut-m2-wave30-ifr-bonus-win-operation`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/42-hard-cut-m2-wave30-ifr-bonus-win-operation-report-20260226.md`

## 2026-02-26 11:42 UTC (M2 Wave 31 - IFRBonusWin Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.payment.frb.IFRBonusWin` -> `com.abs.casino.common.cache.data.payment.frb.IFRBonusWin`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/frb/IFRBonusWin.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/FRBonusWin.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/FRBonusDBLink.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/DBLink.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/FRBonusManager.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
- Post-scan result: legacy refs `0`, abs refs `5`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-113605-hardcut-m2-wave31-ifr-bonus-win`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/43-hard-cut-m2-wave31-ifr-bonus-win-report-20260226.md`

## 2026-02-26 11:51 UTC (M2 Wave 32 - FRBonusNotificationStatus Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.payment.frb.FRBonusNotificationStatus` -> `com.abs.casino.common.cache.data.payment.frb.FRBonusNotificationStatus`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/frb/FRBonusNotificationStatus.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/FRBonusNotification.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/FRBonusManager.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/tracker/FRBonusNotificationTrackerTask.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/FRBonusNotificationManager.java`
- Post-scan result: legacy refs `0`, abs refs `4`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-114542-hardcut-m2-wave32-fr-bonus-notification-status`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/44-hard-cut-m2-wave32-fr-bonus-notification-status-report-20260226.md`

## 2026-02-26 11:55 UTC (M2 Wave 33 - FRBWinOperationStatus Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.payment.frb.FRBWinOperationStatus` -> `com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/frb/FRBWinOperationStatus.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/transactiondata/TransactionData.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/frb/IFRBonusWinOperation.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/FRBWinOperation.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/CommonFRBonusWin.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/FRBonusWin.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/FRBonusManager.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/tracker/FRBonusWinTrackerTask.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/OriginalFRBonusWinManager.java`
- Post-scan result: legacy refs `0`, abs refs `8`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-115322-hardcut-m2-wave33-frb-win-operation-status`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/45-hard-cut-m2-wave33-frb-win-operation-status-report-20260226.md`

## 2026-02-26 12:02 UTC (M2 Wave 34 - CommonFRBonusWin Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.payment.bonus.CommonFRBonusWin` -> `com.abs.casino.common.cache.data.payment.bonus.CommonFRBonusWin`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/CommonFRBonusWin.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/transactiondata/TransactionData.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/OriginalFRBonusWinManager.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/payment/bonus/FRBonusWin.java` (compatibility import update)
- Post-scan result: legacy refs `0`, abs refs `4`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-115923-hardcut-m2-wave34-common-fr-bonus-win`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/46-hard-cut-m2-wave34-common-fr-bonus-win-report-20260226.md`

## 2026-02-26 12:17 UTC (M2 Wave 35 - ServerOnlineStatus Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.cache.data.server.ServerOnlineStatus` -> `com.abs.casino.common.cache.data.server.ServerOnlineStatus`
- Changed target files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/server/ServerOnlineStatus.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/LoadBalancerCache.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/kafka/dto/NotifyOnServerStatusesUpdatedRequest.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/status/WatchServersThreadSlave.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/status/WatchServersThreadMaster.java`
- Compatibility corrections applied to keep validation matrix green:
  - web-gs start action type-alignment (`BaseStartGameAction` in both action packages)
  - common-gs type alignment for restriction interfaces and protocol constant usage (`BasicTransactionDataStorageHelper`, `MassAwardBonusManager`, `NoAwardRestriction`, `PlayerBalanceRestriction`, `DBLink`, `FRBonusManager`)
- Post-scan result: legacy refs `0`, abs refs `5`.
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-121044-hardcut-m2-wave35-server-online-status`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/47-hard-cut-m2-wave35-server-online-status-report-20260226.md`

## 2026-02-26 12:25 UTC (M2 Wave 36 - AllServersOfflineException Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.gs.socket.AllServersOfflineException` -> `com.abs.casino.gs.socket.AllServersOfflineException`
- Changed target file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/gs/socket/AllServersOfflineException.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-122122-hardcut-m2-wave36-all-servers-offline-exception`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/48-hard-cut-m2-wave36-all-servers-offline-exception-report-20260226.md`

## 2026-02-26 12:29 UTC (M2 Wave 37 - ActionRedirectCustomParamsEncoding Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.ActionRedirectCustomParamsEncoding` -> `com.abs.casino.common.web.ActionRedirectCustomParamsEncoding`
- Changed target file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/ActionRedirectCustomParamsEncoding.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-122701-hardcut-m2-wave37-action-redirect-custom-params`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/49-hard-cut-m2-wave37-action-redirect-custom-params-report-20260226.md`

## 2026-02-26 12:34 UTC (M2 Wave 38 - BaseJsonAction Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.BaseJsonAction` -> `com.abs.casino.common.web.BaseJsonAction`
- Changed target file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/BaseJsonAction.java`
- Compatibility import updates in target class:
  - `com.dgphoenix.casino.common.web.BaseAction`
  - `com.dgphoenix.casino.common.web.JsonResult`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-123128-hardcut-m2-wave38-base-json-action`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/50-hard-cut-m2-wave38-base-json-action-report-20260226.md`

## 2026-02-26 12:39 UTC (M2 Wave 39 - InfoException Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.exception.InfoException` -> `com.abs.casino.common.exception.InfoException`
- Changed target file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/exception/InfoException.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-123603-hardcut-m2-wave39-info-exception`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/51-hard-cut-m2-wave39-info-exception-report-20260226.md`

## 2026-02-26 12:45 UTC (M2 Wave 40 - LogoutCommonConstants Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.logout.LogoutCommonConstants` -> `com.abs.casino.common.web.logout.LogoutCommonConstants`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/logout/LogoutCommonConstants.java`
  - `gs-server/game-server/web-gs/src/main/webapp/standlobby.jsp`
- Post-scan result: legacy refs `0`, abs refs `2` (pre-scan legacy refs `2`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-124135-hardcut-m2-wave40-logout-common-constants`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/52-hard-cut-m2-wave40-logout-common-constants-report-20260226.md`

## 2026-02-26 12:50 UTC (M2 Wave 41 - CTLobbyLoginResponse Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.ct.CTLobbyLoginResponse` -> `com.abs.casino.common.web.login.ct.CTLobbyLoginResponse`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/ct/CTLobbyLoginResponse.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-124705-hardcut-m2-wave41-ct-lobby-login-response`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/53-hard-cut-m2-wave41-ct-lobby-login-response-report-20260226.md`

## 2026-02-26 12:55 UTC (M2 Wave 42 - CWGuestLoginGameServerResponse Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.cw.CWGuestLoginGameServerResponse` -> `com.abs.casino.common.web.login.cw.CWGuestLoginGameServerResponse`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/cw/CWGuestLoginGameServerResponse.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-125201-hardcut-m2-wave42-cw-guest-login-response`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/54-hard-cut-m2-wave42-cw-guest-login-response-report-20260226.md`

## 2026-02-26 13:00 UTC (M2 Wave 43 - CWLoginGameServerResponse Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.cw.CWLoginGameServerResponse` -> `com.abs.casino.common.web.login.cw.CWLoginGameServerResponse`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/cw/CWLoginGameServerResponse.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-125635-hardcut-m2-wave43-cw-login-response`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/55-hard-cut-m2-wave43-cw-login-response-report-20260226.md`

## 2026-02-26 13:05 UTC (M2 Wave 44 - CTLobbyLoginRequest Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.ct.CTLobbyLoginRequest` -> `com.abs.casino.common.web.login.ct.CTLobbyLoginRequest`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/ct/CTLobbyLoginRequest.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-130155-hardcut-m2-wave44-ct-lobby-login-request`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/56-hard-cut-m2-wave44-ct-lobby-login-request-report-20260226.md`

## 2026-02-26 13:12 UTC (M2 Wave 45 - LoginCommonConstants Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.LoginCommonConstants` -> `com.abs.casino.common.web.login.LoginCommonConstants`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/LoginCommonConstants.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/login/AbstractGatewayServlet.java`
- Post-scan result: legacy refs `0`, abs refs `2` (pre-scan legacy refs `2`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-130750-hardcut-m2-wave45-login-common-constants`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/57-hard-cut-m2-wave45-login-common-constants-report-20260226.md`

## 2026-02-26 13:18 UTC (M2 Wave 46 - CWStLobbyGuestLoginLobbyRequest Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.cw.CWStLobbyGuestLoginLobbyRequest` -> `com.abs.casino.common.web.login.cw.CWStLobbyGuestLoginLobbyRequest`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/cw/CWStLobbyGuestLoginLobbyRequest.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-131427-hardcut-m2-wave46-cw-stlobby-guest-login-request`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/58-hard-cut-m2-wave46-cw-stlobby-guest-login-request-report-20260226.md`

## 2026-02-26 13:26 UTC (M2 Wave 47 - CWGuestLoginLobbyRequest Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.cw.CWGuestLoginLobbyRequest` -> `com.abs.casino.common.web.login.cw.CWGuestLoginLobbyRequest`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/cw/CWGuestLoginLobbyRequest.java`
- Post-scan result: legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-132052-hardcut-m2-wave47-cw-guest-login-lobby-request`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/59-hard-cut-m2-wave47-cw-guest-login-lobby-request-report-20260226.md`

## 2026-02-26 13:36 UTC (M2 Wave 48 - APUBConstants Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.apub.APUBConstants` -> `com.abs.casino.common.web.login.apub.APUBConstants`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/apub/APUBConstants.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/game/BaseStartGameAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `3` (pre-scan legacy refs `3`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-132929-hardcut-m2-wave48-apub-constants`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/60-hard-cut-m2-wave48-apub-constants-report-20260226.md`

## 2026-02-26 13:40 UTC (M2 Wave 49 - GameServerResponse Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.web.login.apub.GameServerResponse` -> `com.abs.casino.common.web.login.apub.GameServerResponse`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/apub/GameServerResponse.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/cw/CWGuestLogin.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/bonus/BSStartGameAction.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `5` (pre-scan legacy refs `5`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-133833-hardcut-m2-wave49-apub-game-server-response`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/61-hard-cut-m2-wave49-apub-game-server-response-report-20260226.md`

## 2026-02-26 13:46 UTC (M2 Wave 50 - FreeSpaceThresholdType Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.config.FreeSpaceThresholdType` -> `com.abs.casino.common.config.FreeSpaceThresholdType`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/FreeSpaceThresholdType.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/MountMonitoringEntry.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/GameServerConfigTemplate.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/SystemDiagnosisServlet.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `4` (pre-scan legacy refs `2`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-134432-hardcut-m2-wave50-free-space-threshold-type`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/62-hard-cut-m2-wave50-free-space-threshold-type-report-20260226.md`

## 2026-02-26 13:51 UTC (M2 Wave 51 - MountMonitoringEntry Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.config.MountMonitoringEntry` -> `com.abs.casino.common.config.MountMonitoringEntry`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/MountMonitoringEntry.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/GameServerConfigTemplate.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/system/configuration/GameServerConfiguration.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/SystemDiagnosisServlet.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `4` (pre-scan legacy refs `3`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-134842-hardcut-m2-wave51-mount-monitoring-entry`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/63-hard-cut-m2-wave51-mount-monitoring-entry-report-20260226.md`

## 2026-02-26 13:56 UTC (M2 Wave 52 - CommonContextConfiguration Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.config.CommonContextConfiguration` -> `com.abs.casino.common.config.CommonContextConfiguration`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/CommonContextConfiguration.java`
  - `gs-server/common-wallet/src/test/java/com/dgphoenix/casino/payment/wallet/commonwalletmanger/CommonWalletManagerTest.java`
  - `gs-server/support/archiver/src/main/java/com/dgphoenix/casino/support/Archiver.java`
  - `gs-server/support/archiver/src/main/java/com/dgphoenix/casino/support/DsoExport.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/config/WebApplicationContextConfiguration.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `5` (pre-scan legacy refs `5`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-135411-hardcut-m2-wave52-common-context-configuration`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/64-hard-cut-m2-wave52-common-context-configuration-report-20260226.md`

## 2026-02-26 14:01 UTC (M2 Wave 53 - ClusterType Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.config.ClusterType` -> `com.abs.casino.common.config.ClusterType`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/ClusterType.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/HostConfiguration.java`
  - `gs-server/common/src/test/java/com/dgphoenix/casino/common/config/HostConfigurationTest.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `3` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-135859-hardcut-m2-wave53-cluster-type`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/65-hard-cut-m2-wave53-cluster-type-report-20260226.md`

## 2026-02-26 14:12 UTC (M2 Wave 54 - HostConfiguration Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.config.HostConfiguration` -> `com.abs.casino.common.config.HostConfiguration`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/HostConfiguration.java`
  - `gs-server/common/src/test/java/com/dgphoenix/casino/common/config/HostConfigurationTest.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/game/BaseGameInfo.java`
  - `gs-server/promo/core/src/main/java/com/dgphoenix/casino/promo/PromoCampaignManager.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/SharedGameServerComponentsConfiguration.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServerComponentsConfiguration.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/game/BaseStartGameAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `8` (pre-scan legacy refs `7`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-140514-hardcut-m2-wave54-host-configuration`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/66-hard-cut-m2-wave54-host-configuration-report-20260226.md`

## 2026-02-26 14:18 UTC (M2 Wave 55 - ICallbacksExecutor Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.gs.socket.async.ICallbacksExecutor` -> `com.abs.casino.gs.socket.async.ICallbacksExecutor`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/gs/socket/async/ICallbacksExecutor.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/gs/socket/async/CallbacksExecutor.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `2` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-141600-hardcut-m2-wave55-icallbacks-executor`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/67-hard-cut-m2-wave55-icallbacks-executor-report-20260226.md`

## 2026-02-26 14:23 UTC (M2 Wave 56 - CallbacksExecutor Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.gs.socket.async.CallbacksExecutor` -> `com.abs.casino.gs.socket.async.CallbacksExecutor`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/gs/socket/async/CallbacksExecutor.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-142110-hardcut-m2-wave56-callbacks-executor`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/68-hard-cut-m2-wave56-callbacks-executor-report-20260226.md`

## 2026-02-26 14:28 UTC (M2 Wave 57 - ExternalGameProvider Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.games.ExternalGameProvider` -> `com.abs.casino.common.games.ExternalGameProvider`
- Changed file:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/ExternalGameProvider.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `1` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-142543-hardcut-m2-wave57-external-game-provider`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/69-hard-cut-m2-wave57-external-game-provider-report-20260226.md`

## 2026-02-26 14:35 UTC (M2 Wave 58 - IHelperCreator Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.games.IHelperCreator` -> `com.abs.casino.common.games.IHelperCreator`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IHelperCreator.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `3` (pre-scan legacy refs `1`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-143128-hardcut-m2-wave58-ihelpercreator`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/70-hard-cut-m2-wave58-ihelpercreator-report-20260226.md`

## 2026-02-26 14:42 UTC (M2 Wave 59 - IDelegatedStartGameHelper Completed)
- Migrated namespace:
  - `com.dgphoenix.casino.common.games.IDelegatedStartGameHelper` -> `com.abs.casino.common.games.IDelegatedStartGameHelper`
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IDelegatedStartGameHelper.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IHelperCreator.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
- Post-scan result (wave scope): legacy refs `0`, abs refs `6` (pre-scan legacy refs `2`).
- Validation matrix: all 9 commands PASS, including runtime smoke.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-143855-hardcut-m2-wave59-idelegated-start-game-helper`
- Report:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/71-hard-cut-m2-wave59-idelegated-start-game-helper-report-20260226.md`

## 2026-02-26 14:50 UTC (Hard-Cut M2 Wave 60)
- Executed hard-cut namespace wave for `ICassandraHostCdnPersister`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/ICassandraHostCdnPersister.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHostCdnPersister.java`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.ICassandraHostCdnPersister` to `com.abs.casino.common.games.ICassandraHostCdnPersister`.
  - rewired dependent imports in helper/persister implementations.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-144503-hardcut-m2-wave60-icassandra-host-cdn-persister/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2144` remaining (`2277` baseline, `133` reduced).

## 2026-02-26 14:54 UTC (Hard-Cut M2 Wave 61)
- Executed hard-cut namespace wave for `SwfLocationInfo`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/SwfLocationInfo.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IStartGameHelper.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/StartGameServletFilter.java`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.SwfLocationInfo` to `com.abs.casino.common.games.SwfLocationInfo`.
  - rewired dependent imports in helper/filter interfaces.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-145155-hardcut-m2-wave61-swf-location-info/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2143` remaining (`2277` baseline, `134` reduced).

## 2026-02-26 14:59 UTC (Hard-Cut M2 Wave 62)
- Executed hard-cut namespace wave for `CdnCheckResult`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/CdnCheckResult.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/ICassandraHostCdnPersister.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHostCdnPersister.java`
  - `gs-server/game-server/web-gs/src/main/webapp/cdn/info.jsp`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.CdnCheckResult` to `com.abs.casino.common.games.CdnCheckResult`.
  - rewired direct Java/JSP imports in CDN-check flow and host CDN persister interface path.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-145636-hardcut-m2-wave62-cdn-check-result/`
- Outcome:
  - wave scope legacy refs reduced from `3` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2142` remaining (`2277` baseline, `135` reduced).

## 2026-02-26 15:04 UTC (Hard-Cut M2 Wave 63)
- Executed hard-cut namespace wave for `StartGameHelpers`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/CommonWalletManager.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/DBLink.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/StartGameServletFilter.java`
  - `gs-server/game-server/web-gs/src/main/webapp/support/listGameIds.jsp`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.StartGameHelpers` to `com.abs.casino.common.games.StartGameHelpers`.
  - rewired Java/JSP imports in wallet, DBLink, filter, and support view.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-150121-hardcut-m2-wave63-start-game-helpers/`
- Outcome:
  - wave scope legacy refs reduced from `4` to `0`, `com.abs` refs now `4`.
  - global tracked source declarations now `2141` remaining (`2277` baseline, `136` reduced).

## 2026-02-26 15:10 UTC (Hard-Cut M2 Wave 64)
- Executed hard-cut namespace wave for `IStartGameHelper`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IStartGameHelper.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IHelperCreator.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/CommonWalletManager.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/DBLink.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/StartGameServletFilter.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
  - `gs-server/game-server/web-gs/src/main/webapp/support/listGameIds.jsp`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.IStartGameHelper` to `com.abs.casino.common.games.IStartGameHelper`.
  - rewired Java/JSP imports across helper registry, GS init path, wallet, filter, and DBLink.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-150802-hardcut-m2-wave64-istartgamehelper/`
- Outcome:
  - wave scope legacy refs reduced from `6` to `0`, `com.abs` refs now `8`.
  - global tracked source declarations now `2140` remaining (`2277` baseline, `137` reduced).

## 2026-02-26 15:15 UTC (Hard-Cut M2 Wave 65)
- Executed hard-cut namespace wave for `NewTranslationGameHelper`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
  - `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.NewTranslationGameHelper` to `com.abs.casino.common.games.NewTranslationGameHelper`.
  - rewired base-class import in helper implementation and `GameServer` import for helper creation path.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-151308-hardcut-m2-wave65-new-translation-game-helper/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2139` remaining (`2277` baseline, `138` reduced).

## 2026-02-26 15:20 UTC (Hard-Cut M2 Wave 66)
- Executed hard-cut namespace wave for `AbstractStartGameHelper`.
- Changed files:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
- Change detail:
  - migrated `com.dgphoenix.casino.common.games.AbstractStartGameHelper` to `com.abs.casino.common.games.AbstractStartGameHelper`.
  - removed now-obsolete legacy import in `NewTranslationGameHelper` after package alignment.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-151753-hardcut-m2-wave66-abstract-start-game-helper/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2138` remaining (`2277` baseline, `139` reduced).

## 2026-02-26 15:26 UTC (Hard-Cut M2 Wave 67)
- Executed hard-cut namespace wave for `InvalidHashException`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/InvalidHashException.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.InvalidHashException` to `com.abs.casino.actions.api.InvalidHashException`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-152303-hardcut-m2-wave67-invalid-hash-exception/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2137` remaining (`2277` baseline, `140` reduced).

## 2026-02-26 15:37 UTC (Hard-Cut M2 Wave 68)
- Executed hard-cut namespace wave for `ValidateException`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/ValidateException.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.ValidateException` to `com.abs.casino.actions.api.ValidateException`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-153108-hardcut-m2-wave68-validate-exception/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2136` remaining (`2277` baseline, `141` reduced).

## 2026-02-26 15:42 UTC (Hard-Cut M2 Wave 69)
- Executed hard-cut namespace wave for `PlayerHelperInfo`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/PlayerHelperInfo.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.PlayerHelperInfo` to `com.abs.casino.actions.api.PlayerHelperInfo`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-153845-hardcut-m2-wave69-player-helper-info/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2135` remaining (`2277` baseline, `142` reduced).

## 2026-02-26 15:51 UTC (Hard-Cut M2 Wave 70)
- Executed hard-cut namespace wave for `PingSessionAction`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/PingSessionAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.PingSessionAction` to `com.abs.casino.actions.api.PingSessionAction`.
  - updated Struts action mapping type to `com.abs` for `/api/pingSession`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-154653-hardcut-m2-wave70-ping-session-action/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2134` remaining (`2277` baseline, `143` reduced).

## 2026-02-26 16:00 UTC (Hard-Cut M2 Wave 71)
- Executed hard-cut namespace wave for `RefreshBalanceAction`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/RefreshBalanceAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.RefreshBalanceAction` to `com.abs.casino.actions.api.RefreshBalanceAction`.
  - updated Struts action mapping type to `com.abs` for `/refreshbalance`.
  - added explicit `RefreshBalanceForm` import and replaced direct protected field access with getter.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-155237-hardcut-m2-wave71-refresh-balance-action/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2133` remaining (`2277` baseline, `144` reduced).

## 2026-02-26 16:06 UTC (Hard-Cut M2 Wave 72)
- Executed hard-cut namespace wave for `GetLeaderboardUrlsAction`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetLeaderboardUrlsAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.GetLeaderboardUrlsAction` to `com.abs.casino.actions.api.GetLeaderboardUrlsAction`.
  - updated Struts action mapping type to `com.abs` for `/getLeaderboardUrls`.
  - added explicit `GetLeaderboardUrlsForm` import for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-160139-hardcut-m2-wave72-get-leaderboard-urls-action/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2132` remaining (`2277` baseline, `145` reduced).

## 2026-02-26 16:12 UTC (Hard-Cut M2 Wave 73)
- Executed hard-cut namespace wave for `GetBalanceAction`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetBalanceAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.GetBalanceAction` to `com.abs.casino.actions.api.GetBalanceAction`.
  - updated Struts action mapping type to `com.abs` for `/get_balance`.
  - added explicit `GetBalanceForm` import for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-160747-hardcut-m2-wave73-get-balance-action/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2131` remaining (`2277` baseline, `146` reduced).

## 2026-02-26 16:20 UTC (Hard-Cut M2 Wave 74)
- Executed hard-cut namespace wave for `GetBalanceForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetBalanceForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetBalanceAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.GetBalanceForm` to `com.abs.casino.actions.api.GetBalanceForm`.
  - updated Struts form-bean type to `com.abs` for `GetBalanceForm`.
  - removed legacy FQCN import from `GetBalanceAction`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-161441-hardcut-m2-wave74-get-balance-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2130` remaining (`2277` baseline, `147` reduced).

## 2026-02-26 16:26 UTC (Hard-Cut M2 Wave 75)
- Executed hard-cut namespace wave for `GetLeaderboardUrlsForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetLeaderboardUrlsForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/GetLeaderboardUrlsAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.GetLeaderboardUrlsForm` to `com.abs.casino.actions.api.GetLeaderboardUrlsForm`.
  - updated Struts form-bean type to `com.abs` for `GetLeaderboardUrlsForm`.
  - removed legacy FQCN import from `GetLeaderboardUrlsAction`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-162212-hardcut-m2-wave75-get-leaderboard-urls-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2129` remaining (`2277` baseline, `148` reduced).

## 2026-02-26 16:31 UTC (Hard-Cut M2 Wave 76)
- Executed hard-cut namespace wave for `RefreshBalanceForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/RefreshBalanceForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/RefreshBalanceAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.RefreshBalanceForm` to `com.abs.casino.actions.api.RefreshBalanceForm`.
  - updated Struts form-bean type to `com.abs` for `RefreshBalanceForm`.
  - removed legacy FQCN import from `RefreshBalanceAction`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-162735-hardcut-m2-wave76-refresh-balance-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations now `2128` remaining (`2277` baseline, `149` reduced).

## 2026-02-26 16:39 UTC (Hard-Cut M2 Wave 77)
- Executed hard-cut namespace wave for `GetLeaderboardsForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardsForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardsAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.mq.GetLeaderboardsForm` to `com.abs.casino.actions.api.mq.GetLeaderboardsForm`.
  - updated Struts form-bean type to `com.abs` for `GetLeaderboardsForm`.
  - added explicit `com.abs...GetLeaderboardsForm` import in `GetLeaderboardsAction` for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-163541-hardcut-m2-wave77-get-leaderboards-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2127` remaining (`2277` baseline, `150` reduced).

## 2026-02-26 16:47 UTC (Hard-Cut M2 Wave 78)
- Executed hard-cut namespace wave for `GetLeaderboardResultsForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardResultsForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardResultsAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.mq.GetLeaderboardResultsForm` to `com.abs.casino.actions.api.mq.GetLeaderboardResultsForm`.
  - updated Struts form-bean type to `com.abs` for `GetLeaderboardResultsForm`.
  - added explicit `com.abs...GetLeaderboardResultsForm` import in `GetLeaderboardResultsAction` for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-164235-hardcut-m2-wave78-get-leaderboard-results-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2126` remaining (`2277` baseline, `151` reduced).

## 2026-02-26 16:51 UTC (Hard-Cut M2 Wave 79)
- Executed hard-cut namespace wave for `GetLeaderboardResultsAction`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardResultsAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.mq.GetLeaderboardResultsAction` to `com.abs.casino.actions.api.mq.GetLeaderboardResultsAction`.
  - updated Struts action type to `com.abs` for `/mq/getLeaderboardResults`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-164837-hardcut-m2-wave79-get-leaderboard-results-action/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2125` remaining (`2277` baseline, `152` reduced).

## 2026-02-26 16:57 UTC (Hard-Cut M2 Wave 80)
- Executed hard-cut namespace wave for `GetLeaderboardsAction`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/mq/GetLeaderboardsAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.mq.GetLeaderboardsAction` to `com.abs.casino.actions.api.mq.GetLeaderboardsAction`.
  - updated Struts action type to `com.abs` for `/mq/getLeaderboards`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-165456-hardcut-m2-wave80-get-leaderboards-action/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2124` remaining (`2277` baseline, `153` reduced).

## 2026-02-26 17:03 UTC (Hard-Cut M2 Wave 81)
- Executed hard-cut namespace wave for `ErrorCodes` (promo API).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/ErrorCodes.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoAction.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.promo.ErrorCodes` to `com.abs.casino.actions.api.promo.ErrorCodes`.
  - added explicit `com.abs...ErrorCodes` import in `GetTournamentPlayerInfoAction` for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-170055-hardcut-m2-wave81-promo-error-codes/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2123` remaining (`2277` baseline, `154` reduced).

## 2026-02-26 17:09 UTC (Hard-Cut M2 Wave 82)
- Executed hard-cut namespace wave for `GetTournamentPlayerInfoForm` (promo API).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.promo.GetTournamentPlayerInfoForm` to `com.abs.casino.actions.api.promo.GetTournamentPlayerInfoForm`.
  - updated Struts form-bean type to `com.abs` for `GetTournamentPlayerInfoForm`.
  - added explicit `com.abs...GetTournamentPlayerInfoForm` import in `GetTournamentPlayerInfoAction` for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-170702-hardcut-m2-wave82-promo-get-tournament-player-info-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2122` remaining (`2277` baseline, `155` reduced).

## 2026-02-26 17:15 UTC (Hard-Cut M2 Wave 83)
- Executed hard-cut namespace wave for `GetTournamentPlayerInfoAction` (promo API).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.promo.GetTournamentPlayerInfoAction` to `com.abs.casino.actions.api.promo.GetTournamentPlayerInfoAction`.
  - updated Struts action type to `com.abs` for `/getTournamentPlayerInfo`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-171229-hardcut-m2-wave83-promo-get-tournament-player-info-action/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2121` remaining (`2277` baseline, `156` reduced).

## 2026-02-26 17:20 UTC (Hard-Cut M2 Wave 84)
- Executed hard-cut namespace wave for `GetVBAForm` (VBA API).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/vba/GetVBAForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/vba/GetVBAAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.vba.GetVBAForm` to `com.abs.casino.actions.api.vba.GetVBAForm`.
  - updated Struts form-bean type to `com.abs` for `GetVBAForm`.
  - added explicit `com.abs...GetVBAForm` import in `GetVBAAction` for cross-package compatibility.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-171713-hardcut-m2-wave84-vba-getvba-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations now `2120` remaining (`2277` baseline, `157` reduced).

## 2026-02-26 17:24 UTC (Hard-Cut M2 Wave 85)
- Executed hard-cut namespace wave for `GetVBAAction` (VBA API).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/vba/GetVBAAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.vba.GetVBAAction` to `com.abs.casino.actions.api.vba.GetVBAAction`.
  - updated Struts action type to `com.abs` for `/getvba`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-172248-hardcut-m2-wave85-vba-getvba-action/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2119` remaining (`2277` baseline, `158` reduced).

## 2026-02-26 17:31 UTC (Hard-Cut M2 Wave 86)
- Executed hard-cut namespace wave for `UpperCaseNameCoder` (API response serializer helper).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/UpperCaseNameCoder.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.response.UpperCaseNameCoder` to `com.abs.casino.actions.api.response.UpperCaseNameCoder`.
  - added explicit `com.abs...UpperCaseNameCoder` import bridge in `APIResponseBuilder`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-173016-hardcut-m2-wave86-upper-case-name-coder/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2119` remaining (`2277` baseline, `158` reduced).

## 2026-02-26 17:36 UTC (Hard-Cut M2 Wave 87)
- Executed hard-cut namespace wave for `ErrorResponse` (API response model).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/ErrorResponse.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.response.ErrorResponse` to `com.abs.casino.actions.api.response.ErrorResponse`.
  - added explicit `com.abs...ErrorResponse` import bridge in `APIResponseBuilder`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-173552-hardcut-m2-wave87-error-response/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations now `2118` remaining (`2277` baseline, `159` reduced).

## 2026-02-26 17:56 UTC (Hard-Cut M2 Wave 88)
- Executed hard-cut namespace wave for `SuccessResponse` (API response model).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/SuccessResponse.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.response.SuccessResponse` to `com.abs.casino.actions.api.response.SuccessResponse`.
  - added explicit `com.abs...SuccessResponse` import bridge in `APIResponseBuilder`.
  - fixed constructor visibility (`protected` -> `public`) after compile gate failure in initial run.
- Validation:
  - initial matrix run failed at `07-web-gs-package` due constructor access mismatch after package split.
  - rerun after visibility fix passed full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - failed attempt: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-175344-hardcut-m2-wave88-success-response/`
  - final passing run: `docs/projects/02-runtime-renaming-refactor/evidence/20260226-175510-hardcut-m2-wave88-success-response-rerun/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations/files now `2117` remaining (`2277` baseline, `160` reduced).

## 2026-02-26 18:00 UTC (Hard-Cut M2 Wave 89)
- Executed hard-cut namespace wave for `Response` base class (API response hierarchy).
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/Response.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/ErrorResponse.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/SuccessResponse.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.response.Response` to `com.abs.casino.actions.api.response.Response`.
  - updated dependent imports to `com.abs...Response` in `APIResponseBuilder`, `ErrorResponse`, and `SuccessResponse`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-175902-hardcut-m2-wave89-response-base/`
- Outcome:
  - wave scope legacy refs reduced from `3` to `0`, `com.abs` refs now `4`.
  - global tracked source declarations/files now `2116` remaining (`2277` baseline, `161` reduced).

## 2026-02-26 18:05 UTC (Hard-Cut M2 Wave 90)
- Executed hard-cut namespace wave for `APIResponseBuilder`.
- Changed file:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.response.APIResponseBuilder` to `com.abs.casino.actions.api.response.APIResponseBuilder`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-180231-hardcut-m2-wave90-api-response-builder/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `1`.
  - global tracked source declarations/files now `2115` remaining (`2277` baseline, `162` reduced).

## 2026-02-26 18:10 UTC (Hard-Cut M2 Wave 91)
- Executed hard-cut namespace batch for `actions/api/bonus/response` package family.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/BaseBonus.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/Bonus.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/FRBonus.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/response/JSONResponse.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/bonus/AbstractBonusAction.java`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.bonus.response` package declarations to `com.abs` for four response classes.
  - updated dependent import in `AbstractBonusAction` to `com.abs...JSONResponse`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-180634-hardcut-m2-wave91-bonus-response-batch/`
- Outcome:
  - wave scope legacy refs reduced from `6` to `0`, `com.abs` refs now `6`.
  - global tracked source declarations/files now `2111` remaining (`2277` baseline, `166` reduced).

## 2026-02-26 18:13 UTC (Hard-Cut M2 Wave 92)
- Executed hard-cut namespace wave for VBA history actions.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/history/vba/HistoryByRoundAction.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/history/vba/HistoryByTokenAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated both action package declarations to `com.abs`.
  - updated Struts action mappings to `com.abs` class names for `/vabs/historyByRound` and `/vabs/historyByToken`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-181059-hardcut-m2-wave92-history-vba-actions/`
- Outcome:
  - wave scope legacy refs reduced from `4` to `0`, `com.abs` refs now `4`.
  - global tracked source declarations/files now `2109` remaining (`2277` baseline, `168` reduced).

## 2026-02-26 18:17 UTC (Hard-Cut M2 Wave 93)
- Executed hard-cut namespace wave for `CancelFRBForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.frbonus.CancelFRBForm` to `com.abs`.
  - updated dependent import in `CancelFRBAction`.
  - rewired Struts `FRBCancelForm` form-bean type to `com.abs`.
- Validation PASS:
  - full 9-step matrix (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-persisters`, `cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-181509-hardcut-m2-wave93-frbonus-cancel-form/`
- Outcome:
  - wave scope legacy refs reduced from `2` to `0`, `com.abs` refs now `3`.
  - global tracked source declarations/files now `2108` remaining (`2277` baseline, `169` reduced).

## 2026-02-26 18:27 UTC (Hard-Cut M2 Wave 94)
- Executed hard-cut namespace wave for `CancelFRBLiteForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBLiteForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CancelFRBLiteAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.frbonus.CancelFRBLiteForm` package to `com.abs`.
  - updated dependent import in `CancelFRBLiteAction`.
  - rewired Struts `CancelFRBLiteForm` form-bean type to `com.abs`.
- Validation:
  - initial failed attempt (wrong reactor paths): `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182126-hardcut-m2-wave94-frbonus-cancel-lite-form/`
  - second failed attempt (missing cluster.properties for game-server modules): `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182240-hardcut-m2-wave94-frbonus-cancel-lite-form-rerun/`
  - partial attempt (7/9): `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182406-hardcut-m2-wave94-frbonus-cancel-lite-form-rerun2/`
  - final passing run (9/9): `docs/projects/02-runtime-renaming-refactor/evidence/20260226-182613-hardcut-m2-wave94-frbonus-cancel-lite-form-rerun3/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations/files now `2107` remaining (`2277` baseline, `170` reduced).

## 2026-02-26 18:33 UTC (Hard-Cut M2 Wave 95)
- Executed hard-cut namespace wave for `AwardFRBForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.frbonus.AwardFRBForm` package to `com.abs`.
  - updated dependent import in `AwardFRBAction`.
  - rewired Struts `FRBAwardForm` form-bean type to `com.abs`.
- Validation PASS:
  - full 9-step matrix (common, common-wallet, sb-utils, promo/persisters, common-persisters, cache, web-gs, mp core/persistance, refactor smoke).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-183115-hardcut-m2-wave95-frbonus-award-form/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations/files now `2106` remaining (`2277` baseline, `171` reduced).

## 2026-02-26 18:38 UTC (Hard-Cut M2 Wave 96)
- Executed hard-cut namespace wave for `AwardFRBLiteForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBLiteForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBLiteAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.frbonus.AwardFRBLiteForm` package to `com.abs`.
  - updated dependent import in `AwardFRBLiteAction`.
  - rewired Struts `FRBAwardLiteForm` form-bean type to `com.abs`.
- Validation PASS:
  - full 9-step matrix (common, common-wallet, sb-utils, promo/persisters, common-persisters, cache, web-gs, mp core/persistance, refactor smoke).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-183626-hardcut-m2-wave96-frbonus-award-lite-form/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations/files now `2105` remaining (`2277` baseline, `172` reduced).

## 2026-02-26 18:43 UTC (Hard-Cut M2 Wave 97)
- Executed hard-cut namespace wave for `CheckFRBForm`.
- Changed files:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CheckFRBForm.java`
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/CheckFRBAction.java`
  - `gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- Change detail:
  - migrated `com.dgphoenix.casino.actions.api.frbonus.CheckFRBForm` package to `com.abs`.
  - updated dependent import in `CheckFRBAction`.
  - rewired Struts `FRBCheckForm` form-bean type to `com.abs`.
- Validation PASS:
  - full 9-step matrix (common, common-wallet, sb-utils, promo/persisters, common-persisters, cache, web-gs, mp core/persistance, refactor smoke).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-184111-hardcut-m2-wave97-frbonus-check-form/`
- Outcome:
  - wave scope legacy refs reduced from `1` to `0`, `com.abs` refs now `2`.
  - global tracked source declarations/files now `2104` remaining (`2277` baseline, `173` reduced).

## 2026-02-26 19:37 UTC (Hard-Cut M2 Wave 98A + 98B + 99)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W98A`: support/tool + diagnosis declarations migrated to `com.abs` (17 classes).
  - `W98B`: bonus/frbonus form declarations migrated to `com.abs` (10 forms), plus dependent action import rewires and Struts form-bean rewires.
  - `W99`: integration rewire of diagnosis servlet FQCNs in `WEB-INF/web.xml`.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-193037-hardcut-m2-wave98ab-wave99-parallel-batches/target-files.txt`.
- Validation:
  - fast gate initial failure due missing `${cluster.properties}` in shell context; rerun with `-Dcluster.properties=local/local-machine.properties` passed (`web-gs package` + `refactor smoke`).
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-193037-hardcut-m2-wave98ab-wave99-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/110-hard-cut-m2-wave98ab-wave99-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `27 -> 0` legacy declarations, `27` `com.abs` declarations.
  - global tracked source declarations/files now `2077` remaining (`2277` baseline, `200` reduced).

## 2026-02-26 19:54 UTC (Hard-Cut M2 Wave 100A + 100B + 101)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W100A`: migrated 15 bonus/frbonus API action declarations to `com.abs` and rewired corresponding Struts action `type` mappings.
  - `W100B`: migrated 12 routing/request declarations to `com.abs` with bounded dependent import rewires.
  - `W101`: integrated both batches and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-195111-hardcut-m2-wave100ab-wave101-parallel-batches/target-files.txt`.
- Validation PASS:
  - fast gate (`web-gs package`, `refactor smoke`) passed.
  - full 9-step matrix passed (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-195111-hardcut-m2-wave100ab-wave101-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/111-hard-cut-m2-wave100ab-wave101-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `27 -> 0` legacy declarations, `27` `com.abs` declarations.
  - global tracked source declarations/files now `2050` remaining (`2277` baseline, `227` reduced).

## 2026-02-26 20:12 UTC (Hard-Cut M2 Wave 102A + 102B + 103)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W102A`: migrated 13 FRB transport/MQB response declarations to `com.abs`.
  - `W102B`: migrated 12 low-risk entity/lobby/web/cache/error declarations to `com.abs`.
  - `W103`: integrated both batches, applied compatibility correction, and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-200827-hardcut-m2-wave102ab-wave103-parallel-batches/target-files.txt`.
- Validation:
  - fast gate initial failure in `web-gs package` due game-history generic/type drift after migration; compatibility fix applied and rerun passed.
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-200827-hardcut-m2-wave102ab-wave103-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/112-hard-cut-m2-wave102ab-wave103-parallel-batches-report-20260226.md`
- Outcome:
  - integration-scope declarations moved to `com.abs`: `24` net (1 reverted for compatibility).
  - global tracked source declarations/files now `2026` remaining (`2277` baseline, `251` reduced).

## 2026-02-26 20:29 UTC (Hard-Cut M2 Wave 104A + 104B + 105)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W104A`: migrated 10 low-risk form/API declaration packages to `com.abs`.
  - `W104B`: migrated 10 low-risk support/cache/web declaration packages to `com.abs`, plus bounded rewires in `log4j2.xml` and `support/getSessionError.jsp`.
  - `W105`: integrated both batches and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-202349-hardcut-m2-wave104ab-wave105-parallel-batches/target-files.txt`.
- Validation:
  - fast gate initial run failed due command-path issues (reactor root + smoke command path); rerun with corrected commands passed.
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-202349-hardcut-m2-wave104ab-wave105-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/113-hard-cut-m2-wave104ab-wave105-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `20 -> 0` legacy declarations, `20` `com.abs` declarations.
  - global tracked source declarations/files now `2006` remaining (`2277` baseline, `271` reduced).

## 2026-02-26 20:44 UTC (Hard-Cut M2 Wave 106A + 106B + 107)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W106A`: migrated 11 low-risk controller/config declaration packages to `com.abs`.
  - `W106B`: migrated 19 low-risk support/tool declaration packages to `com.abs` plus bounded rewires in `WEB-INF/struts-config.xml` and support JSPs.
  - `W107`: integrated both batches and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-203744-hardcut-m2-wave106ab-wave107-parallel-batches/target-files.txt`.
- Validation PASS:
  - fast gate (`web-gs package`, `refactor smoke`) passed.
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-203744-hardcut-m2-wave106ab-wave107-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/114-hard-cut-m2-wave106ab-wave107-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `30 -> 0` legacy declarations, `30` `com.abs` declarations.
  - global tracked source declarations/files now `1976` remaining (`2277` baseline, `301` reduced).

## 2026-02-26 21:10 UTC (Hard-Cut M2 Wave 108A + 108B + 109, stabilized)
- Executed batched-safe parallel hard-cut migration proposal:
  - `W108A`: migrated 20 support/cache declaration packages to `com.abs`.
  - `W108B`: attempted 12 declaration migrations, then reverted for compatibility due repeated compile drift.
  - `W109`: integrated stabilized set and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-205749-hardcut-m2-wave108ab-wave109-parallel-batches/target-files.txt`.
- Validation:
  - fast gate required multiple bounded fixes and then passed on rerun6 (`web-gs package`, `refactor smoke`).
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-205749-hardcut-m2-wave108ab-wave109-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/115-hard-cut-m2-wave108ab-wave109-stabilized-report-20260226.md`
- Outcome:
  - final stabilized declaration migrations: `20` net (`W108A` retained, `W108B` reverted).
  - global tracked source declarations/files now `1956` remaining (`2277` baseline, `321` reduced).

## 2026-02-26 21:36 UTC (Hard-Cut M2 Wave 110A + 110B + 111)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W110A`: migrated 10 low-risk web servlet declaration packages to `com.abs`.
  - `W110B`: migrated 11 low-risk support/cache form declaration packages to `com.abs` with bounded rewires in support actions + `WEB-INF/struts-config.xml` + one JSP.
  - `W111`: integrated both batches and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-212148-hardcut-m2-wave110ab-wave111-parallel-batches/target-files.txt`.
- Validation PASS:
  - fast gate (`web-gs package`, `refactor smoke`) passed.
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-212148-hardcut-m2-wave110ab-wave111-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/116-hard-cut-m2-wave110ab-wave111-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `21 -> 0` legacy declarations, `21` `com.abs` declarations.
  - global tracked source declarations/files now `1935` remaining (`2277` baseline, `342` reduced).

## 2026-02-26 21:56 UTC (Hard-Cut M2 Wave 112A + 112B + 113)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W112A`: migrated 12 start-game/login/processors declaration packages to `com.abs` with bounded import/FQCN rewires in request/form/action files.
  - `W112B`: migrated 18 support/cache action declaration packages to `com.abs` with bounded rewires in `DomainNameAction` and `WEB-INF/struts-config.xml`.
  - `W113`: integrated both batches and validated.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-214021-hardcut-m2-wave112ab-wave113-parallel-batches/target-files.txt`.
- Validation PASS:
  - fast gate (`web-gs package`, `refactor smoke`) passed.
  - full 9-step matrix passed `9/9` (`common`, `common-wallet`, `sb-utils`, `promo/persisters`, `common-gs`, `cassandra-cache`, `web-gs`, `mp core/persistance`, `refactor smoke`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-214021-hardcut-m2-wave112ab-wave113-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/117-hard-cut-m2-wave112ab-wave113-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `30`.
  - global tracked source declarations/files now `1904` remaining (`2277` baseline, `373` reduced).

## 2026-02-26 22:18 UTC (Hard-Cut M2 Wave 114A + 114B + 115, stabilized)
- Executed batched-safe parallel hard-cut migration proposal:
  - `W114A`: migrated 11 history/protection declaration packages to `com.abs` with bounded rewires in history support and JSP/Struts paths.
  - `W114B`: attempted 10 login/config declaration migrations, then reverted to `HEAD` for compatibility after repeated fast-gate compile drift.
  - `W115`: integrated stabilized set and validated.
- Validation:
  - fast gate passed on rerun4 after bounded compatibility stabilization.
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-215554-hardcut-m2-wave114ab-wave115-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/118-hard-cut-m2-wave114ab-wave115-stabilized-report-20260226.md`
- Outcome:
  - final retained declaration migrations: `11` net (A retained, B reverted).
  - global tracked source declarations/files now `1893` remaining (`2277` baseline, `384` reduced).

## 2026-02-26 22:43 UTC (Hard-Cut M2 Wave 116A + 116B + 117)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W116A`: migrated 10 lobby/tournament/battleground declaration packages to `com.abs` with bounded JSP rewires.
  - `W116B`: migrated 10 game-start declaration packages to `com.abs` with bounded rewires in 4 owned files.
  - `W117`: applied shared integration rewires in `WEB-INF/struts-config.xml` and validated.
- Validation:
  - fast gate initial package run failed on missing `CommonBonusStartGameForm` import in `CommonFRBStartGameForm`; bounded fix applied.
  - fast gate rerun2 passed; full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-221842-hardcut-m2-wave116ab-wave117-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/119-hard-cut-m2-wave116ab-wave117-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `20`.
  - global tracked source declarations/files now `1873` remaining (`2277` baseline, `404` reduced).

## 2026-02-26 22:45 UTC (Hard-Cut M2 Wave 118A + 119, stabilized)
- Executed batched-safe hard-cut cycle from in-progress W118 checkpoint:
  - `W118A`: migrated 10 enter/start-game declaration packages to `com.abs`.
  - `W118B`: not retained after explorer risk check (login/helper overlap cluster).
  - `W119`: integrated retained A-only rewires and validated.
- Validation:
  - fast gate initial package run failed on missing base-class imports in `CommonBonusStartGameForm` and `CWStartGameBySessionForm`; bounded import fixes applied.
  - fast gate rerun2 passed; full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-223528-hardcut-m2-wave118ab-wave119-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/120-hard-cut-m2-wave118a-wave119-stabilized-report-20260226.md`
- Outcome:
  - net retained declaration migrations: `10`.
  - global tracked source declarations/files now `1863` remaining (`2277` baseline, `414` reduced).

## 2026-02-26 23:05 UTC (Hard-Cut M2 Wave 120A + 120B + 121)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W120A`: migrated 10 enter/config/login declaration packages to `com.abs`.
  - `W120B`: migrated 10 login/helper/session declaration packages to `com.abs`.
  - `W121`: integrated both batches and validated.
- Validation:
  - fast gate initial + rerun2 + rerun3 package compile failures during cross-batch import/type stabilization.
  - fast gate rerun4 passed; full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-225312-hardcut-m2-wave120ab-wave121-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/121-hard-cut-m2-wave120ab-wave121-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `20`.
  - global tracked source declarations/files now `1843` remaining (`2277` baseline, `434` reduced).

## 2026-02-26 23:50 UTC (Hard-Cut M2 Wave 122A + 122B + 123)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W122A`: migrated 12 DTO/message/service declaration packages in `common-gs` to `com.abs`.
  - `W122B`: migrated 10 configuration/initializer declaration packages in `common-gs` to `com.abs`.
  - `W123`: integrated with bounded rewires in `web-gs` and compatibility-safe type alignment in battleground/Kafka service flow.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-231233-hardcut-m2-wave122ab-wave123-parallel-batches/target-files.txt`.
- Validation:
  - fast gate required iterative stabilization; final rerun10 passed (`common-gs install`, `web-gs package`, `refactor smoke`).
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-231233-hardcut-m2-wave122ab-wave123-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/122-hard-cut-m2-wave122ab-wave123-parallel-batches-report-20260226.md`
- Outcome:
  - scoped declaration migrations: `22`.
  - global tracked source declarations/files now `1821` remaining (`2277` baseline, `456` reduced).

## 2026-02-27 00:08 UTC (Hard-Cut M2 Wave 124A + 124B + 125)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W124A`: migrated 10 `common-gs` action/form declaration packages to `com.abs`.
  - `W124B`: migrated 11 `common-wallet` protocol/client declaration packages to `com.abs`.
  - `W125`: integrated both batches with bounded rewires and compatibility stabilization.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260226-235810-hardcut-m2-wave124ab-wave125-parallel-batches/target-files.txt`.
- Validation:
  - fast gate rerun1 failed on bounded type/import drift (`IStartGameForm` generic bounds, `AbstractWalletProtocolManager` visibility).
  - applied bounded stabilization, fast gate rerun2 passed.
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260226-235810-hardcut-m2-wave124ab-wave125-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/123-hard-cut-m2-wave124ab-wave125-parallel-batches-report-20260227.md`
- Outcome:
  - scoped declaration migrations: `21`.
  - global tracked source declarations/files now `1800` remaining (`2277` baseline, `477` reduced).

## 2026-02-27 00:24 UTC (Hard-Cut M2 Wave 126A + 126B + 127)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W126A`: migrated 16 `common-gs` inservice Kafka handler declaration packages to `com.abs`.
  - `W126B`: migrated 12 `common-gs` API XML request/response declaration packages to `com.abs`.
  - `W127`: integrated both batches with bounded importer rewires.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260227-001501-hardcut-m2-wave126ab-wave127-parallel-batches/target-files.txt`.
- Validation:
  - fast gate rerun1 failed on JSP import/type drift in `tools/api/service.jsp`.
  - applied bounded JSP import fix, fast gate rerun2 passed.
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-001501-hardcut-m2-wave126ab-wave127-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/124-hard-cut-m2-wave126ab-wave127-parallel-batches-report-20260227.md`
- Outcome:
  - scoped declaration migrations: `28`.
  - global tracked source declarations/files now `1772` remaining (`2277` baseline, `505` reduced).

## 2026-02-27 00:48 UTC (Hard-Cut M2 Wave 128A + 128B + 129)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W128A`: migrated 11 `support/archiver` declaration packages to `com.abs` and rewired launch scripts.
  - `W128B`: migrated 14 `common-promo` tournament-feed declaration packages to `com.abs` with bounded importer rewires.
  - `W129`: integrated both batches and stabilized dependency-order validation for touched promo/common-gs path.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260227-003110-hardcut-m2-wave128ab-wave129-parallel-batches/target-files.txt`.
- Validation:
  - initial fast-gate attempts exposed dependency-order/type drift during mixed artifact state.
  - applied bounded stabilization (`common-promo` + `promo/persisters` pre-install), fast gate rerun4 passed `5/5`.
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-003110-hardcut-m2-wave128ab-wave129-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/125-hard-cut-m2-wave128ab-wave129-parallel-batches-report-20260227.md`
- Outcome:
  - scoped declaration migrations: `25`.
  - global tracked source declarations/files now `1747` remaining (`2277` baseline, `530` reduced).

## 2026-02-27 01:11 UTC (Hard-Cut M2 Wave 130A + 130B + 131)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W130A`: migrated 17 `mp-server/kafka/dto/privateroom` declaration packages to `com.abs`.
  - `W130B`: migrated 12 `mp-server/kafka/dto/bots` declaration packages to `com.abs`.
  - `W131`: integrated both batches with bounded importer stabilization in MP web handler path.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260227-005510-hardcut-m2-wave130ab-wave131-parallel-batches/target-files.txt`.
- Validation:
  - fast gate rerun1/2 failed on unresolved privateroom DTO imports and reactor-order artifact issues.
  - applied bounded integration stabilization (migrated `KafkaMultiPlayerResponseService` DTO imports; reverted non-applicable `BGOStatusUtil` rewire), fast gate rerun4 passed `4/4`.
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-005510-hardcut-m2-wave130ab-wave131-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/126-hard-cut-m2-wave130ab-wave131-parallel-batches-report-20260227.md`
- Outcome:
  - scoped declaration migrations: `29`.
  - global tracked source declarations/files now `1719` remaining (`2277` baseline, `558` reduced).

## 2026-02-27 01:26 UTC (Hard-Cut M2 Wave 132A + 132B + 133)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W132A`: migrated 12 `cassandra.persist.mp` declaration packages to `com.abs`.
  - `W132B`: migrated 14 `sb-utils/common.util.test.api` declaration packages to `com.abs`.
  - `W133`: integrated both batches with bounded rewires in owned importer/test files.
- Changed files:
  - full file list in `docs/projects/02-runtime-renaming-refactor/evidence/20260227-011942-hardcut-m2-wave132ab-wave133-parallel-batches/target-files.txt`.
- Validation:
  - fast gate passed on rerun1 (`5/5`).
  - full 9-step matrix passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-011942-hardcut-m2-wave132ab-wave133-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/127-hard-cut-m2-wave132ab-wave133-parallel-batches-report-20260227.md`
- Outcome:
  - scoped declaration migrations: `31` net.
  - global tracked source declarations/files now `1688` remaining (`2277` baseline, `589` reduced).

### 2026-02-27 02:01 UTC
- Continued Project 02 hard-cut runtime renaming with parallel batch waves `134A/134B` and integration wave `135`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-013115-hardcut-m2-wave134ab-wave135-parallel-batches/validation-status.txt`
  - `docs/projects/02-runtime-renaming-refactor/128-hard-cut-m2-wave134ab-wave135-parallel-batches-report-20260227.md`
- Result:
  - migrated `20` declaration packages to `com.abs` across `sb-utils/common.util.xml.parser` and `promo/events/process`, with bounded importer rewires and compatibility-safe `HistoryInformerManager` alignment.
  - fast gate passed on rerun4; full validation matrix passed `9/9` on rerun4.
  - updated tracked declarations/files: `1668` remaining (`2277` baseline, `609` reduced, `26.745718%` burndown).
- Next:
  - continue next non-overlapping batched cycle under the same evidence-first protocol.

## 2026-02-27 02:54 UTC (Hard-Cut M2 Wave 136A + 136B + 137, Stabilized)
- Continued batched-safe hard-cut migration from W135 checkpoint with non-overlapping ownership:
  - Planned `W136A`: `websocket/tournaments/handlers` + `sb-utils/common/socket` declarations.
  - Planned `W136B`: `promo/messages/handlers` + `transactiondata/storeddataprocessor` declarations.
  - Integration `W137`: bounded importer rewires.
- Stabilization and retention:
  - Deferred `sb-utils/common/socket` declaration slice for runtime compatibility.
  - Retained stable declaration migrations in:
    - `common-gs/websocket/tournaments/handlers` (`8`)
    - `common-gs/promo/messages/handlers` (`8`)
    - `common-gs/transactiondata/storeddataprocessor` (`9`)
  - Retained bounded rewires (`4`) in:
    - `GameServerComponentsConfiguration.java`
    - `GameCommandsProcessorsConfiguration.java`
    - `WebSocketSessionsController.java`
    - `TournamentMessageHandlersFactory.java`
- Validation:
  - `rerun1`: fail (compile + smoke)
  - `rerun2`: compile pass, smoke fail
  - `rerun3`: steps `1..8` pass, smoke fail (runtime OOM instability)
  - `rerun4`: fast gate `5/5 PASS`; full matrix `9/9 PASS`
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-021132-hardcut-m2-wave136ab-wave137-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/129-hard-cut-m2-wave136ab-wave137-parallel-batches-report-20260227.md`
- Outcome:
  - net retained declaration migrations: `25`
  - global tracked source declarations/files now `1642` remaining (`2277` baseline, `635` reduced, `27.888450%` burndown)

## 2026-02-27 03:06 UTC (Hard-Cut M2 Wave 138A + 138B + 139, Stabilized)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W138A`: 15 declaration migrations in `common-gs/kafka/dto/privateroom/{request,response}`.
  - `W138B`: planned 12 declaration migrations in `sb-utils/common/vault` + `sb-utils/common.util.xml.xstreampool`.
  - `W139`: bounded integration rewires.
- Stabilization:
  - `rerun1` failed in `common-gs` due unresolved `com.abs...xstreampool` under current dependency order.
  - deferred `W138B` for compatibility safety.
  - retained `W138A` + 3 bounded rewires (`KafkaRequestMultiPlayer`, `BattlegroundService`, `BGOStatusUtil`).
- Validation:
  - `rerun2` fast gate PASS `5/5`.
  - `rerun2` full matrix PASS `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-030611-hardcut-m2-wave138ab-wave139-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/130-hard-cut-m2-wave138ab-wave139-stabilized-report-20260227.md`
- Outcome:
  - net retained declaration migrations: `15`
  - global tracked source declarations/files now `1627` remaining (`2277` baseline, `650` reduced, `28.546333%` burndown)

## 2026-02-27 03:35 UTC (Hard-Cut M2 Wave 140A + 140B + 141, Stabilized)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W140A`: planned 14 declaration migrations in `common-promo/messages/{client/requests,server/notifications/prizes,server/responses}`.
  - `W140B`: planned 14 declaration migrations in `sb-utils/src/test` scope.
  - `W141`: integration/stabilization and authoritative validation.
- Stabilization:
  - `rerun1` failed at `sb-utils` test compile due B-scope package compatibility drift.
  - deferred `W140B` for compatibility safety (`stabilization-batchB-restore-list.txt`).
  - `rerun2` failed at `common-gs` due non-parity command flags (tests executed; arm64 LZ4 native mismatch).
  - aligned gate parity (`-DskipTests` where required), `rerun3` passed.
- Validation:
  - fast gate `5/5 PASS` (rerun3).
  - full matrix `9/9 PASS` (rerun3).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-032629-hardcut-m2-wave140ab-wave141-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/131-hard-cut-m2-wave140ab-wave141-stabilized-report-20260227.md`
- Outcome:
  - net retained declaration migrations: `14` (`W140A` retained, `W140B` deferred).
  - global tracked source declarations/files now `1613` remaining (`2277` baseline, `664` reduced, `29.161177%` burndown).

## 2026-02-27 04:04 UTC (Hard-Cut M2 Wave 142A + 142B + 143)
- Executed batched-safe parallel hard-cut migration with non-overlapping ownership:
  - `W142A`: migrated 16 declaration packages in `common/client/canex/request/{friends,onlineplayer}`.
  - `W142B`: migrated 10 declaration packages in `common/client/canex/request/onlinerooms` + `common/transactiondata/storeddate/identifier`.
  - `W143`: integrated both batches with bounded verification (no additional rewires retained).
- Validation:
  - fast gate passed on rerun1 (`5/5`).
  - full 9-step matrix passed on rerun1 (`9/9`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-040102-hardcut-m2-wave142ab-wave143-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/132-hard-cut-m2-wave142ab-wave143-parallel-batches-report-20260227.md`
- Outcome:
  - scoped declaration migrations retained: `26`.
  - global tracked source declarations/files now `1587` remaining (`2277` baseline, `690` reduced, `30.303030%` burndown).

## 2026-02-27 04:21 UTC (Hard-Cut M2 Wave 144A + 144B + 145, Stabilized)
- Continued batched-safe hard-cut migration from W143 checkpoint with non-overlapping ownership:
  - `W144A`: migrated 13 declaration packages in `common-gs/promo/tournaments/messages`.
  - `W144B`: migrated 15 declaration packages in `common-gs/promo/tournaments/messages` + `common-gs/battleground/messages`.
  - `W145`: integration rewires and compatibility stabilization in `common-gs` importers.
- Stabilization:
  - `rerun1..2` failed on stale tournament import references.
  - bounded import rewires applied for moved tournament/battleground declarations.
  - `rerun3..5` exposed and resolved type-identity drift in:
    - `AccountManager` (`PlayerGameSettingsType`)
    - `PaymentManager` (`PaymentMeanType`, `PaymentMeanId`)
  - aligned `common-gs` validation command with explicit `-Dcluster.properties=common.properties`.
- Validation:
  - fast gate `5/5 PASS` on rerun6.
  - full matrix `9/9 PASS` on rerun1.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-041057-hardcut-m2-wave144ab-wave145-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/133-hard-cut-m2-wave144ab-wave145-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `28`.
  - retained bounded rewires: `27`.
  - global tracked source declarations/files now `1559` remaining (`2277` baseline, `718` reduced, `31.532718%` burndown).

## 2026-02-27 04:40 UTC (Hard-Cut M2 Wave 146A + 146B + 147, Stabilized)
- Continued batched-safe hard-cut migration from W145 checkpoint with non-overlapping ownership:
  - `W146A`: migrated 20 declaration packages in `sb-utils/common/mp`.
  - `W146B`: migrated 18 declaration packages in `sb-utils/common/util/xml` + `common-gs` xml test scope.
  - `W147`: bounded importer rewires and compatibility stabilization.
- Parallel execution mode:
  - explorer produced non-overlapping batch sets.
  - worker thread-cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
- Stabilization:
  - `rerun1` failed (`common-gs` compile) due stale dependency-order artifact.
  - `rerun2` failed (`common-gs`) on mixed `MQData` type identity.
  - added `common-persisters` install before `common-gs` in fast gate for this wave.
  - full matrix `rerun1` failed at `step02` (`common-wallet`) exposing latent package bridge drift.
  - applied bounded bridge fix in `CanexCWClient` to explicitly extend migrated `com.abs` v4 REST client class.
- Validation:
  - fast gate `5/5 PASS` on rerun3.
  - full matrix `9/9 PASS` on rerun2.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-043019-hardcut-m2-wave146ab-wave147-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/134-hard-cut-m2-wave146ab-wave147-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `38`.
  - retained bounded rewires: `36`.
  - global tracked source declarations/files now `1521` remaining (`2277` baseline, `756` reduced, `33.201581%` burndown).

## 2026-02-27 05:15 UTC (Hard-Cut M2 Wave 148A + 148B + 149, Stabilized)
- Continued batched-safe hard-cut migration from W147 checkpoint with non-overlapping ownership:
  - `W148A`: migrated 10 declaration packages in `sb-utils/common/util/string/mappers` + `sb-utils/common/util/xml/xstreampool`.
  - `W148B`: migrated 10 declaration packages in `sb-utils/common/vault` + `common/util/hardware/data`.
  - `W149`: bounded importer rewires and validation stabilization.
- Parallel execution mode:
  - explorer produced non-overlapping batch sets.
  - worker thread-cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
- Stabilization:
  - fast gate `rerun1` failed at `common-gs` because migrated `hardware.data` declarations were not yet installed from `common`.
  - fast gate `rerun2` aligned dependency order by adding `common` install pre-step.
  - full matrix `rerun1` failed at `step08` due module-POM mp-server invocation (missing reactor deps).
  - full matrix `rerun2` corrected to root reactor invocation (`mp-server/pom.xml -pl persistance -am`).
- Validation:
  - fast gate `6/6 PASS` on rerun2.
  - full matrix `9/9 PASS` on rerun2.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-044917-hardcut-m2-wave148ab-wave149-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/135-hard-cut-m2-wave148ab-wave149-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `6`.
  - global tracked source declarations/files now `1501` remaining (`2277` baseline, `776` reduced, `34.079930%` burndown).

## 2026-02-27 05:25 UTC (Hard-Cut M2 Wave 150A + 150B + 151, Stabilized)
- Continued batched-safe hard-cut migration from W149 checkpoint with non-overlapping ownership:
  - `W150A`: migrated 11 declaration packages in `sb-utils/common/util/support` + `utils/common/util/system`.
  - `W150B`: migrated 10 declaration packages in `common/client/canex/request/privateroom` + `common-promo/messages/server/notifications/tournament`.
  - `W151`: bounded importer rewires and compatibility stabilization.
- Parallel execution mode:
  - explorer produced non-overlapping batch sets.
  - worker thread-cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
- Stabilization:
  - fast gate `rerun1` failed at step1 (`common`) due stale `IJsonCWClient` import lineage.
  - fast gate `rerun2` failed at step1 (`common`) due dependency-order drift (`common` before migrated `sb-utils` support declarations).
  - fast gate `rerun3` failed at step4 (`common-wallet`) due `CanexCWClient` type-identity mismatch against `IJsonCWClient`.
  - fast gate `rerun4` passed after bounded import/signature alignment and corrected step ordering.
- Validation:
  - fast gate `9/9 PASS` on rerun4.
  - full matrix `9/9 PASS` on rerun1 (with wave-specific pre-setup installs for `utils`, `sb-utils`, `common-promo`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-050718-hardcut-m2-wave150ab-wave151-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/136-hard-cut-m2-wave150ab-wave151-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `21`.
  - retained bounded rewires: `26`.
  - global tracked source declarations/files now `1480` remaining (`2277` baseline, `797` reduced, `35.002196%` burndown).

## 2026-02-27 05:37 UTC (Hard-Cut M2 Wave 152A + 152B + 153, Stabilized)
- Continued batched-safe hard-cut migration from W151 checkpoint with non-overlapping ownership:
  - `W152A`: migrated 20 declaration packages in `sb-utils/common/util/web` + `utils/common/util/web`.
  - `W152B`: migrated 18 declaration packages in `promo/persisters`.
  - `W153`: bounded importer rewires and compatibility stabilization.
- Parallel execution mode:
  - explorer produced non-overlapping declaration batches with 3 rewire overlaps.
  - worker-thread cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
- Stabilization:
  - fast gate `rerun1` failed at `step8` (`common-gs`) due mixed canex request DTO lineage in `MQServiceHandler`.
  - aligned canex request imports and FQCN status types to `com.abs` lineage in `MQServiceHandler`.
  - fast gate `rerun2` passed (`10/10`).
- Validation:
  - full matrix `9/9 PASS` on rerun1 (with pre-setup installs for `utils`, `sb-utils`, `common-promo`, `promo-core`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-052810-hardcut-m2-wave152ab-wave153-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/137-hard-cut-m2-wave152ab-wave153-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `38`.
  - retained bounded rewires: `53`.
  - global tracked source declarations/files now `1442` remaining (`2277` baseline, `835` reduced, `36.671058%` burndown).

## 2026-02-27 05:47 UTC (Hard-Cut M2 Wave 154A + 154B + 155, Stabilized)
- Continued batched-safe hard-cut migration from W153 checkpoint with non-overlapping ownership:
  - `W154A`: migrated 17 declaration packages in `common/socket` + `filters`.
  - `W154B`: migrated 17 declaration packages in `common/util/property` + `gs/managers/payment/bonus/tracker`.
  - `W155`: bounded importer rewires and compatibility validation.
- Parallel execution mode:
  - explorer verified no declaration or rewire overlap.
  - worker-thread cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
- Validation:
  - fast gate `8/8 PASS` on rerun1.
  - full matrix `9/9 PASS` on rerun1.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-054024-hardcut-m2-wave154ab-wave155-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/138-hard-cut-m2-wave154ab-wave155-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `34`.
  - retained bounded rewires: `24`.
  - global tracked source declarations/files now `1407` remaining (`2277` baseline, `870` reduced, `38.208169%` burndown).

## 2026-02-27 05:57 UTC (Hard-Cut M2 Wave 156A + 156B + 157, Stabilized)
- Continued batched-safe hard-cut migration from W155 checkpoint with non-overlapping ownership:
  - `W156A`: migrated 10 declaration packages in `configuration` + `promo/wins/handlers`.
  - `W156B`: migrated 11 declaration packages in `bonus` + `gs/managers/payment/transfer/processor`.
  - `W157`: bounded importer rewires and validation stabilization.
- Parallel execution mode:
  - explorer verified no declaration or rewire overlap.
  - worker-thread cap limited concurrent workers; retained degraded-safe parallel mode (worker B + main-owned batch A), with strict file ownership maintained.
- Validation:
  - fast gate `8/8 PASS` on rerun1.
  - full matrix `9/9 PASS` on rerun1.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-055309-hardcut-m2-wave156ab-wave157-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/139-hard-cut-m2-wave156ab-wave157-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `21`.
  - retained bounded rewires: `6`.
  - global tracked source declarations/files now `1386` remaining (`2277` baseline, `891` reduced, `39.130435%` burndown).

## 2026-02-27 06:20 UTC (Hard-Cut M2 Wave 158A + 158B + 159, Stabilized)
- Continued batched-safe hard-cut migration from W157 checkpoint with non-overlapping ownership:
  - `W158A`: migrated 12 declaration packages in `gs.maintenance`, `gs.maintenance.converters`, and `gs.managers.payment.wallet.common.xml`.
  - `W158B`: migrated 10 declaration packages in `common.promo.ai` and `gs.managers.payment.bonus.client.frb`.
  - `W159`: bounded importer rewires and compatibility stabilization.
- Parallel execution mode:
  - explorer verified no declaration or rewire overlap.
  - worker-thread cap limited concurrent workers; retained degraded-safe parallel mode (worker A + main-owned batch B), with strict file ownership maintained.
- Stabilization:
  - fast gate initial run failed at `step5 common-persisters` due dependency order after `common.promo.ai` migration.
  - fast gate `rerun2` added `common-promo` install pre-step and passed `9/9`.
- Validation:
  - full matrix `9/9 PASS` on rerun1.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-061319-hardcut-m2-wave158ab-wave159-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/140-hard-cut-m2-wave158ab-wave159-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `22`.
  - retained bounded rewires: `9`.
  - global tracked source declarations/files now `1364` remaining (`2277` baseline, `913` reduced, `40.096618%` burndown).

## 2026-02-27 06:42 UTC (Hard-Cut M2 Wave 160A + 160B + 161, Stabilized)
- Continued batched-safe hard-cut migration from W159 checkpoint with non-overlapping ownership:
  - `W160A`: migrated 10 declaration packages in `gs.api`, `gs.external.operation`, `gs.managers.game.socket`, `gs.managers.payment.wallet.common.remote`, `gs.managers.payment.wallet.common.stub`, `gs.managers.payment.wallet.processor`, `services.transfer`.
  - `W160B`: migrated 10 declaration packages in `common.promo.icon`, `common.feeds`, `common.mail`, `common.string`, `common.web.jackpot`, `gs.certificates`, `slottest.utils`.
  - `W161`: no external Java rewires retained.
- Stabilization:
  - corrected explorer-provided path mismatches in batch B before final edit pass.
  - fast gate passed `9/9`.
  - full matrix rerun2 passed `9/9` after correcting step04 module path (`gs-server/promo/persisters`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-062841-hardcut-m2-wave160ab-wave161-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/141-hard-cut-m2-wave160ab-wave161-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `1343` remaining (`2277` baseline, `934` reduced, `41.018884%` burndown).

## 2026-02-27 07:20 UTC (Hard-Cut M2 Wave 162A + 162B + 163, Stabilized)
- Continued batched-safe hard-cut migration from W161 checkpoint with non-overlapping ownership:
  - `W162A`: migrated 10 declaration packages in `cassandra.inject` tests, `payment.wallet.commonwalletmanger` tests, `controller.mqb` tests, and `common.util.compress` tests.
  - `W162B`: migrated 10 declaration packages in `gs.singlegames.tools.cbservtools.autofinish`, `gs.managers.freegame` tests, `controller.frbonus` tests, `gs.managers.game.favorite` tests, and `util` classes.
  - `W163`: bounded importer rewires in `ats/BotConfigInfo`, `gs/socket/mq/BattlegroundService`, and `services/mp/MPBotConfigInfoService`.
- Parallel execution mode:
  - explorer produced non-overlapping low-risk batches.
  - worker A completed batch A (10/10), worker B completed batch B + rewires (13/13), no overlap.
- Stabilization:
  - fast gate initial/rerun2 failed at `common-gs` due mixed `friends.Status` lineage after utility package migration.
  - fixed compatibility mapping by restoring `com.dgphoenix...friends.Status` in `BGFStatusUtil`.
  - fast gate rerun3/rerun4 then failed at `web-gs` test compile due mixed service/model import lineage in `GameUserHistoryInfoControllerTest`.
  - aligned test imports to current migrated package boundaries; targeted `web-gs` precheck rerun5 passed.
  - fast gate rerun6 passed `9/9` and promoted to canonical evidence.
- Validation:
  - full matrix `9/9 PASS` on rerun1 (with pre-setup installs for `utils`, `sb-utils`, `common-promo`) and canonical promotion.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-064930-hardcut-m2-wave162ab-wave163-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/142-hard-cut-m2-wave162ab-wave163-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `3`.
  - global tracked source declarations/files now `1324` remaining (`2277` baseline, `953` reduced, `41.853316%` burndown).

## 2026-02-27 07:35 UTC (Hard-Cut M2 Wave 164A + 164B + 165, Stabilized)
- Continued batched-safe hard-cut migration from W163 checkpoint with non-overlapping ownership:
  - `W164A`: migrated 12 declaration packages in `cassandra` test scopes.
  - `W164B`: initially migrated 12 declaration packages in `sb-utils` test scopes.
  - `W165`: bounded stabilization and safe-scope retention.
- Parallel execution mode:
  - explorer produced two non-overlapping low-risk batches with one bounded rewire.
  - worker owned Batch A; main owned Batch B due thread-cap fallback.
- Stabilization:
  - fast gate rerun1 failed at `sb-utils` install (`testCompile`) due broad same-package symbol-resolution breaks from Batch B package-boundary migration.
  - rolled back Batch B to `HEAD` for safety and retained Batch A only.
  - fast gate rerun2 passed `9/9`.
- Validation:
  - full matrix `9/9 PASS` on rerun1 (with pre-setup installs for `utils`, `sb-utils`, `common-promo`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-071748-hardcut-m2-wave164ab-wave165-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/143-hard-cut-m2-wave164ab-wave165-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `12`.
  - retained bounded rewires: `1`.
  - global tracked source declarations/files now `1312` remaining (`2277` baseline, `965` reduced, `42.380325%` burndown).

## 2026-02-27 07:52 UTC (Hard-Cut M2 Wave 166A + 166B + 167, Stabilized)
- Continued batched-safe hard-cut migration from W165 checkpoint with non-overlapping ownership:
  - `W166A`: migrated 10 declaration packages in `cassandra-cache/cache` keyspace/configuration scope.
  - `W166B`: initially migrated 10 declaration packages in `cassandra-cache/cache` factory/locking/persist scope with broad rewires.
  - `W167`: bounded stabilization and safe-scope retention.
- Parallel execution mode:
  - explorer produced two non-overlapping declaration batches with explicit rewire lists.
  - worker owned Batch A; main owned Batch B due thread-cap fallback.
- Stabilization:
  - fast gate rerun1 failed at `common-persisters` install due unresolved-symbol cascade in `CassandraTransactionDataPersister` after Batch B + cross-boundary rewires.
  - rolled back main-owned Batch B and overlap rewires.
  - retained safe subset: Batch A declarations + shared `PersistersFactory` declaration + `IKeyspaceManager` bounded rewire.
  - fast gate rerun2 passed `9/9`.
- Validation:
  - full matrix `9/9 PASS` on rerun1 (with pre-setup installs for `utils`, `sb-utils`, `common-promo`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-073734-hardcut-m2-wave166ab-wave167-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/144-hard-cut-m2-wave166ab-wave167-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `12`.
  - retained bounded rewires: `1`.
  - global tracked source declarations/files now `1300` remaining (`2277` baseline, `977` reduced, `42.907334%` burndown).

## 2026-02-27 08:17 UTC (Hard-Cut M2 Wave 168A + 168B + 169, Stabilized)
- Continued batched-safe hard-cut migration from W167 checkpoint with explorer split and worker ownership:
  - `W168A`: planned 5 declarations with bounded cross-module rewires.
  - `W168B`: planned 5 declarations with 1 bounded rewire.
  - `W169`: stabilization and safe-scope retention.
- Parallel execution mode:
  - explorer produced disjoint A/B batches.
  - worker completed Group A while main handled Group B (thread-cap fallback prevented second worker spawn).
- Stabilization:
  - fast gate rerun1 failed at `common-persisters` due unresolved-symbol cascade from broad Group A rewires.
  - rolled back unsafe edits and retained only low-risk subset: `ICallInfo`, `NtpTimeGenerator`.
  - fast gate rerun2 passed `9/9`.
- Validation:
  - full matrix rerun1 failed at step08 due incorrect module path (`gs-server/mp-server/...`).
  - full matrix rerun2 passed `9/9` after correcting step08 to `mp-server/persistance/pom.xml`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-080044-hardcut-m2-wave168ab-wave169-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/145-hard-cut-m2-wave168ab-wave169-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `2`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `1298` remaining (`2277` baseline, `979` reduced, `42.995169%` burndown).

## 2026-02-27 08:38 UTC (Hard-Cut M2 Wave 170A + 170B + 171, Stabilized)
- Continued batched-safe hard-cut migration from W169 checkpoint with explorer split and worker ownership:
  - `W170A`: planned 2 declarations with bounded rewires.
  - `W170B`: planned 2 declarations with bounded rewires.
  - `W171`: stabilization and safe-scope retention.
- Parallel execution mode:
  - explorer produced safer split and fallback.
  - worker completed Group A while main handled Group B (thread-cap fallback prevented second worker spawn).
- Stabilization:
  - fast gate rerun1 failed at `common-persisters` due unresolved `com.abs.casino.cassandra.IEntityUpdateListener` in compile path.
  - rolled back broad A/B edits.
  - retained safe cache-internal subset: `ColumnIteratorCallback`, `FakeNotAppliedResultSet`, plus bounded import rewire in `AbstractCassandraPersister`.
  - fast gate rerun2 passed `9/9`.
- Validation:
  - full matrix rerun1 passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-082639-hardcut-m2-wave170ab-wave171-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/146-hard-cut-m2-wave170ab-wave171-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `2`.
  - retained bounded rewires: `1`.
  - global tracked source declarations/files now `1296` remaining (`2277` baseline, `981` reduced, `43.083004%` burndown).

## 2026-02-27 08:53 UTC (Hard-Cut M2 Wave 172A + 172B + 173)
- Continued batched-safe hard-cut migration from W171 checkpoint with declaration-only split:
  - `W172A`: `IConfigsInitializer`, `CassandraRemoteCallPersister`.
  - `W172B`: `PersisterDependencyInjector`, `AbstractLockManager`.
  - `W173`: integration and validation.
- Parallel execution mode:
  - worker completed Batch A; main completed Batch B due thread-cap fallback.
- Stabilization:
  - no rollback required.
  - no cross-module rewires in this wave.
- Validation:
  - fast gate rerun1 passed `9/9`.
  - full matrix rerun1 passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-084517-hardcut-m2-wave172ab-wave173-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/147-hard-cut-m2-wave172ab-wave173-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `4`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `1292` remaining (`2277` baseline, `985` reduced, `43.258674%` burndown).

## 2026-02-27 09:07 UTC (Hard-Cut M2 Wave 174A + 174B + 175)
- Continued batched-safe hard-cut migration from W173 checkpoint with declaration-only split:
  - `W174A`: `DistributedLockManager`, `IRemoteUnlocker`.
  - `W174B`: `Session`, `IEntityUpdateListener`.
  - `W175`: integration and validation.
- Parallel execution mode:
  - worker completed Batch A; main completed Batch B due thread-cap fallback.
- Stabilization:
  - no source rollback required.
  - fast-gate rerun1 failed only on smoke-tooling script lookup; rerun2 passed with explicit smoke command.
- Validation:
  - fast gate rerun2 passed `9/9`.
  - full matrix rerun1 passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-085908-hardcut-m2-wave174ab-wave175-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/148-hard-cut-m2-wave174ab-wave175-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `4`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `1288` remaining (`2277` baseline, `989` reduced, `43.434343%` burndown).

## 2026-02-27 09:24 UTC (Hard-Cut M2 Wave 176A + 176B + 177)
- Continued batched-safe hard-cut migration from W175 checkpoint with declaration-only split:
  - `W176A`: `Compression`, `Caching`.
  - `W176B`: `CompactionStrategy`, `ICassandraPersister`.
  - `W177`: integration and validation.
- Parallel execution mode:
  - worker completed Batch A; main completed Batch B due thread-cap fallback.
- Stabilization:
  - no source rollback required.
  - full-matrix rerun1 failed at step08 due incorrect runner path (`gs-server/mp-server/persistance`), not source regressions.
  - rerun2 passed after using corrected path (`/mp-server/persistance`).
- Validation:
  - fast gate rerun1 passed `9/9`.
  - full matrix rerun2 passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-091037-hardcut-m2-wave176ab-wave177-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/149-hard-cut-m2-wave176ab-wave177-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `4`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `1284` remaining (`2277` baseline, `993` reduced, `43.610013%` burndown).

## 2026-02-27 09:41 UTC (Hard-Cut M2 Wave 178A + 178B + 179)
- Continued batched-safe hard-cut migration from W177 checkpoint using non-overlapping parallel ownership:
  - `W178A`: 7 declaration migrations in `cbservtools` command processors.
  - `W178B`: 6 declaration migrations in `tracker`.
  - `W179`: integration and validation.
- Parallel execution mode:
  - explorer selected low-fanout batches.
  - thread-cap fallback forced `1 worker + main`, but ownership remained non-overlapping.
- Stabilization:
  - no rollback required.
  - bounded importer rewires applied in `common-gs` dependents.
  - test package alignment applied for `CurrencyUpdateProcessorTest` to keep `testCompile` compatibility.
- Validation:
  - fast gate rerun1 passed `9/9`.
  - full matrix rerun1 passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-093205-hardcut-m2-wave178ab-wave179-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/150-hard-cut-m2-wave178ab-wave179-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `13`.
  - retained bounded rewires: `10`.
  - global tracked source declarations/files now `1271` remaining (`2277` baseline, `1006` reduced, `44.180940%` burndown).

## 2026-02-27 09:55 UTC (Hard-Cut M2 Wave 180A + 180B + 181)
- Continued batched-safe hard-cut migration from W179 checkpoint with low-fanout common-gs split:
  - `W180A`: 10 declaration migrations in `gs.managers.dblink`.
  - `W180B`: 7 declaration migrations in `gs.singlegames.tools.util`.
  - `W181`: integration and validation.
- Parallel execution mode:
  - explorer produced A/B split and defer list.
  - thread-cap fallback enforced `1 worker + main` while keeping non-overlapping ownership.
- Stabilization:
  - no rollback required.
  - importer fanout exceeded initial estimate; bounded rewire scope expanded to all direct Java importers of migrated packages.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1 passed `9/9`.
  - full matrix rerun1 passed `9/9`.
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-094623-hardcut-m2-wave180ab-wave181-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/151-hard-cut-m2-wave180ab-wave181-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `17`.
  - retained bounded rewires: `52`.
  - global tracked source declarations/files now `1254` remaining (`2277` baseline, `1023` reduced, `44.927536%` burndown).

## 2026-02-27 10:41 UTC (Hard-Cut M2 Wave 182A + 182B + 183)
- Continued batched-safe hard-cut migration from W181 checkpoint with low-fanout `common-gs` split:
  - `W182A`: 5 declaration migrations in `gs.biz` + `leaderboard`.
  - `W182B`: 5 declaration migrations in `promo.feed` + `promo.feed.tournament`.
  - `W183`: integration and validation.
- Parallel execution mode:
  - explorer produced non-overlapping A/B split and bounded importer map.
  - worker handled Batch A while main handled Batch B due thread-cap fallback.
- Stabilization:
  - no rollback required.
  - bounded rewires retained in direct importer files only.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun3: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun2: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-101129-hardcut-m2-wave182ab-wave183-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/152-hard-cut-m2-wave182ab-wave183-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `10`.
  - retained bounded rewires: `5`.
  - global tracked source declarations/files now `1244` remaining (`2277` baseline, `1033` reduced, `45.366711%` burndown).

## 2026-02-27 11:07 UTC (Hard-Cut M2 Wave 184A + 184B + 185)
- Continued batched-safe hard-cut migration from W183 checkpoint with non-overlapping cache/tournament split:
  - `W184A`: 6 declaration migrations in `cache`.
  - `W184B`: 5 declaration migrations in `promo.tournaments`.
  - `W185`: integration and validation.
- Parallel execution mode:
  - explorer selected low-fanout non-overlapping split.
  - thread-cap fallback enforced `1 worker + main` while keeping strict file ownership.
- Stabilization:
  - no rollback required.
  - bounded rewire set expanded after compile surfaced additional tournament static-import dependencies.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun4: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-104909-hardcut-m2-wave184ab-wave185-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/153-hard-cut-m2-wave184ab-wave185-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `11`.
  - retained bounded rewires: `21`.
  - global tracked source declarations/files now `1233` remaining (`2277` baseline, `1044` reduced, `45.849802%` burndown).

## 2026-02-27 11:24 UTC (Hard-Cut M2 Wave 186A + 186B + 187)
- Continued batched-safe hard-cut migration from W185 checkpoint with non-overlapping session/bonus-client split:
  - `W186A`: 5 declaration migrations in `gs.managers.game.session`.
  - `W186B`: 6 declaration migrations in `gs.managers.payment.bonus.client`.
  - `W187`: integration and validation.
- Parallel execution mode:
  - explorer selected low-fanout non-overlapping batches.
  - thread-cap fallback enforced `1 worker + main` while preserving strict file ownership.
- Stabilization:
  - no rollback required.
  - bounded rewires included direct Java importers and bounded BankInfoCache XML class-string rewires.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-111434-hardcut-m2-wave186ab-wave187-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/154-hard-cut-m2-wave186ab-wave187-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `11`.
  - retained bounded rewires: `19`.
  - global tracked source declarations/files now `1222` remaining (`2277` baseline, `1055` reduced, `46.332894%` burndown).

## 2026-02-27 11:41 UTC (Hard-Cut M2 Wave 188A + 188B + 189)
- Continued batched-safe hard-cut migration from W187 checkpoint with non-overlapping login/init split:
  - `W188A`: 7 declaration migrations in `sm.login`.
  - `W188B`: 6 declaration migrations in `init`.
  - `W189`: integration and validation.
- Parallel execution mode:
  - explorer selected non-overlapping low-risk batches.
  - thread-cap fallback enforced `1 worker + main` while preserving strict file ownership.
- Stabilization:
  - no rollback required.
  - bounded rewires retained in direct Java importer files.
  - bounded non-Java rewires retained in `web-gs` (`web.xml`, JSP, `log4j2.xml`) for `com.abs.casino.init.*` class references.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-113123-hardcut-m2-wave188ab-wave189-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/155-hard-cut-m2-wave188ab-wave189-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `13`.
  - retained bounded rewires: `34`.
  - global tracked source declarations/files now `1209` remaining (`2277` baseline, `1068` reduced, `46.903821%` burndown).

## 2026-02-27 12:04 UTC (Hard-Cut M2 Wave 190A + 190B + 191)
- Continued batched-safe hard-cut migration from W189 checkpoint with non-overlapping settings/engine-room split:
  - `W190A`: 7 declaration migrations in `gs.managers.game.settings` (+ direct tests).
  - `W190B`: 8 declaration migrations in `gs.managers.game.engine`, `gs.managers.game.event`, `gs.managers.game.room`.
  - `W191`: integration and validation.
- Parallel execution mode:
  - explorer prepared non-overlapping low-risk batches.
  - thread-cap fallback enforced `1 worker + main` while preserving strict file ownership.
- Stabilization:
  - no rollback required.
  - corrected one explorer path mismatch (`IGameEventProcessor` is in `common-gs`) before validation.
  - bounded rewires retained in direct Java importers plus one bounded JSP import rewire.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-115053-hardcut-m2-wave190ab-wave191-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/156-hard-cut-m2-wave190ab-wave191-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `15`.
  - retained bounded rewires: `9`.
  - global tracked source declarations/files now `1194` remaining (`2277` baseline, `1083` reduced, `47.562582%` burndown).

## 2026-02-27 12:26 UTC (Hard-Cut M2 Wave 192A + 192B + 193)
- Continued batched-safe hard-cut migration from W191 checkpoint with non-overlapping declaration sets:
  - `W192A`: 8 declaration migrations in `gamecombos` + `unj.api`.
  - `W192B`: 8 declaration migrations in `common.geoip` + `statistics`.
  - `W193`: integration and validation.
- Parallel execution mode:
  - explorer generated non-overlapping low-risk manifests.
  - agent thread-cap fallback executed as `1 worker + main` while preserving strict file ownership.
- Stabilization:
  - no rollback required.
  - bounded rewires retained in direct Java importer files plus one bounded `web.xml` class-string update for `RegistratorServlet`.
  - initial fast-gate ordering issue at `STEP01` resolved by prewarm rerun (`rerun2`).
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `STEP01 FAIL` (`rc=1`, cross-module compile ordering after package move).
  - fast gate rerun2: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-121109-hardcut-m2-wave192ab-wave193-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/157-hard-cut-m2-wave192ab-wave193-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `16`.
  - retained bounded rewires: `10`.
  - global tracked source declarations/files now `1178` remaining (`2277` baseline, `1099` reduced, `48.265262%` burndown).

## 2026-02-27 12:45 UTC (Hard-Cut M2 Wave 194A + 194B + 195)
- Continued batched-safe hard-cut migration from W193 checkpoint with non-overlapping declaration sets:
  - `W194A`: 4 declaration migrations in `common.client`.
  - `W194B`: 9 declaration migrations in `websocket.tournaments`.
  - `W195`: integration and validation.
- Parallel execution mode:
  - explorer produced non-overlapping low-risk manifests.
  - agent thread-cap fallback executed as `1 worker + main` while preserving strict ownership.
- Stabilization:
  - no rollback required.
  - bounded rewires retained in direct Java importer files plus bounded `web.xml` class-string update for `TournamentWebSocketServlet`.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-123332-hardcut-m2-wave194ab-wave195-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/158-hard-cut-m2-wave194ab-wave195-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `13`.
  - retained bounded rewires: `23`.
  - global tracked source declarations/files now `1165` remaining (`2277` baseline, `1112` reduced, `48.836188%` burndown).

## 2026-02-27 13:03 UTC (Hard-Cut M2 Wave 196A + 196B + 197)
- Continued batched-safe hard-cut migration from W195 checkpoint with non-overlapping declaration sets:
  - `W196A`: 7 declaration migrations in `websocket`.
  - `W196B`: 4 declaration migrations in `gs.managers.payment.wallet.v3`.
  - `W197`: integration and validation.
- Parallel execution mode:
  - explorer produced non-overlapping low-risk manifests.
  - agent thread-cap fallback executed as `1 worker + main` while preserving strict ownership.
- Stabilization:
  - no rollback required.
  - bounded rewires retained in direct Java importer files plus bounded `web.xml` class-string update for `WebSocketServletImpl`.
  - bounded Java FQCN extension rewire retained in wallet-v4 interface.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-125224-hardcut-m2-wave196ab-wave197-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/159-hard-cut-m2-wave196ab-wave197-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `11`.
  - retained bounded rewires: `34`.
  - global tracked source declarations/files now `1154` remaining (`2277` baseline, `1123` reduced, `49.319280%` burndown).

## 2026-02-27 14:04 UTC (Hard-Cut M2 Wave 198A + 198B + 199)
- Continued batched-safe hard-cut migration from W197 checkpoint with non-overlapping declaration sets:
  - `W198A`: 8 declaration migrations in `services*` and `transactiondata*`.
  - `W198B`: narrowed safe subset to 2 declarations (`UnsupportedCurrencyException`, `RESTServiceClient`) after lock-package rollback.
  - `W199`: integration and validation.
- Parallel execution mode:
  - explorer/worker/main mode used with thread-cap fallback while preserving strict ownership.
- Stabilization:
  - corrected mixed namespace compile drift in `MPBotConfigInfoService` wiring, `LoginService`, payment transfer `IPaymentProcessor` imports, promo message handler request/response imports, and `GameUserHistoryServiceTest` type bindings.
  - restored promo prize-notification package consistency in `common-promo` (kept `com.dgphoenix` for this cluster) to clear module compile failures.
  - updated `RESTServiceClient` and `APIServiceTest` XStream allowlist to `com.abs.casino.gs.api.service.xml.**`.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun8: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-131332-hardcut-m2-wave198ab-wave199-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/160-hard-cut-m2-wave198ab-wave199-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `10`.
  - retained bounded rewires: `8`.
  - global tracked source declarations/files now `1144` remaining (`2277` baseline, `1133` reduced, `49.758454%` burndown).

## 2026-02-27 14:58 UTC (Hard-Cut M2 Wave 200A + 200B + 201)
- Continued batched-safe hard-cut migration from W199 checkpoint with non-overlapping declaration sets:
  - `W200A`: 10 declaration migrations in `cbservtools.commands.processors*` and `commands.responses*`.
  - `W200B`: 10 declaration migrations in `common-promo.feed/network`, `configuration.observable`, `IJPWinQualifier`, `CountryRestrictionServiceTest`, and `log4j2specific` utilities.
  - `W201`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded importer/FQCN rewires only for moved symbols (`GameCommandsProcessorsConfiguration`, command processor imports, `DBLink`, `AbstractFeedWriter`, `GameServerServiceConfiguration`, `Configuration`, `FileObserveFactory`, `ReflectionUtilsCompatibilityTest`, `GameLogger`).
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias `HTTP 502`).
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-144036-hardcut-m2-wave200ab-wave201-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/161-hard-cut-m2-wave200ab-wave201-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `16`.
  - global tracked source declarations/files now `1124` remaining (`2277` baseline, `1153` reduced, `50.636803%` burndown).

## 2026-02-27 15:28 UTC (Hard-Cut M2 Wave 202A + 202B + 203)
- Continued batched-safe hard-cut migration from W201 checkpoint with non-overlapping declaration sets:
  - `W202A`: 10 declaration migrations in `common.promo.messages.server.notifications.prizes`, `common.web.diagnostic`, and `configuration.resource.event`.
  - `W202B`: 11 declaration migrations in `gs.managers.game.core/history`, `gs.status`, `system.configuration.identification`, and RNG test helpers.
  - `W203`: integration rewires and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded importer/FQCN rewires retained only for moved symbols from `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
  - added one bounded JSP import rewire in `web-gs/src/main/webapp/vabs/html5template.jspf` (`HistoryManager`) discovered during post-merge namespace scan.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
  - full matrix rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-150630-hardcut-m2-wave202ab-wave203-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/162-hard-cut-m2-wave202ab-wave203-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `21`.
  - retained bounded rewires: `33`.
  - global tracked source declarations/files now `1103` remaining (`2277` baseline, `1174` reduced, `51.559069%` burndown).

## 2026-02-27 15:52 UTC (Hard-Cut M2 Wave 204A + 204B + 205)
- Continued batched-safe hard-cut migration from W203 checkpoint with non-overlapping declaration sets:
  - `W204A`: 10 declaration migrations in `statistics.http` and `common.engine.tracker`.
  - `W204B`: 10 declaration migrations in promo notifications, stored-data identifiers, `bgm`, and upload callback/client surfaces.
  - `W205`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded importer/FQCN rewires retained only for moved symbols from `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
  - fast-gate rerun1 hit transient compile ordering at `STEP01`; rerun2 stabilized and passed `STEP01-08`.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once and failed).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-153111-hardcut-m2-wave204ab-wave205-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/163-hard-cut-m2-wave204ab-wave205-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `60`.
  - global tracked source declarations/files now `1083` remaining (`2277` baseline, `1194` reduced, `52.437418%` burndown).

## 2026-02-27 16:28 UTC (Hard-Cut M2 Wave 206A + 206B + 207)
- Continued batched-safe hard-cut migration from W205 checkpoint with non-overlapping declaration sets:
  - `W206A`: 16 declaration migrations in `common-gs` promo and `promo/core` surfaces.
  - `W206B`: 10 declaration migrations in `sb-utils` `common.configuration` and `common.engine`.
  - `W207`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded importer/FQCN rewires retained only for moved symbols from `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
  - corrected three wrong import rewires in `GameServerComponentsConfiguration` to actual declaration packages (`KafkaRequestMultiPlayer`, `TournamentMessageHandlersFactory`, `GameServerConfiguration`).
  - resolved `ConfigHelper` type mismatch in `CassandraPersistenceContextConfiguration` for `KeyspaceConfigurationFactory` constructor compatibility.
  - warm-installed `promo/persisters` and `promo/core` before canonical rerun.
  - corrected validation runner `STEP08` path to `mp-server/persistance`.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `STEP01 FAIL` (`rc=1`).
  - fast gate rerun2: `STEP06 FAIL` (`rc=1`).
  - fast gate rerun3: `STEP06 FAIL` (`rc=1`, `ConfigHelper` type mismatch).
  - fast gate rerun4: `STEP08 FAIL` (`rc=1`, runner path mismatch).
  - fast gate rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`).
  - full matrix rerun5 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`; recovery retry executed once and failed).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-155735-hardcut-m2-wave206ab-wave207-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/164-hard-cut-m2-wave206ab-wave207-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `26`.
  - retained bounded rewires: `31`.
  - global tracked source declarations/files now `1057` remaining (`2277` baseline, `1220` reduced, `53.579271%` burndown).

## 2026-02-27 16:58 UTC (Hard-Cut M2 Wave 208A + 208B + 209)
- Continued batched-safe hard-cut migration from W207 checkpoint with non-overlapping declaration sets:
  - `W208A`: 18 declaration migrations in `gs.singlegames.tools.cbservtools`.
  - `W208B`: 10 declaration migrations in battleground/lock/wallet-client tests/timeframe/tournament handlers and bonus mass/restriction surfaces.
  - `W209`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded importer/FQCN rewires retained only for moved symbols from `rewires-batchA-all.txt` and `rewires-batchB-all.txt`.
  - fixed `STEP06` compile drift by aligning `MassAwardBonusManager` imports to migrated restriction package.
  - fixed `STEP07` compile drift by aligning `BattlegroundControllerTest` battleground message imports and `support/getMassAwardRestrictions.jsp` `MassAwardRestriction` import.
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `STEP06 FAIL` (`rc=1`).
  - fast gate rerun2: `STEP07 FAIL` (`rc=1`).
  - fast gate rerun3: `STEP07 FAIL` (`rc=1`).
  - fast gate rerun4 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=1`, launch alias `HTTP 502`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=1`, launch alias `HTTP 502`; recovery retry executed once and failed).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-163500-hardcut-m2-wave208ab-wave209-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/165-hard-cut-m2-wave208ab-wave209-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `28`.
  - retained bounded rewires: `43`.
  - global tracked source declarations/files now `1029` remaining (`2277` baseline, `1248` reduced, `54.808959%` burndown).

## 2026-02-27 17:49 UTC (Hard-Cut M2 Wave 210A + 210B + 211)
- Continued batched-safe hard-cut migration from W209 checkpoint with non-overlapping declaration sets:
  - `W210A`: 11 declaration migrations in `sb-utils`/`utils` test surfaces.
  - `W210B`: 12 declaration migrations in `common-gs`/`common-wallet`/`common-promo`/`common-persisters` test surfaces.
  - `W211`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - planned bounded rewires remained empty (`rewires-batchA-all.txt` and `rewires-batchB-all.txt` both empty).
  - resolved post-cut test compile drift with minimal import/access fixes in migrated declarations (no non-owned file overlap).
  - no blind/global replacement performed.
- Validation:
  - fast gate rerun1: `STEP02 FAIL`.
  - fast gate rerun2: `STEP02 FAIL`.
  - fast gate rerun3: `STEP03/STEP04/STEP06 FAIL`.
  - fast gate rerun4 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`; recovery retry executed once and failed).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-170003-hardcut-m2-wave210ab-wave211-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/166-hard-cut-m2-wave210ab-wave211-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `23`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `1006` remaining (`2277` baseline, `1271` reduced, `55.819060%` burndown).

## 2026-02-27 21:30 UTC (Hard-Cut M2 Wave 212A + 212B + 213)
- Continued batched-safe hard-cut migration from W211 checkpoint with non-overlapping declaration sets:
  - `W212A`: 10 declaration migrations in `common` test surfaces.
  - `W212B`: 10 declaration migrations in `common`/`common-gs`/`rng` test surfaces plus low-fanout `sb-utils` declarations.
  - `W213`: bounded rewires and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded rewires retained to planned 8-file scope (`BaseDiagnosisServlet`, `ThreadsCheckTask`, `Configuration`, `ServerConfiguration`, `IFileObservable`, `PropertyObservable`, `WebToolsTest`, `EncodeUtilsTest`).
  - resolved post-cut compile drift with minimal import/access fixes inside migrated declaration tests (explicit legacy-type imports and protected-access helper in `AccountIdGeneratorTest`).
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP01 FAIL`.
  - fast gate batchB rerun1: `STEP01 FAIL`.
  - full matrix rerun1: `PRE01 FAIL`.
  - rerun2: prewarm stabilized, but `STEP01 FAIL` (test import/access drift).
  - rerun3: `STEP01 FAIL` (remaining test drift in `AccountIdGeneratorTest` / `StringIdGeneratorTest`).
  - rerun4: `STEP06 FAIL` (`ForbiddenGamesForBonusProviderTest` unresolved type).
  - rerun5 (canonical):
    - fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
    - fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
    - full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-210136-hardcut-m2-wave212ab-wave213-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/167-hard-cut-m2-wave212ab-wave213-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `8`.
  - global tracked source declarations/files now `986` remaining (`2277` baseline, `1291` reduced, `56.697409%` burndown).

## 2026-02-27 22:33 UTC (Hard-Cut M2 Wave 214A + 214B + 215)
- Continued batched-safe hard-cut migration from W213 checkpoint with non-overlapping declaration sets:
  - `W214A`: 10 declaration migrations across hardware, payment-tracker, kafka-config, logout-tracker, and remotecall interfaces/classes.
  - `W214B`: 10 declaration migrations across KPI/MQ/bet-persister/battleground/system/session/common-web/online-stat surfaces.
  - `W215`: bounded rewires and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - bounded rewires retained to planned 18+18 lists.
  - fixed early `STEP01` compile drift in `InvalidPathStrutsActionExceptionHandler` via explicit `BaseAction` import.
  - fixed `STEP06` mixed-type drift by aligning `BattlegroundConfig` type usage in `BattlegroundService`/`TournamentBuyInHelper` and `IRemoteCall` type usage in remote call command classes used by `RemoteCallHelper`.
  - rerun4 was rejected as non-canonical after runner used wrong `STEP07` path (`gs-api`); rerun5 re-executed with required `STEP07=web-gs` path.
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP01 FAIL`.
  - fast gate batchB rerun1: `STEP01 FAIL`.
  - full matrix rerun1: `PRE01-03 PASS`, `STEP01 FAIL`.
  - rerun2: `STEP06 FAIL`.
  - rerun3: `STEP06 FAIL`.
  - rerun4 (non-canonical path drift): `STEP07 FAIL` (`gs-api` path used by runner, discarded).
  - rerun5 (canonical):
    - fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
    - fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
    - full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-215545-hardcut-m2-wave214ab-wave215-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/168-hard-cut-m2-wave214ab-wave215-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `36`.
  - global tracked source declarations/files now `966` remaining (`2277` baseline, `1311` reduced, `57.575757%` burndown).

## 2026-02-27 23:25 UTC (Hard-Cut M2 Wave 216A + 216B + 217)
- Continued batched-safe hard-cut migration from W215 checkpoint with non-overlapping declaration sets:
  - `W216A`: 10 planned declaration migrations across `common/web`, `common-gs` persistance/kafka handler, and `sb-utils` utility surfaces.
  - `W216B`: 10 planned declaration migrations across `common/api`, `common-gs` kafka/bonus/sm, and `sb-utils` cache/currency/transport/statistics surfaces.
  - `W217`: bounded rewires + validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - initial fast-gate batchA exposed same-package coupling drift at `STEP03` and chained `common-gs` drift at `STEP06`.
  - fixed drift using minimal explicit imports/type-alignment in touched ownership zones (`IGameLogger`, `XmlWriter`, `ServerLockInfo`, `StatisticsManager`, `IPlayerSessionManager`, `IGetAccountInfoProvider`, `IGameServer`, `ILasthandPersister`, `NoneBetPersister`, `KafkaMessageService`).
  - one additional declaration migration retained as stabilization: `KafkaMessageService` (`com.dgphoenix` -> `com.abs`).
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP03 FAIL`.
  - fast gate batchA rerun2: `STEP03 FAIL`.
  - fast gate batchA rerun3-rerun9: `STEP06 FAIL`.
  - fast gate batchA rerun10 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-230214-hardcut-m2-wave216ab-wave217-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/169-hard-cut-m2-wave216ab-wave217-parallel-batches-report-20260227.md`
- Outcome:
  - planned declaration migrations retained: `20`.
  - additional stabilization declaration migration retained: `1`.
  - total retained declaration migrations: `21`.
  - global tracked source declarations/files now `945` remaining (`2277` baseline, `1332` reduced, `58.498024%` burndown).

## 2026-02-27 23:58 UTC (Hard-Cut M2 Wave 218A + 218B + 219)
- Continued batched-safe hard-cut migration from W217 checkpoint with non-overlapping declaration sets:
  - `W218A`: 10 declaration migrations across `common/cache`, `common/cache/data/game`, and `common/util` surfaces.
  - `W218B`: 10 declaration migrations across `sb-utils/common/cache` and `sb-utils/common/util` surfaces.
  - `W219`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - planned rewires remained empty for both batches (`rewires-batchA-all.txt` and `rewires-batchB-all.txt`).
  - fixed `STEP01` compile drift by aligning imports for moved declarations (`IDistributedConfigCache`, `ICreateGameListener`, `MiniGameInfo`, `GameLanguageHelper`, `RoundFinishedHelper`).
  - fixed `STEP03` compile drift by aligning legacy-package JSON interfaces to moved `com.abs` declarations (`JsonSelfSerializable`, `JsonDeserializableDeserializer`, `JsonDeserializableModule`).
  - rerun4 was rejected as non-canonical after runner path drift (`STEP04` wrong module path); rerun5 re-executed with canonical path map.
  - full-matrix rerun1 was rejected as non-canonical after PRE path drift (`PRE02` wrong module path); rerun2 re-executed with canonical PRE map.
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP01 FAIL`.
  - fast gate batchA rerun2: `STEP01 FAIL`.
  - fast gate batchA rerun3: `STEP03 FAIL`.
  - fast gate batchA rerun5 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-233414-hardcut-m2-wave218ab-wave219-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/170-hard-cut-m2-wave218ab-wave219-parallel-batches-report-20260227.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `925` remaining (`2277` baseline, `1352` reduced, `59.376373%` burndown).

## 2026-02-28 00:18 UTC (Hard-Cut M2 Wave 220A + 220B + 221)
- Continued batched-safe hard-cut migration from W219 checkpoint with non-overlapping declaration sets:
  - `W220A`: 10 declaration migrations in `game-server/common-gs/kafka/handler` request-handler surfaces.
  - `W220B`: 10 declaration migrations in adjacent `game-server/common-gs/kafka/handler` request-handler surfaces.
  - `W221`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - planned rewire manifests were empty for both batches.
  - fast gate batchA rerun1 failed at `STEP06` because moved handlers no longer shared package scope with legacy `KafkaOuterRequestHandler` (`com.dgphoenix`).
  - fixed drift with minimal explicit imports in moved handlers (`import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;`).
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP06 FAIL`.
  - fast gate batchA rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-235912-hardcut-m2-wave220ab-wave221-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/171-hard-cut-m2-wave220ab-wave221-parallel-batches-report-20260228.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `905` remaining (`2277` baseline, `1372` reduced, `60.255599%` burndown).

## 2026-02-28 00:44 UTC (Hard-Cut M2 Wave 222A + 222B + 223)
- Continued batched-safe hard-cut migration from W221 checkpoint with non-overlapping declaration sets:
  - `W222A`: 12 declaration migrations in `game-server/common-gs/kafka/handler`.
  - `W222B`: 12 declaration migrations in `common-promo`.
  - `W223`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - planned rewire manifests were empty for both batches.
  - fast gate batchA rerun1 failed at `STEP04` because moved `common-promo` declarations no longer shared package scope with legacy promo declarations.
  - fixed drift with minimal explicit imports in moved promo declarations (`import com.dgphoenix.casino.common.promo.*;`).
  - proactively applied explicit imports in moved handlers (`import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;`) to preserve prior proven compatibility pattern.
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`, prior uncommitted evidence folder) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP04 FAIL`.
  - fast gate batchA rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-004400-hardcut-m2-wave222ab-wave223-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/172-hard-cut-m2-wave222ab-wave223-parallel-batches-report-20260228.md`
- Outcome:
  - retained declaration migrations: `24`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `881` remaining (`2277` baseline, `1396` reduced, `61.308740%` burndown).

## 2026-02-28 01:06 UTC (Hard-Cut M2 Wave 224A + 224B + 225)
- Continued batched-safe hard-cut migration from W223 checkpoint with non-overlapping declaration sets:
  - `W224A`: 10 declaration migrations in `game-server/common-gs` handler/dto/exception/task surfaces.
  - `W224B`: 10 declaration migrations in `game-server/common-gs/kafka/dto` and 10 bounded rewires in `mp-server/web` kafka handlers.
  - `W225`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - fast gate batchA rerun1 and rerun2 failed at `STEP06` due moved declarations losing same-package visibility to legacy types.
  - fixed with minimal import-only compatibility in moved declarations (`KafkaOuterRequestHandler`, `KafkaInServiceRequestHandler`, `AbstractSendAlertException`, `KafkaRequest`, `BGPlayerDto`, `BotConfigInfoDto`).
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`, prior uncommitted evidence folder) outside commit scope.
- Validation:
  - fast gate batchA rerun1: `STEP06 FAIL`.
  - fast gate batchA rerun2: `STEP06 FAIL`.
  - fast gate batchA rerun3 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-010600-hardcut-m2-wave224ab-wave225-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/173-hard-cut-m2-wave224ab-wave225-parallel-batches-report-20260228.md`
- Outcome:
  - retained declaration migrations: `20`.
  - retained bounded rewires: `10`.
  - global tracked source declarations/files now `861` remaining (`2277` baseline, `1416` reduced, `62.186210%` burndown).

## 2026-02-28 01:40 UTC (Hard-Cut M2 Wave 226A + 226B + 227)
- Continued batched-safe hard-cut migration from W225 checkpoint with non-overlapping declaration sets:
  - `W226A`: 14 declaration migrations in `mp-server/thrift-api` plus 1 bounded rewire in `MQThriftService`.
  - `W226B`: 14 declaration migrations in `mp-server/thrift-api` plus 1 bounded rewire in `GameServerThriftService`.
  - `W227`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - refined explorer output to avoid high-rewire plan; selected low-rewire strict-disjoint plan (`rewires: 1 + 1`, no `web-gs` rewires).
  - no additional compile stabilization required after worker edits.
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`, prior uncommitted evidence folder) outside commit scope.
- Validation:
  - fast gate batchA rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-014059-hardcut-m2-wave226ab-wave227-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/174-hard-cut-m2-wave226ab-wave227-parallel-batches-report-20260228.md`
- Outcome:
  - retained declaration migrations: `28`.
  - retained bounded rewires: `2`.
  - global tracked source declarations/files now `833` remaining (`2277` baseline, `1444` reduced, `63.416776%` burndown).

## 2026-02-28 02:05 UTC (Hard-Cut M2 Wave 228A + 228B + 229)
- Continued batched-safe hard-cut migration from W227 checkpoint with non-overlapping declaration sets:
  - `W228A`: 20 declaration migrations in `mp-server/thrift-api`.
  - `W228B`: 12 declaration migrations in `mp-server/thrift-api`.
  - `W229`: integration and validation.
- Parallel execution mode:
  - `1 explorer + 2 workers + main` with strict non-overlap ownership.
- Stabilization:
  - explorer selected strict-disjoint declaration-only plan (`rewires: 0 + 0`, no `web-gs` rewires).
  - retained minimal in-file namespace alignment in owned `TBot.java` (`TBotState` FQCN update).
  - no additional compile stabilization required.
  - no blind/global replacement performed.
  - preserved pre-existing local changes (`cluster-hosts.properties`, `.tmp-w202-*`, prior uncommitted evidence folder) outside commit scope.
- Validation:
  - fast gate batchA rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - fast gate batchB rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, smoke alias `/startgame`).
  - full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`); retry1 failed (`rc=2`).
- Evidence:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-020557-hardcut-m2-wave228ab-wave229-parallel-batches/`
  - report: `docs/projects/02-runtime-renaming-refactor/175-hard-cut-m2-wave228ab-wave229-parallel-batches-report-20260228.md`
- Outcome:
  - retained declaration migrations: `32`.
  - retained bounded rewires: `0`.
  - global tracked source declarations/files now `801` remaining (`2277` baseline, `1476` reduced, `64.822135%` burndown).

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

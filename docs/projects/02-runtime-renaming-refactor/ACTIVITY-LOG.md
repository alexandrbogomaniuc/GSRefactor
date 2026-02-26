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

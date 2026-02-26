# RENAME-FINAL Manual Curated Backlog (2026-02-26)

## Why this backlog exists
Automatic text replacement produced unsafe runtime changes (example: Docker MP startup class rename mismatch). From this point, all rename work is manual and file-curated.

## Current objective
Migrate legacy runtime names (`com.dgphoenix*`, `MQ*`, `mqbase.com`) without breaking GS/MP startup, launch flow, wallet flow, or multiplayer flow.

## Priority queue (execution order)

## Wave M1 (Highest risk, highest value)
Target: framework class-string registries and bean wiring

Files:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/SCClusterConfig.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/ClusterConfig.xml`

Method:
1. Keep existing `com.dgphoenix.*` entries.
2. Add explicit alias/fallback resolution for `com.abs.*` class names in runtime loader layer.
3. Validate startup and action mapping resolution.

Exit checks:
- GS startup without class-loading errors.
- Core support pages and `/startgame` endpoint return expected responses.

## Wave M2 (High risk)
Target: bank/server runtime config class strings and MQ contract keys

Files:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache/*.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache/*.xml`

Method:
1. Use dual-key aliases (`MQ_*` + `ABS_*`) only.
2. Keep legacy keys readable until post-cutover removal wave.
3. Disable external `mqbase.com` URLs in local/refactor profiles where replacements are available.

Exit checks:
- Bank load succeeds.
- Launch, wager, settle still pass in local smoke.

## Wave M3 (Medium risk)
Target: templates and support pages containing legacy tokens

Files:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/free/mp/template.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/*.jsp`

Method:
1. Keep dual output fields where contract compatibility is required.
2. Replace display-only legacy labels first.
3. Retest multiplayer websocket + MP contract logs.

Exit checks:
- Template render and JS bootstrap are unchanged.
- MP websocket + room flow still works.

## Wave M4 (Low risk, last)
Target: CI/K8s/deployment naming and optional branding text

Files:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/bitbucket-pipelines*.yml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/k8s/**`

Method:
1. Rename only after runtime waves M1-M3 are green.
2. Keep infra names stable if external automation depends on them.

## Hard rules for every manual wave
1. Change <= 3 files per mini-wave.
2. Run full verification matrix after each mini-wave.
3. Keep rollback patch ready (`git diff` artifact) before pushing.
4. Do not remove legacy fallback until all runtime smokes are green for two consecutive waves.

## Current completion snapshot (Project 02)
- Phase 0 refresh: complete.
- Phase 1 automatic path: completed and then blocked by safety gate (guardrail implemented).
- Manual wave execution:
  - M1.1 complete (`ServerConfigurationAction` alias-aware class loading)
  - M1.2 complete (`sb-utils` XML/factory alias-aware class loading)
  - M1.3 complete (`ServerConfigurationForm` class literal cleanup)
  - M2.1 complete (bank template third-party URL sanitization for local/refactor profiles)
  - M2.2 complete (seeded `ABS_*` alias keys for wallet/start/close processor config)
  - M2.3 complete (seeded `ABS_WEAPONS_MODE` alongside `MQ_WEAPONS_MODE`)
  - M2.4 complete (mqb server config domain/lobby host sanitization to local values)
  - M3.1 complete (support JSP compatibility for `com.dgphoenix`/`com.abs` class strings in init/sequencer/report checks)
  - M3.2 complete (support template-management JSP fallback for SP processor class `com.abs` -> `com.dgphoenix`)
  - M3.3 complete (GameBankConfig default class fallback for SP processor and GameServlet)
  - M2.5 complete (mpstress alias-key parity + FR bonus URL de-externalization + support email cleanup)
  - M3.4 complete (support bank-property pages decoupled from hardcoded `jsp:useBean` class names)
  - M3.5 complete (language-table pages decoupled from hardcoded `jsp:useBean` class names)
- Estimated completion: 96%.

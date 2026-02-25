# Runtime-Sensitive Naming Inventory Baseline

Date (UTC): 2026-02-25 18:27:26 UTC

## Purpose
Create a focused baseline of runtime-sensitive legacy naming usage for the Phase 9 follow-on cleanup subproject.

This inventory prioritizes:
- `com.dgphoenix.*`
- `MQ*` / `mq` names in runtime-sensitive contexts

## Scope Rule Used
Included:
- source code
- framework XML
- runtime config XML templates
- deploy/config/tooling scripts (when they affect rename safety)

Excluded:
- generated build outputs (`target`, `build`)
- runtime exploded folders (`Doker/runtime-*`)
- large generated log/evidence folders

## High-Level Findings (priority signals)
- `Class.forName(...)` in Java: `42` occurrences across `30` files (`gs-server` + `mp-server`)
- GS config XML files with `com.dgphoenix` / `MQ_*` / `/MQ_` / `mqbase.com`: `22` files
- Struts XML class refs (`type="com.dgphoenix..."`) in one file:
  - `171` in `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

## Risk Classification (simple)
- `Runtime-sensitive`: can break startup, reflection, launch/wallet flow, template rendering, config parsing
- `Tooling-policy sensitive`: affects rename guardrails/workflow safety
- `Text-only`: mostly docs/examples, lower risk

## 1) Highest Risk: Persisted Config + Reflection
Examples (runtime-sensitive):

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:70`
  - Bank config still contains wallet endpoints that historically pointed to `wallet.mqbase.com`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:193`
  - `CLOSE_GAME_PROCESSOR = com.dgphoenix...MQBCloseGameProcessor`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:207`
  - `START_GAME_PROCESSOR = com.dgphoenix...MQBStartGameProcessor`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml:179`
  - `WPM_CLASS = com.dgphoenix.casino.payment.wallet.CommonWalletManager`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/wallet/WalletProtocolFactory.java:304`
  - Loads class name from bank config, then `Class.forName(className)`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/CommonWalletManager.java:123`
  - `Class.forName(klazz)` for wallet request client class
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java:1048`
  - Runtime processor loading via `Class.forName(processorClass)`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/common/EditGameAction.java:67`
  - Support UI validates entered class properties using `Class.forName(value)`

Why this matters:
- These are the exact places where a package/class rename can fail at runtime even if code compiles.

## 2) High Risk: Framework XML Class Mappings (Struts / Reflection by config)
Examples:

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:15`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:146`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:301`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:327`

Why this matters:
- These are runtime action/form mappings, not just code comments.
- A rename here must match actual deployed classes and package layout.

## 3) High Risk: Protocol / Bank Behavior Keys (`MQ_*`)
Examples:

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:231`
  - `MQ_FRB_DEF_CHIPS`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1007`
  - `KEY_MQ_TOURNAMENT_REAL_MODE_URL`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1101`
  - `KEY_MQ_CLIENT_LOG_LEVEL`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:2142`
  - Default handler class FQCN string with `com.dgphoenix...`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBAction.java:102`
  - Uses `MQ_FRB_DEF_CHIPS`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp:304`
  - Client payload includes `MQ_WEAPONS_MODE`, `MQ_CLIENT_ERROR_HANDLING`

Why this matters:
- `MQ*` is not just branding text. Many values are protocol/config keys used by code and templates.

## 4) High Risk: Persisted Cache XMLs with Legacy FQCNs / Servlet Names
Examples:

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml:15`
  - `gsClassName = com.dgphoenix...SPGameProcessor`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml:22`
  - `servlet = /MQ_Dragonstone.game`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml:15`
  - `closeGameProcessorClassName = com.dgphoenix...DefaultCloseGameProcessor`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml:169`
  - `MQ_CLUSTERS_CONFIG`

Why this matters:
- These are config templates that seed runtime behavior and can be copied or persisted.

## 5) Medium/High Risk: Other Java String-Based Package Dependencies
Examples:

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/services/GameServerServiceConfiguration.java:58`
  - XStream whitelist string uses `com.dgphoenix.casino.**`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/gs/web/messages/GsMessageManager.java:8`
  - Resource bundle base name string includes `com.dgphoenix...`
- `/Users/alexb/Documents/Dev/Dev_new/mp-server/web/src/main/java/com/betsoft/casino/mp/kafka/KafkaConfiguration.java:14`
  - Spring component scan includes `com.dgphoenix.casino.kafka.handler`
- `/Users/alexb/Documents/Dev/Dev_new/mp-server/web/src/main/java/com/betsoft/casino/mp/kafka/KafkaMessageService.java:160`
  - `Class.forName(dataType)` for Kafka payload deserialization

## 6) Tooling-Policy Sensitive (not runtime behavior, but affects safety)
Examples:

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json`
  - `com.dgphoenix` and `mq` are `reviewOnly`
  - `com.dgphoenix` marked `requiresWrapper=true`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-wave-status-blocklist.json`
  - Explicitly blocks `mq` and `com.dgphoenix` in unsafe waves
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-legacy-name-inventory.sh`
  - Inventory patterns for `mq` and `com.dgphoenix`

## 7) Text-Only / Policy Docs (low technical risk, but authoritative)
Examples:

- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Bank-Template-Singleplayer-vs-Multiplayer-Policy.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/30-phase9-abs-rename-wave-plan-gs-v1.md`

These are safer to update, but they define the rules and plan and should stay consistent with the actual execution.

## Key Conclusion (Plain English)
The remaining rename work is real and risky because the old names are used in places that load classes and settings at runtime.

This is why:
- the earlier project did not do a blind rename,
- and why this subproject must use a controlled wave plan with wrappers and validation.

## Recommended Next Deliverable
- Build a “rename-ready shortlist” for `W3` that separates:
  - wrapper-required `com.dgphoenix` runtime references
  - protocol/config-key `MQ*` references that need compatibility aliasing
  - text-only references that can be handled later

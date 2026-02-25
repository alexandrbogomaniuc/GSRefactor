# RN1 Rename-Ready Shortlist (v1)

Date (UTC): 2026-02-25 18:27:26 UTC

## Purpose
Turn the runtime-sensitive inventory into a practical shortlist for the next safe implementation waves.

This file answers:
- What should be handled with wrappers?
- What needs key aliasing?
- What must stay review-only for now?
- What is safe to leave for later?

## Strategy Groups

### Group A: Wrapper-Required `com.dgphoenix` runtime class/package references (highest priority)
These load classes by string or framework XML and can break runtime if renamed directly.

#### A1. Bank-config-driven class loading (must support old names during migration)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/wallet/WalletProtocolFactory.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/CommonWalletManager.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`

Migration strategy:
- Add compatibility wrapper/delegate support before any config rename.
- Accept old FQCNs and allow staged new FQCN rollout.

#### A2. Support UI class validation path (must not reject transitional values)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/common/EditGameAction.java`

Migration strategy:
- Ensure support UI validation accepts both old and new class names during transition.

#### A3. Framework XML class mappings (Struts action/form types)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml`

Migration strategy:
- Do not mass-edit first.
- Introduce wrapper/migration path and validate startup and support routes before wave apply.

## Group B: `MQ*` Runtime Key / Protocol Alias Candidates (high priority, alias-first)
These are not just branding strings; many are active runtime keys and template payload fields.

#### B1. BankInfo constants and accessors (`MQ_*` key family)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java`

Examples:
- `MQ_FRB_DEF_CHIPS`
- `KEY_MQ_TOURNAMENT_REAL_MODE_URL`
- `KEY_MQ_CLIENT_LOG_LEVEL`

Migration strategy:
- Dual-read aliases (old key + new key) before any rename of persisted data.
- Prefer aliasing in getters/parsers, not immediate key replacement in all configs.

#### B2. Template payload keys / JS-visible names
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/real/mp/template.jsp`

Examples:
- `MQ_WEAPONS_MODE`
- `MQ_CLIENT_ERROR_HANDLING`
- `DISABLE_MQ_BACKGROUND_LOADING`

Migration strategy:
- Treat as protocol compatibility surface.
- Only rename with client compatibility proof (or dual field support).

#### B3. JSP/action feature usage of `MQ*` bank keys
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBAction.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/frbonus/AwardFRBLiteAction.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/stlobbies/tournaments/tournament.jsp`

Migration strategy:
- Add alias-reading in `BankInfo` first, then refactor callers.

## Group C: Persisted Config XMLs (runtime data/templates) — controlled migration only
These often contain both `com.dgphoenix` and `MQ*` values and may seed or mirror runtime state.

#### C1. Bank/server config template XMLs
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml`

Migration strategy:
- Do not rename first.
- Implement runtime compatibility layer, then migrate templates with validation.

#### C2. Persisted data examples (non-core but still runtime-visible)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.CurrencyCache.xml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.SubCasinoCache.xml`

Migration strategy:
- Review after core runtime compatibility is proven.

## Group D: Java string dependencies not obvious from package imports (wrapper/alias review required)
These can fail silently or at startup if renamed without compatibility handling.

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/services/GameServerServiceConfiguration.java`
  - XStream whitelist string
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/gs/web/messages/GsMessageManager.java`
  - Resource bundle base name string
- `/Users/alexb/Documents/Dev/Dev_new/mp-server/web/src/main/java/com/betsoft/casino/mp/kafka/KafkaConfiguration.java`
  - Spring component scan string
- `/Users/alexb/Documents/Dev/Dev_new/mp-server/web/src/main/java/com/betsoft/casino/mp/kafka/KafkaMessageService.java`
  - `Class.forName(dataType)` for Kafka payloads

Migration strategy:
- Treat as runtime-sensitive string references and validate startup + message handling after changes.

## Group E: Tooling/Policy Files (change carefully, but not runtime behavior)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-wave-status-blocklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-legacy-name-inventory.sh`

Migration strategy:
- Update only as part of approved rename-wave planning.
- Do not weaken review-only guards for convenience.

## Group F: Text-Only / Low-Risk (defer until runtime waves are safe)
- phase docs
- status reports
- user-facing planning notes

Migration strategy:
- Clean later in a final consistency wave.

## Recommended Next Coding Wave (RN2 -> RN3 bridge)
Start with compatibility behavior, not renames:

1. `com.dgphoenix` wrapper path for bank-config-driven class loading
2. `BankInfo` dual-read aliases for selected `MQ_*` keys (read old + new)
3. Validation updates proving old values still work and new values can be introduced

## Explicit Non-Goals for RN1
- No code renames yet
- No package moves yet
- No template key renames yet
- No manifest guard weakening

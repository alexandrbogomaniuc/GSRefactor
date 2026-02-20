# 04 Bank And Game Settings

## Goal
Build a settings traceability map.

## Critical Identity Mapping (New)
### Plain-English
- The wallet service can return player id `8`, but GS currently resolves many operations by its stored `externalId`.
- In our current data, that external id is token-style (`bav_game_session_001`) for account `40962`.
- So support tools and wallet operations that rely on `externalId` must use the same value GS saved, not only the numeric id from wallet auth response.

### Technical
- Lookup path:
  - `WalletsManagerAction` -> `AccountManager.getByCompositeKey(...)` -> Cassandra `accountcf_ext (bankid, extid -> accountid)`.
- Proven row:
  - bank `6274`: `extid=bav_game_session_001` -> `accountid=40962`.
  - no row for `extid=8` in this environment at time of check.
- Files:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/support/walletsmanager/WalletsManagerAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/account/AccountManager.java`

### Operator impact
- For pending transaction cleanup in walletsManager, use:
  - `extUserId=bav_game_session_001` or `accountId=40962`
  - not `extUserId=8` (in current DB state).

## Proven Bank Config UI Entry Points
- Main support tools page: `/support/index.jsp`
- Bank/Subcasino config start page: `/support/cache/bank/common/subcasinoSelect.jsp`
- Bank list selector page: `/support/bankSupport.do`
- Bank details page: `/support/bankInfo.do?bankId=<BANK_ID>`
- Bank properties editor: `/support/bankSelectAction.do?bankId=<BANK_ID>`

Important:
- `/support/bankSelectAction.do` without `bankId` is invalid and can fail in `Long.parseLong(...)`.
  Evidence: `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/BankSelectAction.java`
- `/support/bankSelectAction.do` is not the list page. Use `/support/bankSupport.do` first (it renders dropdown options from `BankInfoCache`).
  Evidence:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/BankSupportAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/support/cache/bank/properties/edit/allBanks.jsp`

### HTTP Evidence (2026-02-10)
Validated from inside GS container (`http://localhost:8080`):
- `GET /support/bankSupport.do` -> `200`
- `GET /support/bankSelectAction.do` -> `500` (missing required `bankId`)
- `GET /support/bankSelectAction.do?bankId=6274` -> `200`

Observed dropdown options in `/support/bankSupport.do`:
- `6274` -> `TEST_BAV`
- `6275` -> `TEST_BAV_VND`
- `271` -> `Default`

### Live UI Verification (Chrome MCP, 2026-02-10)
Verified directly in browser against `http://localhost:81`:
- `/support/bankSupport.do` renders bank dropdown with:
  - `DEFAULT BANK`
  - `TEST_BAV`
  - `TEST_BAV_VND`
  - `Default`
- `/support/bankSelectAction.do` (without `bankId`) returns `HTTP 500`.
  Stack trace includes:
  - `java.lang.NumberFormatException: null`
  - `com.dgphoenix.casino.support.cache.bank.edit.actions.editproperties.BankSelectAction.execute(BankSelectAction.java:28)`
- `/support/bankSelectAction.do?bankId=6274` opens `Edit properties page`.
- `/support/bankSelectAction.do?bankId=271` opens `Edit properties page`.

Plain-English explanation:
- The page `bankSelectAction.do` is not a general entry page. It requires a specific bank id.
- If `bankId` is missing, GS tries to convert an empty value to a number and fails.

Code evidence:
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/BankSelectAction.java:28`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/BankSupportAction.java:30`

### Why Two "Default" Entries Can Appear
You can see both:
- `DEFAULT BANK` (id `0`)
- `Default` (id `271`)

Reason:
- GS creates a synthetic default bank `id=0` in memory when cache starts, even if Cassandra does not have row `key=0`.
- Bank `271` is a normal Cassandra-loaded bank with description `Default`.

Code evidence:
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/BankInfoCache.java:69`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/BankInfoCache.java:70`

## Why Only Default Bank Can Appear
Observed behavior from GS logs:
- Early startup at `2026-02-10 04:28` loaded only one bank:
  - `CassandraBankInfoPersister ... loadAll: count=1`
  - `CassandraSubCasinoPersister ... size()=1`
- Later startup at `2026-02-10 04:49` loaded three banks:
  - `CassandraBankInfoPersister ... loadAll: count=3`
  - `CassandraSubCasinoPersister ... size()=2`

Plain-English explanation:
- If GS starts before full bank/subcasino seed is applied (or before restart after seed), the in-memory bank cache can contain only the default profile.
- After Cassandra seed and GS restart, the support dropdown reflects all banks.

Evidence:
- GS logs (`docker logs gp3-gs-1`) with startup timestamps and `loadAll` counters.
- Cache classes:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBankInfoPersister.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/BankInfoCache.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankConstants.java` (`DEFAULT_BANK_ID = 0`)

## Current Cassandra State (2026-02-10)
- `rcasinoscks.bankinfocf`: `3` rows (`271`, `6274`, `6275`)
- `rcasinoscks.subcasinocf`: `2` rows (`58`, `507`)
- `rcasinoscks.gameinfocf`: includes new key `6274+838` for baseline game launch
- `rcasinoscks.gametinfocf`: `8` base templates (including `838`)
- `rcasinoscks.currencycf`: added missing `VND` row (now present with existing `USD/MQC/MMC/...`)

Clarification:
- `6274+838` exists and is launch-ready (`bankidx=6274`, `bankandcuridx=6274_USD`).
- `6275+838` is now present and launch-ready (`bankidx=6275`, `bankandcuridx=6275_VND`).

Applied updates:
- Bank rows with localhost-ready wallet/origin settings:
  `/Users/alexb/Documents/Dev/readme all you need to know from md files/Game provider side_Source code_mq-gs-clean-version_FINAL_BAV_BANKS.cql`
- Subcasino localhost domains:
  `/Users/alexb/Documents/Dev/readme all you need to know from md files/FIX_SUBCASINO_LOCALHOST.cql`
- Baseline game row for bank `6274`:
  `/Users/alexb/Documents/Dev/readme all you need to know from md files/update_838_fixed.cql`
- Added matching baseline game row for bank `6275`:
  `/Users/alexb/Documents/Dev/docs/sql/add_game_838_for_bank_6275.cql`

Manual fix applied after script:
- `rcasinoscks.gameinfocf` key `6274+838` had null indexed fields.
- Set `bankidx=6274`, `bankandcuridx=6274_USD` for lookup compatibility.
- Added `VND` currency row in `rcasinoscks.currencycf` to remove bank `6275` currency-cache warning at GS startup.

### SubCasino Hostname Filters (Critical For Launch)
These domain lists are checked in subcasino-level launch validation flows.

Current rows:
- `subcasinocf.key=58`:
  - `name=Default`
  - `defaultBank=271`
  - `bankIds=[271]`
  - `domainNames=[default-gp3.local.com, localhost, 127.0.0.1]`
- `subcasinocf.key=507`:
  - `name=MQ TECNOLOGIA`
  - `defaultBank=6274`
  - `bankIds=[6274,6275]`
  - `domainNames=[default.mqbase.com, localhost:8081, http://localhost, http://localhost:80, http://host.docker.internal, http://host.docker.internal:80, http://host.docker.internal:8081, localhost]`

Plain-English explanation:
- Even if bank-level `ALLOWED_ORIGIN`/`ALLOWED_DOMAINS` looks correct, subcasino `domainNames` can still block launch URLs.
- For local baseline, `localhost` and `host.docker.internal` entries are already present for subcasino `507`.

## Save Flow (Proven)
1. UI loads bank properties via:
   `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/BankSelectAction.java`
2. User clicks `save` in:
   `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/support/cache/bank/properties/edit/editProperties.jsp`
3. Save handler updates `BankInfo` fields and properties:
   `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/EditPropertiesAction.java`
4. Persist + cluster notify via Kafka refresh:
   `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/persistance/remotecall/RemoteCallHelper.java`

## Settings Traceability (Hostname/Origin Related)
| Setting | Defined In | Loaded In | Applied In | Behavior Impact | How To Test |
|---|---|---|---|---|---|
| `allowedRefererDomains` | Bank object XML field in active runtime file `/Users/alexb/Documents/Dev/Doker/runtime-gs/default-configs/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | `BankSelectAction` -> `bankPropForm.setAllowedRefererDomains(...)` | `CommonActionForm.checkLaunchPermission(...)` -> `bankInfo.isDomainAllowed(...)` | Controls which `Referer` hostnames are accepted for launch checks. | Open game launch from `http://localhost:80` and `http://localhost:81`; expect allowed. Use a different domain; expect launch denied when check is active. |
| `forbiddenRefererDomains` | Same bank XML and bank editor UI | `BankSelectAction` | `CommonActionForm.checkLaunchPermission(...)` -> `bankInfo.isDomainAllowed(...)` | Explicit deny list for referer hosts. | Add domain to forbidden list, reload, retry launch from that domain; expect deny. |
| `NEED_ALLOWED_REFERER_DOMAINS` | Bank property key in `BankInfo` (`KEY_NEED_ALLOWED_REFERER_DOMAINS`) | `BankInfo.isNeedAllowedRefererDomains()` | `BankInfo.isDomainAllowed(...)` | If true and allowed list is empty, launch check denies by design. | Clear allowed list and set `NEED_ALLOWED_REFERER_DOMAINS=true`; expect deny. |
| `ALLOWED_ORIGIN` | Bank property key `KEY_ALLOWED_ORIGIN` in `BankInfo` | `BankInfo.getAllowedOrigin()` | MP template `postMessage` origin filter in `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp` | Limits which browser origins can receive SID via `postMessage`. | In browser devtools, send `postMessage` from allowed and non-allowed origins; only allowed origin should receive SID response. |
| `ALLOWED_DOMAINS` | Bank property key `KEY_ALLOWED_DOMAINS` in `BankInfo` | `BankInfo.getAllowedDomains()` | Home/cashier URL validation in `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp` (`isValidUrl`) | Controls which top-level domains are accepted from launch params like `homeUrl` and `cashierUrl`. | Pass `homeUrl`/`cashierUrl` with allowed domain and non-allowed domain; expect fallback to bank defaults for non-allowed. |
| `START_GAME_DOMAIN` | Bank property in runtime bank XML | `BankInfo.getStartGameDomain()` | Used in launch/session related flows (for example restart/join logic) | Influences generated start-game domain usage in specific flows. | Trigger restart/join flow and verify generated URLs/domain use expected host. |
| `USE_SAME_DOMAIN_FOR_START_GAME` | Bank property in runtime bank XML | `BankInfo` property map | Applied by launch URL generation logic using bank properties | Keeps start flow on same domain when enabled. | Compare launch URL host with flag on vs off. |
| `COMMON_WALLET_AUTH_REQUIRED` | Bank property key `KEY_CW_AUTH_REQUIRED` in `BankInfo` | `BankInfo.isRequiredAuthParam()` | Wallet request auth enrichment in `/Users/alexb/Documents/Dev/mq-gs-clean-version/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v2/RESTCWClient.java` (`handleCWAuth`) | If enabled, wallet API password is attached to wallet calls. | Toggle flag for a test bank, trigger wallet operation (auth/balance), and compare outgoing request params/logs for API password field presence. |

Code evidence (domain/origin checks):
- Launch referer gate:
  `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/actions/enter/CommonActionForm.java:153`
- Referer allow/deny evaluation:
  `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:2279`
- `ALLOWED_ORIGIN` getter:
  `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:3709`
- `ALLOWED_DOMAINS` getter:
  `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:3718`
- URL/domain validation in template:
  `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:385`

## Runtime Values Applied In Current Local Stack
File:
- `/Users/alexb/Documents/Dev/Doker/runtime-gs/default-configs/com.dgphoenix.casino.common.cache.BankInfoCache.xml`

Current proven values:
- `ALLOWED_ORIGIN` includes localhost and 127.0.0.1 with ports 80/81 and both http/https.
- `allowedRefererDomains` includes `localhost`, `localhost:80`, `localhost:81`, `127.0.0.1`, `127.0.0.1:80`, `127.0.0.1:81`.
- `forbiddenRefererDomains` is empty.

Additional proven values for bank `6274` (from Cassandra `bankinfocf.jcn`):
- `ALLOWED_ORIGIN=http://localhost:8081,http://localhost:8080,https://mqbase.com`
- `ALLOWED_DOMAINS=localhost,mqbase.com`
- `START_GAME_DOMAIN=localhost:8081`
- `COMMON_WALLET_AUTH_REQUIRED=true`

## Bank Differences Snapshot (Support UI, 2026-02-10)
Source pages:
- `/support/bankSelectAction.do?bankId=271`
- `/support/bankSelectAction.do?bankId=6274`
- `/support/bankSelectAction.do?bankId=6275`

| Bank ID | Name | Default Currency | COMMON_WALLET_AUTH_REQUIRED | START_GAME_DOMAIN | ALLOWED_ORIGIN | ALLOWED_DOMAINS |
|---|---|---|---|---|---|---|
| `271` | `Default` | `EUR` | `false` | `localhost` | empty | empty |
| `6274` | `TEST_BAV` | `USD` | `true` | `localhost:8081` | `http://localhost:8081,http://localhost:8080,https://mqbase.com` | `localhost,mqbase.com` |
| `6275` | `TEST_BAV_VND` | `VND` | `true` | `localhost:8081` | `http://localhost:8081,http://localhost:8080,https://mqbase.com` | `localhost,mqbase.com` |

## Host Header Warning (Observed)
Recent GS logs include:
- `Bad header found, Host is: localhost:81, but X-Forwarded-Host or X-Forwarded-Server undefined`

What it means in plain English:
- GS expected reverse-proxy forwarding headers, but direct localhost requests do not send them.
- The code logs a warning and falls back to `Host` header, so requests still continue.

Code evidence:
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/HttpServletRequestProxy.java:81`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/HttpServletRequestProxy.java:87`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/RequestSchemeFilter.java:27`

## Notes About DomainWhiteList Tool
- Struts routes `/support/domainwl.do` and `/support/editdomains.do` manage `DomainWhiteListCache` (game-domain list).
- This is a separate mechanism from bank `allowedRefererDomains` and `ALLOWED_ORIGIN`.
- Evidence:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/DomainWhiteListAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/EditDomainAction.java`

## Launch Parameter Gotcha (Critical)
- MP template JSP expects uppercase keys from `BaseAction` constants:
  - `BANKID`
  - `SID`
  - `LANG`
- `gameId` remains lowercase.

Working launch example:
- `/free/mp/template.jsp?BANKID=6274&SID=bav_game_session_001&gameId=838&LANG=en`

Non-working example:
- `/free/mp/template.jsp?bankId=6274&sessionId=...&lang=en`
  - Fails because `free/mp/template.jsp` checks `BANKID` and logs `MQ TEMPLATE.JSP:: bankId not found`.
  - Evidence: `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/free/mp/template.jsp`

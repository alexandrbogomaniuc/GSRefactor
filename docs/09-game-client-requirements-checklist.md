# 09 Game Client Requirements Checklist

## Purpose
This document is the phase-0 checklist extracted from `Game_Client_Requirements_MAIN`.

Plain-English:
- It is a "what to verify" list for game client behavior.
- It is not yet the final deep interpretation of each rule line-by-line.

Technical:
- Source PDF:
  - `/Users/alexb/Documents/Dev/readme all you need to know from md files/New Game requirements to work with bsg GS/Game_Client_Requirements_MAIN-1.pdf`
- Extraction method used in this phase:
  - Chrome DevTools MCP PDF `Document outline` (section + sub-section titles).

## Extraction Status
- `Completed`: top-level sections and visible sub-requirements from the document outline.
- `Pending`: page-by-page deep read of each section body text and exact pass/fail criteria.

## Checklist (Phase-0 Outline)

### Structure and Levels
- [ ] Purpose of the document
- [ ] Levels of Requirements
- [ ] Description of Customer Config Level
- [ ] Description of Bank Properties Level

### Autoplay
- [ ] Display Autoplay
- [ ] Do not display line-specific payouts during Autoplay
- [ ] Return individual display of line payouts after Autoplay ends
- [ ] Display an error after trying to use Autoplay in FRB mode
- [ ] Allow reel speed control in Autoplay mode

### Double Up
- [ ] Enable/Disable DoubleUP

### Buy Feature
- [ ] Disable Buy feature
- [ ] Disable Buy feature for Cash Bonus

### Respin
- [ ] Requirements for Respin (detail text extraction pending)

### Hold and Win Feature
- [ ] Requirements for Hold and Win Feature (detail text extraction pending)

### Free Spin
- [ ] Requirements for Free Spin (detail text extraction pending)

### Turboplay
- [ ] Enable/Disable Turboplay

### Big/Huge/Mega Win
- [ ] Big Win animation structure
- [ ] Situations where Big Win must be displayed
- [ ] Situations where Big Win must NOT be displayed
- [ ] Big Win duration requirements
- [ ] Maintain low-performance mode

### Jackpots
- [ ] Description of BigWin and Jackpot behavior
- [ ] Enable/Disable jackpot notifications

### Reality Check
- [ ] Enable/disable Reality Check
- [ ] Set RC check and display interval
- [ ] Set RC reminder percent
- [ ] Set Info Page about Reality Check
- [ ] Enable/Disable custom RC dialog
- [ ] Enable/disable Confirm button on RC dialog
- [ ] Enable/Disable bet history button in RC dialog
- [ ] Enable/Disable End Session/Exit button in RC dialog

### Loading Screen
- [ ] Loading Screen base structure
- [ ] Loading Screen additional unification requirements
- [ ] Enable/Disable Betsoft logo display in game

### Main Game Screen
- [ ] Set card distribution animation default speed
- [ ] Set regular reels spin time
- [ ] Enable/Disable Sound Toggle display on main screen
- [ ] Enable/Disable sounds in UE/ToGo games
- [ ] Set default language of the game
- [ ] Display game localization error
- [ ] Enable/Disable forced spin stop
- [ ] Enable spin profiling
- [ ] Disable/Enable in-game history
- [ ] Set game history endpoint
- [ ] Display game history in the same window
- [ ] Enable/disable holiday mode in the game
- [ ] Display TAP panel symbols

### Paytable
- [ ] Pause animations
- [ ] Enable/Disable usage of modified game scale

### Rules Tab
- [ ] Display game RTP in Rules tab
- [ ] Allow printable rules
- [ ] Display option redirecting to full game rules
- [ ] Display Maximum Possible Win (deprecated)
- [ ] Display Rules button on main screen

### Options Tab
- [ ] Main requirements for the Options tab

### Certification-Related Client Features
- [ ] Display game symbols on dice-like background images
- [ ] Enable/Disable current date and time display
- [ ] Enable/Disable game session duration display
- [ ] Enable/Disable Loss Limit display
- [ ] Enable/Disable display of total bet and total win per session
- [ ] Enable/Disable display of remaining time for session duration limit

### QLC
- [ ] Set customization of game localization for specific licensee
- [ ] Set storage path for customization files
- [ ] Display custom error messages in game
- [ ] Enable/Disable custom translations of main game messages
- [ ] Enable/Disable custom translations for tips and controls in rules
- [ ] Enable/Disable game title localization

### Bets, Payouts, and Balance Display
- [ ] Display payouts in real currency
- [ ] Set custom currency symbol
- [ ] Truncate fractional part of monetary parameters
- [ ] Enable/Disable delayed wallet messages
- [ ] Send external wallet messages

### Pariplay Events / Post Message
- [ ] Stop game when Pariplay event message is received
- [ ] Continue game from same state after Pariplay event message
- [ ] Stop Autoplay when Pariplay event message is received

### Home and Cashier Functions
- [ ] Redirect player to home page of licensee site
- [ ] Redirect player to cashier page
- [ ] Set launch home from iframe
- [ ] Enable/disable using homeUrl and cashierUrl in parent window
- [ ] Enable/Disable open cashier in pop-up
- [ ] Enable/Disable redirect and close game on BuyIn
- [ ] Enable/Disable redirect without closing game on BuyIn or Home
- [ ] Enable/Disable pop-up messages about Guest/Free mode

### Free Round Bonus (FRB)
- [ ] Do not restart on FRB

### Additional Requirements
- [ ] Set additional settings
- [ ] Redirect to full UNJ rules
- [ ] Enable/Disable logout on error
- [ ] Specify location of customer-level settings

## First Pass Traceability To GS/MP Config

Plain-English:
- The checklist above is requirement language.
- This section maps those requirements to real GS settings and code hooks we can already prove.
- If a setting is not found in GS/MP code yet, it is marked as `not found yet`.

### Autoplay
- Proven keys and locations:
  - `AUTOPLAY_MODE` in bank config XML:
    - `/Users/alexb/Documents/Dev/Doker/runtime-gs/default-configs/com.dgphoenix.casino.common.cache.BankInfoCache.xml:171`
  - `AUTOPLAY_VALUES` in bank config XML:
    - `/Users/alexb/Documents/Dev/Doker/runtime-gs/default-configs/com.dgphoenix.casino.common.cache.BankInfoCache.xml:363`
  - `KEY_AUTOPLAY_VALUES` constant and getter:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:266`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1889`
  - Per-game autoplay capability:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/sb-utils/src/main/java/com/dgphoenix/casino/common/cache/data/game/BaseGameConstants.java:165`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/game/BaseGameInfo.java:592`
- Status: partially mapped in GS; client-side consumption path still needs deep tracing in game client code.

### Turboplay
- Search result in GS/MP code: no explicit `TURBO`/`TURBOPLAY` setting found yet.
- Status: not found yet; likely controlled in game client layer and/or customer settings descriptors.

### Reality Check
- Proven keys and locations:
  - `ENABLE_REALITY_CHECK` in bank config XML:
    - `/Users/alexb/Documents/Dev/Doker/runtime-gs/default-configs/com.dgphoenix.casino.common.cache.BankInfoCache.xml:319`
  - Client param key `rcInterval`:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/web/BaseAction.java:113`
  - Extended account key:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/account/ExtendedAccountInfo.java:5`
  - Wallet error code `316` (`REALITY_CHECK_REQUIRED`):
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/sb-utils/src/main/java/com/dgphoenix/casino/gs/managers/payment/wallet/CommonWalletErrors.java:27`
  - Wallet error handling path:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v2/AbstractCWClient.java:58`
- Status: core hooks mapped.

### Rules Tab / Rules Path
- Proven keys and locations:
  - `RULES_PATH` config key:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/config/GameServerConfigTemplate.java:115`
  - Getter:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/config/GameServerConfigTemplate.java:842`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/system/configuration/GameServerConfiguration.java:423`
  - Local value:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml:141`
- Status: key and value mapped; downstream consumer path in templates/scripts needs deeper trace.

### Home / Cashier / URL Control
- Proven keys and locations:
  - `KEY_MOBILE_HOME_URL`, `KEY_MOBILE_CASHIER_URL`, `KEY_HOME_URL`, `KEY_JS_HOME`, `KEY_LAUNCH_HOME_FROM_IFRAME`:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:313`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:315`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:318`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:323`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:326`
  - URL validation and allowed domain filter:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:385`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:393`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:397`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:405`
- Status: mapped and proven.

### Post Message / Parent Communication
- Proven keys and locations:
  - `KEY_POST_MESSAGE_TO_OPENER`, `KEY_POST_SID_TO_PARENT`, `KEY_ALLOWED_ORIGIN`, `KEY_ALLOWED_DOMAINS`:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1055`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1057`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1060`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:1066`
  - Runtime getters:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:3701`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:3705`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:3709`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:3718`
  - MP template JS listener:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:234`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:236`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:242`
- Status: mapped and proven.

### FRB (Free Round Bonus) Restart / Scope Controls
- Proven keys and locations:
  - `KEY_NO_FRB_RESTART` definition and getter:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:566`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java:2217`
  - Launch `noFRB` parameter propagation:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/game/BaseStartGameAction.java:570`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp:288`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/free/mp/template.jsp:87`
  - FRB games whitelist/blacklist parsing:
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/BankInfoCache.java:91`
    - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/BankInfoCache.java:96`
- Status: mapped with concrete GS hooks.

## Next Step (Deep Extraction)
- For each checklist item, add:
  - exact config/flag name(s),
  - where it is defined (bank config, customer config, game config),
  - where GS/MP code applies it,
  - how to verify with concrete request + expected output.

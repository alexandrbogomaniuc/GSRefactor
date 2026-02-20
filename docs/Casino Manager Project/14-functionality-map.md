# CM Functionality Map And Implementation Plan

## Scope Of This Pass
- Full provider menu/URL map was recrawled and stored in `/Users/alexb/Documents/Dev/docs/Casino Manager Project/13-provider-url-map.md`.
- First deep functional implementation milestone is focused on player tooling:
  - `Player Search`
  - clickable player row to `Summary Info`
  - `View` sections and actionable player controls.

## Provider Contracts Used
- Player search layout: `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/provider-playerSearch-layout-20260217.json`
- Player search data sample: `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/provider-playerSearch-result-20260217.json`
- Player summary full payload: `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/provider-playerSummaryInfo-complete-20260217.json`

## Cassandra Tables Parsed In This Milestone
- `rcasinoscks.accountcf_ext`
  - source for `(bankid, extid, accountid)` mapping in player search.
- `rcasinoscks.accountcf`
  - source for account JSON (`nickName`, `externalId`, `balance`, `locked`, `registerTime`, `lastLoginTime`, `currency`, `testUser`, `subCasinoId`, `bankId`).
- `rcasinoscks.bankinfocf`
  - source for bank title/metadata (`externalBankIdDescription`, `subCasinoId`).
- `rcasinoscks.subcasinocf`
  - source for subcasino display name.
- `rcasinoks.gamesessioncf`
  - source for player activity totals (`bets`, `income`, `payout`, `bonusbet`, `bonuswin`, `cltype`, `et`) used by search and summary.

## Implemented Local Endpoints (Player Slice)
- `GET /cm/reports/playerSearch`
  - supports provider-style filters (`clusterId`, `subcasinoList`, `bankList`, `nickName`, `accountId`, `extId`, `fuzzySearch`, `regAfterTime`, `regBeforeTime`, `accountStatus`, `mainPerPage`).
  - returns provider-like columns including status, totals, platform, dates, currency.
- `GET /cm/players/:bankId/:accountId/summary`
  - returns summary cards + menu model (`Actions`, `View`) and computed sections.
- `GET /cm/players/:bankId/:accountId/game-info`
- `GET /cm/players/:bankId/:accountId/payment-detail`
- `GET /cm/players/:bankId/:accountId/bonus-detail`
- `GET /cm/players/:bankId/:accountId/frbonus-detail`
- `GET /cm/players/:bankId/:accountId/change-history`
- `POST /cm/players/:bankId/:accountId/actions/lockAccount`
- `POST /cm/players/:bankId/:accountId/actions/makeTester`

## UI Features Implemented In This Milestone
- Dedicated `Player Search` workspace panel:
  - functional filters and search execution.
  - provider-style result columns (expanded from minimal placeholder output).
  - row click and three-dots action entry open player summary tab.
- Dedicated `Summary Info` panel:
  - `Actions` dropdown with real write actions (`lock/unlock`, `set/unset tester`).
  - `View` dropdown with functional sections (`summary`, `game`, `payments`, `bonus`, `frbonus`, `history`).
- Font alignment pass:
  - normalized typography sizes in summary/info cards and edit modal.
- Branding cleanup:
  - removed Betsoft footer mention from local CM UI.

## Role And Access Rules
- Read access for player reports remains controlled by `VIEW_PLAYER_SEARCH`.
- Mutating player actions are restricted to users with management capability (`canManageAny`).
- Support role remains read-only.

## Step-By-Step Continuation Plan
1. Complete parity for remaining management and statistics reports from the 94-endpoint map.
2. For each report: replicate filter contract, table columns, and row transitions before moving to next report.
3. Add per-report smoke assertions and endpoint contract snapshots under `docs/Casino Manager Project/evidence`.
4. Introduce hourly sync worker to populate `cm-mirror` for heavy reports and decouple UI response times.
5. Add final parity matrix (`provider endpoint` -> `local endpoint` -> `status` -> `test evidence`).

## Milestone Update 2026-02-18 (Player Actions + Game Info Filters)
- Provider contracts recrawled and stored:
  - `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/provider-awardBonus-action-20260218.json`
  - `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/provider-awardFRBonus-action-20260218.json`
  - `/Users/alexb/Documents/Dev/docs/Casino Manager Project/evidence/provider-playerGameInfo-layout-20260218.json`
- Implemented actions parity in local player summary:
  - `Lock/Unlock`
  - `Award Bonus`
  - `Award FRBonus`
  - `Set tester`
- Added persistence for awarded bonus entities to Cassandra tables:
  - bonus: `rcasinoscks.bonuscf`, `rcasinoscks.bonuscf_acc`, `rcasinoks.bonusarchcf`
  - free rounds bonus: `rcasinoscks.frbonuscf`, `rcasinoscks.frbonuscf_acc`, `rcasinoks.frbonusarchcf`
- Player summary aggregates now read live awarded data and update:
  - `bonuses` counters/amounts
  - `frb` counters/rounds
- Implemented provider-style game-info filtering and dual table modes:
  - filters: `dateFrom`, `dateTo`, `playerMode`, `platform`, `gameType`, `isJackpot`, `showBySessions`
  - modes:
    - grouped games mode (`showBySessions=false`)
    - session rows mode (`showBySessions=true`)
- UI now includes:
  - award bonus modal with provider-like fields and derived rollover values
  - award FR bonus modal with rounds/chips/bet-type/single-game fields
  - game info filter block + search in player summary tab

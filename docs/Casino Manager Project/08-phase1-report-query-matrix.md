# Phase-1 Report Query Matrix

## Scope
This matrix covers the phase-1 report pack:
- `playerSearch`
- `bankList`
- `transactions`
- `gameSessionSearch`
- `walletOperationAlerts`

Provider contracts were extracted from live CM `layout` payloads during the `2026-02-16` crawl.

## 1) playerSearch
- Endpoint:
  - `https://api.casino-manager.discreetgaming.com/api/reports/playerSearch/layout`
- Filters (flattened):
  - `bankPicker` (`bank-picker`)
  - `fuzzySearch` (`checkbox`)
  - `nickName` (`text-input`)
  - `accountId` (`integer-input`)
  - `extId` (`text-input`)
  - `regAfterTime` (`date-time-picker`)
  - `regBeforeTime` (`date-time-picker`)
  - `currencyId` (`combobox`)
  - `category` (`combobox`)
  - `regPlatform` (`combobox`)
  - `accountStatus` (`combobox`)
  - `streamer` (`combobox`)
- Key output columns:
  - `accountId`, `accountNickname`, `externalId`, `subcasinoName`, `bankName`, `accountStatus`, `currencyCode`, `lastLoginTime`, `registrationTime`, `balance`.
- Source domain:
  - `rcasinoscks.accountcf`
  - `rcasinoscks.accountcf_ext`
  - `rcasinoscks.bankinfocf`
  - `rcasinoscks.subcasinocf`
- Phase-1 query table:
  - `cm_read.player_search_by_bank`
- Query strategy:
  - partition by `bank_id`,
  - bounded reads per bank + app-level post-filter on nickname/ext/status/date/currency for phase-1.

## 2) bankList
- Endpoint:
  - `https://api.casino-manager.discreetgaming.com/api/reports/bankList/layout`
- Filters:
  - `bankPicker` (`bank-picker`)
- Key output columns:
  - `subcasinoName`, `bankId`, `internalId`, `externalId`, `bankName`, `referrer`, `lastBet`, `bankStatus`.
- Source domain:
  - `rcasinoscks.bankinfocf`
  - `rcasinoscks.subcasinocf`
- Phase-1 query table:
  - `cm_read.bank_list_by_subcasino`
- Query strategy:
  - partition by `subcasino_id`,
  - optional bank id filtering in application layer.

## 3) transactions
- Endpoint:
  - `https://api.casino-manager.discreetgaming.com/api/reports/transactions/layout`
- Filters (flattened):
  - `dateRangePicker` (`date-range-picker`)
  - `bankPicker` (`bank-picker`)
  - `currencyConverter` (`currency-converter`)
  - `statusId` (`combobox`)
  - `typeId` (`combobox`)
- Key output columns:
  - `transactionId`, `accountNickname`, `accountExternalId`, `subcasinoName`, `bankName`, `typeName`, `amount`, `startTime`, `finishTime`, `statusName`, `externalTransactionId`.
- Source domain:
  - `rcasinoscks.paymenttransactioncf2`
  - `rcasinoks.betcf` (status/reconciliation joins where needed)
  - bank/subcasino/account projection tables.
- Phase-1 query table:
  - `cm_read.transactions_by_bank_day`
- Query strategy:
  - required bank + date-window query,
  - partition key `((bank_id, day))`,
  - clustering by `start_time DESC, transaction_id ASC`,
  - post-filter by `status_id` and `type_id` when needed.

## 4) gameSessionSearch
- Endpoint:
  - `https://api.casino-manager.discreetgaming.com/api/reports/gameSessionSearch/layout`
- Filters:
  - `gameSessionId` (`integer-input`)
- Key output columns:
  - `gameSessionId`, `accountNickname`, `accountExternalId`, `subcasinoName`, `bankName`, `platformName`, `gameTitle`, `startTime`, `endTime`, `income`, `payout`, `gainLoss`.
- Source domain:
  - `rcasinoks.gamesessioncf`
  - account/bank/subcasino/game projection tables.
- Phase-1 query table:
  - `cm_read.game_session_by_id`
- Query strategy:
  - exact lookup by `game_session_id` primary key (single-row read).

## 5) walletOperationAlerts
- Endpoint:
  - `https://api.casino-manager.discreetgaming.com/api/reports/walletOperationAlerts/layout`
- Filters:
  - `dateRangePicker` (`date-range-picker`)
  - `bankPicker` (`bank-picker`)
  - `gamePicker` (`multi-select`)
  - `nickname` (`text-input`)
  - `extId` (`text-input`)
  - `status` (`combobox`)
- Key output columns:
  - `alertId`, `process`, `nickname`, `accountExtId`, `subcasinoName`, `bankName`, `gameName`, `alertTime`, `amount`, `currency`, `operationType`, `status`, `transactionId`, `externalTransactionId`, `roundId`, `gameSessionId`, `lastRequest`.
- Source domain:
  - `rcasinoks.wopcf`
  - account/bank/subcasino/game projection tables.
- Phase-1 query table:
  - `cm_read.wallet_alerts_by_bank_day`
- Query strategy:
  - required bank + date-window query,
  - partition key `((bank_id, day))`,
  - clustering by `alert_time DESC, alert_id ASC`,
  - post-filter by `status`, `game_id`, `nickname`, `ext_id` for phase-1.

## Implementation Notes
- Do not query `jcn/scn` blobs directly from UI paths.
- Sync worker should decode and pre-materialize CM read models.
- Avoid `ALLOW FILTERING` in runtime query paths.

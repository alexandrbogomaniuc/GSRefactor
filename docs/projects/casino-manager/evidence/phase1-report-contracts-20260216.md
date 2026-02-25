# Phase-1 Report Contracts (2026-02-16 UTC)

Contracts extracted from authenticated CM `layout` endpoints.

## playerSearch
- Report ID: `playerSearch`
- Filters:
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
- Columns (`22`):
  - `accountId`, `accountName`, `subcasinoId`, `bankId`, `lastPlatformId`, `currencyId`, `totalRecords`, `rowNumber`, `accountNickname`, `externalId`, `subcasinoName`, `bankName`, `accountStatus`, `totalAccountSessionCount`, `totalBetsCount`, `totalIncome`, `gameRevenue`, `balance`, `lastPlatform`, `lastLoginTime`, `registrationTime`, `currencyCode`

## bankList
- Report ID: `bankList`
- Filters:
  - `bankPicker` (`bank-picker`)
- Columns (`11`):
  - `rowNumber`, `subcasinoName`, `bankId`, `internalId`, `externalId`, `bankName`, `referrer`, `lastBet`, `status`, `bankStatus`, `totalRecords`

## transactions
- Report ID: `transactions`
- Filters:
  - `dateRangePicker` (`date-range-picker`)
  - `bankPicker` (`bank-picker`)
  - `currencyConverter` (`currency-converter`)
  - `statusId` (`combobox`)
  - `typeId` (`combobox`)
- Columns (`19`):
  - `rowNumber`, `transactionId`, `accountId`, `accountName`, `accountNickname`, `accountExternalId`, `subcasinoId`, `subcasinoName`, `bankId`, `bankName`, `typeId`, `typeName`, `amount`, `startTime`, `finishTime`, `statusId`, `statusName`, `externalTransactionId`, `totalRecords`

## gameSessionSearch
- Report ID: `gameSessionSearch`
- Filters:
  - `gameSessionId` (`integer-input`)
- Columns (`21`):
  - `gameSessionId`, `accountId`, `accountName`, `accountNickname`, `accountExternalId`, `subcasinoId`, `subcasinoName`, `bankId`, `bankName`, `platformId`, `platformName`, `gameId`, `gameTitle`, `startTime`, `endTime`, `income`, `payout`, `betsCount`, `gainLoss`, `startBalance`, `endBalance`

## walletOperationAlerts
- Report ID: `walletOperationAlerts`
- Filters:
  - `dateRangePicker` (`date-range-picker`)
  - `bankPicker` (`bank-picker`)
  - `gamePicker` (`multi-select`)
  - `nickname` (`text-input`)
  - `extId` (`text-input`)
  - `status` (`combobox`)
- Columns (`25`):
  - `rowNumber`, `alertId`, `process`, `nickname`, `accountExtId`, `subcasinoName`, `bankName`, `gameName`, `alertTime`, `amount`, `currency`, `operationType`, `status`, `transactionId`, `externalTransactionId`, `roundId`, `gameSessionId`, `lastRequest`, `bankId`, `subcasinoId`, `clusterId`, `accountId`, `accountName`, `gameId`, `totalRecords`

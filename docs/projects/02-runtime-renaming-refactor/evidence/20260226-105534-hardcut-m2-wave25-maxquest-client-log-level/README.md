# Evidence - Hard-Cut M2 Wave 25 (MaxQuestClientLogLevel)

Date (UTC): 2026-02-26
Wave: `W25-maxquest-client-log-level`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.bank.MaxQuestClientLogLevel` -> `com.abs.casino.common.cache.data.bank.MaxQuestClientLogLevel`

Scope adjustments:
- Added explicit import for `PlayerGameSettingsType` in `BankInfo` because it was migrated in Wave 24 and is no longer in the same package.

## Scan result
- legacy refs after wave: 0
- abs refs after wave: 2

## Validation result
- required matrix commands passed: 9/9
- one environment-only rerun was needed for web-gs packaging (`cluster.properties` not set in shell session)
- detailed logs: `validation-status.txt` and `*.log`

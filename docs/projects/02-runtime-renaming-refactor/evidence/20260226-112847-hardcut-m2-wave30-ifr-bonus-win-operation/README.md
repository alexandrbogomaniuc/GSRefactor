# Evidence - Hard-Cut M2 Wave 30 (IFRBonusWinOperation)

Date (UTC): 2026-02-26
Wave: `W30-ifr-bonus-win-operation`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.payment.frb.IFRBonusWinOperation` -> `com.abs.casino.common.cache.data.payment.frb.IFRBonusWinOperation`

Scope adjustments:
- Updated dependent imports in `IFRBonusWin`, `FRBWinOperation`, and `FRBRESTClient`.
- Added explicit `FRBWinOperationStatus` import inside migrated interface for compatibility while status enum remains in `com.dgphoenix`.

## Scan result
- legacy refs after wave: 0
- abs refs after wave: 3

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`

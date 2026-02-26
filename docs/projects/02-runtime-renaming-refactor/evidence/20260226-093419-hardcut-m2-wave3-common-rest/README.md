# Evidence - Hard-Cut M2 Wave 3 (Common REST Package)

Date (UTC): 2026-02-26
Wave: \'W3-common-rest\'

## Scope
Migrated package/import references:
- from `com.dgphoenix.casino.common.rest`
- to `com.abs.casino.common.rest`

## Key files
- package declarations:
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/rest/CustomRestTemplate.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/rest/AddableHttpRequest.java`
  - `gs-server/common/src/main/java/com/dgphoenix/casino/common/rest/CustomResponseErrorHandler.java`
- import consumers:
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/CanexCWClient.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/CustomRESTCWClient.java`
  - `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/client/v4/StandardJsonCWClient.java`
  - `gs-server/common-wallet/src/test/java/com/dgphoenix/casino/payment/wallet/client/v4/CanexCWClientTest.java`

## Scan result
- legacy refs after wave: 0
- abs refs after wave: 7

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: see `validation-status.txt` and `*.log`

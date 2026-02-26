# RENAME-FINAL Mini-Wave M1.2 (XML/Factory Classloader Compatibility)

## Change scope
Converted dynamic class loading in XML parser/factory utilities from direct `Class.forName` to compatibility alias loader.

Changed files:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/sb-utils/src/main/java/com/dgphoenix/casino/common/util/xml/parser/XmlHandlerRegistry.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/sb-utils/src/main/java/com/dgphoenix/casino/common/util/xml/parser/XmlHandler.java`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/sb-utils/src/main/java/com/dgphoenix/casino/common/util/test/api/ClientFactory.java`

## Why
These paths instantiate classes from string names. During staged rename (`com.dgphoenix.*` -> `com.abs.*`), direct loading can fail even when compatible classes exist. Alias-aware loading keeps runtime behavior stable.

## Implementation detail
- Replaced:
  - `Class.forName(...)`
- With:
  - `ReflectionUtils.forNameWithCompatibilityAliases(...)`

## Validation
- `mvn test` in `sb-utils`: PASS (`57` tests, `0` failures, `0` errors)
- Full matrix PASS:
  - promo/common install
  - cache tests (`63` pass)
  - web-gs package
  - mp subset package

Logs:
- `mvn-sb-utils-test.log`
- `mvn-gs-promo-persisters-install.log`
- `mvn-gs-common-persisters-install.log`
- `mvn-gs-cache-test.log`
- `mvn-web-gs-package.log`
- `mvn-mp-core-interfaces-core-persistance-package.log`

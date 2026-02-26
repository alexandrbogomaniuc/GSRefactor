# RENAME-FINAL Mini-Wave M1.1 (Support Classloader Compatibility)

## Change
Updated support configuration reflection flow to resolve class names through compatibility alias loader instead of direct `Class.forName`.

Changed file:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/configuration/ServerConfigurationAction.java`

## Why
Support configuration actions can process class-string values during admin flows. Using alias-aware loading keeps these flows compatible with staged rename (`com.abs.*` <-> `com.dgphoenix.*`) without forcing immediate config rewrites.

## What was changed
- Added `ReflectionUtils` import.
- Replaced direct `Class.forName(className)` calls with `ReflectionUtils.forNameWithCompatibilityAliases(className)`.
- Reused resolved `configClass` instance in getter/setter reflection path.

## Validation
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `web-gs`: PASS
- log: `mvn-web-gs-package.log`

## Scope note
- No runtime business logic changes.
- No bank/template payload keys changed in this mini-wave.

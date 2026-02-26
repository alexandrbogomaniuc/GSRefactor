# Hard-Cut M2 Wave 1 Report (Annotations)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W1-annotations`
Status: `COMPLETE`

## Scope
Changed only annotation package/import references:
- from `com.dgphoenix.casino.tools.annotations`
- to `com.abs.casino.tools.annotations`

## Files changed
- `gs-server/annotations/src/main/java/com/dgphoenix/casino/tools/annotations/IgnoreValidation.java`
- `gs-server/annotations/src/main/java/com/dgphoenix/casino/tools/annotations/Preset.java`
- `gs-server/annotations/src/main/java/com/dgphoenix/casino/tools/annotations/Transient.java`
- `gs-server/kryo-validator/src/main/java/com/dgphoenix/casino/tools/kryo/InstanceCreator.java`
- `gs-server/kryo-validator/src/main/java/com/dgphoenix/casino/tools/kryo/generator/EnumGenerator.java`
- `gs-server/kryo-validator/src/main/java/com/dgphoenix/casino/tools/kryo/generator/StringGenerator.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/correct/CompositeEntity.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/correct/PredefinedEntity.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/correct/RootClass.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/correct/SimpleEntity.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/correct/StatelessClass.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/correct/SubClass.java`
- `gs-server/kryo-validator/src/test/java/com/dgphoenix/casino/tools/kryo/custom/ClassWithCustomConstructor.java`
- `gs-server/common-promo/src/main/java/com/dgphoenix/casino/common/promo/MaxBalanceTournamentPromoTemplate.java`
- `gs-server/common-promo/src/main/java/com/dgphoenix/casino/common/promo/TotalWagerTournamentPromoTemplate.java`
- `gs-server/common-promo/src/main/java/com/dgphoenix/casino/common/promo/TournamentPromoTemplate.java`
- `gs-server/utils/src/main/java/com/dgphoenix/casino/common/feeds/FeedQueue.java`

## Verification result
Evidence folder:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-092251-hardcut-m2-wave1-annotations`

Key results:
- legacy annotation package refs in `gs-server`: `0`
- new `com.abs` annotation refs in `gs-server`: `18`
- validation commands with `BUILD SUCCESS`: `6`

Validated commands:
- `mvn -DskipTests install` in `gs-server/annotations`
- `mvn test` in `gs-server/kryo-validator`
- `mvn test` in `gs-server/utils`
- `mvn -DskipTests install` in `gs-server/common-promo`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Risk and rollback
- Risk level: low.
- This wave only changes annotation package/import names.
- Rollback is single commit revert if needed.

## Next wave proposal
- M2 Wave 2: migrate one additional low-risk, narrow package family with the same guardrails (pre-scan -> edit -> full matrix -> evidence).

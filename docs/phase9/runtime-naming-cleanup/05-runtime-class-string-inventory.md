# Runtime Class-String Inventory (Code)

Date (UTC): 2026-02-25

## Purpose
Create an exact code-level inventory of runtime-sensitive naming references that can break behavior if renamed blindly.

## Scan Scope
- Java runtime code only (`src/main/java`)
- Excluded docs and tests
- Focus tokens:
  - `Class.forName(...)`
  - bank-config class-string loaders (`WPM_CLASS`, `START_GAME_PROCESSOR`, `CLOSE_GAME_PROCESSOR`)
  - executable `MQ_*` key usage

## Scan Summary
- `Class.forName` calls: 38 (26 files)
- Bank-config class-string accessor hits: 23 (5 files)
- Executable `MQ_*` key usage hits: 22 (5 files)

## Highest-Risk Code Hotspots
1. `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java` (lines around 1039, 1062)  
   Why risky: loads start/close processors by class string at runtime.
2. `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/wallet/WalletProtocolFactory.java` (line around 304)  
   Why risky: wallet manager class (`WPM_CLASS`) is instantiated by class string.
3. `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java` (multiple key access points)  
   Why risky: central source of runtime class keys and `MQ_*` settings consumed across GS/MP.
4. `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/kafka/service/KafkaRecieverService.java` (lines around 200, 321)  
   Why risky: deserializes message types via runtime class name from payload metadata.
5. `mp-server/web/src/main/java/com/betsoft/casino/mp/kafka/KafkaRecieverService.java` (lines around 196, 320)  
   Why risky: same runtime deserialization risk on MP side.
6. `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/engine/GameEngineManager.java` (line around 62)  
   Why risky: engine implementation loaded by class name.
7. `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/transfer/processor/PaymentProcessorFactory.java` (line around 41)  
   Why risky: payment processor class loaded from config.
8. `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/socket/mq/MQServiceHandler.java` (multiple lines around 327-356)  
   Why risky: publishes `MQ_*` settings into MP-facing runtime payload.
9. `mp-server/web/src/main/java/com/betsoft/casino/mp/web/handlers/lobby/EnterLobbyHandler.java` (lines around 669, 687, 790, 792)  
   Why risky: consumes `MQ_*` keys in runtime lobby/game flow.
10. `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/configuration/ServerConfigurationAction.java` (multiple reflection lines)  
    Why risky: support UI validates/invokes class names dynamically.

## Newly Completed Compatibility Work (already merged)
- RN2 Wave A: `BankInfo` alias reads for selected `MQ_*` keys (`feed2f3f`)
- RN2 Wave B: compatibility class loading (`com.abs.*` <-> `com.dgphoenix.*`) in critical runtime loaders (`1045b5ec`)

## Evidence Files
- `docs/phase9/runtime-naming-cleanup/evidence/20260225-class_refs.txt`
- `docs/phase9/runtime-naming-cleanup/evidence/20260225-mq_refs.txt`


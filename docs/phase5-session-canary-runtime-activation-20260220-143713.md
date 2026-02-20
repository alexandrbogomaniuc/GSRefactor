# Phase 5 GS Canary Hook Runtime Activation (Refactor Stack)

Timestamp (UTC): 2026-02-20 14:37:13 UTC

## Goal
Activate newly added GS source hook in refactor runtime (`Dev_new`) without touching legacy `Dev` stack.

## Runtime activation steps
1. Compiled updated classes directly into refactor runtime webapp classes directory:
- Target runtime classes root:
  - `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/WEB-INF/classes`
- Command used:
```bash
ROOT='/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT'
SRC_BASE='/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java'
SERVLET_JAR='/Users/alexb/.m2/repository/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar'
CP="$ROOT/WEB-INF/classes:$SERVLET_JAR:$(printf '%s:' $ROOT/WEB-INF/lib/*.jar | sed 's/:$//')"

javac -cp "$CP" -d "$ROOT/WEB-INF/classes" \
  "$SRC_BASE/com/dgphoenix/casino/actions/enter/game/routing/SessionServiceRoutingBridge.java" \
  "$SRC_BASE/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java"
```
- Produced classes:
  - `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/WEB-INF/classes/com/dgphoenix/casino/actions/enter/game/routing/SessionServiceRoutingBridge.class`
  - `/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/webapps/gs/ROOT/WEB-INF/classes/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.class`

2. Restarted refactor GS container:
```bash
docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml restart gs
```

## Validation
- Launch/reconnect no-regression probes on facade origin (`http://localhost:18080`) after restart:
  - `/startgame?...` -> `200`, title `Max Quest: Dragonstone`
  - `/cwstartgamev2.do?...` -> `200`, title `Max Quest: Dragonstone`
  - `/restartgame.do?...` -> `200`, title `Max Quest: Dragonstone`

- Session-service runtime snapshots:
  - `/health` -> `{"status":"ok","service":"session-service",...}`
  - `/api/v1/routing/decision?bankId=271` -> `routeEnabled:false`, `canaryBanks:["6274"]`
  - `/api/v1/sessions?bankId=271` -> `{"sessions":[]}`

## Notes
- Hook is active in runtime classes, but route remains disabled in running session-service config (`routeEnabled:false`) so no shadow session writes are expected yet.
- Attempted Maven build validation is still blocked in this environment by sandboxed Maven local-repo write restrictions and missing private artifacts (`gsn-utils-restricted`, `gsn-common`).

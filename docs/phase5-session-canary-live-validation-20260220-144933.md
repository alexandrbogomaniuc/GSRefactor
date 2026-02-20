# Phase 5 Session Canary Live Validation (2026-02-20 14:49:33)

## Goal
Validate that GS launch path performs shadow session create in `session-service` when canary routing is enabled for selected bank.

## Preconditions
- `SESSION_SERVICE_ROUTE_ENABLED=true`
- `SESSION_SERVICE_CANARY_BANKS=271`
- GS refactor runtime contains canary hook classes.

## Commands and results

1. Confirm decision endpoint (inside service container)
```bash
docker exec refactor-session-service-1 sh -lc "wget -qO- 'http://127.0.0.1:18073/api/v1/routing/decision?bankId=271'"
```
Result:
```json
{"routeEnabled":true,"canaryBanks":["271"],"bankId":"271","routeToSessionService":true}
```

2. Trigger GS launch for canary bank (inside GS container)
```bash
docker exec refactor-gs-1 sh -lc "curl -sS -o /tmp/launch271.html -w 'HTTP:%{http_code}\\n' 'http://127.0.0.1:8080/cwstartgamev2.do?bankId=271&gameId=838&mode=real&token=test_user_271&lang=en'"
```
Result:
```text
HTTP:302
```

3. Verify session list after launch
```bash
docker exec refactor-session-service-1 sh -lc "wget -qO- 'http://127.0.0.1:18073/api/v1/sessions?bankId=271'"
```
Result includes newly created session:
```json
{"bankId":"271","sessionId":"1_218302aaf1cb7b81f0e20000019ca984_R1EGQWxBHgsQOUFTWksLDwY","metadata":{"source":"cwstartgame","mode":"REAL"},"history":[{"action":"CREATE","operationId":"launch:271:1_218302aaf1cb7b81f0e20000019ca984_R1EGQWxBHgsQOUFTWksLDwY"}]}
```

## Conclusion
Canary shadow-write path is active and working for bank `271` with fail-open compatibility preserved.

## Rollback
```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/set-session-canary.sh --enabled false
```

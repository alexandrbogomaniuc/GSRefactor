# Phase 5 Session Service Outbox Relay Foundation (2026-02-20 14:55:47)

## Goal
Add Kafka publication path for Session Service outbox events without breaking compatibility.

## Delivered
- Added feature-flagged outbox relay worker:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/src/outboxRelay.js`
- Wired relay startup/shutdown in service runtime:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/src/server.js`
- Added dependency:
  - `kafkajs` in `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/package.json`
- Added cluster-config keys and compose wiring:
  - `SESSION_SERVICE_KAFKA_BROKERS`
  - `SESSION_SERVICE_OUTBOX_TOPIC`
  - `SESSION_SERVICE_OUTBOX_RELAY_ENABLED`
  - `SESSION_SERVICE_OUTBOX_RELAY_POLL_MS`

## Safety mode
- Relay is `OFF` by default (`SESSION_SERVICE_OUTBOX_RELAY_ENABLED=false`).
- Legacy behavior remains unchanged.

## Verification
1. JS syntax check:
```bash
cd /Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service
node --check src/outboxRelay.js
node --check src/server.js
```
Result: `node-check:ok`

2. Cluster config sync:
```bash
cd /Users/alexb/Documents/Dev/Dev_new/gs-server
deploy/scripts/sync-cluster-hosts.sh
```
Result: `.env` and portal classpath config updated with new keys.

3. Runtime build/recreate:
```bash
docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml build session-service
docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml up -d --no-deps --force-recreate session-service
docker logs --tail 80 refactor-session-service-1
```
Result includes:
- `session-service listening on port 18073`
- `session-service outbox relay disabled`

4. Post-rebuild canary sanity:
- `GET /api/v1/routing/decision?bankId=271` -> `routeToSessionService=true`
- `cwstartgamev2.do` launch -> `HTTP:302`
- `GET /api/v1/sessions?bankId=271` shows newly created session
  - `sessionId=1_8a4fd67654b07b81f0e30000019cb3cb_R1EGQWxBHgsQOUFTWksLDwY`
  - `operationId=launch:271:1_8a4fd67654b07b81f0e30000019cb3cb_R1EGQWxBHgsQOUFTWksLDwY`

5. Kafka topic verification:
```bash
docker exec refactor-kafka-1 bash -lc "kafka-console-consumer --bootstrap-server kafka:9092 --topic abs.session.events.v1 --from-beginning --max-messages 20 --timeout-ms 10000"
```
Observed events include canary bank payloads, including latest created session:
- `bankId=271`
- `sessionId=1_049fa39c0ff07b81f0e40000019cbbe2_R1EGQWxBHgsQOUFTWksLDwY`
- `eventType=session.created`

## Next
- Enable relay only in refactor canary runtime and validate topic publication + downstream consumer compatibility before wider rollout.

## Follow-up implemented
- `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-outbox-dlq-contract-gate-20260220-151251.md`

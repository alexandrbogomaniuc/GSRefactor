# 02 Runtime Deployment

## Goal
Define a reproducible baseline startup sequence.

## Baseline Order (Proven)
1. Start infra + services via compose.
2. Start/rebuild casino-side wallet service (port `8000`) when wallet code changed.
3. Wait for GS bootstrap to pass Cassandra/ZK/Kafka checks.
3. Verify GS and MP logs for startup/failure signals.
4. Verify support UI endpoints.
5. Verify Cassandra has expected banks/games.

## Primary Compose
- File:
  - `/Users/alexb/Documents/Dev/Doker/gs-docker/configs/docker-compose.yml`
- Key startup wiring:
  - `gs` depends on `c1`, `zookeeper`, `kafka`, `mp`
  - `mp` depends on `c1`, `zookeeper`, `kafka`
  - `static` depends on `gs`

## Commands (With Expected Outcomes)

1) Start stack
- Command:
  - `cd /Users/alexb/Documents/Dev/Doker/gs-docker/configs && docker compose up -d --build`
- What it does:
  - Builds GS/static images and starts all services.
- Success looks like:
  - `docker ps` shows `gp3-gs-1`, `gp3-mp-1`, `gp3-static-1`, `gp3-kafka-1`, `gp3-c1-1`, `gp3-zookeeper-1` in `Up` state.
- Failure looks like:
  - Container exits/restarts or missing services.
- Next step:
  - Run per-container logs (`docker logs --tail 500 <container>`).

2) Check running containers
- Command:
  - `docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'`
- What it does:
  - Shows current runtime status and exposed ports.
- Success looks like:
  - GS on `81->8080`, static on `80->80`, MP on `6300/6301`, casino-side on `8000`, Cassandra on `9142->9042`.
- Failure looks like:
  - One or more expected services missing.
- Next step:
  - Restart compose and inspect missing service logs first.

3) Read GS logs (fixed size)
- Command:
  - `docker logs --tail 500 gp3-gs-1`
- What it does:
  - Reads latest GS logs without flooding.
- Success looks like:
  - Periodic task logs, no fatal exceptions, GS keeps running.
- Failure looks like:
  - Crash loop, repeated fatal exception blocks, no ongoing scheduler activity.
- Next step:
  - Run filtered grep command below.

4) Filter GS logs for startup/errors
- Command:
  - `/bin/zsh -lc "docker logs --tail 500 gp3-gs-1 2>&1 | rg -i 'error|exception|caused by|fail|invalid|started|initializ|keyspace|mq|maxquest|mp'"`
- What it does:
  - Extracts signal lines from noisy logs.
- Success looks like:
  - Informational startup lines plus only known non-blocking warnings.
- Failure looks like:
  - Repeated `Exception` / `Caused by` tied to startup path.
- Next step:
  - Trace each exception class and source class in code.

5) Read MP logs (fixed size)
- Command:
  - `docker logs --tail 500 gp3-mp-1`
- What it does:
  - Reads latest MP logs.
- Success looks like:
  - Stable periodic logs (`WatchServersThreadMaster changedServers={}`), no crash loop.
- Failure looks like:
  - Fatal exception bursts or frequent process restarts.
- Next step:
  - Check websocket listeners and restart MP if needed.

6) (If wallet code changed) rebuild casino-side
- Command:
  - `docker compose -f "/Users/alexb/Documents/Dev/Casino side/inst_app/docker-compose.yml" up -d --build`
- What it does:
  - Rebuilds and recreates casino-side API container with latest endpoint code.
- Success looks like:
  - `casino_side` is `Up`, and `/bav/*` endpoints return `200` on test calls.
- Failure looks like:
  - Old behavior remains (for example `422 int_parsing` for token-style `userId`).
- Next step:
  - Verify running code signature in-container and retest endpoints.
  - Isolate first exception and map to MP startup class.

6) Verify bank support page
- Command:
  - `curl -i -s http://localhost:81/support/bankSupport.do`
- What it does:
  - Checks GS support UI response and visible bank list.
- Success looks like:
  - `HTTP/1.1 200 OK` and options include `DEFAULT BANK`, `TEST_BAV`, `TEST_BAV_VND`, `Default`.
- Failure looks like:
  - Non-200 status or only partially loaded bank list.
- Next step:
  - Check GS startup log period and BankInfo load count lines.

7) Verify game-bank mappings in Cassandra
- Command:
  - `docker exec -i gp3-c1-1 cqlsh -e "SELECT key, bankidx, bankandcuridx FROM rcasinoscks.gameinfocf;"`
- What it does:
  - Confirms which games are mapped to which banks/currencies.
- Success looks like:
  - Rows include `6274+838` with `bankidx=6274` and `bankandcuridx=6274_USD`.
- Failure looks like:
  - Missing rows or null index fields.
- Next step:
  - Re-apply target CQL and restart GS.

## Current Known-Good Signals (2026-02-10)
- GS log has:
  - `Initialization was successfully completed`
  - `Initializer: ALL INITIALIZED`
- MP log is stable with periodic housekeeping and no fatal crash loop.

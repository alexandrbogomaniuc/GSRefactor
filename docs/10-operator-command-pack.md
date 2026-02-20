# 10 Operator Command Pack

## Goal
Fast, repeatable commands for local baseline checks with clear success/failure signals.

## 1) Check containers
Command:
```bash
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
```

What it does:
- Shows running containers, uptime, and exposed ports.

Success looks like:
- `gp3-gs-1`, `gp3-mp-1`, `gp3-c1-1`, `gp3-kafka-1`, `gp3-zookeeper-1`, `gp3-static-1` are `Up`.

Failure looks like:
- One or more containers missing or `Exited`.

Next:
- Start runtime compose stack, then re-run this command.

## 2) GS log tail (fixed size)
Command:
```bash
docker logs --tail 500 gp3-gs-1
```

What it does:
- Prints last 500 GS log lines.

Success looks like:
- Regular periodic activity logs, no startup crash loop.

Failure looks like:
- Repeated fatal exceptions or restart loop messages.

Next:
- Run filtered error scan (command #4), then inspect related source class.

## 3) MP log tail (fixed size)
Command:
```bash
docker logs --tail 500 gp3-mp-1
```

What it does:
- Prints last 500 MP log lines.

Success looks like:
- Stable periodic tasks and no fatal startup errors.

Failure looks like:
- Repeated `Exception`, `Caused by`, bind/listener failures.

Next:
- Run filtered error scan (command #5), then inspect MP startup config.

## 4) GS filtered error scan
Command:
```bash
docker logs --tail 500 gp3-gs-1 2>&1 | rg -i 'error|exception|caused by|fail|invalid|started|initializ|keyspace|mq|maxquest|mp'
```

What it does:
- Finds likely startup/flow problems quickly in GS logs.

Success looks like:
- Only known non-blocking warnings (for example transient NTP timeout).

Failure looks like:
- Business-flow exceptions (for example `NumberFormatException` in support action).

Next:
- Open referenced class + line and reproduce with minimal request.

## 5) MP filtered error scan
Command:
```bash
docker logs --tail 500 gp3-mp-1 2>&1 | rg -i 'error|exception|caused by|critical|unable|serverid|started|startreactorserver|startjettyserver|bind|listening'
```

What it does:
- Pulls critical MP startup/runtime signals from noisy logs.

Success looks like:
- No fatal exceptions, normal `started`/heartbeat lines.

Failure looks like:
- Bind/listen errors, startup exceptions, persistent connectivity failures.

Next:
- Validate MP ports and dependency connectivity (Cassandra/Kafka).

## 6) List game mappings by bank
Command:
```bash
docker exec -i gp3-c1-1 cqlsh -e 'SELECT key, bankidx, bankandcuridx FROM rcasinoscks.gameinfocf;'
```

What it does:
- Shows which game rows exist per bank.

Success looks like:
- Expected keys (for example `6274+838`) are present with non-null indexed columns.

Failure looks like:
- Missing expected key or null `bankidx`/`bankandcuridx`.

Next:
- Apply/repair seed CQL for missing mapping, then restart GS.

## 7) Inspect a specific bank JSON
Command:
```bash
docker exec -i gp3-c1-1 cqlsh -e "SELECT key, jcn FROM rcasinoscks.bankinfocf WHERE key=6274;"
```

What it does:
- Returns full bank config JSON (`jcn`) used by GS.

Success looks like:
- Required properties present (for example `ALLOWED_ORIGIN`, `ALLOWED_DOMAINS`, `START_GAME_DOMAIN`).

Failure looks like:
- Row missing or critical property absent/wrong.

Next:
- Update with support UI or CQL patch, then verify in GS behavior.

## 8) Support UI checks (browser)
URLs:
- `http://localhost:81/support/bankSupport.do`
- `http://localhost:81/support/bankSelectAction.do?bankId=6274`
- `http://localhost:81/support/bankSelectAction.do?bankId=271`

What it does:
- Verifies bank list and bank property editor paths.

Success looks like:
- `bankSupport.do` shows bank dropdown.
- `bankSelectAction.do?bankId=<id>` opens edit page.

Failure looks like:
- `HTTP 500` when `bankId` is missing.

Next:
- Always use `bankSelectAction.do` with explicit `bankId`.

## 9) Add missing bank-game mapping (example: 6275+838)
Command:
```bash
docker exec -i gp3-c1-1 cqlsh < /Users/alexb/Documents/Dev/docs/sql/add_game_838_for_bank_6275.cql
docker exec -i gp3-c1-1 cqlsh -e "SELECT key,bankidx,bankandcuridx FROM rcasinoscks.gameinfocf WHERE key='6275+838';"
```

What it does:
- Adds the game row for bank `6275` and verifies indexed fields.

Success looks like:
- Query returns `6275+838` with `bankidx=6275` and `bankandcuridx=6275_VND`.

Failure looks like:
- `0 rows` or wrong index values.

Next:
- Restart GS and confirm no startup warnings about missing bank/game mapping.

## 10) Launch smoke test (both banks, same game)
Command:
```bash
docker exec gp3-gs-1 /bin/sh -lc "wget -S -O- 'http://localhost:8080/free/mp/template.jsp?BANKID=6274&SID=bav_game_session_001&gameId=838&LANG=en' 2>&1 | head -n 30"
docker exec gp3-gs-1 /bin/sh -lc "wget -S -O- 'http://localhost:8080/free/mp/template.jsp?BANKID=6275&SID=bav_game_session_002&gameId=838&LANG=en' 2>&1 | head -n 30"
docker exec gp3-gs-1 /bin/sh -lc "wget -S -O- 'http://localhost:8080/free/mp/template.jsp?bankId=6275&sessionId=bav_game_session_002&gameId=838&lang=en' 2>&1 | head -n 30"
```

What it does:
- Verifies launch behavior for both configured banks and confirms invalid param-shape handling.

Success looks like:
- First two calls: `HTTP/1.1 200 OK`.
- Third call: `HTTP/1.1 302 Found` to `/error_pages/sessionerror.jsp`.

Failure looks like:
- `500`, missing game template content, or no redirect on invalid param shape.

Next:
- If valid calls fail, re-check `gameinfocf` rows (`6274+838`, `6275+838`) and bank/domain settings.

## 11) Approval-friendly command set (recommended)
Use these command families to avoid repeated approvals:
- `docker ps`
- `docker logs --tail 500 <container>`
- `docker exec -i gp3-c1-1 cqlsh ...`
- `docker exec gp3-gs-1 /bin/sh -lc \"wget -S -O- 'http://localhost:8080/...template.jsp?...'\"`
- `curl -s http://localhost:81/support/...`

What it does:
- Covers health checks, log triage, Cassandra verification/patching, launch smoke tests, and support UI checks.

Success looks like:
- Most daily diagnostics can run without additional approval prompts.

Failure looks like:
- A new command pattern not covered by these families still triggers approval.

Next:
- If needed, add one more approved prefix for any missing recurring command family.

## 12) Wallet identity verification and pending-op cleanup
Command:
```bash
docker exec -i gp3-c1-1 cqlsh -e "SELECT bankid, extid, accountid FROM rcasinoscks.accountcf_ext WHERE bankid=6274 AND extid IN ('8','bav_game_session_001');"
docker exec gp3-gs-1 /bin/sh -lc "wget -S -O- 'http://localhost:8080/tools/walletsManager.do?accountId=&subcasinoId=507&bankId=6274&extUserId=bav_game_session_001&gameId=&accountData=show' 2>&1 | head -n 120"
```

What it does:
- Confirms which external id is currently mapped to account id.
- Opens walletsManager data for that ext id to inspect/delete stuck pending transactions.

Success looks like:
- Cassandra returns mapped row (`bav_game_session_001 -> 40962` in current environment).
- walletsManager returns `200` and shows pending operation rows.

Failure looks like:
- No mapping row for expected ext id, or walletsManager shows `Cannot find the player`.

Next:
- Use the ext id that actually exists in `accountcf_ext` (or `accountId` directly), then clear stale pending operation once and re-test full flow.

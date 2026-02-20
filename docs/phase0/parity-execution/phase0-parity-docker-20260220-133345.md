# Phase 0 Parity Execution Report (Docker-local GS probe)

- Timestamp (UTC): 2026-02-20 13:33:45
- Probe mode: run-via-docker-exec
- GS target: refactor-gs-1 -> http://127.0.0.1:8080
- Fixture file: /Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-fixture.env

| Test ID | Flow | Status | HTTP | Evidence |
|---|---|---|---|---|
| P0-LA-01 | Launch | FAIL_CONTRACT | 302 | `P0-LA-01-20260220-133345.docker.body.txt` |
| P0-LA-02 | LaunchInvalidParams | PASS_CONTRACT | 200 | `P0-LA-02-20260220-133345.docker.body.txt` |
| P0-LA-03 | LaunchAlias | FAIL_HTTP | 404 | `P0-LA-03-20260220-133345.docker.body.txt` |
| P0-WA-01 | Wager | PASS_CONTRACT | 200 | `P0-WA-01-20260220-133345.docker.body.txt` |
| P0-WA-00 | WagerInvalidParams | PASS_CONTRACT | 200 | `P0-WA-00-20260220-133345.docker.body.txt` |
| P0-SE-01 | Settle | PASS_CONTRACT | 200 | `P0-SE-01-20260220-133345.docker.body.txt` |
| P0-SE-00 | SettleInvalidParams | PASS_CONTRACT | 200 | `P0-SE-00-20260220-133345.docker.body.txt` |

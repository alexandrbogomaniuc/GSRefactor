# Phase 2 Correlation Probe

- Timestamp (UTC): 2026-02-20 10:05:40
- Base URL: http://127.0.0.1:18080
- Target URL: http://127.0.0.1:18080/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=invalid_probe_token&lang=en
- HTTP: 200

## Request headers sent
- X-Trace-Id: probe-trace-001
- X-Session-Id: probe-session-001
- X-Bank-Id: 6274
- X-Game-Id: 838
- X-Operation-Id: probe-op-001
- X-Config-Version: cfg-probe-001

## Echo validation
| Header | Status |
|---|---|
| X-Trace-Id | FAIL |
| X-Session-Id | FAIL |
| X-Operation-Id | FAIL |
| X-Config-Version | FAIL |

## Artifacts
- Response headers: `/Users/alexb/Documents/Dev/Dev_new/docs/phase2/correlation-probes/correlation-headers-20260220-100539.txt`
- Response body: `/Users/alexb/Documents/Dev/Dev_new/docs/phase2/correlation-probes/correlation-body-20260220-100539.txt`

# Phase 4 Protocol Runtime Evidence (20260220-175027 UTC)

- bankId: 6275
- transport: host
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- parity_check: FAIL
- wallet_shadow_probe: FAIL

## Parity Check Output
```text
curl: (7) Failed to connect to 127.0.0.1 port 18078 after 0 ms: Couldn't connect to server
```

## Wallet Shadow Probe Output
```text
curl: (7) Failed to connect to 127.0.0.1 port 18081 after 0 ms: Couldn't connect to server
grep: /var/folders/7b/h207_tk50f5d2xyw6wxg0_mw0000gn/T/tmp.qaeq1JCb6P/launch.body: No such file or directory
FAIL: could not auto-resolve sessionId from startgame launch
Launch headers:
Launch body snippet:
sed: /var/folders/7b/h207_tk50f5d2xyw6wxg0_mw0000gn/T/tmp.qaeq1JCb6P/launch.body: No such file or directory
```

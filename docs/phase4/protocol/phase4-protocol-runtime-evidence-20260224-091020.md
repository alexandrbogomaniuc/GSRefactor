# Phase 4 Protocol Runtime Evidence (20260224-091020 UTC)

- bankId: 6275
- transport: host
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- allowMissingRuntime: true
- runtime_readiness: FAIL
- parity_check: SKIP_RUNTIME_NOT_READY
- wallet_shadow_probe: SKIP_RUNTIME_NOT_READY
- json_security_probe: SKIPPED
- note: runtime probes skipped because readiness failed and allowMissingRuntime=true

## Runtime Readiness Output
```text
Phase 4 Runtime Readiness
  protocol: 127.0.0.1:18078
  gs:       127.0.0.1:18081
FAIL protocol endpoint unreachable (127.0.0.1:18078)
FAIL gs endpoint unreachable (127.0.0.1:18081)
NOT_READY: fix failed checks before running phase4 evidence pack
```

## Parity Check Output
```text
Runtime probes skipped because readiness_status=FAIL and --allow-missing-runtime=true.
```

## Wallet Shadow Probe Output
```text
Runtime probes skipped because readiness_status=FAIL and --allow-missing-runtime=true.
```

## JSON Security Probe Output
```text
Runtime probes skipped because readiness_status=FAIL and --allow-missing-runtime=true.
```

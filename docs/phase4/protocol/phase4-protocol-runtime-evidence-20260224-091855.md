# Phase 4 Protocol Runtime Evidence (20260224-091855 UTC)

- bankId: 6275
- transport: docker
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- allowMissingRuntime: true
- runtime_readiness: SKIP_DOCKER_API_DENIED
- parity_check: SKIP_RUNTIME_NOT_READY
- wallet_shadow_probe: SKIP_RUNTIME_NOT_READY
- json_security_probe: SKIPPED
- note: runtime probes skipped because readiness failed and allowMissingRuntime=true

## Runtime Readiness Output
```text
Phase 4 Runtime Readiness
  transport: docker
  protocol-container: refactor-protocol-adapter-1
  gs-container:       refactor-gs-1
permission denied while trying to connect to the docker API at unix:///Users/alexb/.docker/run/docker.sock
FAIL protocol endpoint unreachable (container-local refactor-protocol-adapter-1:18078)
permission denied while trying to connect to the docker API at unix:///Users/alexb/.docker/run/docker.sock
FAIL gs endpoint unreachable (container-local refactor-gs-1:8080)
permission denied while trying to connect to the docker API at unix:///Users/alexb/.docker/run/docker.sock
FAIL docker socket not accessible
NOT_READY: fix failed checks before running phase4 evidence pack
```

## Parity Check Output
```text
Runtime probes skipped because readiness_status=SKIP_DOCKER_API_DENIED and --allow-missing-runtime=true.
```

## Wallet Shadow Probe Output
```text
Runtime probes skipped because readiness_status=SKIP_DOCKER_API_DENIED and --allow-missing-runtime=true.
```

## JSON Security Probe Output
```text
Runtime probes skipped because readiness_status=SKIP_DOCKER_API_DENIED and --allow-missing-runtime=true.
```

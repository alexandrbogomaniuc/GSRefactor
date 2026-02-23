# Phase 5/6 Local Verification Suite (20260223-132527 UTC)

- scope: offline/local validation for recently implemented refactor services and tooling
- pass: 14
- fail: 0
- skip: 0

## Summary
- [PASS] Bash syntax: Phase 5 bonus/FRB scripts
- [PASS] Bash syntax: Phase 5 history scripts
- [PASS] Bash syntax: Phase 6 multiplayer scripts
- [PASS] CLI help: Phase 5 bonus/FRB evidence-pack
- [PASS] CLI help: Phase 5 history evidence-pack
- [PASS] CLI help: Phase 6 multiplayer routing-policy probe
- [PASS] CLI help: Phase 6 multiplayer evidence-pack
- [PASS] Executable logic smoke: Phase 5/6 stores and multiplayer policy
- [PASS] Node syntax: bonus-frb-service
- [PASS] Node syntax: history-service
- [PASS] Node syntax: multiplayer-service
- [PASS] JSON parse: modernization checklist
- [PASS] Git whitespace check
- [PASS] Compose config services (refactor stack)

## Outputs
### Bash syntax: Phase 5 bonus/FRB scripts
- status: PASS
```text

```

### Bash syntax: Phase 5 history scripts
- status: PASS
```text

```

### Bash syntax: Phase 6 multiplayer scripts
- status: PASS
```text

```

### CLI help: Phase 5 bonus/FRB evidence-pack
- status: PASS
```text
Usage: phase5-bonus-frb-runtime-evidence-pack.sh [options]

Options:
  --bank-id ID               Default: 6275
  --transport MODE           host|docker (default: host)
  --bonus-base-url URL       Default: http://127.0.0.1:18076
  --readiness-bonus-host H   Default: 127.0.0.1
  --readiness-bonus-port P   Default: 18076
  --readiness-gs-host H      Default: 127.0.0.1
  --readiness-gs-port P      Default: 18081
  --check-docker BOOL        true|false (default: true)
  --out-dir DIR              Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase5/bonus-frb
  -h, --help                 Show this help

```

### CLI help: Phase 5 history evidence-pack
- status: PASS
```text
Usage: phase5-history-runtime-evidence-pack.sh [options]

Options:
  --bank-id ID               Default: 6275
  --transport MODE           host|docker (default: host)
  --history-base-url URL     Default: http://127.0.0.1:18077
  --readiness-history-host H Default: 127.0.0.1
  --readiness-history-port P Default: 18077
  --readiness-gs-host H      Default: 127.0.0.1
  --readiness-gs-port P      Default: 18081
  --check-docker BOOL        true|false (default: true)
  --out-dir DIR              Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase5/history
  -h, --help                 Show this help

```

### CLI help: Phase 6 multiplayer routing-policy probe
- status: PASS
```text
Usage: phase6-multiplayer-routing-policy-probe.sh [options]

Options:
  --bank-id ID                    Default: 6275
  --game-id ID                    Default: 838
  --session-id ID                 Optional (auto-generated if empty)
  --transport MODE                host|docker (default: host)
  --multiplayer-base-url URL      Default: http://127.0.0.1:18079
  --multiplayer-container NAME    Default: refactor-multiplayer-service-1
  --expect-bank-mp-enabled BOOL   true|false (default: false)
  --expect-non-mp-reason VALUE    Default: non_multiplayer_game
  --expect-mp-reason VALUE        Default: bank_multiplayer_disabled
  -h, --help                      Show this help

```

### CLI help: Phase 6 multiplayer evidence-pack
- status: PASS
```text
Usage: phase6-multiplayer-runtime-evidence-pack.sh [options]

Options:
  --bank-id ID                   Default: 6275
  --game-id ID                   Default: 838
  --transport MODE               host|docker (default: host)
  --multiplayer-base-url URL     Default: http://127.0.0.1:18079
  --run-sync-canary BOOL         true|false (default: false)
  --readiness-multiplayer-host H Default: 127.0.0.1
  --readiness-multiplayer-port P Default: 18079
  --readiness-gs-host H          Default: 127.0.0.1
  --readiness-gs-port P          Default: 18081
  --check-docker BOOL            true|false (default: true)
  --out-dir DIR                  Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase6/multiplayer
  -h, --help                     Show this help

```

### Executable logic smoke: Phase 5/6 stores and multiplayer policy
- status: PASS
```text
PASS bonus-frb store smoke
PASS history store smoke
PASS multiplayer store smoke
PASS multiplayer policy smoke
PASS: phase5/6 local logic smoke suite

```

### Node syntax: bonus-frb-service
- status: PASS
```text

```

### Node syntax: history-service
- status: PASS
```text

```

### Node syntax: multiplayer-service
- status: PASS
```text

```

### JSON parse: modernization checklist
- status: PASS
```text
OK

```

### Git whitespace check
- status: PASS
```text

```

### Compose config services (refactor stack)
- status: PASS
```text
time="2026-02-23T13:25:29Z" level=warning msg="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
wallet-adapter bonus-frb-service c1 protocol-adapter zookeeper kafka mp gs static history-service session-service redis gameplay-orchestrator multiplayer-service config-service
```

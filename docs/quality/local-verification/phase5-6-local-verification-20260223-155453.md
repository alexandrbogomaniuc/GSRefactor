# Phase 5/6 Local Verification Suite (20260223-155453 UTC)

- scope: offline/local validation for recently implemented refactor services and tooling
- pass: 34
- fail: 0
- skip: 0

## Summary
- [PASS] CLI help: Phase 4 protocol security logic smoke
- [PASS] CLI help: Phase 4 protocol JSON security runtime canary
- [PASS] CLI help: Phase 8 precision/min-bet audit scanner
- [PASS] CLI help: Phase 8 precision regression vector smoke
- [PASS] CLI help: Phase 8 precision remediation buckets
- [PASS] CLI help: Phase 8 Wave 1 reporting/display vector smoke
- [PASS] CLI help: Phase 8 Wave 1 NumberUtils.asMoney parity smoke
- [PASS] CLI help: Phase 8 Wave 2 settings/coin-rule vector smoke
- [PASS] CLI help: Phase 8 Wave 3 dual-calculation comparison vector smoke
- [PASS] Executable logic smoke: Phase 4 protocol hash/replay security
- [PASS] Executable logic smoke: Phase 8 precision regression vectors
- [PASS] Executable logic smoke: Phase 8 precision remediation buckets
- [PASS] Executable logic smoke: Phase 8 Wave 1 reporting/display vectors
- [PASS] Executable logic smoke: Phase 8 Wave 1 NumberUtils.asMoney parity
- [PASS] Executable logic smoke: Phase 8 Wave 2 settings/coin-rule vectors
- [PASS] Executable logic smoke: Phase 8 Wave 3 dual-calculation comparison vectors
- [PASS] Bash syntax: Phase 5 bonus/FRB scripts
- [PASS] Bash syntax: Phase 5 history scripts
- [PASS] Bash syntax: Phase 5 wallet scripts
- [PASS] Bash syntax: Phase 5 gameplay scripts
- [PASS] Bash syntax: Phase 6 multiplayer scripts
- [PASS] CLI help: Phase 5 bonus/FRB evidence-pack
- [PASS] CLI help: Phase 5 history evidence-pack
- [PASS] CLI help: Phase 5 wallet evidence-pack
- [PASS] CLI help: Phase 5 gameplay evidence-pack
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
### CLI help: Phase 4 protocol security logic smoke
- status: PASS
```text
Usage: phase4-protocol-security-logic-smoke.sh [options]

Options:
  --work-dir DIR   Optional temp work dir (default: auto mktemp)
  -h, --help       Show this help

```

### CLI help: Phase 4 protocol JSON security runtime canary
- status: PASS
```text
Usage: phase4-protocol-json-security-canary-probe.sh [options]

Options:
  --bank-id ID              Default: 6275
  --base-url URL            Default: http://127.0.0.1:18078
  --hmac-secret VALUE       Optional non-prod test secret for local/runtime validation
  --require-secret BOOL     true|false (default: false)
  --enforcement-mode MODE   SHADOW|ENFORCE (default: ENFORCE)
  --timestamp UNIX_SEC      Optional fixed timestamp for probe requests (default: now)
  -h, --help                Show this help

```

### CLI help: Phase 8 precision/min-bet audit scanner
- status: PASS
```text
Usage: phase8-precision-minbet-audit-scan.sh [options]

Options:
  --gs-root DIR   GS root to scan (default: /Users/alexb/Documents/Dev/Dev_new/gs-server)
  --out-dir DIR   Output dir (default: /Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision)
  -h, --help      Show help

```

### CLI help: Phase 8 precision regression vector smoke
- status: PASS
```text
Usage: phase8-precision-regression-vector-smoke.sh [options]

Options:
  --scale N       Decimal scale for test vectors (default: 3)
  -h, --help      Show help

```

### CLI help: Phase 8 precision remediation buckets
- status: PASS
```text
Usage: phase8-precision-remediation-buckets.sh [options]

Options:
  --root DIR      Project root (default: /Users/alexb/Documents/Dev/Dev_new)
  --gs-root DIR   GS root (default: /Users/alexb/Documents/Dev/Dev_new/gs-server)
  --out-dir DIR   Output dir (default: /Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision)
  -h, --help      Show help

```

### CLI help: Phase 8 Wave 1 reporting/display vector smoke
- status: PASS
```text
Usage: phase8-precision-wave1-reporting-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 Wave 1 (reporting/display)
cent conversions and 2-decimal rounding boundaries.

```

### CLI help: Phase 8 Wave 1 NumberUtils.asMoney parity smoke
- status: PASS
```text
Usage: phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh

Deterministic parity smoke for NumberUtils.asMoney legacy cent rounding semantics
(Math.round-based), including negative half-cent edge cases.

```

### CLI help: Phase 8 Wave 2 settings/coin-rule vector smoke
- status: PASS
```text
Usage: phase8-precision-wave2-settings-coinrule-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 Wave 2 (game settings / coin-rule assumptions),
covering line-based base-bet normalization and nearest-coin selection under scale 2 and scale 3.

```

### CLI help: Phase 8 Wave 3 dual-calculation comparison vector smoke
- status: PASS
```text
Usage: phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 Wave 3 (dual-calculation comparison scaffold).
Validates that the new generalized precision helpers preserve legacy scale=2 behavior and
provides deterministic delta visibility for future scale=3 enablement (comparison only).

```

### Executable logic smoke: Phase 4 protocol hash/replay security
- status: PASS
```text
PASS protocol hash smoke (POST rawBody HMAC)
PASS protocol hash smoke (GET hash rule)
PASS protocol hash smoke (exempt endpoint)
PASS protocol hash smoke (ENFORCE missing hash blocked)
PASS protocol replay smoke (nonce reuse blocked)
PASS: phase4 protocol security logic smoke suite

```

### Executable logic smoke: Phase 8 precision regression vectors
- status: PASS
```text
# Phase 8 Precision Regression Vector Smoke (scale=3)
PASS parse 0.001 -> 1 unit
PASS parse 0.01 -> 10 units
PASS parse 0.3 -> 300 units
PASS format 300 units -> 0.300
PASS 30 lines * 0.001 = 0.030
PASS 30 lines * 0.01 = 0.300
PASS 25 lines * 0.004 = 0.100
PASS sum preserves exact thousandths
PASS reject > scale precision (0.0009)
PASS reject malformed decimal
summary pass=10 fail=0

```

### Executable logic smoke: Phase 8 precision remediation buckets
- status: PASS
```text
phase8_bucket_report=/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260223-155454.md

```

### Executable logic smoke: Phase 8 Wave 1 reporting/display vectors
- status: PASS
```text
# Phase 8 Wave 1 Reporting/Display Precision Vector Smoke
PASS format 1 cent -> 0.01
PASS format 100 cents -> 1.00
PASS format 12345 cents -> 123.45
PASS score 12.345 -> 1235 cents (HALF_UP)
PASS score 12.344 -> 1234 cents (HALF_UP)
PASS score 0.005 -> 1 cent (HALF_UP)
PASS score 0.004 -> 0 cent (HALF_UP)
PASS milli 19 -> 0.01 (DOWN)
PASS milli 19 -> 0.02 (HALF_UP)
PASS negative rounding preserved
PASS reject malformed score
PASS reject >3 decimals
summary pass=12 fail=0

```

### Executable logic smoke: Phase 8 Wave 1 NumberUtils.asMoney parity
- status: PASS
```text
# Phase 8 Wave 1 NumberUtils.asMoney Parity Smoke
PASS in=0 legacy=0.00 refac=0.00 expected=0.00
PASS in=0.004 legacy=0.00 refac=0.00 expected=0.00
PASS in=0.005 legacy=0.01 refac=0.01 expected=0.01
PASS in=1.234 legacy=1.23 refac=1.23 expected=1.23
PASS in=1.235 legacy=1.24 refac=1.24 expected=1.24
PASS in=-0.004 legacy=0.00 refac=0.00 expected=0.00
PASS in=-0.005 legacy=0.00 refac=0.00 expected=0.00
PASS in=-0.006 legacy=-0.01 refac=-0.01 expected=-0.01
PASS in=-1.234 legacy=-1.23 refac=-1.23 expected=-1.23
PASS in=-1.235 legacy=-1.24 refac=-1.24 expected=-1.24
PASS in=9999999.999 legacy=10000000.00 refac=10000000.00 expected=10000000.00
PASS negative-half-cent legacy semantics preserved (-0.005 -> 0.00)
summary pass=12 fail=0

```

### Executable logic smoke: Phase 8 Wave 2 settings/coin-rule vectors
- status: PASS
```text
# Phase 8 Wave 2 Settings/Coin-Rule Vector Smoke
PASS legacy scale2 baseBet(30) = 30.00
PASS target scale3 baseBet(30) = 30.000
PASS 30 lines * 0.01 (scale2) = 0.30
PASS 30 lines * 0.001 (scale3) = 0.030
PASS nearest coin legacy target 0.30 picks 0.01
PASS nearest coin scale3 target 0.030 picks 0.001
PASS nearest coin scale3 target 0.060 picks 0.002
PASS nearest coin tie keeps first (stable)
PASS reject over-precision for scale2
PASS reject malformed target
summary pass=10 fail=0

```

### Executable logic smoke: Phase 8 Wave 3 dual-calculation comparison vectors
- status: PASS
```text
# Phase 8 Wave 3 Dual-Calculation Comparison Vector Smoke
PASS scale2 multiplier parity
PASS scale2 base bet parity (30 lines)
PASS scale2 base bet parity (243 lines)
PASS scale2 template max parity 1234.56
PASS scale2 total bet parity 30 lines * 0.01
PASS scale2 total bet parity 25 lines * 0.05
PASS scale2 nearest coin parity target 0.30
PASS scale3 generalized supports 0.001 * 30 = 0.030
PASS scale3 generalized template max 1234.567 parses
PASS scale3 nearest coin target 0.060 picks 0.002
PASS delta visibility: legacy cannot parse 0.001 while scale3 can
PASS delta visibility: malformed target still rejected
summary pass=12 fail=0

```

### Bash syntax: Phase 5 bonus/FRB scripts
- status: PASS
```text

```

### Bash syntax: Phase 5 history scripts
- status: PASS
```text

```

### Bash syntax: Phase 5 wallet scripts
- status: PASS
```text

```

### Bash syntax: Phase 5 gameplay scripts
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

### CLI help: Phase 5 wallet evidence-pack
- status: PASS
```text
Usage: phase5-wallet-runtime-evidence-pack.sh [options]

Options:
  --bank-id ID                Default: 6275
  --transport MODE            host|docker (default: host)
  --gs-base-url URL           Default: http://127.0.0.1:18081
  --wallet-base-url URL       Default: http://127.0.0.1:18075
  --readiness-wallet-host H   Default: 127.0.0.1
  --readiness-wallet-port P   Default: 18075
  --readiness-gs-host H       Default: 127.0.0.1
  --readiness-gs-port P       Default: 18081
  --check-docker BOOL         true|false (default: true)
  --out-dir DIR               Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase5/wallet
  -h, --help                  Show this help

```

### CLI help: Phase 5 gameplay evidence-pack
- status: PASS
```text
Usage: phase5-gameplay-runtime-evidence-pack.sh [options]

Options:
  --bank-id ID                Default: 6275
  --game-id ID                Default: 838
  --token TOKEN               Default: test_user_6275
  --mode MODE                 Default: real
  --lang LANG                 Default: en
  --transport MODE            host|docker (default: host)
  --gs-base-url URL           Default: http://127.0.0.1:18081
  --gameplay-base-url URL     Default: http://127.0.0.1:18074
  --require-redis-hit BOOL    true|false (default: false)
  --readiness-gameplay-host H Default: 127.0.0.1
  --readiness-gameplay-port P Default: 18074
  --readiness-gs-host H       Default: 127.0.0.1
  --readiness-gs-port P       Default: 18081
  --readiness-redis-host H    Default: 127.0.0.1
  --readiness-redis-port P    Default: 16379
  --check-docker BOOL         true|false (default: true)
  --out-dir DIR               Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase5/gameplay
  -h, --help                  Show this help

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
time="2026-02-23T15:54:56Z" level=warning msg="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
bonus-frb-service config-service redis gameplay-orchestrator protocol-adapter wallet-adapter c1 multiplayer-service session-service zookeeper kafka mp gs static history-service
```

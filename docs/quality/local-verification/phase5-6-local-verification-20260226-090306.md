# Phase 5/6 Local Verification Suite (20260226-090306 UTC)

- scope: offline/local validation for recently implemented refactor services and tooling
- pass: 82
- fail: 0
- skip: 0

## Summary
- [PASS] CLI help: Phase 4 protocol security logic smoke
- [PASS] CLI help: Phase 4 protocol JSON security runtime canary
- [PASS] CLI help: Phase 4 protocol runtime evidence pack
- [PASS] CLI help: Phase 8 precision/min-bet audit scanner
- [PASS] CLI help: Phase 8 precision regression vector smoke
- [PASS] CLI help: Phase 8 precision remediation buckets
- [PASS] CLI help: Phase 8 Wave 1 reporting/display vector smoke
- [PASS] CLI help: Phase 8 Wave 1 NumberUtils.asMoney parity smoke
- [PASS] CLI help: Phase 8 Wave 2 settings/coin-rule vector smoke
- [PASS] CLI help: Phase 8 history/reporting export precision vector smoke
- [PASS] CLI help: Phase 8 wallet contract/rounding precision vector smoke
- [PASS] CLI help: Phase 8 non-prod precision canary readiness/evidence tools
- [PASS] CLI help: Phase 8 Wave 3 dual-calculation comparison vector smoke
- [PASS] CLI help: Phase 8 Wave 3 apply-mode vector smoke
- [PASS] CLI help: Phase 8 precision policy/matrix tools
- [PASS] CLI help: Phase 8 Wave 3 discrepancy evidence smoke
- [PASS] CLI help: Phase 8 Wave 3 discrepancy export tool
- [PASS] CLI help: Phase 7 target upgrade rehearsal orchestrator
- [PASS] CLI help: Phase 9 ABS compatibility mapping tools
- [PASS] CLI help: Phase 9 ABS rename candidate scanner
- [PASS] CLI help: Phase 9 ABS candidate diff tool
- [PASS] CLI help: Phase 9 ABS execution plan generator
- [PASS] CLI help: Phase 9 ABS patch-plan export
- [PASS] CLI help: Phase 9 W0 text replace executor
- [PASS] CLI help: Phase 9 W0 approval artifact generator
- [PASS] CLI help: Phase 4 protocol status report generator
- [PASS] CLI help: Phase 5/6 service extraction status report generator
- [PASS] CLI help: Phase 2 observability baseline status report generator
- [PASS] CLI help: Phase 0 legacy parity status report generator
- [PASS] CLI help: Security hardening status report generator
- [PASS] CLI help: Program deploy readiness status report generator
- [PASS] CLI help: Legacy mixed-topology validation pack
- [PASS] CLI help: Phase 8 Wave 3 discrepancy compare/export tool
- [PASS] Executable logic smoke: Phase 4 protocol hash/replay security
- [PASS] Executable logic smoke: Phase 4 runtime evidence degraded classification
- [PASS] Executable logic smoke: Phase 8 precision regression vectors
- [PASS] Executable logic smoke: Phase 8 precision remediation buckets
- [PASS] Executable logic smoke: Phase 8 Wave 1 reporting/display vectors
- [PASS] Executable logic smoke: Phase 8 Wave 1 NumberUtils.asMoney parity
- [PASS] Executable logic smoke: Phase 8 Wave 2 settings/coin-rule vectors
- [PASS] Executable logic smoke: Phase 8 history/reporting export precision vectors
- [PASS] Executable logic smoke: Phase 8 wallet contract/rounding precision vectors
- [PASS] Executable logic smoke: Phase 8 non-prod canary readiness/evidence scaffold
- [PASS] Executable logic smoke: Phase 8 Wave 3 dual-calculation comparison vectors
- [PASS] Executable logic smoke: Phase 8 Wave 3 apply-mode vectors
- [PASS] Executable logic smoke: Phase 8 precision policy/matrix generator
- [PASS] Executable logic smoke: Phase 8 Wave 3 discrepancy evidence scaffold
- [PASS] Executable logic smoke: Phase 8 Wave 3 discrepancy export parser
- [PASS] Executable logic smoke: Phase 7 target upgrade rehearsal orchestrator (dry-run)
- [PASS] Executable logic smoke: Phase 9 ABS compatibility mapping manifest
- [PASS] Executable logic smoke: Phase 9 ABS rename candidate scanner
- [PASS] Executable logic smoke: Phase 9 ABS candidate diff tool
- [PASS] Executable logic smoke: Phase 9 ABS execution plan generator
- [PASS] Executable logic smoke: Phase 9 ABS patch-plan export
- [PASS] Executable logic smoke: Phase 9 W0 text replace executor
- [PASS] Executable logic smoke: Phase 9 W0 approval artifact + apply guard
- [PASS] Executable logic smoke: Phase 4 protocol status report generator
- [PASS] Executable logic smoke: Phase 5/6 service extraction status report generator
- [PASS] Executable logic smoke: Phase 2 observability baseline status report generator
- [PASS] Executable logic smoke: Phase 0 legacy parity status report generator
- [PASS] Executable logic smoke: Security hardening status report generator
- [PASS] Executable logic smoke: Program deploy readiness status report generator
- [PASS] Executable logic smoke: Legacy mixed-topology validation pack
- [PASS] Executable logic smoke: Phase 8 Wave 3 discrepancy compare/export CLI
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

### CLI help: Phase 4 protocol runtime evidence pack
- status: PASS
```text
Usage: phase4-protocol-runtime-evidence-pack.sh [options]

Options:
  --bank-id ID       Default: 6275
  --base-url URL     Default: http://127.0.0.1:18078
  --gs-base-url URL  Default: http://127.0.0.1:18081
  --transport MODE   host|docker (default: host)
  --session-id SID   Optional (used by wallet probe)
  --sub-casino-id ID Optional (appended to startgame launch URL for auto session resolution)
  --token TOKEN      Default: test_user_6275 (used by wallet probe when session-id is omitted)
  --run-security-probe BOOL     true|false (default: false)
  --security-require-secret B   true|false (default: false)
  --allow-missing-runtime B     true|false (default: false)
  --out-dir DIR      Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol
  -h, --help         Show this help
Usage: phase4-protocol-runtime-evidence-pack-degraded-smoke.sh

Runs Phase 4 runtime evidence pack with unreachable URLs and verifies
degraded classification statuses are reported (skip/block, not fail).

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

### CLI help: Phase 8 history/reporting export precision vector smoke
- status: PASS
```text
Usage: phase8-precision-history-reporting-export-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 history/reporting/export precision handling.
Validates integer-minor-unit aggregation and fixed-scale export formatting for scale2 and scale3.

```

### CLI help: Phase 8 wallet contract/rounding precision vector smoke
- status: PASS
```text
Usage: phase8-precision-wallet-contract-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 wallet contract and rounding handling.
Validates minor-unit roundtrip, fixed-scale canonical decimal formatting, and JSON HMAC sensitivity.

```

### CLI help: Phase 8 non-prod precision canary readiness/evidence tools
- status: PASS
```text
Usage: phase8-precision-nonprod-canary-readiness-check.sh [options]

Checks readiness for Phase 8 non-prod precision canary execution on the refactor GS stack.
It does not enable precision flags or restart containers.

Options:
  --allow-missing-runtime B   true|false (default: false)
  --gs-container NAME         Default: refactor-gs-1
  --log-dir DIR               Default: /Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/logs/gs
  -h, --help                  Show help
Usage: phase8-precision-nonprod-canary-evidence-pack.sh [options]

Creates a Phase 8 non-prod canary readiness/evidence report. This pack is execution-ready scaffolding
for the final runtime canary blocker; it does not restart GS or toggle JVM flags.

Options:
  --out-dir DIR               Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision
  --allow-missing-runtime B   true|false (default: true)
  -h, --help                  Show help
Usage: phase8-precision-close-after-canary.sh [options]

Closes Phase 8 after a successful non-prod runtime precision canary by:
  1) validating runtime evidence (status=READY, dual-calc logs > threshold),
  2) clearing the nonprod_canary_runtime policy blocker,
  3) regenerating the Phase 8 precision matrix and requiring phase8ReadyToClose=yes,
  4) marking checklist item pu-precision-audit as done,
  5) syncing policy copy + dashboard embedded progress (optional).

Options:
  --policy-file PATH             Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase8-precision-policy.json
  --checklist-file PATH          Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json
  --evidence-dir DIR             Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision
  --evidence-report PATH         Use specific canary evidence report (default: latest in evidence-dir)
  --matrix-script PATH           Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh
  --matrix-out-dir DIR           Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision
  --policy-sync-script PATH      Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-phase8-precision-policy.sh
  --dashboard-sync-script PATH   Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh
  --sync-policy-copy B           true|false (default: true)
  --sync-dashboard B             true|false (default: true)
  --update-checklist B           true|false (default: true)
  --docs-dir DIR                 Default: /Users/alexb/Documents/Dev/Dev_new/docs
  --doc-number N|auto            Default: auto
  --doc-ts TS                    UTC timestamp suffix (default: now)
  --doc-slug SLUG                Default: phase8-precision-runtime-canary-phase-closure
  --require-status STATUS        Default: READY
  --min-dual-calc-log-lines N    Default: 1
  --dry-run B                    true|false (default: false)
  -h, --help                     Show help
Usage: phase8-precision-nonprod-canary-run.sh [options]

Runs the Phase 8 non-prod GS precision canary on refactor-gs-1 by restarting GS with JVM flags,
triggering a launch request, and generating an evidence pack. Use --dry-run to print commands only.

Options:
  --compose-file PATH      Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml
  --env-file PATH          Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env
  --gs-container NAME      Default: refactor-gs-1
  --bank-id ID             Default: 6275
  --game-id ID             Default: 838
  --token TOKEN            Default: phase8_canary_6275
  --mode MODE              Default: real
  --lang LANG              Default: en
  --flags STRING           JVM precision flags (default: Phase 8 canary flags)
  --build-gs B             true|false (default: true)
  --restore-default B      true|false (default: true)
  --wait-seconds N         Default: 20
  --auto-close-phase8 B    true|false (default: true)
  --close-script PATH      Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-close-after-canary.sh
  --dry-run B              true|false (default: false)
  -h, --help               Show help

```

### CLI help: Phase 8 Wave 3 dual-calculation comparison vector smoke
- status: PASS
```text
Usage: phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 Wave 3 (dual-calculation comparison scaffold).
Validates that the new generalized precision helpers preserve legacy scale=2 behavior and
provides deterministic delta visibility for future scale=3 enablement (comparison only).

```

### CLI help: Phase 8 Wave 3 apply-mode vector smoke
- status: PASS
```text
Usage: phase8-precision-wave3-applymode-vector-smoke.sh

Deterministic non-runtime vector smoke for Phase 8 Wave 3 scale-ready apply-mode scaffolding.
Validates disabled-by-default behavior, minor-unit scale property parsing, and scale-aware
settings/coin-rule calculations (apply-mode only when explicitly enabled).

```

### CLI help: Phase 8 precision policy/matrix tools
- status: PASS
```text
Usage: sync-phase8-precision-policy.sh

Syncs Phase 8 precision policy JSON from deploy config into the GS classpath resource copy.
Usage: phase8-precision-verification-matrix.sh [options]

Generates a Phase 8 precision verification matrix report from the versioned policy file.
This is a GS-side planning/verification artifact (no runtime activation).

Options:
  --policy-file PATH   Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase8-precision-policy.json
  --out-dir DIR        Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision
  -h, --help           Show help
Usage: phase8-precision-policy-matrix-smoke.sh

Deterministic smoke for Phase 8 precision policy + matrix generator.

```

### CLI help: Phase 8 Wave 3 discrepancy evidence smoke
- status: PASS
```text
Usage: phase8-precision-wave3-discrepancy-evidence-smoke.sh

Deterministic non-runtime smoke for Phase 8 Wave 3 discrepancy evidence scaffolding.
Validates counter increment, throttled snapshot emission rules, and snapshot message shape
used by disabled-by-default parity hooks in GS settings/coin-rule paths.

```

### CLI help: Phase 8 Wave 3 discrepancy export tool
- status: PASS
```text
Usage: phase8-precision-wave3-discrepancy-export.sh [options]

Parse Phase 8 Wave 3 discrepancy snapshot log lines emitted by GS parity hooks
(phase8-precision-dual-calc ...) and export a structured JSON summary.

Options:
  --log-file FILE      Input log file (repeatable). If omitted, reads stdin.
  --out-file FILE      Write JSON output to file (default: stdout)
  --pretty BOOL        true|false pretty JSON (default: true)
  -h, --help           Show this help
Usage: phase8-precision-wave3-discrepancy-export-smoke.sh

Deterministic smoke for Phase 8 Wave 3 discrepancy export parser. Builds a synthetic log,
runs the export script, and validates the aggregated JSON summary.

```

### CLI help: Phase 7 target upgrade rehearsal orchestrator
- status: PASS
```text
Usage: phase7-cassandra-target-bootstrap-and-critical-copy.sh [options]

Options:
  --target-service NAME     Default: c1-refactor
  --source-container NAME   Default: refactor-c1-1
  --target-container NAME   Default: refactor-c1-refactor-1
  --table-list FILE         Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt
  --output-dir DIR          Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra
  --wait-seconds N          Default: 180
  -h, --help                Show help
Usage: phase7-cassandra-upgrade-target-rehearsal.sh [options]

Options:
  --source-container NAME  Default: gp3-c1-1
  --target-container NAME  Default: refactor-c1-refactor-1
  --table-list FILE        Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt
  --output-dir DIR         Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra
  --wait-seconds N         Default: 180
  --dry-run true|false     Default: false
  -h, --help               Show help
Usage: phase7-cassandra-upgrade-target-rehearsal-smoke.sh

Runs the Phase 7 upgrade-target rehearsal orchestrator in dry-run mode and validates
that it emits a report with the expected readiness markers.

```

### CLI help: Phase 9 ABS compatibility mapping tools
- status: PASS
```text
Usage: phase9-abs-compatibility-map-validate.sh [--map-file FILE]

Validates the Phase 9 ABS compatibility mapping manifest (GS scope).
Usage: phase9-abs-compatibility-map-smoke.sh

Runs a smoke validation for the Phase 9 ABS compatibility mapping manifest.

```

### CLI help: Phase 9 ABS rename candidate scanner
- status: PASS
```text
Usage: phase9-abs-rename-candidate-scan.sh [options]

Manifest-driven Phase 9 ABS rename candidate scanner (GS scope).
Produces wave-specific candidate report and can block unsafe auto-apply plans
when review-only mappings (for example mq) are present.

Options:
  --root DIR                Default: /Users/alexb/Documents/Dev/Dev_new/gs-server
  --map-file FILE           Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
  --out-dir DIR             Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase9
  --wave ID                 Optional wave filter (e.g. W0, W1, W3). Default: all
  --enforce-auto-apply B    true|false (default: false)
  --max-files-per-mapping N Default: 10
  --safe-targets-only B     true|false (default: false)
  --path-profile NAME       Optional manifest path profile override
  -h, --help                Show this help
Usage: phase9-abs-rename-candidate-scan-smoke.sh

Smoke-tests Phase 9 ABS rename candidate scanner and review-only blocking.

```

### CLI help: Phase 9 ABS candidate diff tool
- status: PASS
```text
Usage: phase9-abs-rename-candidate-diff.sh [options]

Runs Phase 9 candidate scanner twice (full vs wave path profile) and writes a
comparison report with per-legacy deltas.

Options:
  --root DIR                Default: /Users/alexb/Documents/Dev/Dev_new/gs-server
  --map-file FILE           Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
  --out-dir DIR             Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase9
  --wave ID                 Default: W0
  --max-files-per-mapping N Default: 10
  -h, --help                Show this help
Usage: phase9-abs-rename-candidate-diff-smoke.sh

Smoke-tests Phase 9 candidate diff report generation (full vs wave profile).

```

### CLI help: Phase 9 ABS execution plan generator
- status: PASS
```text
Usage: phase9-abs-rename-execution-plan.sh [options]

Generates a Phase 9 rename execution review plan from a candidate scan report.
This does not modify files; it produces a reviewable checklist and file shortlist.

Options:
  --scan-report FILE   Input candidate scan report (default: latest for wave in out-dir)
  --map-file FILE      Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
  --out-dir DIR        Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase9
  --wave ID            Default: W0
  -h, --help           Show this help
Usage: phase9-abs-rename-execution-plan-smoke.sh

Smoke-tests Phase 9 execution plan generation from a candidate scan report.

```

### CLI help: Phase 9 ABS patch-plan export
- status: PASS
```text
Usage: phase9-abs-rename-patch-plan-export.sh [options]

Generates a Phase 9 review-only per-file grouped patch-plan export from a
candidate scan report. No files are modified.

Options:
  --root DIR                 Source root for snippet extraction (default: /Users/alexb/Documents/Dev/Dev_new/gs-server)
  --scan-report FILE         Input candidate scan report (default: latest in out-dir)
  --map-file FILE            Manifest (default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json)
  --out-dir DIR              Output directory (default: /Users/alexb/Documents/Dev/Dev_new/docs/phase9)
  --wave ID                  Wave filter (default: W0)
  --context-lines N          rg context lines for snippets (default: 0)
  --max-snippets-per-file N  Cap snippets per file section (default: 8)
  -h, --help                 Show this help
Usage: phase9-abs-rename-patch-plan-export-smoke.sh

Smoke-tests Phase 9 per-file grouped patch-plan export generation.

```

### CLI help: Phase 9 W0 text replace executor
- status: PASS
```text
Usage: phase9-abs-rename-w0-text-replace.sh [options]

Manifest-guarded Phase 9 W0 text replacement executor for review-approved patch-plan
exports. Supports dry-run and apply modes. Blocks review-only mappings.

Options:
  --root DIR              Source root for file updates (default: /Users/alexb/Documents/Dev/Dev_new/gs-server)
  --map-file FILE         Manifest (default: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json)
  --patch-plan FILE       Patch-plan export report (default: latest in out-dir)
  --out-dir DIR           Report directory (default: /Users/alexb/Documents/Dev/Dev_new/docs/phase9)
  --wave ID               Wave label for reporting (default: W0)
  --mode MODE             dry-run|apply (default: dry-run)
  --max-files N           Safety cap for file sections processed (default: 200)
  --approval-file FILE    Required for --mode apply (explicit approval artifact with file allowlist)
  -h, --help              Show this help
Usage: phase9-abs-rename-w0-text-replace-smoke.sh

Smoke-tests Phase 9 W0 text replacement dry-run/apply executor.

```

### CLI help: Phase 9 W0 approval artifact generator
- status: PASS
```text
Usage: phase9-abs-rename-w0-approval-artifact-generate.sh [options]

Generates a Phase 9 W0 apply approval artifact JSON from a dry-run report and/or
patch-plan report. Artifact contains an explicit file allowlist and patch-plan link.

Options:
  --dry-run-report FILE   Input dry-run report (preferred source for file allowlist)
  --patch-plan FILE       Patch-plan report (required if no dry-run report)
  --out-dir DIR           Output directory (default: /Users/alexb/Documents/Dev/Dev_new/docs/phase9)
  --wave ID               Wave (default: W0)
  --approver NAME         Optional approver name (default: generated placeholder)
  --notes TEXT            Optional notes
  --approval-id ID        Optional approval id (default: generated)
  -h, --help              Show this help
Usage: phase9-abs-rename-w0-approval-artifact-generate-smoke.sh

Smoke-tests Phase 9 W0 approval artifact generation and apply guard flow.

```

### CLI help: Phase 4 protocol status report generator
- status: PASS
```text
Usage: phase4-protocol-status-report-generate.sh [options]

Generate a Phase 4 protocol adapter status report from runtime evidence-pack output
and the shared local verification suite report.

Options:
  --runtime-evidence FILE   Default: latest phase4 runtime evidence report
  --verify-report FILE      Default: latest local verification suite report
  --out-dir DIR             Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol
  -h, --help                Show help

```

### CLI help: Phase 5/6 service extraction status report generator
- status: PASS
```text
Usage: phase5-6-service-extraction-status-report-generate.sh [options]

Generate a Phase 5/6 service extraction status report from runtime evidence-pack reports
and the shared local verification suite report.

Options:
  --verify-report FILE      Default: latest local verification suite report
  --gameplay-evidence FILE  Default: latest gameplay runtime evidence report
  --wallet-evidence FILE    Default: latest wallet runtime evidence report
  --bonus-evidence FILE     Default: latest bonus-frb runtime evidence report
  --history-evidence FILE   Default: latest history runtime evidence report
  --mp-evidence FILE        Default: latest multiplayer runtime evidence report
  --out-dir DIR             Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase5-6
  -h, --help                Show help

```

### CLI help: Phase 2 observability baseline status report generator
- status: PASS
```text
Usage: phase2-observability-status-report-generate.sh [options]

Generate an observability baseline status report (trace/correlation, error taxonomy,
dashboard/runbook visibility, and runtime correlation probe evidence).

Options:
  --trace-doc FILE           Default: /Users/alexb/Documents/Dev/Dev_new/docs/29-trace-correlation-standard-v1.md
  --taxonomy-doc FILE        Default: /Users/alexb/Documents/Dev/Dev_new/docs/27-error-taxonomy-v1.md
  --correlation-probe FILE   Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase2/correlation-probes/correlation-probe-20260220-104035.md
  --runbook-doc FILE         Default: /Users/alexb/Documents/Dev/Dev_new/docs/60-support-modernization-runbook-page-20260220-182600.md
  --runbook-status-doc FILE  Default: /Users/alexb/Documents/Dev/Dev_new/docs/61-support-runbook-status-snapshot-20260220-183000.md
  --dashboard-doc FILE       Default: /Users/alexb/Documents/Dev/Dev_new/docs/36-modernization-visual-dashboard.md
  --verify-report FILE       Default: latest local verification suite report
  --out-dir DIR              Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase2/observability
  -h, --help                 Show help

```

### CLI help: Phase 0 legacy parity status report generator
- status: PASS
```text
Usage: phase0-legacy-parity-status-report-generate.sh [options]

Generate legacy parity status report focused on:
  - FRB/bonus parity suite stabilization coverage
  - multiplayer legacy compatibility guardrails (deferred dedicated runtime validation)

Options:
  --phase0-doc FILE            Default: /Users/alexb/Documents/Dev/Dev_new/docs/23-phase-0-baseline-and-parity-capture.md
  --launch-forensics-doc FILE  Default: /Users/alexb/Documents/Dev/Dev_new/docs/11-game-launch-forensics.md
  --phase5-6-closure-doc FILE  Default: /Users/alexb/Documents/Dev/Dev_new/docs/155-phase5-6-service-extraction-phase-closure-tested-no-go-runtime-blocked-20260224-120000.md
  --mp-boundary-doc FILE       Default: /Users/alexb/Documents/Dev/Dev_new/docs/39-phase6-multiplayer-boundary-and-bypass-v1.md
  --mp-shadow-doc FILE         Default: /Users/alexb/Documents/Dev/Dev_new/docs/73-phase6-gs-multiplayer-shadow-bridge-20260220-191800.md
  --mp-policy-doc FILE         Default: /Users/alexb/Documents/Dev/Dev_new/docs/74-phase6-multiplayer-routing-policy-probe-and-test-gate-20260220-192600.md
  --verify-report FILE         Default: latest local verification suite report
  --out-dir DIR                Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-status
  -h, --help                   Show help

```

### CLI help: Security hardening status report generator
- status: PASS
```text
Usage: security-hardening-status-report-generate.sh [options]

Generate security hardening baseline status report for refactor services and protocol security tooling.

Options:
  --verify-report FILE   Default: latest local verification suite report
  --audit-summary FILE   Default: latest /Users/alexb/Documents/Dev/Dev_new/docs/security/dependency-audit/audit-summary-prod.json
  --out-dir DIR          Default: /Users/alexb/Documents/Dev/Dev_new/docs/security
  -h, --help             Show help

```

### CLI help: Program deploy readiness status report generator
- status: PASS
```text
Usage: program-deploy-readiness-status-report.sh [options]

Generate program deploy/cutover readiness status from latest phase closure/status reports.

Options:
  --checklist FILE        Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json
  --verify-report FILE    Default: latest local verification report
  --phase4-report FILE    Default: latest phase4 protocol status report
  --phase56-report FILE   Default: latest phase5-6 service extraction status report
  --phase7-doc FILE       Default: docs/134 phase7 no-go rehearsal closure
  --phase7-mismatch FILE  Default: latest phase7 full-copy count-mismatches TSV (if present)
  --security-report FILE  Default: latest security hardening status report
  --legacy-report FILE    Default: latest legacy parity status report
  --legacy-mixed FILE     Default: latest manual full-flow result, else manual result, else preflight report
  --out-dir DIR           Default: /Users/alexb/Documents/Dev/Dev_new/docs/release-readiness
  -h, --help              Show help

```

### CLI help: Legacy mixed-topology validation pack
- status: PASS
```text
Usage: legacy-mixed-topology-validation-pack.sh [options]

Generate/execute the dedicated mixed-topology validation wave:
  refactored GS + legacy MP/client infrastructure.

Options:
  --dry-run B              true|false (default: false)
  --refactor-gs-url URL    Default: http://127.0.0.1:18081
  --legacy-mp-url URL      Default: http://127.0.0.1:6300
  --legacy-client-url URL  Default: http://127.0.0.1:80
  --bank-id ID             Default: 6275
  --game-id ID             Default: 838
  --timeout-sec N          Default: 5
  --out-dir DIR            Default: /Users/alexb/Documents/Dev/Dev_new/docs/validation/legacy-mixed-topology
  -h, --help               Show help

```

### CLI help: Phase 8 Wave 3 discrepancy compare/export tool
- status: PASS
```text
Usage: phase8-precision-wave3-discrepancy-compare-export.sh [options]

Build a compact Phase 8 Wave 3 discrepancy comparison report from two JSON exports
generated by phase8-precision-wave3-discrepancy-export.sh. Applies a named threshold
policy and emits JSON (and optional Markdown).

Options:
  --a-file FILE        Baseline A discrepancy export JSON (required)
  --b-file FILE        Comparison B discrepancy export JSON (required)
  --policy NAME        strict|canary_gate|shadow_observe|demo_sample_pass (default: strict)
  --threshold-mismatch-a N       Override seeded policy mismatchA threshold
  --threshold-mismatch-b N       Override seeded policy mismatchB threshold
  --threshold-mismatch-delta N   Override seeded policy mismatchDelta threshold
  --threshold-snapshot-delta N   Override seeded policy snapshotDelta threshold
  --allow-new-metrics-in-b BOOL  Override seeded policy allowNewMetricsInB (true|false)
  --out-file FILE      Write JSON report output (default: stdout)
  --md-out-file FILE   Write Markdown report output (optional)
  --pretty BOOL        true|false pretty JSON (default: true)
  -h, --help           Show this help
Usage: phase8-precision-wave3-discrepancy-compare-export-smoke.sh

Deterministic smoke for Phase 8 Wave 3 discrepancy compare/export CLI. Creates
synthetic A/B export JSON, runs strict and demo policies, and validates JSON/MD outputs.

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

### Executable logic smoke: Phase 4 runtime evidence degraded classification
- status: PASS
```text
PASS: degraded Phase 4 runtime evidence classification works
  report: /var/folders/7b/h207_tk50f5d2xyw6wxg0_mw0000gn/T/tmp.j9r8gCUcSy/out/phase4-protocol-runtime-evidence-20260226-090309.md

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
phase8_bucket_report=/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-remediation-buckets-20260226-090309.md

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

### Executable logic smoke: Phase 8 history/reporting export precision vectors
- status: PASS
```text
# Phase 8 History/Reporting Export Precision Vector Smoke
PASS scale2 sum exact 0.10+0.20+0.30=0.60
PASS scale3 sum exact 0.001+0.002+0.003=0.006
PASS scale3 mixed sum exact 1.234+2.000-0.001=3.233
PASS scale2 export row keeps fixed 2dp
PASS scale3 export row keeps fixed 3dp
PASS scale3 csv quoting works
PASS legacy scale2 rejects 3dp export input
PASS invalid amount rejected
PASS scale3 thousandth wager+win total exact
PASS negative sign formatting preserved
summary pass=10 fail=0

```

### Executable logic smoke: Phase 8 wallet contract/rounding precision vectors
- status: PASS
```text
# Phase 8 Wallet Contract/Rounding Precision Vector Smoke
PASS scale2 roundtrip 12.34
PASS scale3 roundtrip 12.345
PASS scale3 rejects 4dp
PASS canonical wallet body fixes trailing zeros for scale3
PASS canonical wallet body preserves strings/ids
PASS HMAC changes when decimal formatting changes
PASS HMAC stable for same canonical body
PASS minor-unit sum exact scale3 (wallet settle)
PASS legacy scale2 rejects thousandth wallet amount
PASS negative adjustment canonical formatting
summary pass=10 fail=0

```

### Executable logic smoke: Phase 8 non-prod canary readiness/evidence scaffold
- status: PASS
```text
status=READY
gs_container=refactor-gs-1
log_dir=/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/logs/gs
precision_dual_calc_log_lines=2
policy_file=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase8-precision-policy.json
matrix_report=/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-verification-matrix-20260226-090310.md
matrix_blocking_count=0
matrix_remaining_blockers=none
canary_flags_hint=-Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3
report=/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-nonprod-canary-evidence-20260226-090310.md
ok
summary pass=1 fail=0
step=baseline_log_count
[DRY-RUN] before_count=$( (docker logs refactor-gs-1 2>&1 | rg -c 'phase8-precision-dual-calc') || true )
step=recreate_gs_with_phase8_flags
[DRY-RUN] cd /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor && GS_JAVA_OPTS='-Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3 -Dabs.gs.phase8.precision.dualCalc.logEvery=1' docker compose -f '/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml' --env-file '/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env' up -d --force-recreate --no-deps gs
step=wait_for_gs
[DRY-RUN] sleep 1
step=inspect_gs_args
[DRY-RUN] docker inspect refactor-gs-1 --format '{{.Path}} {{join .Args " "}}'
step=trigger_startgame_canary
[DRY-RUN] docker exec refactor-gs-1 sh -lc "curl -sS -o /tmp/phase8_canary.body -D /tmp/phase8_canary.hdr -w '%{http_code}' 'http://127.0.0.1:8080/cwstartgamev2.do?bankId=6275&gameId=838&mode=real&token=phase8_canary_6275&lang=en'"
[DRY-RUN] docker exec refactor-gs-1 sh -lc 'head -n 8 /tmp/phase8_canary.hdr; echo ---; head -c 400 /tmp/phase8_canary.body'
step=evidence_pack
[DRY-RUN] '/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh' --allow-missing-runtime false
step=auto_close_phase8
[DRY-RUN] '/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-close-after-canary.sh'
step=restore_default_gs_flags
[DRY-RUN] cd /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor && GS_JAVA_OPTS='' docker compose -f '/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml' --env-file '/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env' up -d --force-recreate --no-deps gs
[DRY-RUN] sleep 1
done=true

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

### Executable logic smoke: Phase 8 Wave 3 apply-mode vectors
- status: PASS
```text
# Phase 8 Wave 3 Apply-Mode Vector Smoke
PASS apply mode disabled by default
PASS empty scale defaults to legacy scale2
PASS invalid scale falls back to legacy scale2
PASS enabled scale3 selected when configured
PASS template max uses legacy scale2 when apply disabled
PASS template max uses scale3 when apply enabled
PASS base bet uses scale2 when apply disabled
PASS base bet uses scale3 when apply enabled
PASS 30 lines * 0.001 with apply enabled scale3 = 0.030
PASS 30 lines * 0.001 with apply disabled rejects legacy scale2 precision
PASS scale0 is supported in resolver
PASS apply enabled with invalid scale still legacy scale2
summary pass=12 fail=0

```

### Executable logic smoke: Phase 8 precision policy/matrix generator
- status: PASS
```text
synced phase8 precision policy -> /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/phase8-precision-policy.json
PASS policy schema/basic contents blockingCount=0
report=/var/folders/7b/h207_tk50f5d2xyw6wxg0_mw0000gn/T/tmp.OyiXCNjRo7/matrix/phase8-precision-verification-matrix-20260226-090311.md
PASS matrix header
PASS scale3 currency row
PASS wallet blocking category row
PASS closure gate summary (closed state)

```

### Executable logic smoke: Phase 8 Wave 3 discrepancy evidence scaffold
- status: PASS
```text
# Phase 8 Wave 3 Discrepancy Evidence Smoke
PASS default logEvery fallback
PASS blank logEvery fallback
PASS invalid logEvery fallback
PASS non-positive logEvery fallback
PASS valid logEvery accepted
PASS logs first check
PASS skips middle check
PASS logs interval check
PASS logs mismatch immediately
PASS dynamic snapshot shape includes ids and values
PASS helper snapshot shape includes template credits
PASS counter progression example
summary pass=12 fail=0

```

### Executable logic smoke: Phase 8 Wave 3 discrepancy export parser
- status: PASS
```text
PASS summary line emitted
PASS totalSnapshotLines
PASS metricCount
PASS base metric snapshots
PASS base metric maxCheckCount
PASS base metric last ids
PASS template metric mismatch observed
PASS template metric last values
PASS template metric mismatch event count tracks nonzero snapshots

```

### Executable logic smoke: Phase 7 target upgrade rehearsal orchestrator (dry-run)
- status: PASS
```text
report=/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/phase7-cassandra-upgrade-target-rehearsal-20260226-090311.md
PHASE7_TARGET_REHEARSAL_SMOKE_OK

```

### Executable logic smoke: Phase 9 ABS compatibility mapping manifest
- status: PASS
```text
PHASE9_ABS_MAP_OK
mappings=9
waves=5
reviewOnly=9
PHASE9_ABS_MAP_SMOKE_OK

```

### Executable logic smoke: Phase 9 ABS rename candidate scanner
- status: PASS
```text
PASS: phase9 abs candidate scanner smoke

```

### Executable logic smoke: Phase 9 ABS candidate diff tool
- status: PASS
```text
PASS: phase9 abs candidate diff smoke

```

### Executable logic smoke: Phase 9 ABS execution plan generator
- status: PASS
```text
PASS: phase9 abs execution plan smoke

```

### Executable logic smoke: Phase 9 ABS patch-plan export
- status: PASS
```text
PASS: phase9 abs patch-plan export smoke

```

### Executable logic smoke: Phase 9 W0 text replace executor
- status: PASS
```text
PASS: phase9 abs w0 text replace smoke

```

### Executable logic smoke: Phase 9 W0 approval artifact + apply guard
- status: PASS
```text
PASS: phase9 abs w0 approval artifact + apply guard smoke

```

### Executable logic smoke: Phase 4 protocol status report generator
- status: PASS
```text
PHASE4_STATUS_REPORT_SMOKE_OK

```

### Executable logic smoke: Phase 5/6 service extraction status report generator
- status: PASS
```text
PHASE56_SERVICE_STATUS_REPORT_SMOKE_OK

```

### Executable logic smoke: Phase 2 observability baseline status report generator
- status: PASS
```text
PHASE2_OBSERVABILITY_STATUS_REPORT_SMOKE_OK

```

### Executable logic smoke: Phase 0 legacy parity status report generator
- status: PASS
```text
PHASE0_LEGACY_PARITY_STATUS_REPORT_SMOKE_OK

```

### Executable logic smoke: Security hardening status report generator
- status: PASS
```text
SECURITY_HARDENING_STATUS_REPORT_SMOKE_OK

```

### Executable logic smoke: Program deploy readiness status report generator
- status: PASS
```text
PROGRAM_DEPLOY_READINESS_STATUS_SMOKE_OK

```

### Executable logic smoke: Legacy mixed-topology validation pack
- status: PASS
```text
LEGACY_MIXED_TOPOLOGY_VALIDATION_PACK_SMOKE_OK

```

### Executable logic smoke: Phase 8 Wave 3 discrepancy compare/export CLI
- status: PASS
```text
PASS strict summary emitted
PASS strict policy name
PASS strict fails
PASS demo summary emitted
PASS demo policy name
PASS demo passes
PASS metric union count
PASS new metric only in B flagged
PASS markdown includes policy line
PASS markdown includes table header
PASS override summary emitted
PASS override path keeps seed profile
PASS override path passes
PASS override values applied
PASS override metadata present

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
  --sub-casino-id ID          Optional (appended to startgame launch URL for auto session resolution)
  --token TOKEN               Default: test_user_6275
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
  --sub-casino-id ID          Optional (appended to startgame launch URL for auto session resolution)
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
  --expect-non-mp-route BOOL      true|false (default: false)
  --expect-mp-reason VALUE        Default: bank_multiplayer_disabled
  --expect-mp-route BOOL          true|false (default: false)
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
  --policy-expect-bank-mp-enabled BOOL  true|false (default: false)
  --policy-expect-non-mp-reason VALUE   Default: non_multiplayer_game
  --policy-expect-non-mp-route BOOL     true|false (default: false)
  --policy-expect-mp-reason VALUE       Default: bank_multiplayer_disabled
  --policy-expect-mp-route BOOL         true|false (default: false)
  --out-dir DIR                  Default: /Users/alexb/Documents/Dev/Dev_new/gs-server/../docs/phase6/multiplayer
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
time="2026-02-26T09:03:16Z" level=warning msg="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
redis gameplay-orchestrator protocol-adapter history-service c1-refactor zookeeper kafka mp gs static wallet-adapter bonus-frb-service c1 config-service multiplayer-service session-service
```

# Phase 8 Non-Prod Precision Canary Evidence Pack (20260224-142333 UTC)

## Scope
Execution-ready readiness/evidence scaffold for the final Phase 8 runtime canary blocker.
No container restart or JVM flag mutation performed by this script.

## Readiness Snapshot
```text
status=READY
gs_container=refactor-gs-1
log_dir=/Users/alexb/Documents/Dev/Dev_new/Doker/runtime-gs/logs/gs
precision_dual_calc_log_lines=2
policy_file=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase8-precision-policy.json
matrix_report=/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision/phase8-precision-verification-matrix-20260224-142334.md
matrix_blocking_count=0
matrix_remaining_blockers=none
canary_flags_hint=-Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3
```

## Next Command (Manual Runtime Canary)
```text
1) Restart refactor GS with JVM flags: -Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3
2) Execute canary requests for selected scale3 currency/bank profile(s)
3) Re-run this evidence pack and confirm precision_dual_calc_log_lines > 0 and matrix_blocking_count reaches 0 only after policy update with captured runtime evidence
```

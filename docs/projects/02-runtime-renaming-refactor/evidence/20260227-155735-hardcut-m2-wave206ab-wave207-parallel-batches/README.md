# Evidence Summary - W206A/W206B + W207

## Scope
- `W206A`: 16 declaration migrations in `common-gs` promo and `promo/core` surfaces.
- `W206B`: 10 declaration migrations in `sb-utils` `common.configuration` and `common.engine`.
- `W207`: integration and validation.

## Final Validation Snapshot
- Fast gate canonical: `fast-gate-status-rerun5.txt`
  - `STEP01-08 PASS`
  - `STEP09 FAIL` (`rc=2`, launch smoke alias)
- Full matrix canonical: `validation-status-rerun5.txt`
  - `PRE01-03 PASS`
  - `STEP01-08 PASS`
  - `STEP09 FAIL` (`rc=2`; recovery retry executed once and failed)

## Key Artifacts
- Targets: `target-batchA.txt`, `target-batchB.txt`
- Rewires: `rewires-batchA-all.txt`, `rewires-batchB-all.txt`
- Overlap proof: `overlap-metrics.txt`
- Warm-up installs: `PRE04-promo-persisters-rerun3.log`, `PRE05-promo-core-rerun3.log`
- Compile probe: `STEP06-rerun4-probe.log`
- Fast gate canonical: `fast-gate-runner-rerun5.log`, `fast-gate-STEP01-rerun5.log` ... `fast-gate-STEP09-rerun5.log`, `fast-gate-status-rerun5.txt`
- Full matrix canonical: `validation-runner-rerun5.log`, `PRE01-rerun5.log` ... `PRE03-rerun5.log`, `STEP01-rerun5.log` ... `STEP09-rerun5.log`, `STEP09-rerun5-retry1-rerun5.log`, `validation-status-rerun5.txt`

# W134A/W134B + W135 Evidence

This folder contains full execution evidence for hard-cut parallel waves W134A/W134B with integration W135.

## Scope
- W134A: 10 declaration migrations in `sb-utils/common.util.xml.parser`.
- W134B: 10 declaration migrations in `promo.events.process`.
- W135: bounded integration rewires, including compatibility fix in `HistoryInformerManager` for `HistoryInformerItem` package alignment.

## Validation
- Fast gate: PASS (final `rerun4`).
- Full matrix: PASS 9/9 (final `rerun4`).
- Authoritative status files:
  - `fast-gate-status.txt`
  - `validation-status.txt`

## Key artifacts
- Batch manifests: `target-batchA.txt`, `target-batchB.txt`, `rewires-batchA.txt`, `rewires-batchB.txt`, `overlap-check.txt`
- Fast gate logs: `fast-gate-*.log`
- Matrix logs: `01*.log` ... `09*.log`
- Runner/status logs: `validation-runner*.log`, `validation-status*.txt`
- Rollup metrics: `global-remaining-count.txt`, `wave-summary.txt`, `target-files.txt`

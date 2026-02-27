# Evidence - Hard-Cut M2 Wave 210A/210B + 211

- Batch manifests:
  - `target-batchA.txt` (11 declarations)
  - `target-batchB.txt` (12 declarations)
  - `rewires-batchA-all.txt` (empty)
  - `rewires-batchB-all.txt` (empty)
  - `overlap-metrics.txt` (`decl/rewire/cross overlap = 0`)
- Fast gate:
  - rerun1/rerun2/rerun3 failure evidence (`fast-gate-STEP*.log`, `fast-gate-runner-rerun*.log`, `fast-gate-status-rerun*.txt`)
  - canonical rerun4: `STEP01-08 PASS`, `STEP09 FAIL rc=2`
- Full matrix canonical rerun1:
  - `PRE01-rerun1.log` ... `PRE03-rerun1.log`
  - `STEP01-rerun1.log` ... `STEP09-rerun1.log`
  - `STEP09-rerun1-retry1.log`
  - `validation-runner-rerun1.log`, `prewarm-status-rerun1.txt`, `validation-status-rerun1.txt`

# Evidence Bundle - Hard-Cut M2 W130A/W130B + W131

- Scope: non-overlapping parallel DTO batches in `mp-server/kafka` (privateroom + bots) with bounded importer rewires.
- Fast gate: initial reruns failed due missed importer and reactor-order artifact issues; stabilized and PASS on rerun4.
- Full matrix: PASS (9/9).
- Global tracked declarations/files remaining: 1719 (baseline 2277).

Artifacts:
- Batch manifests: target-batchA.txt, rewires-batchA.txt, target-batchB.txt, rewires-batchB.txt, overlap-check.txt
- Validation: fast-gate logs/status + 01..09 matrix logs + validation-runner.log + validation-status.txt
- Summary: target-files.txt, global-remaining-count.txt, wave-summary.txt

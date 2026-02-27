# Evidence Bundle - Hard-Cut M2 W132A/W132B + W133

- Scope: non-overlapping parallel batches across `cassandra.persist.mp` and `sb-utils test api` families.
- Fast gate: PASS on rerun1 (5/5).
- Full matrix: PASS (9/9).
- Global tracked declarations/files remaining: 1688 (baseline 2277).

Artifacts:
- Batch manifests: target-batchA.txt, rewires-batchA.txt, target-batchB.txt, rewires-batchB.txt, overlap-check.txt
- Validation: fast-gate logs/status + 01..09 matrix logs + validation-runner.log + validation-status.txt
- Summary: target-files.txt, global-remaining-count.txt, wave-summary.txt

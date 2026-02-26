# RENAME-FINAL: Phase 0 Refresh + Phase 1 Safety Gate (2026-02-26 06:31 UTC)

## What was executed
1. Refreshed runtime naming inventory.
2. Validated compatibility mapping manifest.
3. Generated candidate scan and W0 patch plan.
4. Executed guarded W0 dry-run and apply flow.
5. Audited actual diffs for runtime safety.
6. Rolled back W0 applied file changes after safety finding.
7. Added guardrail in map to block automatic brand-token apply.
8. Re-ran map validation and candidate scan to confirm guardrail behavior.

## Key outputs
- Pre-scan report:
  - `phase9-abs-rename-candidate-scan-20260226-062933.md`
- Patch plan:
  - `phase9-abs-rename-patch-plan-W0-20260226-062933.md`
- Dry-run report:
  - `phase9-abs-rename-w0-text-replace-dry-run-20260226-063004.md`
- Apply report (before rollback):
  - `phase9-abs-rename-w0-text-replace-apply-20260226-063020.md`
- Post-guardrail scan report:
  - `phase9-abs-rename-candidate-scan-20260226-063246.md`

## Safety finding (critical)
W0 auto-apply changed runtime-sensitive values, including MP startup class in Docker compose:
- `com.betsoft.casino.mp.web.NettyServer` -> `com.abs.casino.mp.web.NettyServer`

Current source code still defines:
- `com.betsoft.casino.mp.web.NettyServer`

This would risk startup failure in runtime environments if applied.

## Action taken
- All W0-applied runtime/config file edits were rolled back.
- Only evidence artifacts were retained.
- Compatibility map updated to enforce manual review:
  - W0 `allowsAutomaticApply=false`
  - brand mappings (`betsoft`, `nucleus`, `maxquest`, `maxduel`, `discreetgaming`) set to `reviewOnly=true`

## Validation after rollback and guardrail
- `phase9-abs-compatibility-map-validate.sh`: PASS (`reviewOnly=9`)
- Candidate scan post-guardrail:
  - `Auto-candidate mappings: 0`
  - `Review-only mappings with hits: 8`

## Build/verification matrix
(Executed while W0 apply was in test flow; compile stability confirmed and no committed runtime change remains.)
- `mvn -DskipTests install` in `promo/persisters`: PASS
- `mvn -DskipTests install` in `common-persisters`: PASS
- `mvn test` in `cache`: PASS (`63` tests)
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `web-gs`: PASS
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`: PASS

## Current conclusion
- Project 02 Phase 0 is refreshed and evidenced.
- Automatic W0 rename is now intentionally blocked by policy.
- Next safe execution path is manual curated waves for runtime-sensitive rename targets (`com.dgphoenix*`, `MQ*`) with explicit per-file allowlists and runtime smoke after each mini-wave.

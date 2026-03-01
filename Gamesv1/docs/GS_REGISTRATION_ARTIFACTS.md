# GS_REGISTRATION_ARTIFACTS

This document describes what GS Ops needs to register, enable, canary, and rollback a release produced by Gamesv1.

## Required Inputs from Release Pack

From `games/<gameId>/release-packs/<releaseId>/`:

1. `artifacts/release-metadata.json`
2. `artifacts/gs-compatibility.json`
3. `artifacts/package-versions.json`
4. `artifacts/math-package.manifest-reference.json`
5. `artifacts/client-bundle.manifest.json`
6. `artifacts/asset.manifest.json`
7. `artifacts/localization.manifest.json`
8. `checksums.sha256.json`
9. `GS_REGISTRATION_PACK.md`
10. `ROLLBACK_PACK.md`
11. `CANARY_CHECKLIST.md`
12. `SMOKE_TEST_CHECKLIST.md`

Notes:
- `games/<gameId>/release-packs/` is generated and gitignored.
- Use `docs/examples/release-pack/` only for static documentation examples.

## GS Ops Registration Flow

1. Validate checksums.
2. Confirm CDN/static assets exist for release version.
3. Register release metadata and compatibility payload in GS.
4. Register math package reference as immutable release input.
5. Enable candidate release for canary audience.
6. Execute canary + smoke checklists.
7. Promote release to full traffic only after pass.

## Rollback Flow

1. Disable current candidate release in GS routing.
2. Re-enable previous known-good release from `ROLLBACK_PACK.md`.
3. Re-run minimal smoke checks:
- launch
- normal round
- reconnect
- wallet consistency

## Smoke Checklist Items (Must Be Executed)

- launch
- normal round
- reconnect
- turbo
- history
- localization override
- trunc-cents
- delayed wallet messages
- free spins
- buy feature
- rollback

## Canary Checklist (Operational)

- candidate registration complete
- CDN/static paths validated
- guest/free/real launch validated
- error rate monitored in canary window
- rollback path pre-validated

## Security Requirement

Release registration artifacts are metadata-only and must not contain secrets.

# GS_REGISTRATION_ARTIFACTS

GS Ops registration inputs produced by Gamesv1 release pack.

Canonical source:
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`

## Required inputs

From `games/<gameId>/release-packs/<releaseId>/`:

1. `artifacts/release-metadata.json`
2. `artifacts/gs-compatibility.json`
3. `artifacts/package-versions.json`
4. `artifacts/math-package.manifest-reference.json`
5. `artifacts/client-bundle.manifest.json`
6. `artifacts/asset.manifest.json`
7. `artifacts/localization.manifest.json`
8. `artifacts/registration-artifact.json`
9. `checksums.sha256.json`
10. `GS_REGISTRATION_PACK.md`
11. `ROLLBACK_PACK.md`
12. `CANARY_CHECKLIST.md`
13. `SMOKE_TEST_CHECKLIST.md`

## Registration flow

1. Validate checksums.
2. Verify CDN/static URLs and release version consistency.
3. Register `registration-artifact.json` and linked manifests.
4. Enable candidate via canary workflow.
5. Promote after canary/smoke pass.

## Rollback flow

1. Disable current release.
2. Re-enable known-good release from rollback pack.
3. Re-run launch + round + reconnect + wallet consistency checks.

## Security requirement

Registration artifacts are metadata-only and must not contain secrets.

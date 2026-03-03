# GS_REGISTRATION_ARTIFACTS

GS Ops registration inputs produced by Gamesv1 release pack.

Canonical source:
- `docs/gs/release-registration-contract.md`
- `docs/gs/enable-disable-canary-rollback.md`
- `docs/gs/fixtures/release-registration.sample.json`
- `docs/gs/schemas/release-registration.schema.json`

## Required inputs

From `games/<gameId>/release-packs/<releaseId>/`:

1. `artifacts/release-registration.json`
2. `artifacts/client-artifact-manifest.json`
3. `artifacts/asset-manifest.json`
4. `artifacts/localization-manifest.json`
5. `artifacts/math-package-manifest.reference.json`
6. `artifacts/capability-profile.reference.json`
7. `artifacts/compatibility-metadata.json`
8. `artifacts/package-versions.json`
9. `artifacts/release-metadata.json`
10. `artifacts/integrity-metadata.json`
11. `checksums.sha256.json`
12. `GS_REGISTRATION_PACK.md`
13. `ROLLBACK_PACK.md`
14. `CANARY_CHECKLIST.md`
15. `SMOKE_TEST_CHECKLIST.md`

## Registration flow

1. Validate `checksums.sha256.json` and `artifacts/integrity-metadata.json`.
2. Verify CDN/static URLs and release version consistency.
3. Register `artifacts/release-registration.json` (`slot-release-registration-v1`) and linked manifests.
   - registration shape is validated against `docs/gs/schemas/release-registration.schema.json`
4. Enable candidate via canary workflow.
5. Promote after canary/smoke pass.

## Rollback flow

1. Disable current release.
2. Re-enable known-good release listed in rollback metadata.
3. Re-run launch + round + reconnect + wallet consistency checks.

## Security requirement

Registration artifacts are metadata-only and must not contain secrets.

## Math artifact rule

- Registration uses `mathPackageManifestReference` that points to immutable `math/math-package-manifest.json`.
- `gs/template-params.json` is not a release registration artifact.

# GS Release Registration Contract

Status: canonical registration artifact contract for release packs.

## Required files

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

## Registration envelope

`release-registration.json` schema: `slot-release-registration-v1`.

Canonical artifacts for this shape:
- `docs/gs/fixtures/release-registration.sample.json`
- `docs/gs/schemas/release-registration.schema.json`

Must include:
- `gameId`, `gameName`, `version`, `releaseId`, `gitSha`
- entrypoint/asset/localization URLs
- `mathPackageManifestReference`
- `capabilityProfile` hash/reference
- feature flags / RTP models / limits
- compatibility + integrity references
- canary + rollback metadata

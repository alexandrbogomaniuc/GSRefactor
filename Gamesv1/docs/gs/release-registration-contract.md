# GS Release Registration Contract

Status: canonical registration artifact contract for release packs produced by Gamesv1.

## Required Artifact Files

All files are generated under `games/<gameId>/release-packs/<releaseId>/`.

1. `artifacts/client-bundle.manifest.json`
2. `artifacts/asset.manifest.json`
3. `artifacts/localization.manifest.json`
4. `artifacts/math-package.manifest-reference.json`
5. `artifacts/package-versions.json`
6. `artifacts/release-metadata.json`
7. `artifacts/gs-compatibility.json`
8. `artifacts/registration-artifact.json`
9. `checksums.sha256.json`
10. `GS_REGISTRATION_PACK.md`
11. `ROLLBACK_PACK.md`
12. `CANARY_CHECKLIST.md`
13. `SMOKE_TEST_CHECKLIST.md`

## Registration Artifact (`registration-artifact.json`)

Must include:
- `gameId`, `version`, `releaseId`, `gitSha`
- `entrypointUrl`, `assetManifestUrl`, `localizationBaseUrl`
- `mathPackageReference`
- `featureFlags`
- `rtpModels`
- `limits` (min/max/default/maxExposure)
- `canaryEligible`

## Rules

1. Artifacts must be deterministic/reproducible for same input commit + version.
2. No secrets/tokens/credentials in generated files.
3. Static assets and math artifacts remain separate concerns.
4. Registration references immutable, versioned URLs only.

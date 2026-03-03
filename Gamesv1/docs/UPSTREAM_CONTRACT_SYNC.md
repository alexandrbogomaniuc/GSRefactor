# UPSTREAM_CONTRACT_SYNC

Canonical contract source for `docs/gs` is the upstream GS pack.

## Upstream source path

- Expected upstream path: `/Users/alexb/Documents/Dev_New/docs/gs/`
- In Windows environments, pass an accessible equivalent with:
  - `--upstream <path>`
  - or `GS_CONTRACT_UPSTREAM_PATH=<path>`

## Mirror rule

`docs/gs` in this repo must be a byte-exact mirror of upstream:

1. Delete repo `docs/gs`
2. Copy upstream `docs/gs` as-is
3. Do not edit files inside mirrored `docs/gs`

No local reinterpretation in canonical scope:
- no local fixture wrappers if not upstream
- no local TS/Zod helpers in `docs/gs`
- no locally generated schema wrappers in `docs/gs`

If helper code is needed, keep it outside canonical scope, for example:
- `docs/generated/gs-contract/*`
- `packages/core-protocol/generated/gs-contract/*`

## Lock format enforced

Verifier now expects upstream `contract-lock.json` with these top-level fields:
- `generatedAtUtc`
- `hashAlgorithm`
- `contractVersions`
- `canonical`

`canonical` is treated as the canonical file-hash map/scope.

## Verification command

```bash
corepack pnpm run verify:gs-contract-pack -- --upstream <accessible-upstream-path>
```

The verifier fails if:
- upstream path is inaccessible
- lock format is not the upstream format above
- any canonical file is missing
- any extra file exists in canonical `docs/gs` scope
- any byte/hash differs between upstream and repo mirror

## Scope classification

Canonical:
- `docs/gs/*` (exact upstream mirror only)

Repo-generated helpers (non-canonical):
- `docs/generated/gs-contract/*`

Legacy/experimental:
- `packages/core-protocol/src/ws/*`
- `docs/protocol/*` legacy markers

Supplemental/non-canonical notes:
- docs outside `docs/gs/*`

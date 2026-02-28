# GS Registration Pack

- Release ID: `1.0.0+4abc2a03`
- Game: `premium-slot` (TemplateSlot)
- Version: `1.0.0`
- Git SHA: `4abc2a035760b102abe41dcfa62ebb02c2ed36b3`
- Created At: `2026-02-28T08:41:23Z`

## Artifact Set

- client bundle manifest
- asset manifest
- localization manifest
- math package manifest reference
- package versions
- release metadata
- GS compatibility metadata
- checksums

## GS Ops Actions

1. Verify checksums from `checksums.sha256.json`.
2. Upload client/static assets to the versioned CDN path.
3. Register release metadata and compatibility payload in GS registration workflow.
4. Enable for canary environment first.
5. Promote after smoke checklist passes.

## No-Secret Guarantee

Generated artifacts intentionally exclude credentials/tokens/secrets.

# PROMO_ASSET_PIPELINE

## Scope

Promo pipeline covers non-runtime gameplay marketing assets and release handoff packaging.

## Asset Classes

- store/app icons
- static banners (landscape/portrait/square)
- teaser/gameplay clips
- key-art exports for catalogs and GS registration notes

## Canonical Paths

- Source: `games/<gameId>/raw-assets/promo/`
- Manifest tracking: `games/<gameId>/docs/asset-manifest.sample.json`
- Release-pack inclusion: promo assets referenced from generated artifact manifests

## Export + Naming Policy

Follow `docs/ART_AND_PROMO_PIPELINE.md` naming rules and compression budgets.

Required naming pattern:

`<gameId>_<group>_<asset>_<variant>@<scale>x.<ext>`

## Validation Gate

Promo assets must pass:

1. dimensions match required variants
2. codecs/compression within budgets
3. all files listed in asset manifest
4. release-pack references resolvable output paths

## Operational Handoff

Release manager receives:

- promo asset checklist status
- integrity checksums in release artifact pack
- canary checklist references for public-facing promo links

# ART_PIPELINE_PRODUCTIZATION

## Objective

Define a reusable art integration contract for new games built on the premium slot shell.

This document extends:

- `docs/ART_AND_PROMO_PIPELINE.md`
- `docs/ASSET_MANIFEST_SPEC.md`

## Canonical Folder Integration

Per game (`games/<gameId>/`):

- `raw-assets/preload/` -> preload card/logo assets
- `raw-assets/main/` -> in-game symbols/background/UI skins/win assets
- `raw-assets/promo/` -> marketing assets
- `docs/asset-manifest.sample.json` -> art deliverable contract
- `src/manifest.json` -> generated runtime bundle manifest

## Required Plug-in Surfaces

1. Symbol art
- static symbols and animated symbol sources
- spine/spritesheet fallback for low-perf mode

2. Background art
- landscape + portrait variants where needed
- optional dynamic layers for feature modes

3. UI skins
- control icons and panel textures for premium HUD
- skin-specific color tokens and panel assets

4. Promo assets
- icons, banners, short promo video variants

5. Preloaders
- static preloader image and optional loop video

6. Video overlays
- big/huge/mega win overlays
- optional feature-entry overlays (bounded by budgets)

## Scaffolder Safety

`tools/create-game.ts` already scaffolds canonical raw-asset folders and an asset-manifest placeholder.

Rules:

- do not add alternate ad-hoc asset roots
- do not rename canonical folders used by release-pack
- keep manifest fields aligned with `docs/ASSET_MANIFEST_SPEC.md`

## Release Pipeline Alignment

Release-pack uses canonical manifests and runtime paths from scaffolded structure.

Art integration is accepted only when:

- source asset folders are canonical
- manifest entries are complete
- bundle mapping (`preload/main/promo`) is valid
- generated release artifacts remain deterministic

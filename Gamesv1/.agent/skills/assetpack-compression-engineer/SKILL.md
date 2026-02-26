---
name: assetpack-compression-engineer
description: Use this skill when setting up AssetPack pipelines, atlases, KTX2/Basis compressed textures, per-scene bundles, and artist-friendly asset workflows for PixiJS games.
---

# AssetPack + Compression Engineer

## Goal
Ship high-quality visuals with mobile-safe performance.

## Instructions
1) Define assets_src → assets_build pipeline.
2) Add atlasing strategy (UI atlas, symbols atlas, FX atlas).
3) Add compressed texture outputs (KTX2/Basis) with fallbacks.
4) Document “artist workflow” in docs/ASSET_PIPELINE.md.

## Constraints
- Never require manual “drag files into dist”.
- Never commit huge raw exports into the runtime bundle without packing.

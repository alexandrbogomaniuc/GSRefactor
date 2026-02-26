# /add_assetpack — AssetPack + Bundles + Compressed Textures

When triggered:
1) Add AssetPack build pipeline.
2) Create assets_src/ and assets_build/ folders and document workflow for artists.
3) Create Pixi Assets bundle manifests per scene.
4) Add compressed texture generation plan (KTX2/Basis) and fallback behavior.
Acceptance:
- A sample atlas is produced into assets_build/
- App loads from assets_build/ using Pixi Assets

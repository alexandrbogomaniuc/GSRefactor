# Asset Pipeline Rules (Quality + Performance)

Core principles:
- No “random PNG dumping”. Assets must be packed and optimized.
- Use atlases for sprites/UI where batching matters.
- Use compressed GPU textures (KTX2/Basis) for shipping builds.
- Bundle assets per scene (boot/basegame/bonus) for fast startup.

Project conventions:
- Raw art goes in: assets_src/
- Built/optimized assets go in: assets_build/
- Gameplay code never references raw file paths; only asset keys/aliases via AssetService.
- Enforce naming conventions: snake_case for files, consistent symbol IDs.

Mobile constraints:
- Keep textures within reasonable size; prefer multiple atlases over single gigantic sheets.
- Avoid huge transparent full-screen textures.

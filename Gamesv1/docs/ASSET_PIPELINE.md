# Asset Pipeline

## Workflow: `assets_src` → `assets_build`
All raw art assets go into `assets_src/`.
Our AssetPack configuration processes these files and outputs optimized graphics to `assets_build/`. The game code ONLY references files located in `assets_build/` via the Pixi Asset bundle manifest.

## Atlas Strategy
We consolidate textures to reduce draw calls:
- `ui_atlas`: Shared navigation, menus, and controls.
- `symbols_atlas`: Slot game symbols (low, mid, high paying).
- `fx_atlas`: Particles, generic animated sweeps, sparks, and glow materials.

## Compressed Textures (KTX2/Basis)
- WebGL texture uploads cause significant hitching if textures are too large.
- AssetPack script converts heavy graphics into Basis/KTX2 format.
- High-priority/large assets MUST use compressed textures; mobile devices unpack these natively with drastically less memory overhead.
- Fallback to Standard WebP/PNG for older browsers.
- **🚨 PixiJS v8 specific limitations (from Github MCP):** 
  - v8 dropped support for older compressed formats (`.ktx` and DDS DX10/BC7/BC6H). We MUST explicitly use `KTX2` or `Basis`.
  - There is a known issue where Basis textures resolve `alphaMode` to `0` instead of `1` by default. If we see transparency rendering bugs on mobile devices utilizing Basis, explicitly set `alphaMode: 1` during texture loading or material binding.

## Naming Conventions
- Snake case for all filenames (`red_gem.png`, `wild_symbol.png`).
- Meaningful suffixes if necessary (e.g., `_diffuse`, `_normal`, `_glow`).

## Artist Handoff Checklist
- [ ] Are source assets neatly placed in the correct `assets_src` subfolder?
- [ ] Are animations pre-rendered cleanly and appropriately sized, or exported via Spine?
- [ ] Do any new textures break maximum atlas dimensions (2048x2048 recommended for mobile)?
- [ ] Has the AssetPack build run locally without warnings?

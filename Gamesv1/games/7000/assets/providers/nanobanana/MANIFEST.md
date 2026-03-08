# MANIFEST

Provider root: `Gamesv1/games/7000/assets/providers/nanobanana`

## Runtime Status

- Source branch: `origin/assets/7000-nanobanana-crazy-rooster-phase1-20260307-1358`
- Original source path on that branch: `Gamesv1/games/7000/raw-assets/providers/nanobanana`
- This repo folder is a QA-beta assembly created from those committed source files.
- `atlas_vfx.png` / `atlas_vfx.json` were not supplied on the source branch and are intentionally absent here.

## Committed Runtime Files

- `runtime/background-desktop-1920x1080.png`
- `runtime/background-landscape-safe-1600x900.png`
- `runtime/background-portrait-1080x1920.png`
- `runtime/atlas_symbols.png`
- `runtime/atlas_symbols.json`
- `runtime/atlas_ui.png`
- `runtime/atlas_ui.json`

## Validation Note

The QA runtime should validate this pack at startup and log a clear warning that NanoBanana is missing a committed VFX atlas, then continue with safe placeholder behavior instead of crashing.

# ART_AND_PROMO_PIPELINE

This document defines the art production and export pipeline for Gamesv1.
It is the source of truth for art teams and AI agents before release handoff.

## 1) Required Deliverables

Each game must provide these deliverables:

1. Icons
- `icon_512.png` (512x512, RGBA)
- `icon_1024.png` (1024x1024, RGBA)
- `icon_square_safe.png` (1024x1024, title/logo inside 80% safe zone)

2. Banners
- `banner_landscape_1920x1080.jpg`
- `banner_landscape_1280x720.jpg`
- `banner_portrait_1080x1920.jpg`
- `banner_square_1080x1080.jpg`

3. Promo Videos
- `promo_teaser_15s.mp4`
- `promo_gameplay_30s.mp4`
- optional WebM versions for web embeds

4. Preloaders
- `preloader_static_1024x1024.png`
- `preloader_loop_3s.mp4` (optional)
- `preloader_logo.svg` (optional vector)

5. Big Win Videos
- `big_win.mp4`
- `huge_win.mp4`
- `mega_win.mp4`

6. Symbol Animations
- Spine exports for animated symbols (`.json/.skel` + `.atlas` + texture pages)
- Static first-frame fallback PNG per symbol
- Optional spritesheet fallback for low-performance mode

7. UI Kit Deliverables
- Shared UI atlas inputs (buttons, toggles, panel frames, badges)
- HUD icon set (spin, turbo, autoplay, buybonus, settings, pause)
- Nine-slice source textures for scalable UI blocks

## 2) Export Specs

### Images
- Master format: PNG (RGBA), sRGB.
- Runtime optimized format: WebP for atlases when supported by pipeline.
- Naming: `<gameId>_<group>_<asset>_<variant>@<scale>x.<ext>`
- Compression:
  - PNG: lossless, stripped metadata.
  - JPG: quality 85 for promo banners only.

### Video
- Resolution: 1920x1080 master, plus 1280x720 optional derivative.
- FPS: 30 (default) or 60 for highly kinetic reels FX.
- Codec/container:
  - Primary: H.264 in MP4 (`yuv420p`)
  - Optional fallback: VP9 WebM
- Bitrate targets:
  - 1080p30: 8-12 Mbps
  - 720p30: 4-6 Mbps
- Audio: AAC 128-192 kbps stereo when audio track is required.

### Spritesheets
- Power-of-two pages only.
- Preferred page sizes: 1024, 2048, 4096.
- Trim transparent bounds; keep pivot metadata.

## 3) Spine Export Rules

1. First frame must be usable as static symbol.
- Every symbol animation needs frame 0 as the neutral in-reel state.
- Frame 0 center/pivot must match static symbol pivot.

2. Atlas limits.
- Mobile target: max 2048x2048 page.
- Desktop target: max 4096x4096 page.
- Avoid single-symbol atlases unless required for streaming.

3. Naming contract.
- Skeleton: `sym_<symbolId>`
- Animations: `idle`, `win`, `anticipation`, optional `intro`
- Skin names must be deterministic and lowercase.

4. Export settings.
- Keep nonessential bones/attachments removed.
- Do not export test animations.
- Include version metadata in manifest.

## 4) After Effects -> Spritesheet/Video Pipeline

1. Author comps in AE at 1920x1080 (or approved target size).
2. Render image sequence (`PNG`) for sprite-based assets.
3. Convert image sequence to spritesheet with pivot metadata.
4. Render MP4 deliverables for preloader/promo/big-win videos.
5. Run optimization pass:
- videos: re-encode to target bitrate/profile
- images: compression and metadata strip
6. Move exported files into game `raw-assets/` source folders.
7. Rebuild AssetPack and validate generated runtime manifest.

## 5) Quality Gates

### Atlas Size Gates
- Hard fail if any page exceeds:
  - Mobile bundles: 2048x2048
  - Desktop bundles: 4096x4096

### Texture Memory Budgets (Decoded in GPU memory)
- Mobile startup budget: <= 64 MB
- Desktop startup budget: <= 128 MB
- Per-feature incremental budget (single feature reveal): <= 16 MB

### GPU Upload Budgets
- Initial upload burst at boot: <= 40 MB within first 3 seconds
- Single-event upload burst (feature trigger): <= 8 MB
- No frame hitch > 50 ms on reference mid-tier mobile device

### Video Gates
- Preloader video: <= 4 MB
- Big win tier video: <= 8 MB each
- Promo gameplay 30s: <= 35 MB (1080p)

## 6) File and Folder Contract

- Source files: `games/<gameId>/raw-assets/`
- Generated runtime manifest: `games/<gameId>/src/manifest.json`
- Per-game art manifest: `games/<gameId>/docs/asset-manifest.sample.json`
- Spec reference: `docs/ASSET_MANIFEST_SPEC.md`

## 7) Checklist: Art Team

1. All required deliverables exported and named by spec.
2. Spine symbols include valid frame-0 static pose.
3. Atlas page sizes comply with limits.
4. Texture/video assets meet compression and size budgets.
5. Files placed in game `raw-assets/` with correct grouping.
6. Placeholder/debug assets removed.
7. Deliverables listed in per-game asset manifest with status and owner.

## 8) Checklist: AI Agent

1. Validate `games/<gameId>/docs/asset-manifest.sample.json` against `docs/ASSET_MANIFEST_SPEC.md`.
2. Ensure all required categories are present (icons, banners, promo, preloader, big win, symbols, ui-kit).
3. Verify naming pattern and bundle mapping (`preload`, `main`, `promo`).
4. Ensure no runtime code references missing aliases.
5. Run build so AssetPack regenerates `src/manifest.json`.
6. Verify boot path loads `preload` then `main` bundles.
7. Report missing deliverables as explicit TODOs in PR summary.

# MANIFEST

Provider root: `Gamesv1/games/7000/assets/providers/openai`

All runtime art in this pack is original and intended for the `openai` provider path only.

## Runtime Status

- This folder is a runtime-ready provider pack and not a canonical AssetPack source tree.
- The repo-wide documented source pipeline is `raw-assets/preload/`, `raw-assets/main/`, and `raw-assets/promo/`.
- No `Gamesv1/games/7000/raw-assets/providers/openai` folder is committed because there is no existing provider-local raw-assets convention elsewhere in the repo.
- If engineering later needs pipeline sources, they should be created under canonical `raw-assets/preload/` and `raw-assets/main/` locations rather than under a new provider-local subtree.

## Backgrounds

| File | Resolution | Intended Use |
| --- | --- | --- |
| `runtime/background-desktop-1920x1080.png` | `1920x1080` | default desktop backdrop behind a centered 3x4 reel block |
| `runtime/background-landscape-safe-1600x900.png` | `1600x900` | landscape-safe crop with protected reel halo and horizon line |
| `runtime/background-portrait-1080x1920.png` | `1080x1920` | portrait stack with extra vertical breathing room above and below the reel area |

## Atlas Pivots

- UI atlas frames: center pivot `(0.5, 0.5)`
- Symbol atlas frames: slightly low visual center `(0.5, 0.56)` to keep labels optically balanced
- VFX atlas frames: center pivot `(0.5, 0.5)`

## `atlas_ui`

`runtime/atlas_ui.png` and `runtime/atlas_ui.json`

| Frame | Size | Intended Pivot |
| --- | --- | --- |
| `reel-frame-panel` | `620x880` | `(0.5, 0.5)` |
| `reel-separator-vertical` | `18x760` | `(0.5, 0.5)` |
| `reel-separator-horizontal` | `540x18` | `(0.5, 0.5)` |
| `button-spin` | `320x108` | `(0.5, 0.5)` |
| `button-spin-pressed` | `320x108` | `(0.5, 0.5)` |
| `button-autoplay` | `320x108` | `(0.5, 0.5)` |
| `button-autoplay-pressed` | `320x108` | `(0.5, 0.5)` |
| `button-buybonus` | `360x108` | `(0.5, 0.5)` |
| `button-buybonus-pressed` | `360x108` | `(0.5, 0.5)` |
| `button-bet-plus` | `180x92` | `(0.5, 0.5)` |
| `button-bet-minus` | `180x92` | `(0.5, 0.5)` |
| `payline-pill` | `220x72` | `(0.5, 0.5)` |

## `atlas_symbols`

`runtime/atlas_symbols.png` and `runtime/atlas_symbols.json`

| Frame | Size | Intended Pivot |
| --- | --- | --- |
| `symbol-0-egg` | `256x256` | `(0.5, 0.56)` |
| `symbol-1-cherries` | `256x256` | `(0.5, 0.56)` |
| `symbol-2-lemon` | `256x256` | `(0.5, 0.56)` |
| `symbol-3-orange` | `256x256` | `(0.5, 0.56)` |
| `symbol-4-plum` | `256x256` | `(0.5, 0.56)` |
| `symbol-5-bar` | `256x256` | `(0.5, 0.56)` |
| `symbol-6-seven` | `256x256` | `(0.5, 0.56)` |
| `symbol-7-coin` | `256x256` | `(0.5, 0.56)` |
| `symbol-8-bolt` | `256x256` | `(0.5, 0.56)` |
| `symbol-9-rooster` | `256x256` | `(0.5, 0.56)` |
| `collector-symbol` | `256x256` | `(0.5, 0.56)` |
| `coin-multiplier-2x` | `196x196` | `(0.5, 0.56)` |
| `coin-multiplier-3x` | `196x196` | `(0.5, 0.56)` |
| `coin-multiplier-5x` | `196x196` | `(0.5, 0.56)` |
| `coin-multiplier-10x` | `196x196` | `(0.5, 0.56)` |

## `atlas_vfx`

`runtime/atlas_vfx.png` and `runtime/atlas_vfx.json`

| Frame | Size | Intended Pivot |
| --- | --- | --- |
| `lightning-arc-01` | `200x200` | `(0.5, 0.5)` |
| `lightning-arc-02` | `200x200` | `(0.5, 0.5)` |
| `lightning-arc-03` | `200x200` | `(0.5, 0.5)` |
| `lightning-arc-04` | `200x200` | `(0.5, 0.5)` |
| `lightning-arc-05` | `200x200` | `(0.5, 0.5)` |
| `lightning-arc-06` | `200x200` | `(0.5, 0.5)` |
| `spark-burst-01` | `180x180` | `(0.5, 0.5)` |
| `spark-burst-02` | `180x180` | `(0.5, 0.5)` |
| `spark-burst-03` | `180x180` | `(0.5, 0.5)` |
| `collector-ring` | `220x220` | `(0.5, 0.5)` |

## Stable Runtime Keys

Code should request the committed runtime art by these exact identifiers:

- Background stems: `background-desktop-1920x1080`, `background-landscape-safe-1600x900`, `background-portrait-1080x1920`
- UI atlas frames: `reel-frame-panel`, `reel-separator-vertical`, `reel-separator-horizontal`, `button-spin`, `button-spin-pressed`, `button-autoplay`, `button-autoplay-pressed`, `button-buybonus`, `button-buybonus-pressed`, `button-bet-plus`, `button-bet-minus`, `payline-pill`
- Symbol atlas frames: `symbol-0-egg`, `symbol-1-cherries`, `symbol-2-lemon`, `symbol-3-orange`, `symbol-4-plum`, `symbol-5-bar`, `symbol-6-seven`, `symbol-7-coin`, `symbol-8-bolt`, `symbol-9-rooster`, `collector-symbol`, `coin-multiplier-2x`, `coin-multiplier-3x`, `coin-multiplier-5x`, `coin-multiplier-10x`
- VFX atlas frames: `lightning-arc-01`, `lightning-arc-02`, `lightning-arc-03`, `lightning-arc-04`, `lightning-arc-05`, `lightning-arc-06`, `spark-burst-01`, `spark-burst-02`, `spark-burst-03`, `collector-ring`

## Intentionally Absent

- No standalone `BetOnline` runtime lockup asset is committed.
- No explicit `boost` VFX key is committed.
- No explicit coin-VFX key is committed.
- No pressed-state keys are committed for `button-bet-plus` or `button-bet-minus`.
- Masters are local-only working files and are intentionally not committed in this provider pack.

## Preview Set

| File | Resolution | Audit Focus |
| --- | --- | --- |
| `previews/preview-01-background-desktop.png` | `1440x810` | desktop crop read |
| `previews/preview-02-background-portrait.png` | `810x1440` | portrait crop read |
| `previews/preview-03-reel-frame.png` | `860x1120` | reel shell silhouette |
| `previews/preview-04-symbol-grid.png` | `1200x800` | base symbol clarity |
| `previews/preview-05-feature-symbols.png` | `1200x440` | collector and multiplier readability |
| `previews/preview-06-buttons.png` | `1500x360` | CTA hierarchy and label fit |
| `previews/preview-07-lightning-vfx.png` | `1280x420` | VFX frame cadence |
| `previews/preview-08-typography.png` | `1400x900` | headline and numeral guidance |
| `previews/preview-09-full-layout.png` | `1600x900` | assembled vertical slice |

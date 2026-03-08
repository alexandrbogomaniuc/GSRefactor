# OpenAI Original Art Kit

This provider pack is original artwork for game `7000`. It follows the donor's broad layout rhythm of a 3x4 hold-and-win slot shell, but it does not reuse donor logos, donor paint-overs, or BetOnline brand marks.

## Style Rules

- Lighting direction: warm sunrise key light from upper left; cool cyan electric rim light from upper right for feature states and lightning only.
- Palette: barn umber `#512112`, brass gold `#F3C35A`, ember coral `#D94A2F`, hot salmon `#FF6B58`, electric cyan `#63DBFF`, teal `#2AB2C7`, plum `#7A2ED6`.
- Surface treatment: brushed brass borders, cocoa shadows, low-sheen enamel panels, soft bloom behind feature symbols only.
- Edge padding: packed with an 8 px gutter; keep at least 12 px internal safe space around labels and icon silhouettes when extending the set.
- Symbol framing: all core symbols sit on the same rounded dark-card plate so reel rhythm stays consistent across providers.

## Typography Guidance

- Headline / button face: use a condensed all-caps display such as `Impact`, `Bebas Neue`, or a similar narrow poster cut.
- Secondary UI labels: use `Arial Narrow Bold` or an equivalent condensed grotesk.
- Number style: tabular lining numerals, heavy amber fill, dark cocoa outline, optional cyan rim only for boosted values or feature calls.
- Copy tone: short verbs and nouns only; avoid script fonts, serif body copy, or ornate western signage.
- Text lockup guidance: the review preview that reads `BetOnline` is an original placeholder lockup for spacing and tone study only, not a reusable trademark asset.

## Runtime Files

| File | Purpose | Resolution | Size |
| --- | --- | --- | --- |
| `runtime/atlas_ui.png` + `runtime/atlas_ui.json` | reel chrome, separators, buttons, payline pill | `2048x2048`, 12 frames | `381,490 B` + `5,135 B` |
| `runtime/atlas_symbols.png` + `runtime/atlas_symbols.json` | 10 base reel symbols, collector, 4 multiplier coins | `2048x2048`, 15 frames | `1,097,599 B` + `6,361 B` |
| `runtime/atlas_vfx.png` + `runtime/atlas_vfx.json` | 6 lightning arcs, 3 spark bursts, 1 collector ring | `1024x1024`, 10 frames | `204,719 B` + `4,298 B` |
| `runtime/background-desktop-1920x1080.png` | desktop hero background | `1920x1080` | `2,012,262 B` |
| `runtime/background-landscape-safe-1600x900.png` | safe landscape crop | `1600x900` | `1,421,943 B` |
| `runtime/background-portrait-1080x1920.png` | portrait crop | `1080x1920` | `1,985,282 B` |

## Code Request Keys

Request background PNGs by filename stem and request atlas entries by the exact frame keys in the committed JSON.

### Background stems

- `background-desktop-1920x1080`
- `background-landscape-safe-1600x900`
- `background-portrait-1080x1920`

### UI atlas keys

- `reel-frame-panel`
- `reel-separator-vertical`
- `reel-separator-horizontal`
- `button-spin`
- `button-spin-pressed`
- `button-autoplay`
- `button-autoplay-pressed`
- `button-buybonus`
- `button-buybonus-pressed`
- `button-bet-plus`
- `button-bet-minus`
- `payline-pill`

### Symbol atlas keys

- `symbol-0-egg`
- `symbol-1-cherries`
- `symbol-2-lemon`
- `symbol-3-orange`
- `symbol-4-plum`
- `symbol-5-bar`
- `symbol-6-seven`
- `symbol-7-coin`
- `symbol-8-bolt`
- `symbol-9-rooster`
- `collector-symbol`
- `coin-multiplier-2x`
- `coin-multiplier-3x`
- `coin-multiplier-5x`
- `coin-multiplier-10x`

### VFX atlas keys

- `lightning-arc-01`
- `lightning-arc-02`
- `lightning-arc-03`
- `lightning-arc-04`
- `lightning-arc-05`
- `lightning-arc-06`
- `spark-burst-01`
- `spark-burst-02`
- `spark-burst-03`
- `collector-ring`

## Not Included / Intentionally Absent

- No standalone `BetOnline` runtime lockup asset is committed in this provider pack. The wider shell supports `brand.logoAssetKey` or `brand.logoUrl`, but no committed game `7000` code references a dedicated lockup asset key here.
- No explicit `boost` VFX key is committed. The current pack covers the needed vertical-slice energy beats with `lightning-arc-*`, `spark-burst-*`, and `collector-ring`.
- No explicit coin-VFX atlas key is committed. Coin presentation is handled by the symbol atlas via `symbol-7-coin` and the multiplier coin frames.
- No pressed-state keys are committed for `button-bet-plus` or `button-bet-minus`, and no committed repo code references pressed variants for those keys.

## Pipeline Status

- This pack is committed as a runtime-ready provider handoff under `Gamesv1/games/7000/assets/providers/openai/runtime/`.
- The repo's canonical AssetPack source contract is `raw-assets/preload/`, `raw-assets/main/`, and `raw-assets/promo/`; there is no existing `raw-assets/providers/<provider>` convention to mirror for game `7000`.
- No `Gamesv1/games/7000/raw-assets/providers/openai` tree is added here because that would invent a new scheme that is not documented elsewhere in the repo.
- If engineering later wants source assets in the AssetPack pipeline, they should be introduced under the canonical `raw-assets/preload/` and `raw-assets/main/` folders rather than under a provider-local `raw-assets/providers/openai` subtree.

## Masters

High-resolution working masters are intentionally local-only and are not committed here. The repo keeps only runtime-ready atlases/backgrounds plus a small preview set, and local master exports should remain ignored.

## Exact Regeneration Prompts

Use these prompts verbatim when regenerating the review-safe pack from scratch.

### Background desktop

`Premium commercial slot-game background, original IP only, sunrise farmyard vista behind a centered 3x4 reel machine, weathered red barn architecture, brass trim, warm amber sunlight from upper left, subtle cyan electrical rim light from upper right, cinematic depth haze, rich painted materials, high contrast around gameplay center, no logos, no text, no watermark, no characters in foreground, clean negative space for reels, 1920x1080 composition.`

### Background portrait

`Premium commercial slot-game background, original IP only, portrait adaptation of a sunrise farmyard slot scene, taller sky, extended barn framing, warm amber sunlight from upper left, restrained cyan electric rim accents, darkened center lane for a 3x4 reel stack, no logos, no text, no watermark, readable at mobile scale, 1080x1920 composition.`

### Background landscape-safe

`Premium commercial slot-game background, original IP only, landscape-safe crop of a sunrise barnyard slot backdrop, protect the central reel area, soft horizon glow, brass-and-wood material cues, no logos, no text, no watermark, controlled detail away from center, 1600x900 composition.`

### Reel frame and separators

`Original slot-game reel frame kit, brushed brass outer frame, deep cocoa enamel interior, premium bevel highlights, subtle cyan electric accents only on feature edges, include one large reel frame panel, one vertical separator, one horizontal divider, crisp silhouettes, transparent background, atlas-ready.`

### Core reel symbols

`Original premium slot symbols on matching rounded dark-card plates, high readability at small reel size, strong silhouette hierarchy, polished fruit and classic slot icon set consisting of egg, cherries, lemon, orange, plum, BAR, red seven, gold coin, electric bolt, rooster mascot, consistent brass-and-enamel finish, transparent background, atlas-ready.`

### Feature symbols and multiplier coins

`Original premium hold-and-win feature symbols, collector emblem plus gold multiplier coins marked 2x, 3x, 5x, 10x, embossed numerals, warm brass highlights, cocoa shadows, clean silhouette edges, transparent background, atlas-ready.`

### UI buttons

`Original premium slot UI button set, spin, autoplay, buy bonus, bet plus, bet minus, beveled brass edges, enamel red-orange faces, condensed all-caps labels, readable at small size, transparent background, include pressed look only for spin, autoplay, and buy bonus.`

### VFX sprites

`Original slot-game VFX sprite atlas, six controlled lightning arcs, three spark bursts, one collector ring pulse, electric cyan and amber energy, clean alpha edges, strong center-weighted shapes, not noisy, transparent background, atlas-ready.`

### Typography preview

`Original slot-game typography specimen for review only, condensed all-caps headline treatment reading BetOnline as placeholder text study, heavy amber numerals with dark cocoa outline, premium brass panel framing, not a real-world trademark recreation, no official logo styling, presentation board only.`

## Export Resolutions and Naming Conventions

- Background masters exported to runtime as `background-desktop-1920x1080.png`, `background-landscape-safe-1600x900.png`, and `background-portrait-1080x1920.png`.
- UI atlas exported as `atlas_ui.png` with matching `atlas_ui.json`.
- Symbol atlas exported as `atlas_symbols.png` with matching `atlas_symbols.json`.
- VFX atlas exported as `atlas_vfx.png` with matching `atlas_vfx.json`.
- Atlas frame keys use lowercase kebab-case and are stable once committed.
- Buttons use `button-<action>` naming, pressed variants only where committed as separate frames.
- Symbols use `symbol-<index>-<label>` for the base reel set and `coin-multiplier-<value>` for multiplier coins.

## Regenerate Steps

1. Recreate the source images with the exact prompt set above, keeping the same lighting, palette, and framing rules.
2. Clean alpha edges and normalize padding so every atlas item still respects the 8 px gutter and the safe-space rules in this README.
3. Pack the UI, symbol, and VFX sheets back into `atlas_ui`, `atlas_symbols`, and `atlas_vfx` with the same frame keys, pivots, and page sizes already committed.
4. Export backgrounds to the same three runtime filenames and resolutions already committed.
5. Replace the preview PNGs only if the regenerated art materially changes the review surfaces; otherwise leave previews stable.
6. Re-run an audit pass for readability, background contrast, and key compatibility before committing.

## Audit Previews

Nine PNG previews are included under `previews/`:

- desktop background
- portrait background
- reel frame
- symbol grid
- feature symbols
- button set
- lightning/VFX strip
- typography specimen
- full layout composite

Preview resolutions range from `810x1440` to `1600x900`.

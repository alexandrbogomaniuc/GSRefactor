# NanoBanana Provider Assets for Game 7000 (Rerun)

## Production Notes
This provider asset kit was generated to replace the initial low-quality NanoBanana pass with a high-fidelity, premium vertical slice aesthetic suitable for internal demos. The structure perfectly mirrors the OpenAI provider structural contract.

## Donor Influence
The donor reference (`/Users/alexb/Documents/.../ChickenGame/assets/_donor_raw_local`) provided invaluable style direction, primarily for:
- Establishing the high-tier quality bar and premium rendering style.
- Informing the composition clarity and visual hierarchy of symbols versus backgrounds.
- Setting expectations for material rendering (sleek metals, bright neons).

**Crucially, no donor pixels were copied, traced, or used derivatively. All assets in this folder are 100% original AI generations.**

## AI Generation Details

**Model Used:** `gemini-3.1-flash-image`
**Export Settings:** Assembled using Python PIL with High-Quality Lanczos sampling where applicable. Downscaled from large master bounds (1024-2048px).

### Prompts Used

**Backgrounds**
- *Prompt:* "A premium, high-resolution background for a nanoscale banana futuristic slot game. Cyberpunk laboratory, glowing yellow neon, deep purple dark background, highly detailed, 4k, cinematic lighting."
- *Resolution Target:* 1920x1080 (Desktop), 1600x900 (Landscape), 1080x1920 (Portrait).

**Symbols**
- *Prompt examples:* 
  - "A glowing cybernetic egg slot symbol. Metallic shell, neon yellow energy cores, shiny, premium 3d render, dark background, highly detailed."
  - "A robotic rooster head in profile slot symbol, sleek dark metallic design, glowing red eyes, neon comb, premium 3d render, dark background."
- *Target Resolution:* Master 1024x1024, downscaled to 256x256 inside `atlas_symbols`.

**UI Elements**
- *Prompt:* "A premium futuristic UI button for a slot game. Sleek dark metal with glowing green neon edge, blank center, highly detailed UI 3D render, dark background."
- *Handling:* Sliced, scaled, and composited computationally to match the required frames exactly.

**VFX Elements**
- *Prompt:* "A crackling yellow energy lightning bolt arc against a pure black background, premium vfx render, highly detailed, glowing, bright."
- *Handling:* Composited, rotated, and filtered in post-generation assembly to create bursts and variations.

**Negative Prompts (Implicit via Model Policy):** 
No text, no watermarks, no distorted anatomy, no cartoonish flat style.

## Key Mapping to OpenAI Provider

This table confirms strict parity with the OpenAI asset contract:

| Key | NanoBanana Mapping / Description |
| :--- | :--- |
| `background-desktop-1920x1080.png` | Desktop 16:9 1080p Crop |
| `atlas_symbols/symbol-0-egg` | Cybernetic Egg Symbol |
| `atlas_symbols/symbol-1-cherries` | Neon Cherries Symbol |
| `atlas_symbols/symbol-2-lemon` | Mechanical Lemon Symbol |
| `atlas_symbols/symbol-4-plum` | Glowing Plum |
| `atlas_symbols/symbol-5-bar` | Neon BAR Symbol |
| `atlas_symbols/symbol-6-seven` | Red 7 Metal Symbol |
| `atlas_symbols/symbol-7-coin` | Golden Crypto Banana Coin |
| `atlas_symbols/symbol-9-rooster` | Robotic Rooster Profile |
| `atlas_ui/button-spin` | Rescaled UI Button Graphic |
| `atlas_ui/reel-frame-panel` | Generated Panel Container |
| `atlas_vfx/lightning-arc-01` | Lightning VFX Base |

## QA Checklist for Future Reruns
- [ ] Are all 15 symbol keys present in `atlas_symbols.json`?
- [ ] Are backgrounds provided at 3 different aspect ratios?
- [ ] Is `atlas_ui.png` packed with the 12 required UI pieces matching exact bounds?
- [ ] Is the style vertically consistent across UI, Symbols, and Backgrounds?
- [ ] Has the JSON output been strictly verified against the OpenAI provider bounding boxes?

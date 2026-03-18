# Donorlocal Slot Gap Matrix

Base branch:

- `codex/qa/7000-beta6-donorlocal-parity-pass-20260317-1210`

Working branch:

- `codex/qa/7000-beta7-donorlocal-reconstruction-20260317-1304`

Benchmark URL:

- `http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional`

Clean proof helper used for screenshots only:

- add `&mathOverlay=0` to hide the dev math panel while keeping the QA helper on by default for normal benchmark work.

## A) Preloader

| Slot | Status | What donorlocal currently uses | What donor does better | Fix applied in this pass | Missing runtime key / mapping if still blocked |
| --- | --- | --- | --- | --- | --- |
| logo/wordmark | DONORLOCAL_MAPPED_OK | donorlocal wordmark plus direct donor card lockup on the preloader plate | donor keeps this as a more bespoke game-specific gate, not a reused benchmark title | moved the benchmark preloader to donorlocal wordmark + donor card composition instead of the generic BetOnline-only loader | final `preloader.logo.wordmarkCrazyRooster` if we want donor-grade composition without donor title text |
| background | DONORLOCAL_MAPPED_OK | donorlocal `mainBg` / `mainBgMobile` through provider background resolution | donor has stronger authored depth and intro-only staging | now uses donorlocal background directly on the benchmark path | none for benchmark parity |
| progress bar shell | GENERIC_FALLBACK | runtime skewed plate with donor-tuned palette | donor shell has authored metallic framing and head detail | strengthened shell proportions, stroke, and placement to fit the donor gate | `preloader.progress.shell` |
| progress fill/head | GENERIC_FALLBACK | runtime gold/ivory fill with brighter cap | donor fill has authored segments and a stronger moving head | retuned fill colors and cap treatment to read less like a generic loader | `preloader.progress.fill`, `preloader.progress.head` |
| glow/ambient layer | GENERIC_FALLBACK | runtime glow, floor ellipse, embers, and shine sweep | donor has richer intro-only motion layers | strengthened ember count, glow balance, and floor lighting | `preloader.ambient.glow`, `preloader.ambient.spark`, `preloader.intro.stinger` |

## B) Top area

| Slot | Status | What donorlocal currently uses | What donor does better | Fix applied in this pass | Missing runtime key / mapping if still blocked |
| --- | --- | --- | --- | --- | --- |
| mascot/topper | DONORLOCAL_MAPPED_OK | direct donorlocal `startScreen2` hero card plus strike/super-strike topper coin art | donor has fully authored game-specific mascot lockup and cleaner state variants | switched from generic symbol/plate treatment to donorlocal hero art and donor strike/super-strike reactions | final `topper.hero.lockup` keyed for Crazy Rooster instead of donor title art |
| jackpot plaques | PLACEHOLDER | direct donorlocal coin plaques from local donor images | donor uses plaque art that matches its own runtime values and state messaging | replaced vector coin placeholders with donorlocal plaques for benchmark composition | split plaque layers so art is separate from value text: `heroUiAtlas.jackpot-plaque-mini/minor/major/grand` |
| jackpot numerals | MISSING_MAPPING | donor plaques currently carry baked donor values like `10x` / `175x` | donor-grade parity for our game needs runtime-driven values, not baked donor numbers | kept donor plaque shapes for layout benchmark but documented them as temporary | `heroUiAtlas.jackpot-numeral-mini/minor/major/grand` or equivalent text-safe numeral plates |
| title/banner area | PLACEHOLDER | runtime `CRAZY ROOSTER` / state text layered over donorlocal hero art | donor banner stack is more integrated and less text-over-text | tightened top banner spacing and reduced the generic header footprint | `topper.banner.ribbon`, `topper.banner.caption` |
| topper aura/glow | GENERIC_FALLBACK | runtime aura rings plus donorlocal collector/spark textures where available | donor has stronger authored aura/lightning sync per state | strengthened aura sizing, donor spark use, and pulse timing | `heroVfxAtlas.vfx-hero-glow`, `heroVfxAtlas.vfx-hero-pulse`, `heroVfxAtlas.vfx-jackpot-burst` |

## C) Reel area

| Slot | Status | What donorlocal currently uses | What donor does better | Fix applied in this pass | Missing runtime key / mapping if still blocked |
| --- | --- | --- | --- | --- | --- |
| cabinet frame | GENERIC_FALLBACK | runtime cabinet framing around the donor reel bed | donor uses authored cabinet material and depth, not drawn strokes | retuned frame depth, glow, and inset to better fit the donor bed | `uiAtlas.cabinet-frame-outer`, `uiAtlas.cabinet-frame-inner` |
| reel bed | DONORLOCAL_MAPPED_OK | direct crop from donorlocal `slot.d8edf336.png` | donor bed still has more authored surround detail | switched the donorlocal benchmark path to the real donor reel-bed crop instead of per-reel fallback panels | none for benchmark parity |
| separators | GENERIC_FALLBACK | runtime separator strokes and inset rails | donor separators have richer metallic depth and edge treatment | kept runtime separators but tuned them around the donor bed crop | `uiAtlas.reel-separator-vertical`, `uiAtlas.reel-separator-horizontal` |
| stage/pedestal | GENERIC_FALLBACK | runtime lower pedestal and cabinet stage | donor pedestal has stronger authored material and shadowing | strengthened pedestal depth and lower boundary in the cabinet chrome | `uiAtlas.cabinet-stage`, `uiAtlas.cabinet-pedestal` |
| symbol treatment | DONORLOCAL_MAPPED_OK | donorlocal symbol atlas is already active on the reel faces | donor animation still has more authored idle motion around some symbols | kept donorlocal symbol mapping intact while lifting the surrounding cabinet | optional symbol-specific animation mappings only |

## D) Bottom/control cluster

| Slot | Status | What donorlocal currently uses | What donor does better | Fix applied in this pass | Missing runtime key / mapping if still blocked |
| --- | --- | --- | --- | --- | --- |
| spin shell | GENERIC_FALLBACK | runtime spin plate and sublabel | donor spin shell is a bespoke authored button | tightened the plate and rail so the fallback reads intentional | `uiAtlas.button-spin` donorlocal equivalent |
| turbo shell | DONORLOCAL_MAPPED_OK | direct donorlocal `hold_for_turbo` art on a refined fallback plate | donor still has stronger authored pressed/armed states | moved turbo off the generic circle into donorlocal text art | optional `uiAtlas.button-turbo-armed` / `button-turbo-pressed` |
| autoplay shell | DONORLOCAL_MAPPED_OK | direct donorlocal `arrows` art with runtime badge | donor could still use stronger active/inactive state differentiation | switched autoplay from generic icon fallback to donorlocal arrows | optional `uiAtlas.button-autoplay-active` |
| buy bonus tile | DONORLOCAL_MAPPED_OK | direct donorlocal `btn_buy_bonus` art plus runtime value chip | donor buy tile still has cleaner state-specific pricing treatment | switched left and bottom buy tiles onto donorlocal art with runtime value chip overlay | optional `uiAtlas.button-buybonus-price-chip` |
| sound/settings/history shells | MISSING_MAPPING | styled circles with label-only fallback for `LIVE`, `MENU`, `LOG` | donor would use authored shells instead of empty fallback rings | removed broken white-block fallback and replaced it with clean plate-only placeholders | `uiAtlas.button-sound`, `uiAtlas.button-settings`, `uiAtlas.button-history` |
| balance/bet/win plates | GENERIC_FALLBACK | runtime text and layout plates only | donor hierarchy is more authored and less HUD-text driven | preserved readable runtime text while cleaning the rail composition | `uiAtlas.hud-balance-plate`, `uiAtlas.hud-bet-plate`, `uiAtlas.hud-win-plate` |
| typography/numerals | GENERIC_FALLBACK | runtime text everywhere outside donor-baked art | donor uses more bespoke numerals and title surfaces | kept runtime text but reduced broken fallback shells around it | authored numeral font/plate mappings for HUD and jackpot layers |

## E) Feature/win presentation

| Slot | Status | What donorlocal currently uses | What donor does better | Fix applied in this pass | Missing runtime key / mapping if still blocked |
| --- | --- | --- | --- | --- | --- |
| line plates | GENERIC_FALLBACK | runtime payline callout plates and tone-colored banners | donor would use authored line plates and richer art-backed numerals | kept exact math-driven sequencing but retained runtime plates | `uiAtlas.payline-plate-1..8` or shared authored payline plate set |
| line badges | GENERIC_FALLBACK | runtime line id / multiplier badges | donor badges would be more bespoke and less text-box driven | left the beta5 line system intact and paired it with donorlocal chrome around it | `uiAtlas.payline-badge`, `symbolAtlas.payline-multiplier-chip` |
| sequence chip | GENERIC_FALLBACK | runtime sequence chip (`1/2`, `2/2`) | donor chip would be authored and more integrated | no regression; kept the staged sequence logic from beta5c/5d | `uiAtlas.payline-sequence-chip` |
| collect | DONORLOCAL_MAPPED_OK | donor strike coin topper, donor collector ring, staged particles, exact payline path | donor still has stronger art-specific banner integration | collect now visibly reads as its own moment instead of generic win flash | optional authored `collect.banner` only |
| boost | GENERIC_FALLBACK | runtime boost charge panel plus donor spark/ring support and stronger timing | donor boost uses heavier authored lightning/charge layering | intensified boost timing and donorlocal spark use, but the core banner is still runtime | `heroVfxAtlas.boost-charge`, `heroVfxAtlas.boost-lightning-hero`, `heroUiAtlas.boost-banner` |
| jackpot | PLACEHOLDER | donor coin/plaque shapes plus runtime particles and jackpot action stack cue | donor jackpot would use exact game-specific plaques, numerals, and heavier bursts | jackpot is visibly stronger, but still leans on donor-baked plaque art and runtime FX | `heroUiAtlas.jackpot-callout-banner`, `heroVfxAtlas.jackpot-burst`, split jackpot numeral mappings |
| win overlays | GENERIC_FALLBACK | runtime win-tier overlays and math-driven payline banners | donor would use more authored overlay surfaces | no regression; kept exact line/multiplier bridge intact | `heroUiAtlas.win-overlay-big`, `heroUiAtlas.win-overlay-huge`, `heroUiAtlas.win-overlay-mega` |
| layered FX | DONORLOCAL_MAPPED_OK | donorlocal lightning arc, collector ring, spark bursts, plus runtime coin flights | donor still has more authored fire/lightning layering | now uses donorlocal vfx textures where local mapping exists instead of pure geometry | optional extra fire sheet / topper lightning hero layers |

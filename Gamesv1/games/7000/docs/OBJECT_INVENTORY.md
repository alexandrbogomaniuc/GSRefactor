# Game 7000 Runtime Object Inventory

Status legend:

- `mapped`: donorlocal/runtime mapping exists and is active.
- `generic`: functional but still generic runtime art/layout.
- `placeholder`: temporary visual stand-in, usable but not final.
- `missing`: no mapped runtime object yet.

## Base Symbols

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Symbol ID 0 (`symbol-0-egg`) | Base line-pay symbol | `symbolAtlas` frame key | Watermelon/Grapes-family symbol slot | mapped |
| Symbol ID 1 (`symbol-1-cherries`) | Base line-pay symbol | `symbolAtlas` frame key | Cherries | mapped |
| Symbol ID 2 (`symbol-2-lemon`) | Base line-pay symbol | `symbolAtlas` frame key | Lemon | mapped |
| Symbol ID 3 (`symbol-3-orange`) | Base line-pay symbol | `symbolAtlas` frame key | Orange | mapped |
| Symbol ID 4 (`symbol-4-plum`) | Base line-pay symbol | `symbolAtlas` frame key | Plum | mapped |
| Symbol ID 5 (`symbol-5-bar`) | Base line-pay symbol | `symbolAtlas` frame key | BAR | mapped |
| Symbol ID 6 (`symbol-6-seven`) | Wild / top line-pay symbol | `symbolAtlas` frame key | 777 wild | mapped |
| Bell symbol slot (`symbol-bell`) | Donor paytable symbol (`20x`) | Dedicated runtime symbol ID 10; donorlocal uses `symbol-bell`, committed providers fallback to BAR art | Bell | mapped |

## Feature Symbols

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Symbol ID 7 (`symbol-7-coin`) | Bonus Coin carrier | `symbolAtlas` frame key, provisional math symbol id 7 | Bonus Coin | mapped |
| Symbol ID 8 (`symbol-8-bolt`) | Chicken Coin carrier | `symbolAtlas` frame key, provisional math symbol id 8 | Chicken Coin | mapped |
| Symbol ID 9 (`symbol-9-rooster`) | Super Chicken Coin carrier | `symbolAtlas` frame key, provisional math symbol id 9 | Super Chicken Coin | mapped |
| Feature value numerals | Coin value readability | runtime text overlays and frame swaps | authored donor value chips | generic |

## Top Area / Topper

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Mascot/topper main lockup | Top visual anchor | `Beta3VisualChrome` + donorlocal hero textures | Donor hero/topper | mapped |
| Jackpot plaques (shape) | Top jackpot containers | donorlocal plaque textures | Donor jackpot plaques | mapped |
| Jackpot numerals (runtime-owned) | Dynamic jackpot values | runtime text over plaques | donor split plaque + numeral layers | placeholder |
| Top aura/glow | Motion and emphasis | runtime glow + donorlocal vfx textures where available | donor authored aura stack | generic |

## Cabinet / Reel Bed

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Reel bed surface | Main board container | donorlocal reel-bed crop | donor reel bed | mapped |
| Cabinet frame | Board framing and depth | runtime `Beta3VisualChrome` geometry | donor authored cabinet frame | generic |
| Reel separators | Column/row separation | runtime line/separator drawing | donor metallic separators | generic |
| Stage/pedestal | Lower cabinet grounding | runtime stage graphics | donor pedestal | generic |

## Controls / Bottom Cluster

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Spin shell | Primary action | runtime shell | donor authored spin shell | generic |
| Hold-for-turbo control | Turbo trigger | donorlocal `hold_for_turbo` + runtime plate | donor turbo control | mapped |
| Autoplay shell | Auto control | donorlocal arrows + runtime badge | donor autoplay control | mapped |
| Buy Bonus tile | Bonus purchase control | donorlocal button art + runtime value chip | donor buy-bonus tile | mapped |
| Sound/settings/history shells | Secondary controls | runtime circles/text | donor authored small shells | missing |
| Balance/bet/win plates | HUD values | runtime text plates | donor authored HUD plates | generic |

## Win / Feature Overlays

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Payline overlay path | Exact line rendering | `PaylineOverlay.ts` + `mathBridge.lineWins` | donor line path draw | mapped |
| Winning symbol highlight | Per-line symbol emphasis | `WinHighlight.ts` | donor win symbol emphasis | mapped |
| Line callout plate | Line id / multiplier / payout summary | runtime callout UI | donor authored line plate | generic |
| Sequence chip (`1/2`, `2/2`) | Multiline staging state | runtime chip | donor authored sequence chip | generic |
| Collect overlay moment | Collect choreography | donorlocal + runtime choreography hooks | donor collect flow | mapped |
| Boost overlay moment | Boost choreography | runtime-heavy with donorlocal support layers | donor boost stack | generic |
| Jackpot overlay moment | Jackpot choreography | mixed donorlocal/runtime layers | donor jackpot stack | placeholder |
| Big/huge/mega overlays | Win-tier presentations | runtime overlays | donor authored win overlays | generic |

## FX Layers

| Object | Role | Current runtime source | Donor benchmark equivalent | Status |
| --- | --- | --- | --- | --- |
| Lightning arc/path | Boost and feature emphasis | `LightningArcFx` with donorlocal vfx fallback chain | donor lightning path | mapped |
| Fire/topper FX | Topper feature reactions | `LayeredFxController` runtime layering | donor fire stack | generic |
| Coin-fly particles | Collect coin trajectories | `ParticleBurst` / runtime particle config | donor coin_fly package | generic |
| Collector ring/sparks | Collect impact emphasis | donorlocal ring/spark textures + runtime scheduler | donor collect ring stack | mapped |
| Background ambient particles | Idle liveness | runtime particles | donor authored ambience | generic |

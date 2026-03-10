# 23 Rebuild Recommendation

## Objective

- Rebuild the donor's presentation depth with original assets and original branding.
- Treat the donor as a choreography and state-shape benchmark, not a source asset pack.

## Build order

### 1. Lock the event model first

Build these code-level presentation events before asking art to finish anything:
- `ui.intro.show`
- `game.idle.enter`
- `round.spin.start`
- `round.reel.stop.1`
- `round.reel.stop.2`
- `round.reel.stop.3`
- `feature.collect.triggered`
- `feature.boost.triggered`
- `feature.bonus.enter`
- `ui.buyBonus.open`
- `round.autoplay.start`
- `overlay.winTier.enter`
- `overlay.totalSummary.update`

Why first:
- The donor presentation is clearly event-driven.
- Without explicit event hooks, Engineering will end up hardcoding timing into view components and lose the donor's layered feel.

### 2. Implement the runtime slot contract before final art lands

Use `21_RUNTIME_ASSET_SLOT_CONTRACT.md` as the provider-facing contract.

Build placeholder-ready controllers for:
- shell / preload
- cabinet / reel shell
- topper / mascot
- jackpot plaques
- layered FX
- buy-bonus modal cards
- win overlays
- audio routing

Why second:
- The donor depth depends on multiple independent packages firing together.
- A single monolithic atlas swap system will not scale to the required layering.

### 3. Build the reel shell and stop cascade before feature FX

Priority sequence:
1. Reel shell lifecycle from `slot_1.json` staging shape.
2. Per-reel stop event dispatch.
3. Normal settle vs bonus-held settle split.
4. Only then add topper reactions, lightning, fire, and coin-fly choreography.

Why:
- The donor's strongest hard timing truth is the staggered reel lifecycle.
- Everything else should hang off that stable backbone.

### 4. Add topper and jackpot systems as separate controllers

Build the topper as its own layer with these state groups:
- idle
- collect response
- boost enter / loop / exit
- jackpot reaction
- win reaction

Build jackpot plaques separately from the mascot.

Why:
- The donor topper is not just decoration. It anchors the cabinet and becomes the focal point during special states.
- Jackpot plaques need to update independently from the mascot animation.

### 5. Add layered FX as independent packages, not one merged effect

Build at least these effect controllers:
- board-border highlight
- front fire
- back fire
- lightning path
- coin transfer
- total-summary counter dressing

Why:
- The donor repeatedly uses front/back depth and parallel layers.
- Merging all FX into one timeline will make tuning and performance control harder.

### 6. Modal and overlay systems should be shared UI infrastructure

These should be reusable UI shells with provider art injected into them:
- menu overlay
- rules modal
- how-to modal
- settings popup
- buy-bonus modal
- win overlays

Why:
- The donor mixes small anchored overlays and full-screen modal surfaces.
- Engineering will move faster if modal choreography is standardized and provider art is slotted into a shared surface system.

## What should be code vs art

### Code

- Event bus / trigger routing
- Reel lifecycle controller
- Topper state controller
- Jackpot value controller
- FX controller orchestration
- Overlay controller and modal stack
- Audio event router
- Provider asset resolver
- Responsive layout / depth ordering

### Art / animation

- Preload backgrounds and wordmark treatments
- Cabinet frame and separators
- Topper / mascot art and states
- Jackpot plaques and attached jackpot coins
- Full symbol family
- Buy-bonus tier cards
- Layered fire / lightning / coin-fly packs
- Big / mega / total win overlay packages
- Settings / menu / rules / how-to skins

## What should remain provider-specific

Provider-specific content:
- wordmark
- background pack
- cabinet skin
- topper / jackpot art
- symbol atlas
- buy-bonus card art
- VFX atlas / Spine-equivalent exports
- audio bank

Provider-agnostic code:
- event names
- controller interfaces
- modal stack behavior
- audio routing semantics
- reel stop dispatch
- runtime slot contract keys

## What is benchmark-only from the donor

Safe to benchmark:
- 3x4 board staging
- fixed 8-line layout
- topper-above-cabinet composition
- buy-bonus three-tier concept
- collect + boost + bonus layering
- staggered reel-start and reel-stop cadence
- separate big / mega / total overlay phases
- front/back FX layering

Do not copy:
- chicken/farm theme
- gold-chicken mascot identity
- exact red/gold palette
- exact typography
- exact jackpot plaque shapes
- exact card copy and labels
- exact animation curves or frame timing beyond high-level pacing
- donor sound design

## Practical recommendation

Build in this order:
1. Event model and slot contract.
2. Reel shell and stop events.
3. Topper + jackpot controllers.
4. FX controllers.
5. Buy-bonus modal and win overlays.
6. Audio matrix and mixing.
7. Provider packs with original art.

If Engineering follows that order, the project gets the donor's presentation depth without inheriting donor art or donor-specific code assumptions.

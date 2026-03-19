# Rule → Object Bindings (Donorlocal Benchmark)

Branch baseline:

- `codex/qa/7000-math-calibration-pass-20260318-1352`

Working branch:

- `codex/qa/7000-rule-object-binding-20260319-1157`

Scope:

- donorlocal benchmark mode only (`allowDevFallback=1&assetProvider=donorlocal`)
- math source: provisional
- GS/runtime contracts unchanged

## Binding Matrix

| Donor rule | Math condition | Runtime objects involved | Visible trigger / presentation | Donorlocal asset slots used | Status |
| --- | --- | --- | --- | --- | --- |
| 3 reels x 4 rows board | `lineWinRule.reelCount=3`, `rowCount=4`, `CrazyRoosterGameConfig` layout | `CrazyRoosterSlotMachine`, `CrazyRoosterReel`, `MainScreen` layout pipeline | Reel bed and symbols render as 3x4 in base and bonus flows | `uiAtlas.reel-frame-panel` fallback, donorlocal reel bed crop (`slot.d8edf336.png`) | complete |
| 8 fixed paylines, left-to-right, highest-per-line | `paytable.lineWinRule.kind=left_to_right_highest_per_line`, `stackedPaylines=8`; provisional evaluator applies highest-per-line only | `provisionalMathSource`, `PaylineOverlay`, `WinHighlight` | Exact path drawing + per-line symbol highlights + staged line sequence | `uiAtlas.payline-pill`, `uiAtlas.button-buybonus` (boost/jackpot plate tone), `symbolAtlas.coin-multiplier-*` badges | complete |
| Chicken Coin / Super Chicken Coin / Bonus Coin symbol roles | `feature-tables.baseGame.symbolIds` (`bonus=7`,`chicken=8`,`superChicken=9`) | `provisionalMathSource`, `CrazyRoosterSymbol`, `TopperMascotController`, `LayeredFxController` | Distinct collect/boost/jackpot reactions and cue sequencing from symbol-triggered outcomes | `symbol-7-coin`, `symbol-8-bolt`, `symbol-9-rooster`, `collector-symbol`, `coin-multiplier-10x` | complete |
| Bonus Coin only on reels 1 and 3 | `reel-strips-or-weights`: symbol `7` has weight `0` on reel index `1`; trigger validator checks reels `0` and `2` | `provisionalMathSource` reel sampler + trigger checker | Bonus hold cue timing (`round.reel.stop.3.bonusHold`) and bonus-entry sequencing | bonus cue uses donorlocal benchmark FX layer and reel-stop chrome | complete |
| Bonus trigger (bonus on reels 1 and 3 + chicken/super on reel 2) | `isBonusTriggerSatisfied` with `bonusCoinReelIndexes=[0,2]` and `chickenTriggerReelIndex=1` | `provisionalMathSource`, `MainScreen.applyAnimationCue` | `feature.bonus.enter` cue drives hold-and-win intro callout, topper reaction, coin-fly burst | `spark-burst-01`, `coin-multiplier-2x` coin flights, donorlocal topper surfaces | complete |
| Bonus game uses only Bonus/Chicken/Super Chicken symbol family | `holdAndWinBonus.allowedSymbolIds=[7,8,9]` | `provisionalMathSource` hold-and-win grid builder | Bonus entry and ongoing hold visuals stay bound to bonus-family triggers | bonus-family symbol slots above; no line-pay symbol injection in bonus board | complete |
| 3-spin reset rule | `holdAndWinBonus.entrySpins=3`, `resetToThreeOnNewLock=true` | `provisionalMathSource` respin loop, runtime counters | State message + cue sequencing continue through respin reset points | runtime text/counter surfaces (no dedicated donorlocal spin-counter art yet) | partial |
| Collect feature | Triggered from collect symbol outcomes (`collectTriggered`) in base/bonus | `MainScreen`, `TopperMascotController`, `LayeredFxController`, `PaylineOverlay` tone resolver | `feature.collect.triggered` + collect line tone + collect sweep and coin fly | `collector-ring`, `collector-symbol`, `coin-multiplier-2x` | complete |
| Chicken Boost feature | `boostTriggered` true via boost rules and super chicken outcomes | `MainScreen`, `LayeredFxController`, `TopperMascotController`, `Beta3VisualChrome` | `feature.boost.triggered` + boost callout + staged topper/FX pulses | `spark-burst-01`, `coin-multiplier-10x`, donorlocal topper state layers | complete |
| Jackpot tiers (Mini/Minor/Major/Grand) | `jackpots.levels` + `jackpotTier` resolved from provisional outcome | `provisionalMathSource`, `MainScreen.resolveJackpotReactionLevel`, `LayeredFxController`, `Beta3VisualChrome` | Jackpot cue, plaque pulse routing, jackpot callout caption from resolved tier | jackpot plaque surfaces + `spark-burst-03` | partial |
| Pile of Gold feature behavior | Trigger families route FX toward topper/coin-bed anchor (`topperAnchor`) on line, collect, boost, jackpot | `TopperMascotController`, `LayeredFxController`, `ParticleBurst` | Coin-fly and burst effects route into the topper/plaque zone during win/feature cues | topper/hero donorlocal stack + `coin-multiplier-2x` flight sprite | partial |
| Buy Bonus tiers (75/200/300) | `mathBridge.mode in {buy75,buy200,buy300}` with buy tables and HUD buy action payload | `MainScreen.handleBuyFeature`, `MainScreen.scheduleMathBridgeFeatureCues`, `MainScreen.applyAnimationCue` | New explicit `feature.buyBonus.enter` cue with mode-specific label before feature stack | buy tile `button-buybonus`, bonus-tone cue surfaces | complete |
| Win tier presentation (big/huge/mega) | `mathBridge.winTier` from provisional outcome and thresholds | `MainScreen.applyAnimationCue`, `LayeredFxController.playWinPulse`, `TopperMascotController` | `overlay.winTier.enter` drives tier title + topper state + pulse | donorlocal topper + runtime overlay surfaces | complete |
| Bell paytable symbol (20x) | `paytable.linePayouts["10"]=20`, symbol model id `10` in reel weights and symbols | `CrazyRoosterSymbol`, `CrazyRoosterGameConfig`, provisional line evaluator | Bell can appear as a dedicated runtime symbol and be paid via line wins | primary: `symbol-bell`; fallback chain: `symbol-5-bar` when provider lacks bell art | complete |

## Notes

- Bell runtime slot is now present end-to-end in provisional math and renderer.
- Donorlocal is the only provider with direct bell art mapping in current local benchmark assets.
- Committed providers currently use an explicit BAR-art fallback for Bell until dedicated non-donor bell art is added.

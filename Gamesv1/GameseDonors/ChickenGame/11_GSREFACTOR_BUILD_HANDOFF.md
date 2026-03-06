# 11 GSRefactor Build Handoff

## Objective
- Implement a new original game module that preserves GS runtime contracts while reusing shared shell/runtime architecture.

## Where the new module should live
- New game package under `Gamesv1/games/<new_game_id>/` following the same structure as `games/premium-slot`.
  - Evidence: `Gamesv1/games/premium-slot/docs/ARCHITECTURE_MAP.md`

## Shared systems to reuse
- Runtime transport + contract mapping: `GsRuntimeClient` + outcome mapper pattern.
  - Evidence: `Gamesv1/games/premium-slot/docs/ARCHITECTURE_MAP.md`
- Shared premium HUD shell: `packages/ui-kit/src/hud/PremiumTemplateHud.ts` with capability-driven visibility.
  - Evidence: `Gamesv1/docs/SHARED_HUD_ARCHITECTURE.md`, `Gamesv1/packages/ui-kit/src/hud/PremiumTemplateHud.ts`
- Feature modules/presentation orchestration from `@gamesv1/ui-kit`.
  - Evidence: `Gamesv1/games/premium-slot/docs/ARCHITECTURE_MAP.md`

## Contract and runtime constraints (must stay unchanged)
- Keep canonical browser endpoints unchanged (`/slot/v1/bootstrap`, `/slot/v1/opengame`, `/slot/v1/playround`, `/slot/v1/featureaction`, `/slot/v1/resumegame`, `/slot/v1/gethistory`).
  - Evidence: `Gamesv1/docs/gs/browser-runtime-api-contract.md`
- Browser remains presentation-only for session/wallet truth.
  - Evidence: `Gamesv1/.agent/rules/01_rules_protocol.md`, `Gamesv1/docs/SHARED_HUD_ARCHITECTURE.md`
- Use `@gamesv1/core-protocol`; do not introduce direct game-level WebSocket path as canonical runtime.
  - Evidence: `Gamesv1/.agent/rules/01_rules_protocol.md`, `Gamesv1/games/premium-slot/docs/ARCHITECTURE_MAP.md`

## Mapping donor mechanics to GSRefactor modules
- Collect/Boost/Bonus concepts map to feature module states and presentation payload interpretation.
  - Evidence donor mechanics: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`
  - Evidence GS module model: `Gamesv1/games/premium-slot/docs/ARCHITECTURE_MAP.md`

## RoundActionBuilder / BetSelectionBuilder integration plan
- `RoundActionBuilder` (implementation target): build deterministic round action payloads for `playround` and `featureaction` from currently selected bet, feature context, and idempotency-safe metadata; output contract-conformant request bodies only.
- `BetSelectionBuilder` (implementation target): centralize bet-step policy (min/max/step/current selection), UI +/- interaction mapping, and validation before round dispatch.
- Integration point: wire both builders into `MainScreen` control handlers where spin/buy/autoplay actions are resolved before runtime client calls.
  - Reference integration surface: `Gamesv1/games/premium-slot/src/app/screens/main/MainScreen.ts`

## Configurable vs hardcoded
- Configurable:
  - Symbol set, paylines metadata, feature labels/rules text, buy-bonus tier multipliers, VFX timing profiles, audio cue map.
- Hardcoded invariants:
  - GS HTTP runtime contract shape.
  - requestCounter/idempotency ownership on GS side.
  - Browser presentation-only truth model.

## Donor truth carried forward
- Buy Bonus present with 3 tiers and reconciled modal evidence.
- Autoplay captured in fixed replacement.
- Old 20-spin clip replaced by fixed artifact.
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/AUTOPLAY_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/TWENTY_SPIN_RECONCILED.md`

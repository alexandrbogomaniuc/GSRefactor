# PRESENTATION_LAYER_ARCHITECTURE

## Goal

Provide one canonical mapping from GS `presentationPayload` into reusable UI-facing structures for premium-slot and future slots.

## Canonical Mapper

- `packages/ui-kit/src/shell/presentation/PremiumPresentationMapper.ts`

Output model:

- reel presentation (`reels.stopColumns`, `symbolGrid`)
- counters
- messages (`uiMessages`)
- audio cues (`audioCues`)
- animation cues (`animationCues`)
- labels

## Layout Constraints Contract

Mapper layout is now shell-generic and explicit:

- `mapPlayRoundToPresentation(result, layoutConstraints)`
- `layoutConstraints` requires:
  - `reelCount`
  - `rowCount`
  - optional `symbolModulo`

The shared mapper does not import game-specific `GameConfig`.

Game integrations provide constraints from runtime/theme/shared config inputs.
For premium-slot this binding happens in:

- `games/premium-slot/src/app/runtime/RuntimeOutcomeMapper.ts`

## Allowed Browser-Visible Inputs

Mapper consumes only canonical browser-visible payload fields:

- `reelStops`
- `symbolGrid`
- `uiMessages`
- `animationCues`
- `audioCues`
- `counters`
- `labels`

Round metadata consumed from runtime envelope:

- `round.roundId`
- `round.winAmountMinor`

## Explicit Exclusions

Engine-private/server-private fields are not mapped into UI state (for example `serverAudit`, `rngTraceRef`, internal diagnostics).

## Runtime Composition

`games/premium-slot/src/app/screens/main/MainScreen.ts`:

1. receives GS runtime envelope from `GsRuntimeClient.playround(...)`
2. maps envelope via `mapPlayRoundToPresentation(...)`
3. applies resulting cues/overlays/labels to presentation components

This preserves GS-authoritative financial/session truth while keeping presentation deterministic and reusable.

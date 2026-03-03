# CODE_SPLITTING_PLAN

## Current Situation

Build output previously showed a large single chunk warning (>500k minified).
After introducing manual chunking, only `vendor-pixi` remains above the warning threshold.

## Implemented Mitigation

`games/premium-slot/vite.config.ts` now includes Rollup `manualChunks` to split:

- `vendor-pixi` for Pixi/@pixi dependencies
- `vendor-motion` for motion runtime
- `runtime-core` for protocol/compliance modules
- `ui-engine` for ui-kit/pixi-engine shared shell code

This is an implemented mitigation, not only a proposal.

Current outcome:

- app/runtime/ui chunks are now split
- one heavy Pixi vendor chunk remains (`vendor-pixi`)

## Follow-up Steps

1. Verify chunk distribution in each build (chunk list + gzip sizes).
2. Split optional Pixi subsystems behind dynamic imports where practical (for example advanced render paths not needed on initial boot).
3. Keep feature overlays/video players lazily loaded when not required at boot.
4. Track top-3 largest chunks in release checklist for every game.

## Acceptance Criteria

- No single chunk should dominate all render/runtime/vendor logic.
- Chunking must remain deterministic and reproducible.
- Contract and behavior parity with canonical runtime must remain unchanged.

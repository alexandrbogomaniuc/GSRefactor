# Provisional Generator Before/After

Branch: `codex/qa/7000-provisional-generator-debug-20260321-1243`  
Run mode: `base`, no preset (`mathSource=provisional`, no `mathPreset`)  
Seed: `700020260316`  
Spins: `100`

## Before (confirmed from prior audit evidence)

- Win rate: `100 / 100` (`100%`)
- Repeated boards: `95 / 100`
- Unique boards: `5`
- Outcome quality: suspicious (effectively canned/non-random behavior in normal flow)

## After (fresh trace from this branch)

- Win rate: `43 / 100` (`43%`)
- Repeated boards: `0 / 100`
- Unique boards: `100`
- Collect triggers: `9 / 100`
- Boost triggers: `2 / 100`
- Bonus triggers: `0 / 100`
- Jackpot triggers: `1 / 100`

## Code Changes That Produced the Delta

1. [provisionalMathSource.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/provisionalMathSource.ts)
- Removed implicit no-preset fallback into canned `normal` override.
- Added explicit generator tracing (source path, seed before/after, matrices, board hash, rewrite flag).
- Kept explicit presets functional (only applied when `mathPreset` is explicitly provided).
- Decoupled `buildGridFromColumns` from browser-bound game config import so generator can be traced in Node without presentation side effects.

2. [RuntimeOutcomeMapper.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/RuntimeOutcomeMapper.ts)
- Added parsing/passthrough of `mathBridge.generatorTrace` for runtime inspection.

3. [provisional-generator-trace.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/scripts/provisional-generator-trace.ts)
- Added deterministic 100-spin trace/sanity generator with machine-readable output.

## Is behavior now sane for dev iteration?

Yes for this specific generator bug: normal no-preset provisional mode now uses weighted RNG and no longer exhibits the 100%-win tiny-board-loop failure pattern.

## Evidence

- Trace: [PROVISIONAL_GENERATOR_TRACE_100_SPINS.json](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/PROVISIONAL_GENERATOR_TRACE_100_SPINS.json)
- Sanity sample: [RUNTIME_SANITY_100_SPINS_AFTER_GENERATOR_FIX.json](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/RUNTIME_SANITY_100_SPINS_AFTER_GENERATOR_FIX.json)

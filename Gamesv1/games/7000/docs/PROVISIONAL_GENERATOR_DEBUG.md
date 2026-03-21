# Provisional Generator Debug (Normal Mode, No Preset)

Branch: `codex/qa/7000-provisional-generator-debug-20260321-1243`  
Base commit: `a48b449afb6472226cc61ef73a14553fefcabbbd`  
Analyzed URL shape: `?allowDevFallback=1&assetProvider=donorlocal&mathSource=provisional` (no `mathPreset`)

## 1) Exact Runtime Path (Request -> Generation -> Presentation)

1. `MainScreen.handleSpin()`  
   File: [MainScreen.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/screens/main/MainScreen.ts)  
   Action: calls `gsRuntimeClient.playround(selectedBet)`.
2. `GsRuntimeClient.playround(...)`  
   File: [GsRuntimeClient.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/GsRuntimeClient.ts)  
   Condition: when `mathSource=provisional`, bootstrap uses demo transport (`bootstrapDemo()`), so playround is routed to `crazyRoosterDemoRuntime.playround`.
3. `CrazyRoosterDemoRuntime.playround(...)`  
   File: [demoRuntime.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/demoRuntime.ts)  
   Condition: `isProvisionalMathMode()` true -> `envelopeFromProvisionalMath(...)`.
4. `envelopeFromProvisionalMath(...)`  
   File: [demoRuntime.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/demoRuntime.ts)  
   Calls:
   - `resolveMathPreset()` (null when no URL preset/proof override),
   - `ProvisionalMathSource.nextOutcome({ mode, totalBetMinor, requestedPreset })`,
   - `adaptProvisionalMathOutcome(outcome)` and writes `presentationPayload.mathBridge`.
5. `ProvisionalMathSource.nextOutcome(...)`  
   File: [provisionalMathSource.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/provisionalMathSource.ts)  
   Generates board, evaluates paylines/triggers, emits `generatorTrace`.
6. `MainScreen.queueRuntimeEnvelope(...)` + `handleSpinComplete()`  
   File: [MainScreen.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/screens/main/MainScreen.ts)  
   Reads `mathBridge` via `readMathBridgeHints(...)` and schedules line/feature visuals.

## 2) Truth: Normal Mode Source Path

## Before fix (confirmed root cause)
- In `nextOutcome(...)`, code used:
  - `const preset = input.requestedPreset ?? "normal";`
  - `PRESET_OVERRIDES.normal` existed as a canned board.
- Result: no-preset flow collapsed into canned output behavior instead of pure weighted generation.

## After fix
- `requestedPreset` is now explicit nullable state:
  - `const requestedPreset = input.requestedPreset ?? null;`
  - `const presetOverride = requestedPreset ? PRESET_OVERRIDES[requestedPreset] ?? null : null;`
- No `mathPreset` now means:
  - `sourcePathUsed = "weighted-reel-rng"`
  - no canned board override.

## 3) Debug/Preset Leakage Check

- `resolveMathPreset()` in [demoRuntime.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/demoRuntime.ts):
  - returns explicit URL preset if present,
  - otherwise may map `proofState` (`collect`, `boost`, `bonus`, `mega`),
  - otherwise returns `null`.
- For the required URL (`mathSource=provisional`, no `mathPreset`, no `proofState`), no preset leakage occurs.

## 4) RNG Progression Check

- RNG state is now tracked in `ProvisionalMathSource` (`rngState`, `nextRandom`, `getRngState`).
- Trace includes `seedStateBefore` and `seedStateAfter` for each spin.
- 100-spin trace shows monotonic state advancement and unique board hashes per spin.

## 5) Must-win Filtering / Board Promotion / Post-process

- No must-win filter exists in `nextOutcome(...)`.
- No win-forcing post-pass exists in normal mode.
- Trace field `postGenerationRewrite` is `false` for weighted normal spins in the after-fix run.

## 6) Runtime vs Simulation Harness

- Runtime generator path:
  - [provisionalMathSource.ts](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/src/app/runtime/provisionalMathSource.ts)
  - used via demo runtime envelope.
- Simulation harness path:
  - [run-math-sim.mjs](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/scripts/run-math-sim.mjs)
  - separate script implementation (same data files, different runtime wrapper).
- They are aligned on same math JSON inputs but are separate codepaths.

## 7) Why 5 Unique Boards in Prior Evidence

The prior evidence pattern (95 repeats, 5 unique in 100) came from preset/canned behavior leaking into what should have been normal random generation. In this pass, the no-preset path is now explicitly detached from canned overrides, and the fresh trace confirms weighted-RNG path per spin.

## 8) Evidence Files

- Trace: [PROVISIONAL_GENERATOR_TRACE_100_SPINS.json](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/PROVISIONAL_GENERATOR_TRACE_100_SPINS.json)
- Sanity run: [RUNTIME_SANITY_100_SPINS_AFTER_GENERATOR_FIX.json](/Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000/docs/RUNTIME_SANITY_100_SPINS_AFTER_GENERATOR_FIX.json)

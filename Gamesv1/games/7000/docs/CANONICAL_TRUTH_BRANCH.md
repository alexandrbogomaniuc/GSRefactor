# Game 7000 Canonical Truth Branch (2026-03-21)

This document records the single consolidation branch for Game 7000 after the provisional generator/runtime truth fix and build-hygiene pass.

## Canonical Branch

- `codex/qa/7000-canonical-truth-20260321-1754`

## Folded-In Branch Lines

The canonical branch is based on:

- `codex/qa/7000-provisional-generator-debug-20260321-1243` (generator/runtime fix line)

That line already contains:

- `codex/qa/7000-runtime-bugfix-clean-20260321-0805` (win-total propagation fix + runtime evidence)
- `codex/qa/7000-rule-object-binding-20260319-1157` (donorlocal benchmark presentation baseline and rule/object bridge artifacts)
- donorlocal lock commits (`62541c19`, `e57ff764`) that keep donorlocal as the benchmark path and remove non-donor provider runtime drift.

## Reconciliation Decision

- Older donorlocal parity branch line (`beta5d/beta6/beta7`) is historically important but diverged before later donorlocal lock/runtime truth fixes.
- For this consolidation pass we **did not** merge those older divergent heads into code, to avoid reintroducing asset-path drift and stale generator behavior.
- Canonical truth is therefore anchored on the newer clean generator-fix line plus this pass' build-hygiene updates.

## This Pass (Consolidation + Build Hygiene)

Merged/retained as canonical in this branch:

- Provisional generator fix (normal mode no longer must-win/repeating tiny board set)
- Runtime win-total propagation fix
- Current donorlocal benchmark presentation baseline from the rule-object-binding lineage
- Fresh canonical sanity artifacts:
  - `PROVISIONAL_GENERATOR_TRACE_100_SPINS_CANONICAL.json`
  - `RUNTIME_SANITY_100_SPINS_CANONICAL.json`

Build hygiene fixes added in this branch:

- Added `reelStopVariants` to `CrazyRoosterSpinOptions`
- Added `applyPresentationVariants()` to `CrazyRoosterSlotMachine`
- Threaded reel stop variants through reel stop handling in `CrazyRoosterReel`

## Current Benchmark Launch Truth

Use Node 22 and donorlocal benchmark mode:

```bash
export PATH="$HOME/.nvm/versions/node/v22.22.1/bin:$PATH"
cd /Users/alexb/Documents/Dev/_worktrees/7000-runtime-bugfix-clean-20260321-0805/Gamesv1/games/7000
VITE_ASSET_PROVIDER=donorlocal corepack pnpm run dev:benchmark
```

Benchmark URL:

- `http://127.0.0.1:8081/?allowDevFallback=1&assetProvider=donorlocal&mathSource=provisional`

## What Remains Unresolved

- Donor-perfect choreography/art parity is still partial and remains a separate stream from this consolidation pass.
- Donorlocal benchmark relies on local-only donor files and manifest outside committed provider assets.
- Provisional math remains non-certified (development model), even after generator behavior was fixed to sane non-preset randomness.

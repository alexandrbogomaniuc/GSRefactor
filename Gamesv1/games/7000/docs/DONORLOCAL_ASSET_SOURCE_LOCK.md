# DONORLOCAL Asset Source Lock

This lock exists to prevent accidental donorlocal asset regressions.

## Approved source (internal benchmark)

`/Users/alexb/Documents/Dev/_worktrees/7000-beta4a-runtime-slot-system-20260310-0841/Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local`

## Branch path that must point to approved source

`Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local`

This path must be a real folder in the active 7000 worktree (not a symlink).

## Lock command (run before donorlocal benchmark)

```bash
corepack pnpm -C Gamesv1/games/7000 run donorlocal:lock-assets
```

## Why this exists

Previous regressions came from `_donor_raw_local` resolving via symlink or rsync overwrite to a different donor bundle than the approved baseline, which made donorlocal load wrong atlases/effects while the URL looked correct.

Do not repoint this folder to another donor folder unless explicitly approved for a benchmark-source migration.

## Safety rule (hard)

`scripts/lock-donorlocal-assets.sh` is validate-only. It must not copy, sync, or delete donorlocal files.

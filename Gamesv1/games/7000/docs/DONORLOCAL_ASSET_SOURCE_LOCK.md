# DONORLOCAL Asset Source Lock

This lock exists to prevent accidental donorlocal asset regressions.

## Approved source (internal benchmark)

`/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local`

## Branch path that must point to approved source

`Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local`

This path is a symlink in the active 7000 worktree.

## Lock command (run before donorlocal benchmark)

```bash
corepack pnpm -C Gamesv1/games/7000 run donorlocal:lock-assets
```

## Why this exists

Previous regressions came from `_donor_raw_local` pointing to a different donor bundle than the approved baseline, which made donorlocal load wrong atlases/effects while the URL looked correct.

Do not repoint this symlink to another donor folder unless explicitly approved for a benchmark-source migration.

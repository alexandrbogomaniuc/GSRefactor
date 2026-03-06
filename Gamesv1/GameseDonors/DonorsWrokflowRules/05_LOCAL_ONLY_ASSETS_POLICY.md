# 05 Local Only Assets Policy

## Standard local path

- Store any donor-only binaries or exported raw donor files under:
  - `Gamesv1/GameseDonors/<DonorName>/assets/_donor_raw_local/`

## Ignore mechanism

- Ignore that local folder through `.git/info/exclude`.
- Do not use `.gitignore` for donor binaries in this public repo.

## Public repo rule

- Never commit donor binaries, packaged donor builds, raw donor asset dumps, or other redistributable donor payloads to this repository.
- Canonical donor packs may reference local-only handling, but they must not include the binaries themselves.

## Example reference

- ChickenGame local-only example: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`

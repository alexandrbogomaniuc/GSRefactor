# 05 Local Only Assets Policy

## Standard local path

- Store any donor-only binaries or exported raw donor files under:
  - `Gamesv1/GameseDonors/<DonorName>/assets/_donor_raw_local/`

## Preferred capture procedure

- Use Chrome DevTools `Network` tab for local-only donor capture.
- Enable `Preserve log` before donor load or feature probes.
- Use `Save all as HAR with content` for the main archive artifact.
- Keep only sanitized derivative files such as `.json`, `.png`, `.jpg`, `.webp`, `.svg`, `.mp3`, `.ogg`, `.wav`, `.md`, or `.txt` when they are useful for local analysis.

## Ignore mechanism

- Ignore that local folder through `.git/info/exclude`.
- Do not use `.gitignore` for donor binaries in this public repo.

## Redaction baseline

- Never keep raw `authToken`, `token`, `session`, or cookie values.
- Redact credential-like values from HAR files, exported responses, notes, and filenames before storing anything locally.
- If a donor payload cannot be sanitized safely, do not retain it.

## Public repo rule

- Never commit donor binaries, packaged donor builds, raw donor asset dumps, or other redistributable donor payloads to this repository.
- Canonical donor packs may reference local-only handling, but they must not include the binaries themselves.

## Example reference

- ChickenGame local-only example: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`

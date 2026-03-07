# Local Only Donor Assets

## Purpose

- Use this note only for local handling of donor binaries or raw exported donor files for analysis.
- Do not commit donor binaries to this public repository.

## Local storage path

- Store any donor-only binaries under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/`.

## How to populate via Chrome DevTools

1. Open the donor game in Chrome.
2. Open DevTools and switch to the `Network` tab.
3. Enable `Preserve log` before the donor boot flow finishes.
4. Reproduce the state you want to inspect.
5. Right-click in the network request table and use `Save all as HAR with content`.
6. Save the HAR under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/har/`.
7. If specific responses are useful for local analysis, export only sanitized copies into subfolders such as:
   - `images/`
   - `audio/`
   - `json/`
   - `metadata/`

## Recommended local-only file types

- `.har` for full request and response review.
- `.json` for sanitized response bodies or manifest-style payloads.
- `.png`, `.jpg`, `.webp`, `.svg` for image references when locally useful.
- `.mp3`, `.ogg`, `.wav` for audio references when locally useful.
- Plain-text notes such as `.md` or `.txt` for local analysis metadata.

## Redaction rules

- Never store raw `authToken`, `token`, `session`, or cookie values.
- If a HAR or exported response contains credential-like values, redact them before keeping the file locally.
- Do not save raw cookie headers or browser storage dumps.
- Do not place tokens or session values in filenames, notes, or folder names.
- If a response cannot be sanitized safely, do not keep it.

## Ignore method

- Ignore that local folder through `.git/info/exclude`.
- Do not add this path to `.gitignore`.

## Public repo rule

- Never commit donor binaries, packaged donor builds, or raw donor asset dumps to this public repo.
- `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/` is strictly local-only and must remain outside Git history.

## Minimal verification

- Confirm `.git/info/exclude` contains `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/`.
- Run `git status --short` before commit or push.
- Run a repo-local scan for `authToken=`, `token=`, `session=`, and `cookie` before finalizing any donor-doc change.

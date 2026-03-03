# Obsolete GS Contract Variants

Use this folder to archive superseded GS slot contract drafts.

Current obsolete markers:
- Legacy endpoint alias `/slot/v1/history` is obsolete; canonical endpoint is `POST /slot/v1/gethistory`.
- Legacy browser endpoint prefix `/v1/*` is obsolete; canonical browser API uses `/slot/v1/*`.
- Pre-canonical combined fixtures such as `*.success.json` are obsolete once moved under `obsolete/fixtures-legacy/`.
- Any bootstrap wording that implies bootstrap can be returned by `openGame` is obsolete.

Archived fixture files:
- `fixtures-legacy/bootstrap.success.json`
- `fixtures-legacy/opengame.success.json`
- `fixtures-legacy/playround.success.json`
- `fixtures-legacy/playround.duplicate.json`
- `fixtures-legacy/featureaction.success.json`
- `fixtures-legacy/resumegame.success.json`
- `fixtures-legacy/closegame.success.json`
- `fixtures-legacy/gethistory.success.json`

Canonical source of truth remains:
- `docs/gs/README.md`

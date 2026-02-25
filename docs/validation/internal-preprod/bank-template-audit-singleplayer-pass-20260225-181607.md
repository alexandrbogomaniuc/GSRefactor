# Bank Template Audit (Singleplayer) PASS for Internal Test Banks

Date (UTC): 2026-02-25 18:16:07 UTC

## Purpose
Run a repeatable audit (script-based) to confirm the current internal test banks follow the singleplayer-safe template rules:
- no third-party internet URLs
- no third-party allow-list domains
- required singleplayer cleanup fields are disabled/empty

## Audit command used
```bash
node /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/bank-template-audit.mjs \
  --bank-id 6274,6275,6276 \
  --mode singleplayer \
  --json-out /Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/bank-template-audit-singleplayer-20260225-181607.json
```

## Result (Plain English)
- Bank `6274`: PASS
- Bank `6275`: PASS
- Bank `6276`: PASS
- Overall: PASS

This confirms the cleaned banks do not contain the checked third-party internet URLs/domains and match the required singleplayer cleanup state.

## JSON Report Artifact
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/internal-preprod/bank-template-audit-singleplayer-20260225-181607.json`

## Tool Added
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/bank-template-audit.mjs`

## Notes
- This is an audit/check tool (safe by default). It does not modify bank settings.
- Bank settings are still changed through the GS support page workflow.

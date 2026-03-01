# GS Enable / Disable / Canary / Rollback

Status: canonical operational checklist for GS release activation.

## Enable Candidate

1. Validate checksums.
2. Register release artifact set in GS.
3. Enable candidate for canary audience only.
4. Run canary + smoke checklist.

## Promote

1. Verify canary error-rate and runtime health.
2. Promote candidate to full traffic.
3. Store promoted release as known-good.

## Disable / Rollback

1. Disable current candidate/release in GS routing.
2. Re-enable previous known-good release.
3. Validate launch + one round + reconnect restore + wallet consistency.

## Evidence Required

- registration artifact id
- release id
- promoted/rolled-back version
- timestamp + operator
- checklist outcome log

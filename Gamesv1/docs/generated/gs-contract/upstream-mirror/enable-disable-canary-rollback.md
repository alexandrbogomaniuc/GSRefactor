# GS Enable / Disable / Canary / Rollback

1. Register release artifacts.
2. Enable candidate via canary.
3. Run smoke checklist before promotion.
4. Disable candidate and rollback to known-good release if canary fails.
5. Re-verify launch, playround, resume, gethistory after rollback.

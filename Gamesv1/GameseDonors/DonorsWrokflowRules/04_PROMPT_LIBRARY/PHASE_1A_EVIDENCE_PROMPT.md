# Phase 1A Evidence Prompt

## Prompt

Use Git as the audit workflow. Do NOT push to main directly.

This is PHASE 1A ONLY: raw donor evidence capture.

Mandatory startup:
1. Read repo agent rules and workflows.
2. Use a clean git worktree or clean clone from `origin/main`.
3. Create a fresh audit branch.

Rules:
- RAW CAPTURE only.
- Do NOT write synthesis docs, build handoff docs, originality guidance, or promo briefs.
- Keep prose minimal and factual.
- If video works, validate it before trusting it.
- If video fails, say so explicitly and compensate only within the allowed evidence scope.

Create a run folder under:
- `Gamesv1/GameseDonors/_research_runs/<DonorName>/codex-phase1a-<YYYYMMDD-HHMM>/`

Minimum outputs:
- `README.md`
- `SESSION_LOG.md`
- `OBSERVED_SPINS.csv`
- `EVIDENCE_INDEX.csv`
- `JACKPOT_BET_TABLE.csv`
- `OPEN_QUESTIONS.md`
- screenshots, snapshots, console note/export, network note/export, perf notes

Coverage:
- 25+ spins minimum, or more if special states remain unseen
- menu/rules/autoplay/buy-bonus probes where present
- sanitization scan before commit

Stop condition:
- Push the Phase 1A branch and stop.

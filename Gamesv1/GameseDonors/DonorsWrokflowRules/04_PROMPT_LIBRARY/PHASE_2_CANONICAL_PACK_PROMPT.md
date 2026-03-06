# Phase 2 Canonical Pack Prompt

## Prompt

Use Git as the audit workflow. Do NOT push to main directly.

This is PHASE 2: canonical donor intelligence pack.

Base:
- Branch from the approved Phase 1D branch.

Rules:
- Synthesize only from committed research evidence.
- Do not replay the donor in this phase.
- Move research runs into the canonical donor folder with `git mv`; do not duplicate them.
- Every factual statement must cite evidence.
- Mark `INFERENCE` and `UNOBSERVED` explicitly.
- Do not change GS contracts or architecture boundaries.

Canonical destination:
- `Gamesv1/GameseDonors/<DonorName>/`
- `Gamesv1/GameseDonors/<DonorName>/assets/_research_runs/<DonorName>/...`

Required canonical docs:
- `README.md`
- source/access
- executive summary
- screen state map
- control inventory
- gameplay/mechanics
- math hypotheses
- VFX/audio timings
- technical observations
- originality guardrails
- asset generation brief
- promo brief
- GSRefactor build handoff
- build kickoff prompt

Also include:
- visual reference index
- local-only donor assets policy

Stop condition:
- Push the Phase 2 branch and stop.

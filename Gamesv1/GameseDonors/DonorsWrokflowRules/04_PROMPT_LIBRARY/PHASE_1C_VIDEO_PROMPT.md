# Phase 1C Video Prompt

## Prompt

Use Git as the audit workflow. Do NOT push to main directly.

This is PHASE 1C ONLY: video remediation and factual video descriptions.

Base:
- Branch from the approved Phase 1A branch or commit.

Rules:
- Do not redo the full evidence run.
- Validate recording before the main capture set.
- Black, blank, or frozen videos do not count.
- If all recording methods fail, stop and document failure truthfully.

Required:
1. Record a short preflight test clip.
2. Extract first, middle, and last thumbnails.
3. Mark the clip valid only if donor UI is visible and progressing.
4. Capture the required probe clips for idle/spin, loss/win, menu, rules, bet/jackpot change, autoplay, buy bonus, special effect, and continuous run as applicable.

Outputs:
- `README.md`
- `VIDEO_INDEX.csv`
- `VIDEO_DESCRIPTIONS.md`
- `VIDEO_VALIDATION.md`
- `SPIN_RUN_LOG.csv`
- `OPEN_QUESTIONS.md`

Stop condition:
- Push the Phase 1C branch and stop.

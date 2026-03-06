# Phase 1D Reconcile Prompt

## Prompt

Use Git as the audit workflow. Do NOT push to main directly.

This is PHASE 1D ONLY: truth reconciliation against committed media.

Base:
- Branch from the approved Phase 1C branch or commit.

Rules:
- Audit what the committed artifacts actually show.
- Fix metadata when artifacts are good but misdescribed.
- Replace artifacts only when they are misleading for their claimed purpose.
- Do not create new theories beyond visible evidence.

Required outputs:
- `ARTIFACT_AUDIT.md`
- `VIDEO_INDEX_RECONCILED.csv`
- `VIDEO_DESCRIPTIONS_RECONCILED.md`
- `SCREENSHOT_INDEX_RECONCILED.csv`
- `BUY_BONUS_RECONCILED.md`
- `AUTOPLAY_RECONCILED.md`
- `TWENTY_SPIN_RECONCILED.md`
- `OPEN_QUESTIONS.md`

Truth rules:
- Do not call autoplay confirmed unless autonomous repeated spins are visible.
- Do not call Buy Bonus absent if a modal is visible.
- Do not call a clip a 20-spin run unless it truly is one uninterrupted gameplay sequence.

Stop condition:
- Push the Phase 1D branch and stop.

# Donor Workflow Rules

## What this is

- This folder defines the reusable donor investigation SOP for Gamesv1.
- It codifies the evidence-first workflow proven during the ChickenGame run:
  - Phase 1A raw evidence capture
  - Phase 1C video remediation
  - Phase 1D truth reconciliation
  - Phase 2 canonical donor pack
- It is docs/templates only.
- It does not change GS contracts or runtime architecture.

## Why evidence-first matters

- Donor packs become auditable only when every factual claim points to a saved artifact.
- Raw evidence must exist before synthesis, otherwise canonical packs drift into guesses.
- Video claims must be validated because black, frozen, or misdescribed clips create false confidence.
- Reconciliation is necessary because first-pass capture labels are often imperfect.
- Public repos require strict sanitization and strict separation from any local-only donor binaries.

## Definition of Done for a donor pack

- A clean raw-evidence run exists under a research-run folder with screenshots, snapshots, logs, and factual notes.
- Video evidence is either validated or explicitly marked failed; black or unusable clips are not counted.
- Reconciled metadata exists for any artifact that was mislabeled, misdescribed, or replaced.
- The canonical pack is written under the donor's canonical folder and cites evidence for every factual statement.
- `INFERENCE` and `UNOBSERVED` are labeled explicitly instead of being blended into factual prose.
- Sanitization scans confirm no raw auth/session/cookie values were committed.
- No donor binaries or raw donor asset dumps are committed to this public repo.

## Example references

- Visual evidence indexing example: `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`
- Local-only donor binary policy example: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`

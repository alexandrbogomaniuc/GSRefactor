# 01 Phases Overview

## Phase 0: Preflight

### Inputs

- Donor URL or launch entry point
- Clean git worktree or clean clone
- Branch from `origin/main`
- Required repo rules and workflows loaded

### Outputs

- Fresh audit branch
- Target run folder name and timestamp
- Confirmed tool plan
- Stop/go decision for capture

### Stop condition

- Stop if the worktree is dirty, the branch base is wrong, or the required capture path cannot be trusted.

## Phase 1A: Raw evidence capture

### Inputs

- Phase 0 branch and donor URL
- Capture tools for screenshots, snapshots, network, console, perf, and optional video

### Outputs

- Raw research-run folder with screenshots, snapshots, logs, evidence index, observed spins, jackpot table, and open questions
- Minimal prose only
- Sanitization result for the run folder

### Stop condition

- Stop after required coverage is met and the raw-evidence checkpoint is pushed.
- Do not write synthesis docs in Phase 1A.

## Phase 1C: Video remediation

### Inputs

- Phase 1A branch and evidence
- A real recording method that can be validated

### Outputs

- Validated video set
- Video thumbnails
- Video metadata
- Factual video descriptions
- Continuous run artifact if required by scope

### Stop condition

- Stop if recording methods fail and document that failure truthfully.
- Do not fake success with black or frozen video.

## Phase 1D: Reconciliation

### Inputs

- Phase 1C media
- Phase 1A screenshots and notes

### Outputs

- Artifact audit
- Reconciled video index and descriptions
- Reconciled screenshot index
- Corrected conclusions for controls such as Buy Bonus, autoplay, and long-run claims

### Stop condition

- Stop once the committed media and all claimed meanings match.
- Replace artifacts only where necessary.

## Phase 2: Canonical donor pack

### Inputs

- Reconciled research evidence from Phase 1D

### Outputs

- Canonical donor folder under `Gamesv1/GameseDonors/<DonorName>/`
- Canonical docs, build handoff, prompts, and originality guardrails
- Research runs moved into canonical `assets/_research_runs/` without duplication

### Stop condition

- Stop after the canonical pack is pushed.
- Do not continue donor capture during synthesis.

## Optional Phase 2E: Rare-state follow-up

### Inputs

- Published canonical pack
- Clear need for a missing jackpot, big-win, or overlay state

### Outputs

- New rare-state evidence if actually observed
- Or a short negative-result note documenting what was attempted

### Stop condition

- Stop when the hunt stops being cheap, trustworthy, or practical.
- Do not stretch optional follow-up into speculative or low-quality evidence.

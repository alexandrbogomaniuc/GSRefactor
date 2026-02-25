# Portal Rebuild Notes (Milestone 3)

Last updated: 2026-02-25 (UTC)

## Why this portal was rebuilt
The old portal could show a misleading message: the checklist looked complete (`41/41`), which can be misunderstood as "project finished".

This milestone rebuild separates two different things:
1. **Delivery checklist completion** (how many planned items were marked done)
2. **Cutover readiness** (whether the system is actually approved for production cutover)

These are now shown side-by-side in plain English.

## What changed (plain English)
The portal now includes these new sections:
- **Program Snapshot**: short non-technical summary of the real project state
- **Checklist Completion vs Cutover Readiness**: explains why `41/41` does not mean cutover-ready
- **Current Blockers**: plain-English blocker cards with evidence links
- **Requirement Reality Check**: Milestone 2 audit summary (verdict counts + links)
- **Scope Creep / Parallel Workstreams**: separates core GS modernization from New Games and Casino Manager work
- **Latest Evidence Sources**: quick links to the main proof files
- **What Needs Approval Next**: non-technical decision list for stakeholders

The old detailed cards are still kept below these summaries:
- raw cutover readiness status feed
- session outbox canary health
- checklist sections with evidence references

## New data files used by the portal
These files were added so the portal can show the Milestone 2 audit and scope separation:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/audit-requirements-status.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/audit-scope-summary.json`

## Sync script improvements
The dashboard sync script was updated to:
- stop using hardcoded `/Users/alexb/...` defaults
- resolve repo paths dynamically from the script location
- embed the new audit JSON snapshots into the portal HTML for `file://` mode
- keep backward-compatible behavior for the existing checklist/outbox/readiness snapshots

Script:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh`

## What was validated in this milestone
### Passed
- Sync script runs successfully and updates embedded snapshots for:
  - checklist
  - outbox health
  - deploy readiness
  - audit requirements summary
  - audit scope summary
- Portal JavaScript passes syntax check (`node --check` on extracted script)
- Portal loads in **file mode** and renders the new sections with embedded data
- Evidence links render and open as `file://` links in local testing

### Not validated in this milestone (environment limitation)
- Live support-page HTTP rendering (example: `http://127.0.0.1:18081/support/modernizationProgress.html`) was not reachable during this test run because the local GS/support page was not running.

## What this milestone does not change
- It does **not** change runtime behavior of the game server.
- It does **not** resolve the actual cutover blockers.
- It does **not** change the 41/41 checklist data itself.

This milestone only fixes how project status is presented so stakeholders can see the factual state clearly.

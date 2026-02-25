# Document Relocation Map

Last updated: 2026-02-25 UTC
Status: Wave 1 and Wave 2 completed.

## Why this file exists
Project documents were scattered across top-level `docs/`. This map records what was moved into project-named folders so teams can find files quickly and avoid broken assumptions.

## Wave 1: Post-project audit documents
Moved from top-level `docs/` to:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/After-Project-Milestones-Plan.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Requirement-Reality-Check-Audit.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Architecture-Workflow-Visual-Pack.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Portal-Rebuild-Notes.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Config-Portal-User-Guide.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/README-ONBOARDING.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Project-Finalization-Report.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/Bank-Template-Singleplayer-vs-Multiplayer-Policy.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/post-project-audit/audit-evidence/`

Compatibility updates done in this wave:
- Updated support portal data references in:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`
- Updated internal links in moved post-project docs.

## Wave 2: Existing named project folders
Moved:
- `/Users/alexb/Documents/Dev/Dev_new/docs/New games Project/`
  -> `/Users/alexb/Documents/Dev/Dev_new/docs/projects/new-games/`
- `/Users/alexb/Documents/Dev/Dev_new/docs/Casino Manager Project/`
  -> `/Users/alexb/Documents/Dev/Dev_new/docs/projects/casino-manager/`

Compatibility updates done in this wave:
- Updated New Games proof-pack default report path in:
  - `/Users/alexb/Documents/Dev/Dev_new/new-games-server/scripts/perf-proof-pack.sh`
- Updated known references across docs and support portal pages.

## Deferred wave (not moved yet)
Large legacy phase docs in top-level `docs/` remain in place for now because many scripts and support pages still reference exact paths.

Examples of path-coupled areas:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase*-status-report-*.sh`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationDocs.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`

Next wave recommendation:
1. Build a file-by-file mapping for all remaining top-level numbered docs.
2. Patch hardcoded script and support-page references in one controlled commit.
3. Re-run support page smoke checks after relocation.

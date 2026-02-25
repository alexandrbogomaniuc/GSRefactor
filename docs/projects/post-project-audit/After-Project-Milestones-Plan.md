# After-Project Milestones Plan

Last updated: 2026-02-25 (UTC)

## Why this plan exists

The project dashboard shows the checklist as complete, but the project is **not ready for cutover yet**.

This plan pauses normal feature work and moves the project into an **audit and finalization mode**.

The goal is to produce clear proof, simple explanations, and honest status reporting for non-technical stakeholders.

## What this plan will do

- Check what was promised at the start of the project.
- Check what was really built and tested.
- Collect hard proof (reports, logs, test results, screenshots/evidence files).
- Rewrite the progress portal so it shows the true status.
- Create easy-to-read diagrams and guides.
- Improve cross-platform onboarding for the refactor environment.
- Produce a final report that clearly separates:
  - core project work,
  - extra work done outside the main scope.

## What this plan will NOT do

- It will **not** continue normal feature development.
- It will **not** do a production cutover.
- It will **not** rewrite business logic or add new product features.
- It will only change code when needed for:
  - the audit portal/dashboard,
  - evidence integration,
  - cross-platform Docker/onboarding scripts.

## Working rules (simple version)

- All stakeholder-facing writing must use simple English.
- Daily activity must be logged in `docs/12-work-diary.md`.
- After each milestone:
  1. commit changes,
  2. push to Git,
  3. save a memory note,
  4. stop and wait for approval.
- Milestones must be completed in order.

---

## Milestone 2: Reality Check and Evidence Gathering

### Purpose
Create a clear, honest audit of the original project requirements and prove what is really implemented and tested.

### Main outputs

- `docs/projects/post-project-audit/audit-evidence/` (new evidence folder)
- `docs/projects/post-project-audit/Requirement-Reality-Check-Audit.md` (main plain-English audit report)

### What will be checked

We will audit the original planning and requirements documents, including:

- main GS modernization requirements and roadmap
- sprint/phase planning commitments
- initial New Games project decisions and milestones

### What evidence will be collected

For each requirement, we will collect proof such as:

- generated status reports
- test results
- verification suite outputs
- readiness reports
- validation reports
- links to code/config only when needed to prove a claim

### How results will be explained

Each requirement will have a simple-English summary:

- What was promised
- What we found
- Proof we checked
- What this means in simple English
- Is it actually working today? (`Yes / Partly / No / Not proven`)

### Approval stop point

Stop after Milestone 2 when:

- every requirement has a verdict,
- every requirement has evidence,
- the audit report is readable by a non-technical person.

### Commit / push / memory rule

- Commit Milestone 2 outputs only.
- Push to Git.
- Save a memory summary.
- Wait for approval before Milestone 3.

---

## Milestone 3: Rebuild the Progress Portal (True Project Status)

### Purpose
Rebuild the project progress portal so it shows the **real cutover readiness**, not only checklist completion.

### Main outputs

- Updated support portal page (existing modernization progress page)
- New audit summary data files for the portal
- Short portal rebuild notes document

### What the portal must show (in simple English)

- Checklist completion (for example `41/41`)
- Cutover readiness (for example `NO_GO`)
- Current blockers and why they matter
- What is in scope vs what is outside scope
- Links to evidence from Milestone 2
- What approval is needed next

### Important rule

The portal must **not** use “100% complete” as a claim that the project is ready to launch.

### Approval stop point

Stop after Milestone 3 when:

- the portal clearly shows checklist completion and cutover readiness as separate things,
- evidence links work,
- the portal text is understandable for non-technical stakeholders.

### Commit / push / memory rule

- Commit Milestone 3 outputs only.
- Push to Git.
- Save a memory summary.
- Wait for approval before Milestone 4.

---

## Milestone 4: Visual Architecture and Workflow Pack

### Purpose
Create visual diagrams and comparison tables so stakeholders can understand what changed in the system.

### Main output

- `docs/projects/post-project-audit/Architecture-Workflow-Visual-Pack.md`

### What will be included

- “Before” architecture (legacy system)
- “After” architecture (current project state)
- optional “Target” architecture (cutover-ready goal state)
- workflow comparisons:
  - launch flow
  - wager/settle flow
  - config change flow
- version comparison tables (including Cassandra old vs new)
- clear notes on what is still legacy

### Format rule

- Diagrams will use Mermaid (so they render in Markdown).
- Every diagram will include a short “How to read this” note in plain English.

### Approval stop point

Stop after Milestone 4 when a non-technical stakeholder can visually understand:

- what was modernized,
- what stayed legacy,
- where blockers still exist.

### Commit / push / memory rule

- Commit Milestone 4 outputs only.
- Push to Git.
- Save a memory summary.
- Wait for approval before Milestone 5.

---

## Milestone 5: Configuration Portal User Guide

### Purpose
Write a practical, easy guide for non-technical users to use the new configuration portal safely.

### Main output

- `docs/projects/post-project-audit/Config-Portal-User-Guide.md`

### What the guide will include

- plain-language overview of the portal
- what users should and should not do
- step-by-step instructions (Step 1, Step 2, etc.)
- real-world examples of common tasks
- safety checks before publishing changes
- rollback awareness
- common mistakes and recovery steps

### Important truth rule

If a portal feature is only a scaffold or only partly working, the guide will say that clearly.

### Approval stop point

Stop after Milestone 5 when the guide is practical enough for a non-technical operator to follow.

### Commit / push / memory rule

- Commit Milestone 5 outputs only.
- Push to Git.
- Save a memory summary.
- Wait for approval before Milestone 6.

---

## Milestone 6: Cross-Platform Portability and Onboarding (Refactor Only)

### Purpose
Fix path and script portability so a new machine can start the **refactor environment only** (not legacy stacks) using a simple onboarding flow.

### Main outputs

- `docs/projects/post-project-audit/README-ONBOARDING.md`
- a cross-platform launcher script (single entry point)
- updates to refactor startup/support scripts to remove hardcoded local paths

### What “success” means

A new person (or AI agent) on Windows, macOS, or Linux can:

1. pull the `GSRefactor` repo,
2. run one main onboarding command,
3. start the refactor stack (with documented prerequisites),
4. run a smoke check and understand the result.

### Scope limits

- Refactor environment only.
- No legacy stack startup in this milestone.
- Only portability/onboarding script changes are allowed.

### Approval stop point

Stop after Milestone 6 when onboarding is documented and the startup flow is path-portable (not tied to one machine).

### Commit / push / memory rule

- Commit Milestone 6 outputs only.
- Push to Git.
- Save a memory summary.
- Wait for approval before Milestone 7.

---

## Milestone 7: Finalization Report

### Purpose
Produce the final simple-English project closeout report that clearly separates core scope from extra work.

### Main output

- `docs/projects/post-project-audit/Project-Finalization-Report.md`

### What the final report will include

- what the main project was supposed to do
- what was completed inside the main scope
- what is still blocking cutover
- what was done outside the main scope
- major timeline summary
- evidence index (linking to the audit and portal)
- recommended next steps for business/operations

### Required scope separation sections

- Core GS Modernization (In Scope)
- New Games / Plinko Workstream (Parallel Scope)
- Casino Manager Workstream (Out of Main Scope)
- Tooling Depth Expansions (Phase 8/9 scope creep examples)

### Approval stop point

Milestone 7 is the final deliverable for this after-project audit program.

### Commit / push / memory rule

- Commit Milestone 7 outputs only.
- Push to Git.
- Save a memory summary.

---

## Notes about execution order

- Milestones must be completed in order (2 → 3 → 4 → 5 → 6 → 7).
- We will pause after each milestone for your approval.
- If new facts are discovered during the audit, they will be documented with evidence (not guessed).

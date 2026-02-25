# Config Portal User Guide (Milestone 5)

Last updated: 2026-02-25 (UTC)
Audience: non-technical operators, support staff, managers

## 1. What this portal is for (simple English)
The Config Portal is a safer support page for **viewing** important Game Server settings and walking through a **safe change workflow shape** (draft -> validate -> approve -> publish -> rollback).

It helps you:
- see cluster connection settings in one place,
- understand bank setting names and categories,
- view the current effective values for a selected bank,
- practice and track workflow steps safely,
- use browser-local approval and guardrail checks before risky actions.

## 2. What this portal is NOT (important)
This page is **not yet a full replacement** for the legacy bank editor.

Some parts are live and useful today. Some parts are **scaffold** (safe workflow shape) or **browser-local helper tools**.

This guide clearly marks each task as:
- `Works now`
- `Partly works`
- `Planned / not active yet`

## 3. Before you start
### You need
1. A running GS support page (refactor GS runtime) so `/support/configPortal.jsp` opens.
2. A bank selected in the page dropdown (the portal loads one by default when available).
3. A modern browser (the portal uses browser-local storage for some helper features).

### Main page URL
- `/support/configPortal.jsp`

Example local runtime URL when refactor GS is running:
- `http://127.0.0.1:18081/support/configPortal.jsp`

## 4. Page layout (what you will see)
The page is organized into levels:
- **Level 1: Cluster Hosts** (shared host/port values)
- **Level 1b: Session Outbox Safety Controls** (Kafka retry/DLQ safety settings)
- **Level 2: Bank Settings Catalog** (what each bank setting means)
- **Level 3: Effective Values** (what the selected bank is actually using)
- **Level 4: Config Workflow** (draft/validate/approve/publish/rollback scaffold)
- **Level 4c: Publish/Rollback Guardrails + Canary Controls (Browser Local)**
- **Level 4b: Persistent Approval Queue (Browser Local)**

Quick links are also shown near the top (Progress, Docs Index, Runbook, Cluster Hosts).

## 5. Safety rules (read this first)
### What you should do
- Use the portal first to **look up and understand** settings before changing anything.
- Use **Validate** and the guardrail checks before any publish/rollback action.
- Write a clear **Change Reason** so other operators understand what you are doing.
- Use the local approval queue/guardrails as a checklist, even when not required.

### What you should NOT do
- Do **not** treat a green-looking screen as proof that a change was applied everywhere.
- Do **not** click **Publish** or **Rollback** without reading the warnings and checking the guardrail panel.
- Do **not** assume browser-local tools (approval queue, guardrails) update the backend automatically.
- Do **not** skip the legacy tools when you need a real edit path and the new workflow is only scaffold mode.

## 5a. Task map (quick answer to common requests)
- View current cluster settings safely -> **Task 2**
- Find a bank setting -> **Task 1**, **Task 4**, **Task 5**
- Understand what a setting category means -> **Task 4**
- Draft a change / review draft flow -> **Task 6**
- Validate before publish -> **Task 6** and **Task 7**
- Publish/rollback (with current limitations explained) -> **Task 7**
- Confirm whether a change took effect -> **Task 11**

## 6. Real-world tasks (step-by-step)

## Task 1: Open the portal and choose a bank
**Status:** `Works now`

**Use this when:** You want to inspect settings for a specific bank.

### Steps
1. Open `/support/configPortal.jsp`.
2. Find the **Bank** dropdown near the top of the page.
3. Choose the bank you want (the dropdown shows bank id + external id + description).
4. (Optional) Type a word in the **Search** box (for example: `wallet`, `multiplayer`, `FRB`, `limit`).
5. Click **Apply**.
6. To clear the filter, click **Reset**.

### What you should expect
- The page updates the Level 2 and Level 3 tables based on your bank selection and search filter.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`

## Task 2: View cluster-level settings safely (Level 1)
**Status:** `Works now`

**Use this when:** You need to check shared hosts/ports and environment settings without editing them.

### Steps
1. Open `/support/configPortal.jsp`.
2. Scroll to **Level 1: Cluster Hosts**.
3. Read the **Key** and **Value** table.
4. Use the top **Search** box to filter by key name (example: `CASSANDRA`, `KAFKA`, `SESSION_SERVICE`).
5. If you need more key descriptions, click the link to `/support/clusterHosts.jsp` shown under the Level 1 heading.

### What you should expect
- A read-only table showing values from `cluster-hosts.properties`.
- If the source file is missing/empty, the page shows a warning.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/31-cluster-hosts-centralization-and-portal-visibility.md`

## Task 3: Check session outbox safety controls (Level 1b)
**Status:** `Works now`

**Use this when:** You want to review Kafka retry/DLQ safety settings before canary testing or incident recovery work.

### Steps
1. Open `/support/configPortal.jsp`.
2. Scroll to **Level 1b: Session Outbox Safety Controls**.
3. Review the table columns:
   - **Key**
   - **Current Value**
   - **Purpose**
4. Look for missing values (the page shows **MISSING** clearly).
5. Read the runtime endpoint examples shown below the table (outbox/replay endpoints) if you are coordinating with engineering/support.

### What you should expect
- A read-only safety table with descriptions for outbox relay, retry, DLQ, and replay-related keys.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`

## Task 4: Understand what a bank setting category means (Level 2)
**Status:** `Works now`

**Use this when:** You see a setting key and want to understand what area it belongs to (Wallet, Bonus/FRB, Multiplayer, etc.).

### Steps
1. Open `/support/configPortal.jsp`.
2. Scroll to **Level 2: Bank Settings Catalog**.
3. First, read the small category explanation table (Wallet, Bonus/FRB, Multiplayer, Integration URL, Game Limits, Legacy MQ, General).
4. Then review the main catalog table with columns:
   - **Key**
   - **Type**
   - **Category**
   - **Mandatory**
   - **Description**
5. Use the top **Search** box to filter by key, category, or description.

### What you should expect
- A human-readable explanation of bank setting keys and categories.
- Fewer mistakes when discussing settings with developers.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/57-config-portal-category-guide-20260220-181200.md`

## Task 5: View the actual values for one bank (Level 3 Effective Values)
**Status:** `Works now`

**Use this when:** You need to confirm what a bank is currently using (not just what the catalog says a key means).

### Steps
1. Choose a bank at the top of the page and click **Apply**.
2. Scroll to **Level 3: Effective Values (Bank X)**.
3. Review the bank summary block (Bank Id, External Bank Id, SubCasino Id, Default Currency, Configured Property Count).
4. Read the effective values table below it.
5. Use the top **Search** box to filter to a specific key (example: `FRB`, `MULTIPLAYER`, `URL`, `GL_`).

### What you should expect
- The current effective property values that GS is using for the selected bank.
- Rows may include keys that are not in the standard BankInfo catalog (these appear as custom).

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`

## Task 6: Create a draft and run validation checks (Level 4 workflow scaffold)
**Status:** `Partly works`

**Why partly?**
- The workflow UI is real and useful.
- It can save draft/validation state and show results.
- But this is still a **workflow scaffold** and may fall back to **local scaffold mode** instead of a fully active backend workflow.

**Use this when:** You want to prepare and review a change safely before asking engineering or using legacy edit tools.

### Steps
1. Select a bank and click **Apply**.
2. Scroll to **Level 4: Config Workflow (Draft -> Validate -> Approve -> Publish -> Rollback)**.
3. Review these status rows first:
   - **Workflow Status**
   - **Draft Version**
   - **Execution Mode**
   - **Sync Status**
   - **Validation Result**
   - **Validation Details**
4. In the workflow form, fill in:
   - **Draft Version** (example: `ops-bank271-limit-review-001`)
   - **Change Reason** (example: `Review min bet settings before canary wave`)
5. Click **Save Draft**.
6. Click **Validate**.
7. Read **Validation Result** and **Validation Details**.

### What you should expect
- The page records workflow state and shows PASS/FAIL validation details.
- Validation checks are baseline safety checks (selected bank, cluster config presence, missing mandatory keys, etc.).

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`

## Task 7: Use publish/rollback guardrails before risky actions (Level 4c)
**Status:** `Partly works`

**Why partly?**
- The guardrail panel is implemented and useful.
- It stores checklist confirmations in your browser and warns before publish/rollback.
- It does **not** enforce backend policy by itself (it is a safety helper, not a hard server rule yet).

**Use this when:** You want a safer checklist before clicking **Publish** or **Rollback**.

### Steps
1. In **Level 4**, click **Validate** first.
2. Scroll to **Level 4c: Publish/Rollback Guardrails + Canary Controls (Browser Local)**.
3. Review the rule table (Rule / Status / Detail).
4. Tick the local checklist items that you have actually completed:
   - Canary smoke executed
   - Rollback plan ready
   - Peer review/approval captured
   - Incident/comms channel prepared
5. Click **Refresh Guardrails**.
6. Read the summary pills and summary text.
7. If you click **Publish** or **Rollback**, read the warning and decide whether to continue.

### What you should expect
- Guardrail status updates on the page.
- Local warnings appear before publish/rollback if checks are incomplete.
- Your guardrail checks persist in the browser for that bank + draft.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/136-phase3-config-portal-publish-rollback-guardrails-visualization-20260224-091500.md`

## Task 8: Use the local approval queue and history (Level 4b)
**Status:** `Works now` (browser-local helper)

**Important:** This is a **browser-local approval helper**. It does not automatically approve backend changes.

**Use this when:** You want a simple operator checklist trail (queue, approve/reject/publish/rollback decisions) during review sessions.

### Steps
1. In **Level 4**, create or load a draft so it appears in **Session Draft Registry**.
2. In the **Session Draft Registry**, click **Queue** on the draft row you want to track.
3. Scroll to **Level 4b: Persistent Approval Queue (Browser Local)**.
4. In **Queued Draft Approvals**, use the mini buttons as needed:
   - **Approve**
   - **Publish**
   - **Reject**
   - **Rollback**
   - **Remove**
5. Review **Approval Overview** and **Approval Decision History**.

### What you should expect
- Queue and history persist in your browser (localStorage).
- The page tracks counts and progress locally.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/135-phase3-config-portal-persistent-approvals-browser-local-20260224-090500.md`

## Task 9: Export or import the approval queue bundle (Level 4b tools)
**Status:** `Works now` (browser-local helper)

**Use this when:** You want to share or back up the local approval queue/history between sessions or operators.

### Steps
1. Scroll to **Approval Queue Bundle (Export / Import)**.
2. To export:
   1. Click **Export Bundle JSON**.
   2. Copy the JSON from the large text box.
3. To import:
   1. Paste a valid bundle JSON into the text box.
   2. Click **Import Bundle JSON**.
4. Use **Refresh View** to reload the queue/history display.
5. Use **Reset Local Approval Queue** only if you intentionally want to clear local browser tracking.

### What you should expect
- A JSON bundle containing browser-local queue/history data.
- Import/export affects local portal helper data only, not server configuration by itself.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/135-phase3-config-portal-persistent-approvals-browser-local-20260224-090500.md`

## Task 10: Open the legacy bank editor when you need the compatibility tool
**Status:** `Works now`

**Use this when:** The new portal gives you visibility/workflow help, but you still need the older edit flow.

### Steps
1. Choose a bank at the top of `/support/configPortal.jsp`.
2. Click **Open Bank Editor**.
3. Continue in the legacy bank editor flow.
4. Return to the Config Portal to review values, guardrails, or workflow notes.

### What you should expect
- The old tool remains available for backward compatibility.
- The new portal is additive (it does not remove the legacy tool).

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`

## Task 11: Confirm whether a change really took effect
**Status:** `Partly works`

**Why partly?**
- The portal can show workflow status, validation results, and some sync information.
- But in current project state, the portal is still scaffold-first and browser-local helpers do not prove runtime change propagation by themselves.

**Use this when:** You want to avoid false confidence after using the workflow UI.

### Steps
1. In **Level 4**, check:
   - **Execution Mode**
   - **Sync Status**
   - **Sync Message**
   - **Workflow Status**
2. Re-open **Level 3: Effective Values** for the same bank and check the relevant key/value.
3. If you used only browser-local tools (Level 4b/4c), treat that as **review evidence only**, not proof of applied runtime change.
4. If a real config change was made through the legacy path or an active config-service bridge, confirm with engineering/support using runtime evidence pages (Runbook / Progress / Docs Index quick links).

### What you should expect
- The portal helps you confirm workflow/scaffold state.
- Final runtime confirmation may still require support/runbook evidence until the full write-path workflow is fully active.

### Evidence (actual capability)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/136-phase3-config-portal-publish-rollback-guardrails-visualization-20260224-091500.md`

## 7. Quick examples (daily use)
### Example A: "I just need to check a bank's multiplayer-related settings"
- Open `/support/configPortal.jsp`
- Select the bank
- Search `multiplayer`
- Read **Level 2** (meaning of keys)
- Read **Level 3** (actual current values)
- If anything looks risky, note it in **Change Reason** and save a draft for review

### Example B: "I want a safe checklist before someone publishes a canary change"
- Use **Level 4** to save a draft and validate
- Use **Level 4c Guardrails** to confirm canary smoke / rollback plan / peer review / comms
- Use **Level 4b Approval Queue** to record local approvals/history
- If the actual edit path is still legacy, use **Open Bank Editor** and then return to the portal to review

### Example C: "I need to share approval notes with another operator"
- Queue the draft in **Session Draft Registry**
- Use **Approval Queue Bundle -> Export Bundle JSON**
- Share the JSON securely (internal process)
- Other operator imports it using **Import Bundle JSON**

## 8. Current limitations (plain English)
These are not bugs in this guide. They are the current project state.

- The portal is **not yet a full write-path replacement** for the legacy config tools.
- Some workflow features are **scaffold** (safe shape) and some are **browser-local helpers**.
- Browser-local data (guardrails/approval queue/history) is stored in your browser and may not exist on another computer unless exported/imported.
- A successful workflow screen does not always mean a runtime change is globally applied; use runtime evidence/support checks when needed.

## 9. Planned / not active yet (as of this guide)
**Status:** `Planned / not active yet`

The following are planned future improvements described in project docs, but should not be assumed active just because they appear in design documents:
- full persistent backend workflow and approvals replacing local-only helpers,
- full portal module tabs for all domains (clusters, banks, games, currencies, promos) as the primary edit path,
- full write-path publish/rollback with complete backend enforcement,
- deeper provenance/version metadata and stronger operator confirmation of runtime propagation.

## 10. Where to get help
Use the quick links at the top of the portal:
- **Progress** (`/support/modernizationProgress.html`) for overall project and blocker status
- **Docs Index** (`/support/modernizationDocs.jsp`) for evidence and reports
- **Runbook** (`/support/modernizationRunbook.jsp`) for operational checks
- **Cluster Hosts** (`/support/clusterHosts.jsp`) for key descriptions

## 11. Evidence used to write this guide
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/135-phase3-config-portal-persistent-approvals-browser-local-20260224-090500.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/136-phase3-config-portal-publish-rollback-guardrails-visualization-20260224-091500.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/57-config-portal-category-guide-20260220-181200.md`

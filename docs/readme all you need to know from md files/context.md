# PROJECT: iGaming Game Server
# DOCUMENT: Master Context & Source of Truth

## 1. VISION
To build, deploy, and run a reliable Game Server for the iGaming platform, ensuring all dependencies are resolved and the server starts up correctly in the Docker environment.

## 2. TECH STACK & ENVIRONMENT
* **OS:** Windows 11 (User) / Linux (Docker Container)
* **Languages/Tools:** Java 8+, Maven, Docker, Docker Compose, PowerShell
* **Frameworks:** Spring, Jetty, Cassandra, Kafka
* **Key Directories:**
    * Source: `d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\Game provider side\Source code\mq-gs-clean-version`
    * Deploy: `d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\deploy`

## 3. STATUS
### ✅ Completed
* **Fix Maven Build:** Resolved `DuplicateProjectException`.
* **Fix Missing Config:** Added `CLUSTER_TYPE=DEVELOPMENT`.
* **Deployment Automation:** Created `check_and_deploy.ps1`.
* **Build Success:** `gsn-web-gs` built successfully.
* **WebSocket Fix:** Identified and fixed `ws://games/1` error by patching `AbstractStartGameUrlHandler` in MP Server.
* **Duplicate Class Fix:** Removed/Patched duplicate `BaseStartGameAction.java` that was overriding fixes.
* **Git Synchronization:** Resolved over 7,000 changes, cleaned up repository with .gitignore, and synced in chunks to GitHub.

### 🚧 In Progress (Current Focus)
* **Pivot to Standalone RNG**
    * [x] **Disable MP (Java):** Modified `CWStartGameAction.java` to bypass MP logic.
    * [x] **Build:** Rebuild `ROOT.war` with standalone logic.
    * [ ] **Deploy:** Deploy `ROOT.war` to Game Server.
    * [ ] **Verify:** Ensure `cwstartgamev2` loads game client directly.

* **Global Static Build & Serve Architecture**
    * [x] **Client Fix:** Fix `BulgePinchFilter` crash in Dragonstone (`GameField.js`).
    * [ ] **Server Fix:** Redirect `template.jsp` (Real & Free) to serve assets from Static Container (localhost:80).
    * [ ] **Deploy:** Rebuild and deploy patched Game Server (`ROOT.war`).
    * [ ] **Asset Deploy:** Deploy all static assets using `deploy-static-global.ps1`.
    * [ ] **Verify:** Confirm Dragonstone loads from localhost:80.
    * [ ] **Mission Amazon:** Continue resolving circular dependencies.

### 📅 Backlog
* **Clean Up:** Remove temporary build logs.

## 4. ARCHIVE / NOTES
* **Git:** Fixed commit lock issue.
* **MP Server:** URL generation logic for `mpgame` is in `AbstractStartGameUrlHandler`, NOT Game Server.
* **Client Code:** Client JS is served from `gsn-web-gs` WAR, so client edits must be synced there.
* **Tools Routes:**
    * ATS Config: `http://localhost:8081/ats/listAts.jsp` (Check Port 8081)
    * Test Connection: `http://localhost:8081/support/testconnection/index.html`

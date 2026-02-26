# README-ONBOARDING (Refactor-Only Environment)

Quick entrypoint:
- `/Users/alexb/Documents/Dev/Dev_new/docs/START-HERE-REFRACTOR.md`

## What this starts
This guide starts the **refactored GameServer environment only** from the `GSRefactor` repository (`Dev_new`).

It starts the refactor Docker stack (GS, MP, config service, session service, gameplay service, wallet adapter, history, multiplayer, protocol adapter, Kafka, ZooKeeper, Redis, Cassandra 4.x, static facade).

## What this does NOT start
- Legacy `gp3` GameServer / MP / static containers
- Casino Manager project containers
- Root workspace scripts from another repository

This is intentional. It keeps the environment smaller and easier to move to another machine.

## One-command startup (run from the `Dev_new` repo root)
```bash
node ./gs-server/deploy/scripts/refactor-onboard.mjs up
```

Recommended first run:
```bash
node ./gs-server/deploy/scripts/refactor-onboard.mjs preflight
node ./gs-server/deploy/scripts/refactor-onboard.mjs up
node ./gs-server/deploy/scripts/refactor-onboard.mjs smoke
```

## Prerequisites

### macOS
Install these tools and make sure they are available in Terminal:
- Docker Desktop (with Docker Compose plugin)
- Java (JDK)
- Maven (`mvn`)
- Node.js and npm
- `curl`
- `unzip`
- `rsync` (optional, faster file copy; startup still works without it)

### Linux
Install these tools and make sure they are available in your shell:
- Docker Engine + Docker Compose plugin
- Java (JDK)
- Maven (`mvn`)
- Node.js and npm
- `curl`
- `unzip`
- `rsync` (optional)

### Windows
Use one of these options:
- **Recommended:** Git Bash + Docker Desktop + Java + Maven + Node.js
- **Alternative:** WSL2 (Ubuntu) + Docker Desktop integration + Java + Maven + Node.js

Important:
- The startup scripts use `bash` internally.
- If `bash` is not in PATH, set `BASH_BIN` to your Git Bash path before running the command.

Example (PowerShell):
```powershell
$env:BASH_BIN = 'C:\Program Files\Git\bin\bash.exe'
node .\gs-server\deploy\scripts\refactor-onboard.mjs preflight
```

## What the startup command does (simple explanation)
1. Checks that the required tools are installed.
2. Checks the repo has the files it needs.
3. Syncs host/port settings from the central config file.
4. Builds missing runtime pieces (GS web app, MP web app, default HTML5 game assets) if needed.
5. Starts the refactor Docker containers in the right order.
6. Prints quick health checks and the game launch URL.

## First-run time expectations
First run can take a long time (often several minutes) because it may need to:
- build Java projects with Maven,
- build HTML5 game assets with npm,
- build/pull Docker images,
- create runtime folders.

Later starts are much faster if the build outputs already exist.

## How to stop the environment
```bash
node ./gs-server/deploy/scripts/refactor-onboard.mjs down
```

## How to verify the environment is working
### Quick automatic check
```bash
node ./gs-server/deploy/scripts/refactor-onboard.mjs smoke
```

### Manual checks
Open these URLs in your browser:
- Static asset route: [http://127.0.0.1:18080/html5pc/actiongames/dragonstone/lobby/version.json](http://127.0.0.1:18080/html5pc/actiongames/dragonstone/lobby/version.json)
- GS support route: [http://127.0.0.1:18081/support/bankSelectAction.do?bankId=6275](http://127.0.0.1:18081/support/bankSelectAction.do?bankId=6275)
- Config service health: [http://127.0.0.1:18072/health](http://127.0.0.1:18072/health)

Note:
- Root URLs can return startup-time transport errors and are not used as readiness checks.
- Use `smoke` command results and `/startgame` `HTTP 200` as the primary pass/fail signal.
- A `WARN` line for `GS support route (diagnostic)` does not fail onboarding by itself.

### Game launch URL (browser-facing alias, no `cwstartgamev2.do`)
Use this exact URL for the VND test bank:

[http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en](http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en)

Optional second-bank check (Betonline subcasino):

[http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=838&mode=real&token=bav_game_session_001&lang=en](http://127.0.0.1:18080/startgame?bankId=6274&subCasinoId=508&gameId=838&mode=real&token=bav_game_session_001&lang=en)

Why this URL matters:
- `:18080` is the refactor static facade (not port 80)
- `subCasinoId=507` is needed for localhost testing with banks `6274/6275`
- `/startgame` is the clean alias (browser-facing)

Bank id note (important):
- Bank `6276` is an internal id in subcasino `508`.
- Its current external launch id is `6274` for wallet compatibility.
- This means direct `bankId=6276` launch calls can return `Bank is incorrect` by design.

### Configure launch URL without editing code
Default launch values are now kept in:

- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`

Keys used:
- `LAUNCH_BASE_URL`, `LAUNCH_BANK_ID`, `LAUNCH_SUBCASINO_ID`, `LAUNCH_GAME_ID`, `LAUNCH_MODE`, `LAUNCH_TOKEN`, `LAUNCH_LANG`
- Optional secondary route: `SECONDARY_LAUNCH_BANK_ID`, `SECONDARY_LAUNCH_SUBCASINO_ID`, `SECONDARY_LAUNCH_GAME_ID`, `SECONDARY_LAUNCH_MODE`, `SECONDARY_LAUNCH_TOKEN`, `SECONDARY_LAUNCH_LANG`

You can still override any of these from the shell before running `smoke` or `up`:

```bash
export LAUNCH_BANK_ID=6275
export LAUNCH_SUBCASINO_ID=507
export LAUNCH_GAME_ID=838
export LAUNCH_MODE=real
export LAUNCH_TOKEN=bav_game_session_001
export LAUNCH_LANG=en
```

Optional secondary launch check:

```bash
export SECONDARY_LAUNCH_BANK_ID=6274
export SECONDARY_LAUNCH_SUBCASINO_ID=508
node ./gs-server/deploy/scripts/refactor-onboard.mjs smoke
```

## Troubleshooting (plain English)
### "bash was not found" (Windows)
Install Git for Windows and open Git Bash, or set `BASH_BIN` to the Git Bash executable path.

### "Docker daemon is not reachable"
Docker Desktop / Docker Engine is not running, or your user cannot access Docker.

### "Missing command: mvn" or "Missing command: java"
Install Java + Maven and reopen the terminal so PATH updates apply.

### "Missing required path" during startup
The repo may be incomplete or the command is not being run from the `Dev_new` checkout.
Run from the repo root and make sure the repository was cloned fully.

### Startup is very slow on first run
This is normal if Maven/npm builds are running for the first time.

### `smoke` fails on `/startgame`
This usually means one of these:
- stack is not fully up yet (wait 1-2 minutes and rerun `smoke`)
- Cassandra data is missing or not prepared for the test bank
- a service is up but unhealthy (check container logs)

### `smoke` shows only a diagnostic GS warning
If `smoke` exits successfully and `/startgame` is `HTTP 200`, onboarding is considered successful even if the diagnostic GS support route reports a transient warning.

## Known limits (important)
- This onboarding flow starts the **refactor-only** environment, not the legacy stacks.
- The launch URL depends on local test data being available in the refactor Cassandra target.
- This guide does not perform production cutover actions.
- `rsync` is optional; when missing, runtime bootstrap uses a slower copy fallback.

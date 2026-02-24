# Refactor Docker Stack (Isolated)

This stack is isolated from the current runtime stack.
It must be started with compose project name `refactor`.

## Start
```bash
bash /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-start.sh
```

## Stop
```bash
bash /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/refactor-stop.sh
```

## Refactor-only startup from `GSRefactor` (`Dev_new`) on another machine
- You can start the refactor stack without legacy `gp3` containers.
- `refactor-start.sh` can bootstrap missing runtime assets from sources inside `GSRefactor` (`gs-server`, `mp-server`, `legacy-games-client`) when `AUTO_BOOTSTRAP_RUNTIME=1` (default).
- Prerequisites for bootstrap path:
  - Docker / Docker Compose plugin
  - Java + Maven
  - Node.js + npm
  - `curl`, `rsync`, `unzip`
- Optional overrides:
  - `LEGACY_MP_TARGET_DIR=/absolute/path/to/mp-server/web/target` (defaults to `Dev_new/mp-server/web/target`)
  - `LEGACY_HTML5_GAMES="dragonstone"` (space-separated game folders to build/copy)
  - `AUTO_BOOTSTRAP_RUNTIME=0` (disable bootstrap and require preseeded runtime assets)

## Launch URL (refactor static facade)
- Correct alias URL is on refactor static nginx port `18080` (not plain `localhost:80`)
- For localhost banks `6274/6275`, include `subCasinoId=507`
```text
http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en
```

## Key host ports (refactor stack)
- static/nginx: `18080`
- gs: `18081`
- config-service: `18072`
- session-service: `18073`
- gameplay-orchestrator: `18074`
- wallet-adapter: `18075`
- bonus-frb-service: `18076`
- history-service: `18077`
- multiplayer-service: `18079`
- protocol-adapter: `18078`
- gs debug/admin: `16000`, `16001`, `19000`
- mp: `16300`, `16301`
- cassandra: `19142`
- zookeeper: `12181`
- kafka external: `19092`

## Config service quick check
```bash
curl -sS http://127.0.0.1:18072/health
curl -sS http://127.0.0.1:18073/health
curl -sS http://127.0.0.1:18074/health
curl -sS http://127.0.0.1:18075/health
curl -sS http://127.0.0.1:18076/health
curl -sS http://127.0.0.1:18077/health
curl -sS http://127.0.0.1:18079/health
curl -sS http://127.0.0.1:18078/health
```

## Isolation policy
- No mounts from `/Users/alexb/Documents/Dev` outside `/Users/alexb/Documents/Dev/Dev_new`.
- No changes to existing compose files under `deploy/docker/configs`.

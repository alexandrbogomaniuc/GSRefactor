# Refactor Docker Stack (Isolated)

This stack is isolated from the current runtime stack.
It must be started with compose project name `refactor`.

## Start
```bash
cd /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor
docker compose -p refactor up -d --build
```

## Stop
```bash
cd /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor
docker compose -p refactor down
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
curl -sS http://127.0.0.1:18078/health
```

## Isolation policy
- No mounts from `/Users/alexb/Documents/Dev` outside `/Users/alexb/Documents/Dev/Dev_new`.
- No changes to existing compose files under `deploy/docker/configs`.

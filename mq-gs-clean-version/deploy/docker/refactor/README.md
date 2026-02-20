# Refactor Docker Stack (Isolated)

This stack is isolated from the current runtime stack.
It must be started with compose project name `refactor`.

## Start
```bash
cd /Users/alexb/Documents/Dev/Dev_new/mq-gs-clean-version/deploy/docker/refactor
docker compose -p refactor up -d --build
```

## Stop
```bash
cd /Users/alexb/Documents/Dev/Dev_new/mq-gs-clean-version/deploy/docker/refactor
docker compose -p refactor down
```

## Key host ports (refactor stack)
- static/nginx: `18080`
- gs: `18081`
- gs debug/admin: `16000`, `16001`, `19000`
- mp: `16300`, `16301`
- cassandra: `19142`
- zookeeper: `12181`
- kafka external: `19092`

## Isolation policy
- No mounts from `/Users/alexb/Documents/Dev` outside `/Users/alexb/Documents/Dev/Dev_new`.
- No changes to existing compose files under `deploy/docker/configs`.

# CM Dedicated Container Smoke (2026-02-16 16:57 UTC)

## Deploy Command
```bash
cd /Users/alexb/Documents/Dev/cm-module
docker compose up -d --build
```

## Health Check
```bash
curl -sS http://localhost:18070/health
```
Result:
```json
{"ok":true,"service":"cm-module","usersFile":"/app/data/users.json","cassandraContainer":"gp3-c1-1"}
```

## Bootstrap Login Check
```bash
curl -sS -X POST http://localhost:18070/cm-auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"root","password":"root"}'
```
Result highlights:
- login success
- `mustChangePassword=true`

## Notes
- Container is dedicated and exposed on `localhost:18070`.
- Container uses mounted docker socket to query Cassandra container `gp3-c1-1`.

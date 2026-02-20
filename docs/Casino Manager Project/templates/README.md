# Templates

Reference templates for phase-1 implementation:

- `cm-sync-hourly.crontab`
  - cron entry for hourly CM sync.
- `cm-sync-hourly.sh`
  - sync worker shell flow template (lock/checkpoint/stages/audit).
- `cm-auth-reference.ts`
  - framework-agnostic auth logic reference with `root/root` bootstrap.

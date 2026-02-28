# Rollback Pack

- Current candidate: `1.0.0+4abc2a03`
- Previous known-good: _none recorded_

## Rollback Procedure

1. Disable current release in GS registration.
2. Re-enable last validated release recorded by GS Ops.
3. Verify launch URL and one normal round transaction.
4. Verify reconnect restore path and wallet consistency.

## Rollback Validation

- Launch success
- Normal round request/response
- Reconnect restore
- Balance consistency

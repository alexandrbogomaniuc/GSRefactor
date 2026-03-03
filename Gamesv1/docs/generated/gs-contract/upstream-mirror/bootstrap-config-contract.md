# GS Bootstrap Config Contract

Status: canonical bootstrap contract for `slot-browser-v1`.

## Bootstrap request (wire)

`POST /slot/v1/bootstrap`

Required wire fields:
- `contractVersion`
- `sessionId`
- `requestCounter`
- `currentStateVersion`
- `bootstrapRef`

Optional:
- `launchContext` (`gameId`, `bankId`, `playerId`, `language`, `launchParams`)

Read-only rule:
- bootstrap is read-only
- no idempotency/clientOperation headers required

## Bootstrap response (wire object, separate from runtime envelope)

Bootstrap response fields:
- `ok`
- `requestId`
- `session`
- `context`
- `assets`
- `runtime`
- `policies`
- `uiPolicy`
- `integrity`

Bootstrap is configuration authority and must not be merged into mutating runtime envelopes.
Runtime envelopes begin with `opengame`.

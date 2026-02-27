# Spin Profiling (PRECSPINSTAT)

In strict regulatory environments, math models require verification against the actual visual flow times displayed to players. This guarantees clients cannot execute high-speed spins subverting the intended timing boundaries.

The protocol injects a `PRECSPINSTAT` key exactly once on all subsequent `BET_REQUEST` payloads. The server then logs this execution block in the database row alongside the wager result.

## Example Payload Structure
If you just finished playing Wager #1, and you begin spinning for Wager #2 playing Turboplay mode, the outbound `BET_REQUEST` for Wager #2 looks like this:

```json
{
  "version": "1.0",
  "type": "BET_REQUEST",
  "operationId": "dca-8a911-34bb-231a",
  "payload": {
    "betAmount": 10.00,
    "PRECSPINSTAT": {
      "SPINREQTIME": 85,          // The network RTT of Wager #1 (in ms)
      "CMD": "PLACEBET",          // Wager #1's command trigger
      "SPINANMTIME": 1800         // Wager #1's total visual display span (Turboplay 1.8s) 
    }
  }
}
```

## Field Breakdown
- `SPINREQTIME`: The delta calculated instantly from transmitting the stringified JSON `BET_REQUEST` socket payload until parsing the `BET_ACCEPTED` response payload.
- `CMD`: Retains the textual identifier for the network trigger (typically `"PLACEBET"`).
- `SPINANMTIME`: The physical delta collected securely between interpolating the very first Reel stopping animation until the Win Highlight fully yields visual state back to the idle "Play" loop handler.

**Note on Flags**: Profile attaching is ONLY executed if `spinProfilingEnabled` config flag equates to `true`.

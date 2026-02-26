# Protocol: abs.gs.v1

## Overview
The `abs.gs.v1` protocol is the canonical WebSocket communication standard for slot game clients. It guarantees financial correctness through strict operation tracking, idempotent retries, and comprehensive state recovery.

## Message Envelope
All messages sent between the client and server MUST adhere to the following JSON envelope wrapper.

### TypeScript Interfaces & Zod Schemas

```typescript
import { z } from "zod";

// --- GSMessageEnvelope ---
export const GSMessageEnvelopeSchema = z.object({
  version: z.string(),
  type: z.string(),
  traceId: z.string(),
  sessionId: z.string(), 
  bankId: z.string(),
  gameId: z.string(),
  operationId: z.string(), // Crucial for Idempotency
  timestamp: z.string().datetime().optional().or(z.string()), // Usually ISO date
  seq: z.number(),
  payload: z.any() 
});

export type GSMessageEnvelope = z.infer<typeof GSMessageEnvelopeSchema>;
```

### Envelope Fields Definition

| Field | Type | Meaning |
|---|---|---|
| `version` | string | Protocol version (e.g., "1.0"). |
| `type` | string | Command event type (e.g. `BET_REQUEST`, `GAME_READY`). |
| `traceId` | string | Unique network hop UUID for tracing logs. |
| `sessionId` | string | Active user session UUID. |
| `bankId` | string | Target bank/currency provider identifier. |
| `gameId` | string | Game mathematical configuration identifier. |
| `operationId` | string | **Idempotency Key.** Must uniquely identify a monetary action attempt. |
| `timestamp` | string | ISO timestamp of the message origin. |
| `seq` | number | Sequential integer counter mapped per client connection. |
| `payload` | any | Dynamic object body dictated by the `type` field. |

---

## Defined Commands / Payload Types

Scanning the existing Abstract WebSocket Client and Mock Node Server reveals the following actively utilized payload structures. *Some fields have been marked `UNKNOWN` if their exact runtime structure couldn't be definitively established from the mock servers without production log samples.*

```typescript
// --- GAME_READY (Client -> Server) ---
export const GameReadyPayloadSchema = z.object({
  token: z.string() // Auth token
});
export type GameReadyPayload = z.infer<typeof GameReadyPayloadSchema>;

// --- SESSION_ACCEPTED (Server -> Client) ---
export const SessionAcceptedPayloadSchema = z.object({
  balance: z.number(),
  currencyCode: z.string(),
  rcInterval: z.number() // Reality check interval in seconds
});
export type SessionAcceptedPayload = z.infer<typeof SessionAcceptedPayloadSchema>;

// --- SESSION_SYNC (Server -> Client) ---
export const SessionSyncPayloadSchema = z.object({
  balance: z.number(),
  freeRoundsRemaining: z.number().optional(),
  lastKnownState: z.string(), // 'RESERVED' | 'SETTLED'
  gameResult: z.any().optional() // UNKNOWN exact object bounds, appears same as BET_ACCEPTED
});
export type SessionSyncPayload = z.infer<typeof SessionSyncPayloadSchema>;

// --- BET_REQUEST (Client -> Server) ---
export const BetRequestPayloadSchema = z.object({
  betAmount: z.number()
});
export type BetRequestPayload = z.infer<typeof BetRequestPayloadSchema>;

// --- BET_ACCEPTED (Server -> Client) ---
export const BetAcceptedPayloadSchema = z.object({
  totalBet: z.number(),
  totalWin: z.number(),
  resultGrid: z.array(z.array(z.number())) // e.g., [[1,2,3], [4,5,2], [1,1,1]]
});
export type BetAcceptedPayload = z.infer<typeof BetAcceptedPayloadSchema>;

// --- BET_REJECTED (Server -> Client) ---
export const BetRejectedPayloadSchema = z.object({
  reason: z.string()
});
export type BetRejectedPayload = z.infer<typeof BetRejectedPayloadSchema>;

// --- SETTLE_REQUEST (Client -> Server) ---
// Finalizes visual win playback, adds win value natively to client balance tracker
export const SettleRequestPayloadSchema = z.object({});
export type SettleRequestPayload = z.infer<typeof SettleRequestPayloadSchema>;

// --- SETTLE_ACCEPTED (Server -> Client) ---
export const SettleAcceptedPayloadSchema = z.object({});
export type SettleAcceptedPayload = z.infer<typeof SettleAcceptedPayloadSchema>;

// --- BALANCE_SNAPSHOT (Server -> Client) ---
export const BalanceSnapshotPayloadSchema = z.object({
  balance: z.number(),
  currencyCode: z.string().optional(),
  freeRoundsRemaining: z.number().optional()
});
export type BalanceSnapshotPayload = z.infer<typeof BalanceSnapshotPayloadSchema>;

// --- PING (Server -> Client) & PONG (Client -> Server) ---
// Emitted on ~30s intervals
export const PingPongPayloadSchema = z.object({});
export type PingPongPayload = z.infer<typeof PingPongPayloadSchema>;

// --- ERROR (Server -> Client) ---
export const ErrorPayloadSchema = z.any(); // UNKNOWN: Need logs to explicitly type structure (string vs object)
export type ErrorPayload = z.infer<typeof ErrorPayloadSchema>;
```

### Payload Event Directory

| Event Type | Direction | Payload Synopsis |
|---|---|---|
| `GAME_READY` | Client -> Server | Initial handshake token authentication. |
| `SESSION_ACCEPTED` | Server -> Client | Confirms token, provides initial wallet/compliance data. |
| `SESSION_SYNC` | Server -> Client | Recovers interrupted state if client drops mid-spin (requires logs to formalize `gameResult`). |
| `BET_REQUEST` | Client -> Server | Initiates a mathematical spin (requires idempotency `operationId`). |
| `BET_ACCEPTED` | Server -> Client | Supplies deterministic win arrays and grid values. |
| `BET_REJECTED` | Server -> Client | Rejects the spin (NSF, Timeout, Limits). |
| `SETTLE_REQUEST` | Client -> Server | Asserts animations completed. Settles the current `operationId` into the bank. |
| `SETTLE_ACCEPTED` | Server -> Client | Acknowledges settlement. |
| `BALANCE_SNAPSHOT`| Server -> Client | Blind wallet overwrite broadcast. |
| `PING` / `PONG` | Both | Application level heartbeat. |
| `ERROR` | Server -> Client | **UNKNOWN** Exception/Terminal Error structure. Need production log samples. |

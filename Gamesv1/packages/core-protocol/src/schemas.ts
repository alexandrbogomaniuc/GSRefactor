import { z } from "zod";

// Legacy abs.gs.v1 envelope schema kept for experimental/backward-compat flows only.
export const AbsEnvelopeSchema = z.object({
  version: z.string().default("abs.gs.v1"),
  type: z.string(),
  traceId: z.string().optional(),
  sessionId: z.string().optional(),
  operationId: z.string().optional(),
  timestamp: z.number().optional(),
  payload: z.unknown(),
});

export type AbsEnvelope = z.infer<typeof AbsEnvelopeSchema>;

export const ExtGameRequestSchema = z.object({
  token: z.string().optional(),
  payload: z.unknown().optional(),
});

export type ExtGameRequest = z.infer<typeof ExtGameRequestSchema>;

export const RuntimeOpenGameRequestSchema = z.object({
  sessionId: z.string().min(1),
  bankId: z.number().optional(),
  playerId: z.string().optional(),
  gameId: z.number().optional(),
  gsInternalBaseUrl: z.string().url().optional(),
  language: z.string().optional(),
  internalClientCode: z.string().optional(),
});

export const RuntimePlaceBetRequestSchema = z.object({
  sessionId: z.string().min(1),
  requestCounter: z.number().int().nonnegative(),
  clientOperationId: z.string().optional(),
  currentStateVersion: z.string().optional(),
  bets: z
    .array(
      z.object({
        betType: z.string().min(1),
        betAmount: z.number().positive(),
      }),
    )
    .min(1),
});

export const RuntimeCollectRequestSchema = z.object({
  sessionId: z.string().min(1),
  requestCounter: z.number().int().nonnegative(),
  roundId: z.string().min(1),
  clientOperationId: z.string().optional(),
  currentStateVersion: z.string().optional(),
});

export const RuntimeReadHistoryRequestSchema = z.object({
  sessionId: z.string().min(1),
  requestCounter: z.number().int().nonnegative(),
  pageNumber: z.number().int().nonnegative().default(0),
  currentStateVersion: z.string().optional(),
});

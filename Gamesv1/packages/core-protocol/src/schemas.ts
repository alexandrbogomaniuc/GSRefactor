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

export const RuntimeBootstrapRequestSchema = z.object({
  sessionId: z.string().min(1),
  gameId: z.number().optional(),
  bankId: z.number().optional(),
  playerId: z.string().optional(),
  language: z.string().optional(),
  launchParams: z.record(z.string(), z.unknown()).optional(),
});

export const RuntimeOpenGameRequestSchema = z.object({
  sessionId: z.string().min(1),
  requestCounter: z.number().int().nonnegative().optional(),
  bankId: z.number().optional(),
  playerId: z.string().optional(),
  gameId: z.number().optional(),
  gsInternalBaseUrl: z.string().url().optional(),
  language: z.string().optional(),
  internalClientCode: z.string().optional(),
});

const RuntimeOperationMetadataSchema = z.object({
  requestCounter: z.number().int().nonnegative(),
  clientOperationId: z.string().min(1),
  idempotencyKey: z.string().min(1).optional(),
  currentStateVersion: z.string().optional(),
});

export const RuntimePlayRoundRequestSchema = RuntimeOperationMetadataSchema.extend({
  sessionId: z.string().min(1),
  betType: z.string().min(1),
  betAmount: z.number().positive(),
  roundInput: z.record(z.string(), z.unknown()).optional(),
});

export const RuntimeFeatureActionRequestSchema = RuntimeOperationMetadataSchema.extend({
  sessionId: z.string().min(1),
  action: z.string().min(1),
  payload: z.record(z.string(), z.unknown()).optional(),
});

export const RuntimeResumeGameRequestSchema = RuntimeOperationMetadataSchema.partial().extend({
  sessionId: z.string().min(1),
});

export const RuntimeCloseGameRequestSchema = RuntimeOperationMetadataSchema.partial().extend({
  sessionId: z.string().min(1),
  reason: z.string().optional(),
});

export const RuntimeGetHistoryRequestSchema = RuntimeOperationMetadataSchema.partial().extend({
  sessionId: z.string().min(1),
  requestCounter: z.number().int().nonnegative(),
  pageNumber: z.number().int().nonnegative().default(0),
});

import { z } from "zod";

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

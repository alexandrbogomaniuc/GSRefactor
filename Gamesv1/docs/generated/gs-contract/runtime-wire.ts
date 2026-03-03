import { z } from "zod";

const NonNegativeInt = z.number().int().nonnegative();

export const SelectedBetSchema = z.object({
  coinValueMinor: z.number().int().positive(),
  lines: z.number().int().positive(),
  multiplier: z.number().int().positive(),
  totalBetMinor: z.number().int().positive(),
}).strict();

export const SelectedFeatureChoiceSchema = z.object({
  featureType: z.string().min(1),
  action: z.string().min(1),
  priceMinor: NonNegativeInt,
  payload: z.record(z.string(), z.unknown()),
}).strict();

export const HistoryQuerySchema = z.object({
  pageNumber: NonNegativeInt,
  pageSize: NonNegativeInt,
}).strict();

export const BootstrapRequestSchema = z.object({
  contractVersion: z.literal("slot-browser-v1"),
  sessionId: z.string().min(1),
  requestCounter: NonNegativeInt,
  currentStateVersion: NonNegativeInt,
  bootstrapRef: z.string().min(1),
  launchContext: z
    .object({
      gameId: z.number().optional(),
      bankId: z.number().optional(),
      playerId: z.string().optional(),
      language: z.string().optional(),
      launchParams: z.record(z.string(), z.unknown()).optional(),
    })
    .strict()
    .optional(),
}).strict();

const MutatingBaseRequestSchema = z.object({
  contractVersion: z.literal("slot-browser-v1"),
  sessionId: z.string().min(1),
  requestCounter: NonNegativeInt,
  currentStateVersion: NonNegativeInt,
  idempotencyKey: z.string().min(1),
  clientOperationId: z.string().min(1),
}).strict();

export const OpenGameRequestSchema = MutatingBaseRequestSchema.extend({
  bootstrapRef: z.string().optional(),
}).strict();

export const PlayRoundRequestSchema = MutatingBaseRequestSchema.extend({
  bootstrapRef: z.string().optional(),
  selectedBet: SelectedBetSchema,
  selectedFeatureChoice: SelectedFeatureChoiceSchema.optional(),
}).strict();

export const FeatureActionRequestSchema = MutatingBaseRequestSchema.extend({
  bootstrapRef: z.string().optional(),
  selectedBet: SelectedBetSchema.optional(),
  selectedFeatureChoice: SelectedFeatureChoiceSchema.optional(),
}).strict();

export const ResumeGameRequestSchema = MutatingBaseRequestSchema.extend({
  bootstrapRef: z.string().optional(),
  resumeRef: z.string().optional(),
}).strict();

export const CloseGameRequestSchema = MutatingBaseRequestSchema.extend({
  closeReason: z.string().min(1),
}).strict();

export const GetHistoryRequestSchema = z.object({
  contractVersion: z.literal("slot-browser-v1"),
  sessionId: z.string().min(1),
  requestCounter: NonNegativeInt,
  currentStateVersion: NonNegativeInt,
  historyQuery: HistoryQuerySchema,
}).strict();

export const RuntimeEnvelopeResponseSchema = z.object({
  ok: z.boolean(),
  requestId: z.string().min(1),
  sessionId: z.string().min(1),
  requestCounter: NonNegativeInt,
  stateVersion: NonNegativeInt,
  wallet: z.record(z.string(), z.unknown()).nullable(),
  round: z.record(z.string(), z.unknown()).nullable(),
  feature: z.record(z.string(), z.unknown()).nullable(),
  presentationPayload: z.unknown().nullable(),
  restore: z.record(z.string(), z.unknown()).nullable(),
  idempotency: z.record(z.string(), z.unknown()).nullable(),
  retry: z.record(z.string(), z.unknown()).nullable(),
}).strict();

export type BootstrapRequest = z.infer<typeof BootstrapRequestSchema>;
export type OpenGameRequest = z.infer<typeof OpenGameRequestSchema>;
export type PlayRoundRequest = z.infer<typeof PlayRoundRequestSchema>;
export type FeatureActionRequest = z.infer<typeof FeatureActionRequestSchema>;
export type ResumeGameRequest = z.infer<typeof ResumeGameRequestSchema>;
export type CloseGameRequest = z.infer<typeof CloseGameRequestSchema>;
export type GetHistoryRequest = z.infer<typeof GetHistoryRequestSchema>;
export type RuntimeEnvelopeResponse = z.infer<typeof RuntimeEnvelopeResponseSchema>;

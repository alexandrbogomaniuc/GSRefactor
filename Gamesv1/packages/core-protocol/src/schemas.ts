import { z } from "zod";

// Authoritative contract schemas live in docs/gs/schemas/*.schema.json.
// These Zod schemas are local runtime helpers and must stay wire-shape compatible.

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

const NonNegativeInteger = z.number().int().nonnegative();
const ContractVersionSchema = z.literal("slot-browser-v1");
const JsonObjectSchema = z.record(z.string(), z.unknown());

export const BootstrapRefSchema = z
  .object({
    configId: z.string().min(1),
    clientPackageVersion: z.string().min(1),
    mathPackageVersion: z.string().min(1).optional(),
  })
  .strict();

export const SelectedBetSchema = z
  .object({
    coinValueMinor: z.number().int(),
    lines: z.number().int().min(1),
    multiplier: z.number().int().min(1),
    totalBetMinor: z.number().int().nonnegative(),
  })
  .strict();

export const SelectedFeatureChoiceSchema = z
  .object({
    featureType: z.enum(["BUY_FEATURE", "HOLD_AND_WIN", "RESPIN", "FREE_SPINS"]),
    action: z.enum(["PICK", "CONFIRM", "COLLECT", "CONTINUE"]),
    priceMinor: z.number().int(),
    payload: JsonObjectSchema,
  })
  .strict();

export const RuntimeHistoryQuerySchema = z
  .object({
    fromRoundId: z.string().nullable(),
    limit: z.number().int().min(1),
    includeFeatureDetails: z.boolean(),
  })
  .strict();

const RuntimeRequestBaseSchema = z
  .object({
    contractVersion: ContractVersionSchema,
    sessionId: z.string().min(1),
  })
  .strict();

const RuntimeMutatingRequestBaseSchema = RuntimeRequestBaseSchema.extend({
  requestCounter: NonNegativeInteger,
  currentStateVersion: NonNegativeInteger,
  idempotencyKey: z.string().min(1),
  clientOperationId: z.string().min(1),
  bootstrapRef: BootstrapRefSchema,
});

export const RuntimeBootstrapRequestSchema = RuntimeRequestBaseSchema.extend({
  bootstrapRef: BootstrapRefSchema.omit({ mathPackageVersion: true }),
}).strict();

export const RuntimeOpenGameRequestSchema = RuntimeMutatingRequestBaseSchema.extend({
  selectedBet: z.null(),
  selectedFeatureChoice: z.null(),
}).strict();

export const RuntimePlayRoundRequestSchema = RuntimeMutatingRequestBaseSchema.extend({
  selectedBet: SelectedBetSchema,
  selectedFeatureChoice: z.null(),
}).strict();

export const RuntimeFeatureActionRequestSchema = RuntimeMutatingRequestBaseSchema.extend({
  selectedBet: z.union([z.null(), JsonObjectSchema]),
  selectedFeatureChoice: SelectedFeatureChoiceSchema,
}).strict();

export const RuntimeResumeGameRequestSchema = RuntimeMutatingRequestBaseSchema.extend({
  selectedBet: z.null(),
  selectedFeatureChoice: z.null(),
  resumeRef: z.union([z.string(), JsonObjectSchema]).optional(),
}).strict();

export const RuntimeCloseGameRequestSchema = RuntimeMutatingRequestBaseSchema.extend({
  selectedBet: z.null(),
  selectedFeatureChoice: z.null(),
  closeReason: z.string().min(1),
}).strict();

export const RuntimeGetHistoryRequestSchema = RuntimeRequestBaseSchema.extend({
  requestCounter: NonNegativeInteger,
  historyQuery: RuntimeHistoryQuerySchema,
}).strict();

const WalletSchema = z
  .object({
    balanceMinor: z.number().int(),
    previousBalanceMinor: z.number().int(),
    currencyCode: z.string().min(1),
    truncateCents: z.boolean(),
    delayedWalletMessagePending: z.boolean(),
  })
  .strict();

const RoundSchema = z
  .object({
    roundId: z.string().nullable(),
    status: z.enum(["NONE", "IN_PROGRESS", "FINAL"]),
    betMinor: z.number().int(),
    winMinor: z.number().int(),
    netEffectMinor: z.number().int(),
    outcomeHash: z.string().min(1),
  })
  .strict();

const FeatureSchema = z
  .object({
    mode: z.string().min(1),
    remainingActions: z.number().int(),
    nextAllowedActions: z.array(z.string()),
    featureContext: JsonObjectSchema,
  })
  .strict();

const PresentationPayloadSchema = z
  .object({
    featureMode: z.string().min(1),
    reelStops: z.array(z.number().int()),
    symbolGrid: z.array(z.unknown()),
    uiMessages: z.array(z.unknown()),
    animationCues: z.array(z.unknown()),
    audioCues: z.array(z.unknown()),
    counters: z.array(
      z
        .object({
          name: z.string(),
          value: z.number().int(),
        })
        .passthrough(),
    ),
    labels: JsonObjectSchema,
  })
  .strict();

const RestoreSchema = z
  .object({
    hasUnfinishedRound: z.boolean(),
    unfinishedRoundId: z.string().nullable(),
    resumeStateVersion: z.number().int(),
    opaqueRestorePayload: z.string().nullable(),
  })
  .strict();

const IdempotencySchema = z
  .object({
    isDuplicate: z.boolean(),
    duplicateOfRequestId: z.string().nullable(),
    replaySafe: z.boolean(),
  })
  .strict();

const RetrySchema = z
  .object({
    clientMayRetrySameKey: z.boolean(),
    clientMustIncrementCounterOnNewAction: z.boolean(),
  })
  .strict();

export const RuntimeEnvelopeResponseSchema = z
  .object({
    ok: z.literal(true),
    requestId: z.string().min(1),
    sessionId: z.string().min(1),
    requestCounter: NonNegativeInteger,
    stateVersion: NonNegativeInteger,
    wallet: WalletSchema,
    round: RoundSchema,
    feature: FeatureSchema,
    presentationPayload: PresentationPayloadSchema,
    restore: RestoreSchema,
    idempotency: IdempotencySchema,
    retry: RetrySchema,
  })
  .strict();

export const RuntimeBootstrapResponseSchema = z
  .object({
    contractVersion: z.literal("slot-bootstrap-v1"),
    session: z
      .object({
        sessionId: z.string().min(1),
        requestCounter: NonNegativeInteger,
        stateVersion: NonNegativeInteger,
      })
      .strict(),
    context: JsonObjectSchema,
    assets: z
      .object({
        assetBaseUrl: z.string(),
        clientVersion: z.string(),
        clientPackageVersion: z.string(),
        assetBundleHash: z.string(),
      })
      .strict(),
    runtime: z
      .object({
        mathPackageVersion: z.string(),
        rtpModelId: z.string(),
        engineContractVersion: z.literal("slot-runtime-v1"),
      })
      .strict(),
    policies: JsonObjectSchema,
    integrity: z
      .object({
        configIssuedAtUtc: z.string(),
        configId: z.string(),
        configHash: z.string(),
      })
      .strict(),
  })
  .strict();

export type RuntimeBootstrapRequest = z.infer<typeof RuntimeBootstrapRequestSchema>;
export type RuntimeOpenGameRequest = z.infer<typeof RuntimeOpenGameRequestSchema>;
export type RuntimePlayRoundRequest = z.infer<typeof RuntimePlayRoundRequestSchema>;
export type RuntimeFeatureActionRequest = z.infer<typeof RuntimeFeatureActionRequestSchema>;
export type RuntimeResumeGameRequest = z.infer<typeof RuntimeResumeGameRequestSchema>;
export type RuntimeCloseGameRequest = z.infer<typeof RuntimeCloseGameRequestSchema>;
export type RuntimeGetHistoryRequest = z.infer<typeof RuntimeGetHistoryRequestSchema>;
export type RuntimeEnvelopeWireResponse = z.infer<typeof RuntimeEnvelopeResponseSchema>;
export type RuntimeBootstrapWireResponse = z.infer<typeof RuntimeBootstrapResponseSchema>;

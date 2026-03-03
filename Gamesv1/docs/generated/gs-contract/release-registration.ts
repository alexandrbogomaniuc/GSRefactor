import { z } from "zod";

const NullableString = z.string().nullable();

export const ReleaseRegistrationSchema = z.object({
  schema: z.literal("slot-release-registration-v1"),
  gameId: z.string().min(1),
  gameName: z.string().min(1),
  version: z.string().min(1),
  releaseId: z.string().min(1),
  gitSha: z.string().min(1),
  entrypointUrl: z.string().url(),
  assetManifestUrl: z.string().url(),
  localizationBaseUrl: z.string().url(),
  mathPackageManifestReference: z.object({
    strategy: z.string().min(1),
    path: z.string().min(1),
    sha256: z.string().min(1),
    referenceLabel: z.string().min(1),
    launchTimeInjectionAllowed: z.boolean(),
  }).strict(),
  capabilityProfile: z.object({
    hash: z.string().min(1),
    path: z.string().min(1),
    source: z.string().min(1),
  }).strict(),
  featureFlags: z.record(z.string(), z.boolean()),
  rtpModels: z.array(
    z.object({
      id: z.string().min(1),
      rtp: z.number(),
    }),
  ),
  limits: z.object({
    minBet: z.number().int().nonnegative(),
    maxBet: z.number().int().nonnegative(),
    defaultBet: z.number().int().nonnegative(),
    maxExposure: z.number().int().nonnegative(),
  }).strict(),
  canary: z.object({
    eligible: z.boolean(),
    checklist: z.string().min(1),
    previousKnownGoodReleaseId: NullableString,
  }).strict(),
  rollback: z.object({
    checklist: z.string().min(1),
    previousKnownGoodReleaseId: NullableString,
  }).strict(),
  integrity: z.object({
    hashAlgorithm: z.string().min(1),
    checksumsFile: z.string().min(1),
    checksumsSha256: z.string().min(1),
    signature: z.object({
      status: z.string().min(1),
      algorithm: NullableString,
      reference: NullableString,
    }).strict(),
  }).strict(),
}).strict();

export type ReleaseRegistration = z.infer<typeof ReleaseRegistrationSchema>;

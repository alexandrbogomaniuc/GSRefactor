import type { ResolvedConfig } from "@gamesv1/core-compliance";

import type {
  PresentationCounters,
  RoundPresentationModel,
} from "../../app/runtime/RuntimeOutcomeMapper.ts";

export interface FeatureOverlay {
  id: string;
  type: string;
  label: string;
  value?: number;
  visible: boolean;
}

export interface FeatureModuleContext {
  runtimeConfig: ResolvedConfig;
}

export interface FeatureModuleInput {
  runtimeConfig: ResolvedConfig;
  counters: PresentationCounters;
  round: RoundPresentationModel;
}

export interface FeatureModuleOutput {
  overlays?: FeatureOverlay[];
  messages?: string[];
  soundCues?: string[];
  animationCues?: string[];
  controlVisibility?: {
    buyFeature?: boolean;
  };
}

export interface FeatureModule {
  readonly id: string;
  isEnabled(context: FeatureModuleContext): boolean;
  resolve(input: FeatureModuleInput): FeatureModuleOutput;
}

export const readBoolean = (value: unknown): boolean => value === true;

export const readNumber = (value: unknown): number | undefined => {
  if (typeof value !== "number" || !Number.isFinite(value)) return undefined;
  return value;
};

export const readLabelBoolean = (
  labels: Record<string, string>,
  key: string,
): boolean | undefined => {
  const raw = labels[key];
  if (raw === undefined) return undefined;
  if (raw.toLowerCase() === "true") return true;
  if (raw.toLowerCase() === "false") return false;
  return undefined;
};

export const ensurePositiveInt = (value: unknown): number | undefined => {
  const numeric = readNumber(value);
  if (numeric === undefined) return undefined;
  const floored = Math.floor(numeric);
  return floored > 0 ? floored : undefined;
};

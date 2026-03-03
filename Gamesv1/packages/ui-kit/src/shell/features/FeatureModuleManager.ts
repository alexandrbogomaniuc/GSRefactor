import type { ResolvedConfig } from "@gamesv1/core-compliance";

import type { RoundPresentationModel } from "../presentation/PremiumPresentationMapper.ts";
import { BuyFeatureModule } from "./BuyFeatureModule.ts";
import { FreeSpinsFeatureModule } from "./FreeSpinsFeatureModule.ts";
import { HoldAndWinFeatureModule } from "./HoldAndWinFeatureModule.ts";
import { JackpotHooksFeatureModule } from "./JackpotHooksFeatureModule.ts";
import { RespinFeatureModule } from "./RespinFeatureModule.ts";
import type {
  FeatureModule,
  FeatureModuleInput,
  FeatureModuleOutput,
  FeatureOverlay,
} from "./types.ts";

export interface FeatureFrame {
  overlays: FeatureOverlay[];
  messages: string[];
  soundCues: string[];
  animationCues: string[];
  controlVisibility: {
    buyFeature?: boolean;
  };
  enabledModuleIds: string[];
  activeModuleIds: string[];
}

const mergeOutputs = (outputs: FeatureModuleOutput[]): FeatureFrame => ({
  overlays: outputs.flatMap((output) => output.overlays ?? []),
  messages: outputs.flatMap((output) => output.messages ?? []),
  soundCues: outputs.flatMap((output) => output.soundCues ?? []),
  animationCues: outputs.flatMap((output) => output.animationCues ?? []),
  controlVisibility: outputs.reduce(
    (acc, output) => ({ ...acc, ...(output.controlVisibility ?? {}) }),
    {} as FeatureFrame["controlVisibility"],
  ),
  enabledModuleIds: [],
  activeModuleIds: [],
});

const isOutputActive = (output: FeatureModuleOutput): boolean => {
  const hasControlVisibility = Object.values(output.controlVisibility ?? {}).some(
    (value) => value !== undefined,
  );

  return (
    (output.overlays?.length ?? 0) > 0 ||
    (output.messages?.length ?? 0) > 0 ||
    (output.soundCues?.length ?? 0) > 0 ||
    (output.animationCues?.length ?? 0) > 0 ||
    hasControlVisibility
  );
};

export class FeatureModuleManager {
  private readonly modules: FeatureModule[];
  private readonly runtimeConfig: ResolvedConfig;

  constructor(runtimeConfig: ResolvedConfig) {
    this.runtimeConfig = runtimeConfig;
    this.modules = [
      new FreeSpinsFeatureModule(),
      new RespinFeatureModule(),
      new HoldAndWinFeatureModule(),
      new BuyFeatureModule(),
      new JackpotHooksFeatureModule(),
    ];
  }

  public resolve(round: RoundPresentationModel): FeatureFrame {
    const outputs: FeatureModuleOutput[] = [];
    const enabledModuleIds: string[] = [];
    const activeModuleIds: string[] = [];

    for (const module of this.modules) {
      if (!module.isEnabled({ runtimeConfig: this.runtimeConfig })) {
        continue;
      }

      enabledModuleIds.push(module.id);
      const input: FeatureModuleInput = {
        runtimeConfig: this.runtimeConfig,
        counters: round.counters,
        round,
      };
      const output = module.resolve(input);
      outputs.push(output);

      if (isOutputActive(output)) {
        activeModuleIds.push(module.id);
      }
    }

    const merged = mergeOutputs(outputs);
    merged.enabledModuleIds = enabledModuleIds;
    merged.activeModuleIds = activeModuleIds;
    return merged;
  }

  public listEnabledModules(): string[] {
    return this.modules
      .filter((module) => module.isEnabled({ runtimeConfig: this.runtimeConfig }))
      .map((module) => module.id);
  }
}

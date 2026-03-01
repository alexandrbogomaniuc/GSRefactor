import type { ResolvedConfig } from "@gamesv1/core-compliance";

import type {
  PresentationOverlay,
  RoundPresentationModel,
} from "../../app/runtime/RuntimeOutcomeMapper.ts";
import { BuyFeatureModule } from "./BuyFeatureModule.ts";
import { FreeSpinsFeatureModule } from "./FreeSpinsModule.ts";
import { HoldAndWinFeatureModule } from "./HoldAndWinFeatureModule.ts";
import { JackpotHooksFeatureModule } from "./JackpotHooksFeatureModule.ts";
import { RespinFeatureModule } from "./RespinFeatureModule.ts";
import type {
  FeatureModule,
  FeatureModuleInput,
  FeatureModuleOutput,
} from "./types.ts";

export interface FeatureFrame {
  overlays: PresentationOverlay[];
  messages: string[];
  soundCues: string[];
  animationCues: string[];
  controlVisibility: {
    buyFeature?: boolean;
  };
  activeModules: string[];
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
  activeModules: [],
});

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
    const activeModules: string[] = [];

    for (const module of this.modules) {
      if (!module.isEnabled({ runtimeConfig: this.runtimeConfig })) {
        continue;
      }

      activeModules.push(module.id);
      const input: FeatureModuleInput = {
        runtimeConfig: this.runtimeConfig,
        counters: round.counters,
        serverState: round.serverState,
        round,
      };
      outputs.push(module.resolve(input));
    }

    const merged = mergeOutputs(outputs);
    merged.activeModules = activeModules;
    return merged;
  }

  public listEnabledModules(): string[] {
    return this.modules
      .filter((module) => module.isEnabled({ runtimeConfig: this.runtimeConfig }))
      .map((module) => module.id);
  }
}


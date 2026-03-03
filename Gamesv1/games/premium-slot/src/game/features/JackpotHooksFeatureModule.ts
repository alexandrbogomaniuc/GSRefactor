import type { FeatureModule, FeatureModuleContext, FeatureModuleInput } from "./types.ts";
import { readLabelBoolean } from "./types.ts";

export class JackpotHooksFeatureModule implements FeatureModule {
  public readonly id = "jackpot-hooks";

  public isEnabled(context: FeatureModuleContext): boolean {
    return (
      context.runtimeConfig.capabilities.features.jackpotHooks ||
      context.runtimeConfig.jackpotHooks.enabled
    );
  }

  public resolve(input: FeatureModuleInput) {
    const jackpotLevel = input.counters.jackpotLevel;
    const jackpotTriggered = readLabelBoolean(input.round.labels, "jackpotTriggered") === true;

    if (!jackpotTriggered && jackpotLevel === undefined) {
      return {};
    }

    return {
      overlays: [
        {
          id: "jackpot-overlay",
          type: "jackpot",
          label: jackpotLevel !== undefined ? `JACKPOT LV ${jackpotLevel}` : "JACKPOT",
          value: jackpotLevel,
          visible: true,
        },
      ],
      messages: [jackpotTriggered ? "JACKPOT TRIGGERED" : "JACKPOT AVAILABLE"],
      soundCues: ["jackpot-stinger"],
      animationCues: ["jackpot-overlay"],
    };
  }
}

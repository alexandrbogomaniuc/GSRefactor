import type { FeatureModule, FeatureModuleContext, FeatureModuleInput } from "./types.ts";

export class FreeSpinsFeatureModule implements FeatureModule {
  public readonly id = "free-spins";

  public isEnabled(context: FeatureModuleContext): boolean {
    return context.runtimeConfig.capabilities.features.freeSpins;
  }

  public resolve(input: FeatureModuleInput) {
    const remaining = input.counters.freeSpinsRemaining;
    const active = remaining !== undefined ? remaining > 0 : input.serverState.freeSpinsActive === true;

    if (!active) {
      return {};
    }

    return {
      overlays: [
        {
          id: "free-spins-overlay",
          type: "free-spins",
          label: remaining !== undefined ? `FREE SPINS ${remaining}` : "FREE SPINS",
          value: remaining,
          visible: true,
        },
      ],
      messages: [remaining !== undefined ? `FREE SPINS REMAINING: ${remaining}` : "FREE SPINS ACTIVE"],
      animationCues: ["free-spins-pulse"],
    };
  }
}


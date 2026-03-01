import type { FeatureModule, FeatureModuleContext, FeatureModuleInput } from "./types.ts";

export class RespinFeatureModule implements FeatureModule {
  public readonly id = "respin";

  public isEnabled(context: FeatureModuleContext): boolean {
    return context.runtimeConfig.capabilities.features.respin;
  }

  public resolve(input: FeatureModuleInput) {
    const remaining = input.counters.respinRemaining;
    const active = remaining !== undefined ? remaining > 0 : input.serverState.respinActive === true;

    if (!active) {
      return {};
    }

    return {
      overlays: [
        {
          id: "respin-overlay",
          type: "respin",
          label: remaining !== undefined ? `RESPIN ${remaining}` : "RESPIN",
          value: remaining,
          visible: true,
        },
      ],
      messages: [remaining !== undefined ? `RESPIN REMAINING: ${remaining}` : "RESPIN ACTIVE"],
      animationCues: ["respin-highlight"],
    };
  }
}


import type { FeatureModule, FeatureModuleContext, FeatureModuleInput } from "./types.ts";

export class HoldAndWinFeatureModule implements FeatureModule {
  public readonly id = "hold-and-win";

  public isEnabled(context: FeatureModuleContext): boolean {
    return context.runtimeConfig.capabilities.features.holdAndWin;
  }

  public resolve(input: FeatureModuleInput) {
    const remaining = input.counters.holdAndWinRemaining;
    const active = remaining !== undefined ? remaining > 0 : input.serverState.holdAndWinActive === true;

    if (!active) {
      return {};
    }

    return {
      overlays: [
        {
          id: "hold-and-win-overlay",
          type: "hold-and-win",
          label: remaining !== undefined ? `HOLD & WIN ${remaining}` : "HOLD & WIN",
          value: remaining,
          visible: true,
        },
      ],
      messages: [remaining !== undefined ? `HOLD & WIN REMAINING: ${remaining}` : "HOLD & WIN ACTIVE"],
      animationCues: ["hold-and-win-frame"],
    };
  }
}


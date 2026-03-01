export interface HudSchema {
  version: string;
  layout: {
    balance: boolean;
    bet: boolean;
    win: boolean;
  };
  controls: {
    spin: boolean;
    autoplay: boolean;
    turbo: boolean;
    buyFeature: boolean;
    sound: boolean;
    settings: boolean;
    history: boolean;
  };
}

export const DefaultHudSchema: HudSchema = {
  version: "1.0.0",
  layout: {
    balance: true,
    bet: true,
    win: true,
  },
  controls: {
    spin: true,
    autoplay: true,
    turbo: true,
    buyFeature: true,
    sound: true,
    settings: true,
    history: true,
  },
};

export const mergeHudSchema = (overrides: Partial<HudSchema>): HudSchema => ({
  ...DefaultHudSchema,
  ...overrides,
  layout: {
    ...DefaultHudSchema.layout,
    ...(overrides.layout ?? {}),
  },
  controls: {
    ...DefaultHudSchema.controls,
    ...(overrides.controls ?? {}),
  },
});

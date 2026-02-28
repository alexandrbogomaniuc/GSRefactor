import { ResolvedConfig } from "@gamesv1/core-compliance";

let currentConfig: ResolvedConfig | null = null;

export const RuntimeStore = {
  set(config: ResolvedConfig) {
    currentConfig = config;
    console.log("[RuntimeStore] Config Updated:", config);
  },

  get(): ResolvedConfig {
    if (!currentConfig) {
      throw new Error("RuntimeStore: Config has not been initialized yet.");
    }
    return currentConfig;
  },

  get betting() {
    return this.get().betConfig;
  },

  get limits() {
    const config = this.get();
    return {
      minBet: config.minBet,
      maxBet: config.maxBet,
      maxExposure: config.maxExposure,
      defaultBet: config.defaultBet,
    };
  },

  get localization() {
    return this.get().localization;
  },

  get capabilities() {
    return this.get().capabilities;
  },
};

import type { ResolvedConfig } from "@gamesv1/core-compliance";

let resolvedRuntimeConfig: ResolvedConfig | null = null;

export const ResolvedRuntimeConfigStore = {
  set(config: ResolvedConfig): void {
    resolvedRuntimeConfig = config;
    console.log("[ResolvedRuntimeConfigStore] Updated.");
  },

  get(): ResolvedConfig {
    if (!resolvedRuntimeConfig) {
      throw new Error("ResolvedRuntimeConfigStore is not initialized.");
    }
    return resolvedRuntimeConfig;
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

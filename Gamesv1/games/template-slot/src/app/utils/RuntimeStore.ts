import { ResolvedConfig } from "@gs/config";

let currentConfig: ResolvedConfig | null = null;

export const RuntimeStore = {
    set(config: ResolvedConfig) {
        currentConfig = config;
        console.log('[RuntimeStore] Config Updated:', config);
    },

    get(): ResolvedConfig {
        if (!currentConfig) {
            throw new Error('RuntimeStore: Config has not been initialized yet.');
        }
        return currentConfig;
    },

    get game() { return this.get().game; },
    get bank() { return this.get().bank; },
    get customer() { return this.get().customer; }
};

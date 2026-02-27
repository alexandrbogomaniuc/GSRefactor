import {
    resolveRuntimeConfig,
    GameSettings,
    RuntimeBankConfig,
    CustomerConfig,
    GameSettingsSchema
} from "@gs/config";
import { RuntimeStore } from "./RuntimeStore";

export class ConfigManager {
    /**
     * Main entry point to initialize game configuration.
     * Loads local settings, fetches server data (mocked here), and merges them.
     */
    public static async init() {
        // 1. Load Design-time Settings (game.settings.json)
        const settings = await this.loadGameSettings();

        // 2. Load Runtime Data (Simulating server ENTER/LoadGameData response)
        // In a real app, this comes from GsWsTransport or an API call.
        let bankConfig = await this.fetchBankConfig();
        let customerConfig = await this.fetchCustomerConfig();

        // 3. Apply Dev Overrides (URL & Local JSON)
        const combined = await this.applyDevOverrides(bankConfig, customerConfig);
        bankConfig = combined.bank;
        customerConfig = combined.customer;

        // 4. Resolve and Validate
        const resolved = resolveRuntimeConfig(settings, bankConfig, customerConfig);

        // 5. Save to global store
        RuntimeStore.set(resolved);
    }

    private static async loadGameSettings(): Promise<GameSettings> {
        const response = await fetch('./game.settings.json');
        const data = await response.json();
        return GameSettingsSchema.parse(data);
    }

    private static async fetchBankConfig(): Promise<RuntimeBankConfig> {
        // MOCK: This would normally come from the WS 'ENTER' response
        return {
            currency: "EUR",
            symbol: "€",
            fractionDigits: 2,
            truncateCents: false,
            minBet: 10,
            maxBet: 1000,
            defaultBet: 100
        };
    }

    private static async fetchCustomerConfig(): Promise<CustomerConfig> {
        // MOCK: Integration flags
        return {
            turboplayEnabled: true,
            autoplayEnabled: true,
            minSpinTime: 2000,
            postMessageEnabled: true
        };
    }

    private static async applyDevOverrides(bank: RuntimeBankConfig, customer: CustomerConfig) {
        const urlParams = new URLSearchParams(window.location.search);

        // Dev Bank Override: ?devBank=USD_FUN
        const devBank = urlParams.get('devBank');
        if (devBank) {
            console.warn(`[Config] Overriding bank via URL: ${devBank}`);
            // Simulating a lookup or specific override
            if (devBank === 'USD_TRUNC') {
                bank.currency = 'USD';
                bank.truncateCents = true;
                bank.minBet = 1;
                bank.maxBet = 100;
                bank.defaultBet = 10;
            }
        }

        // Try loading dev-bank.json if it exists (local QA tool)
        try {
            const devResponse = await fetch('/env/dev-bank.json');
            if (devResponse.ok) {
                const overrides = await devResponse.json();
                Object.assign(bank, overrides);
                console.warn('[Config] Applied dev-bank.json overrides');
            }
        } catch {
            // Ignore if file doesn't exist
        }

        return { bank, customer };
    }
}

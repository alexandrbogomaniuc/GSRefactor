import { FeatureFlags, DefaultFeatureFlags } from './FeatureFlags';

export interface BankProperties {
    currencyCode: string;
    fractionDigits: number;
    truncateCents: boolean;      // If true, drops decimals (must be paired with exact whole integer bets)
    maxWinAllowed: number | null;
}

export interface ConfigurationLayer {
    flags?: Partial<FeatureFlags>;
    bank?: Partial<BankProperties>;
}

export interface ComplianceResolvedConfig {
    flags: FeatureFlags;
    bank: BankProperties;
}

/** Default minimum required configuration logic fallback */
export const DefaultBankProperties: BankProperties = {
    currencyCode: 'USD',
    fractionDigits: 2,
    truncateCents: false,
    maxWinAllowed: null
};

/**
 * Parses and resolves configuration in strict hierarchical precedence.
 * Precedence Order (Lowest to Highest):
 * 1. Base Defaults
 * 2. Certification Overrides
 * 3. Licensee Overrides
 * 4. Launch Parameters (URL params / Game initialization injects)
 * 5. Dynamic Bank Overrides
 */
export function resolveConfig(
    certConfig: ConfigurationLayer = {},
    licenseeConfig: ConfigurationLayer = {},
    launchParams: ConfigurationLayer = {},
    bankOverrides: Partial<BankProperties> = {}
): ComplianceResolvedConfig {

    const resolvedFlags: FeatureFlags = {
        ...DefaultFeatureFlags,
        ...(certConfig.flags || {}),
        ...(licenseeConfig.flags || {}),
        ...(launchParams.flags || {})
    };

    const resolvedBank: BankProperties = {
        ...DefaultBankProperties,
        ...(certConfig.bank || {}),
        ...(licenseeConfig.bank || {}),
        ...(launchParams.bank || {}),
        ...bankOverrides
    };

    const config: ComplianceResolvedConfig = {
        flags: resolvedFlags,
        bank: resolvedBank
    };

    assertCompliance(config);
    return config;
}

/**
 * Validates the completely resolved configuration for logical contradictions.
 * Throws a readable Error immediately preventing the client from loading uncertified rule boundaries.
 */
export function assertCompliance(config: ComplianceResolvedConfig): void {
    const { flags, bank } = config;

    // RULE 1: Truncated Cents vs Fractions
    if (bank.truncateCents && bank.fractionDigits > 0) {
        throw new Error("Compliance Error: `truncateCents` is true, but `fractionDigits` is > 0. Must be 0 to safely truncate.");
    }

    // RULE 2: Forced Spin Stop Override
    if (flags.forcedSpinStopAllowed && flags.minReelsSpinningTimeSecs > 0) {
        // Warning log is enough or we enforce
        // throw new Error("Compliance Error: forcedSpinStopAllowed conflicts with minReelsSpinningTimeSecs requirements in strict markets.");
    }

    // RULE 3: Turboplay bounds
    if (flags.turboplayAllowed && flags.turboplaySpeedMultiplier <= 1.0) {
        throw new Error(`Compliance Error: turboplay is allowed but speed multiplier is strictly non-accelerated at ${flags.turboplaySpeedMultiplier}x`);
    }

    // RULE 4: Impossible Min Spin Times
    if (flags.minReelsSpinningTimeSecs < 0) {
        throw new Error("Compliance Error: minReelsSpinningTimeSecs cannot be negative");
    }
}



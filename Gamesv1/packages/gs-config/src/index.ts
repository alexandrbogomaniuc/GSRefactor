import { z } from 'zod';

// 1. Static Game Settings (Design-time)
export const MathModelSchema = z.object({
    id: z.string(),
    rtp: z.number(),
    label: z.string().optional(),
});

export type MathModel = z.infer<typeof MathModelSchema>;

export const GsRegistrationSchema = z.object({
    mathModels: z.array(MathModelSchema).min(1),
    volatility: z.enum(["low", "medium-low", "medium", "medium-high", "high"]),
    possibleMaxWinMultiplier: z.number(),
    capWinMultiplier: z.number(),
    releaseTime: z.number(), // seconds
    isFrb: z.boolean().default(false),
    ocb: z.boolean().default(false),
});

export type GsRegistration = z.infer<typeof GsRegistrationSchema>;

export const GameSettingsSchema = z.object({
    schemaVersion: z.string().default("1.0.0"),
    gameId: z.string(),
    gameName: z.string(),
    version: z.string().optional(),
    reels: z.object({
        rows: z.number(),
        cols: z.number(),
    }),
    features: z.object({
        freeSpins: z.boolean().default(false),
        buyFeature: z.boolean().default(false),
        autoplay: z.boolean().default(true),
    }).default({}),
    gs: GsRegistrationSchema.optional(),
});

export type GameSettings = z.infer<typeof GameSettingsSchema>;

// 2. Bank & Currency Config (Runtime - from Server)
export const RuntimeBankConfigSchema = z.object({
    currency: z.string(),
    symbol: z.string().optional(),
    fractionDigits: z.number().default(2),
    truncateCents: z.boolean().default(false),
    minBet: z.number(),
    maxBet: z.number(),
    defaultBet: z.number(),
    betLevels: z.array(z.number()).optional(),
    maxWinAllowed: z.number().optional(),
});

export type RuntimeBankConfig = z.infer<typeof RuntimeBankConfigSchema>;

// 3. Customer & Compliance Config (Runtime - from Server/Client)
export const CustomerConfigSchema = z.object({
    turboplayEnabled: z.boolean().default(true),
    autoplayEnabled: z.boolean().default(true),
    minSpinTime: z.number().default(2000), // ms
    postMessageEnabled: z.boolean().default(true),
    realityCheckInterval: z.number().optional(), // minutes
});

export type CustomerConfig = z.infer<typeof CustomerConfigSchema>;

// 4. Combined Runtime Root Config
export const ResolvedConfigSchema = z.object({
    game: GameSettingsSchema,
    bank: RuntimeBankConfigSchema,
    customer: CustomerConfigSchema,
});

export type ResolvedConfig = z.infer<typeof ResolvedConfigSchema>;

/**
 * Merges various config sources, applies precedence, and validates consistency.
 */
export function resolveRuntimeConfig(
    settings: GameSettings,
    bank: RuntimeBankConfig,
    customer: CustomerConfig
): ResolvedConfig {

    // Validation: Truncate Cents consistency
    if (bank.truncateCents) {
        // If we truncate cents, all bet values in the system must be integers (no decimals)
        const validateInteger = (val: number, name: string) => {
            if (!Number.isInteger(val)) {
                throw new Error(`Config Error: ${name} (${val}) must be an integer when truncateCents is true.`);
            }
        };
        validateInteger(bank.minBet, 'minBet');
        validateInteger(bank.maxBet, 'maxBet');
        validateInteger(bank.defaultBet, 'defaultBet');
    }

    // Validation: Bet range consistency
    if (bank.minBet > bank.maxBet) {
        throw new Error('Config Error: minBet cannot be greater than maxBet.');
    }
    if (bank.defaultBet < bank.minBet || bank.defaultBet > bank.maxBet) {
        throw new Error('Config Error: defaultBet is outside of the min/max bet range.');
    }

    return ResolvedConfigSchema.parse({
        game: settings,
        bank,
        customer
    });
}

import { z } from "zod";

const HexColorSchema = z
  .string()
  .trim()
  .regex(/^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/, "Expected #RRGGBB or #RRGGBBAA");

const PreloaderStyleSchema = z.enum(["wow", "minimal"]);
const PreloaderHeroFxSchema = z.enum(["energyRing", "coinVortex", "slotSweep"]);

const ShellBrandTokensSchema = z.object({
  displayName: z.string().trim().min(1).max(48),
  logoUrl: z.string().trim().min(1).optional(),
  logoAssetKey: z.string().trim().min(1).optional(),
  primaryColor: HexColorSchema,
  accentColor: HexColorSchema,
});

const ShellPreloaderTokensSchema = z.object({
  style: PreloaderStyleSchema,
  heroFx: PreloaderHeroFxSchema,
  vfxIntensity: z.number().min(0).max(1),
  audioStingerCue: z.string().trim().min(1).optional(),
});

export const ShellThemeTokensSchema = z.object({
  brand: ShellBrandTokensSchema,
  preloader: ShellPreloaderTokensSchema,
});

export type ShellBrandTokens = z.infer<typeof ShellBrandTokensSchema>;
export type ShellPreloaderTokens = z.infer<typeof ShellPreloaderTokensSchema>;
export type ShellThemeTokens = z.infer<typeof ShellThemeTokensSchema>;

export interface ShellThemeTokensOverrides {
  brand?: Partial<ShellBrandTokens>;
  preloader?: Partial<ShellPreloaderTokens>;
}

export const DefaultShellThemeTokens: ShellThemeTokens = Object.freeze({
  brand: {
    displayName: "Premium Slots",
    primaryColor: "#E7BC56",
    accentColor: "#FF2F7B",
  },
  preloader: {
    style: "wow",
    heroFx: "energyRing",
    vfxIntensity: 0.82,
  },
});

export const resolveShellThemeTokens = (
  overrides: ShellThemeTokensOverrides = {},
): ShellThemeTokens =>
  ShellThemeTokensSchema.parse({
    brand: {
      ...DefaultShellThemeTokens.brand,
      ...(overrides.brand ?? {}),
    },
    preloader: {
      ...DefaultShellThemeTokens.preloader,
      ...(overrides.preloader ?? {}),
    },
  });

export const getBrandMonogram = (displayName: string): string =>
  displayName
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((segment) => segment[0]?.toUpperCase() ?? "")
    .join("") || "GS";

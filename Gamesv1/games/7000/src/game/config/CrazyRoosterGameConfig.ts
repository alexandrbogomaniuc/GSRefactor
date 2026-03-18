import { GameConfig } from "@gamesv1/ui-kit";

export const CRAZY_ROOSTER_SUPPORTED_ASSET_PROVIDERS = [
  "openai",
  "nanobanana",
  "donorlocal",
] as const;

export type CrazyRoosterAssetProvider =
  (typeof CRAZY_ROOSTER_SUPPORTED_ASSET_PROVIDERS)[number];
export type AssetProvider = CrazyRoosterAssetProvider;

export const CRAZY_ROOSTER_LAYOUT = {
  reelCount: 3,
  numReels: 3,
  rowCount: 4,
  numRows: 4,
  symbolWidth: 172,
  symbolHeight: 148,
  reelSpacing: 14,
  rowSpacing: 14,
  symbolCount: 11,
  extraSymbols: 2,
  minSpinMs: 1300,
  spinStaggerMs: 120,
  stopDelayMs: 420,
};

export const CRAZY_ROOSTER_PAYLINES: number[][] = [
  [0, 0, 0],
  [1, 1, 1],
  [2, 2, 2],
  [3, 3, 3],
  [0, 1, 2],
  [1, 2, 3],
  [2, 1, 0],
  [3, 2, 1],
];

export const CRAZY_ROOSTER_PROVISIONAL_BET_LADDER = [
  0.1,
  0.2,
  0.3,
  0.4,
  0.5,
  0.6,
  0.7,
  0.8,
  0.9,
  1,
  2,
  3,
  4,
  5,
  6,
  7,
  8,
  9,
  10,
  15,
  20,
  25,
  50,
  75,
  100,
  150,
  200,
] as const;

export const CRAZY_ROOSTER_BET_LIMITS = {
  minBet: CRAZY_ROOSTER_PROVISIONAL_BET_LADDER[0],
  defaultBet: 1,
  maxBet: 20000,
} as const;

export const CRAZY_ROOSTER_BUY_TIERS = [
  { id: "bonus-75", label: "BONUS 75", priceMultiplier: 75 },
  { id: "bonus-200", label: "BONUS 200", priceMultiplier: 200 },
  { id: "bonus-300", label: "BONUS 300", priceMultiplier: 300 },
] as const;
export const CRAZY_ROOSTER_BUY_BONUS_TIERS = CRAZY_ROOSTER_BUY_TIERS;

export const CRAZY_ROOSTER_JACKPOTS = {
  miniMultiplier: 25,
  minorMultiplier: 50,
  majorMultiplier: 150,
  grandMultiplier: 1000,
};

export const CRAZY_ROOSTER_BIG_WIN_THRESHOLDS = {
  bigMultiplier: 10,
  hugeMultiplier: 25,
  megaMultiplier: 50,
};

export const CRAZY_ROOSTER_FEATURE_FLAGS = {
  autoplay: true,
  buyFeature: true,
  collectFeature: true,
  boostFeature: true,
  bonusGame: true,
  holdAndWin: true,
  holdForTurbo: true,
  jackpotHooks: true,
} as const;

export const CRAZY_ROOSTER_SYMBOL_LABELS = [
  "EGG",
  "CHERRIES",
  "LEMON",
  "ORANGE",
  "PLUM",
  "BAR",
  "SEVEN",
  "COIN",
  "BOLT",
  "ROOSTER",
  "BELL",
] as const;

export const CRAZY_ROOSTER_SYMBOL_FRAME_KEYS = [
  "symbol-0-egg",
  "symbol-1-cherries",
  "symbol-2-lemon",
  "symbol-3-orange",
  "symbol-4-plum",
  "symbol-5-bar",
  "symbol-6-seven",
  "symbol-7-coin",
  "symbol-8-bolt",
  "symbol-9-rooster",
  "symbol-bell",
] as const;

export const CRAZY_ROOSTER_IDLE_COLUMNS = [
  [0, 1, 8, 3],
  [1, 8, 2, 9],
  [2, 3, 9, 4],
];

export const CRAZY_ROOSTER_BRAND = {
  brandName: "BetOnline",
  displayName: "BetOnline",
  footerText: "Powered By BetOnline Studios",
  themeId: "betonline-crazy-rooster",
  primaryColor: "#000000",
  accentColor: "#C7141A",
  surfaceColor: "#FFFFFF",
  defaultProvider: "openai" as CrazyRoosterAssetProvider,
};

export const CRAZY_ROOSTER_BRAND_NAME = CRAZY_ROOSTER_BRAND.brandName;
export const CRAZY_ROOSTER_DISPLAY_NAME = CRAZY_ROOSTER_BRAND.displayName;
export const CRAZY_ROOSTER_FOOTER = CRAZY_ROOSTER_BRAND.footerText;

export const isAssetProvider = (
  value: string | null | undefined,
): value is CrazyRoosterAssetProvider =>
  CRAZY_ROOSTER_SUPPORTED_ASSET_PROVIDERS.includes(value as CrazyRoosterAssetProvider);

export const resolveExplicitAssetProvider = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
  envProvider = import.meta.env.VITE_ASSET_PROVIDER,
): CrazyRoosterAssetProvider | null => {
  const requested =
    params.get("assetProvider") ?? params.get("provider") ?? envProvider;
  return isAssetProvider(requested) ? requested : null;
};

export const resolveAssetProvider = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
  envProvider = import.meta.env.VITE_ASSET_PROVIDER,
): CrazyRoosterAssetProvider => {
  return resolveExplicitAssetProvider(params, envProvider) ?? CRAZY_ROOSTER_BRAND.defaultProvider;
};

export const applyCrazyRoosterSharedGameConfig = (): void => {
  GameConfig.numReels = CRAZY_ROOSTER_LAYOUT.reelCount;
  GameConfig.numRows = CRAZY_ROOSTER_LAYOUT.rowCount;
  GameConfig.symbolWidth = CRAZY_ROOSTER_LAYOUT.symbolWidth;
  GameConfig.symbolHeight = CRAZY_ROOSTER_LAYOUT.symbolHeight;
  GameConfig.reelSpacing = CRAZY_ROOSTER_LAYOUT.reelSpacing;
  GameConfig.rowSpacing = CRAZY_ROOSTER_LAYOUT.rowSpacing;
  GameConfig.symbolCount = CRAZY_ROOSTER_LAYOUT.symbolCount;
  GameConfig.symbolAtlasPrefix = "symbol_";
};

export const buildGridFromColumns = (columns: number[][]): number[][] =>
  Array.from({ length: CRAZY_ROOSTER_LAYOUT.rowCount }, (_, rowIndex) =>
    Array.from({ length: CRAZY_ROOSTER_LAYOUT.reelCount }, (_, reelIndex) => {
      return columns[reelIndex]?.[rowIndex] ?? 0;
    }),
  );

export const pickBuyTier = (requested: number | null | undefined) =>
  CRAZY_ROOSTER_BUY_TIERS.find((tier) => tier.priceMultiplier === requested) ??
  CRAZY_ROOSTER_BUY_TIERS[0];

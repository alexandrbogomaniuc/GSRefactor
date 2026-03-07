import {
  CRAZY_ROOSTER_BRAND,
  type CrazyRoosterAssetProvider,
} from "../config/CrazyRoosterGameConfig.ts";

const VALID_PROVIDERS = new Set<CrazyRoosterAssetProvider>(["openai", "nanobanana"]);

export const resolveAssetProvider = (
  queryParams = new URLSearchParams(window.location.search),
): CrazyRoosterAssetProvider => {
  const requested = queryParams.get("assetProvider")?.trim().toLowerCase();
  if (requested && VALID_PROVIDERS.has(requested as CrazyRoosterAssetProvider)) {
    return requested as CrazyRoosterAssetProvider;
  }
  return CRAZY_ROOSTER_BRAND.defaultProvider;
};

export const resolveProviderSymbolRoot = (
  provider = resolveAssetProvider(),
): string => `/providers/${provider}/symbols`;

export const resolveProviderSymbolUrl = (
  symbolId: number,
  provider = resolveAssetProvider(),
): string => `${resolveProviderSymbolRoot(provider)}/symbol-${symbolId}.svg`;

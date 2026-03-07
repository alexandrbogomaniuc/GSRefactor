import {
  resolveAssetProvider,
  type CrazyRoosterAssetProvider,
} from "../../game/config/CrazyRoosterGameConfig";

const PROVIDERS = new Set<CrazyRoosterAssetProvider>(["openai", "nanobanana"]);

export const resolveCrazyRoosterAssetProvider = (): CrazyRoosterAssetProvider => {
  const params = new URLSearchParams(window.location.search);
  const requested = params.get("assetProvider")?.trim().toLowerCase();
  if (requested && PROVIDERS.has(requested as CrazyRoosterAssetProvider)) {
    return requested as CrazyRoosterAssetProvider;
  }

  return resolveAssetProvider(params);
};

export const resolveCrazyRoosterAssetRoot = (): string =>
  `/providers/${resolveCrazyRoosterAssetProvider()}/symbols`;

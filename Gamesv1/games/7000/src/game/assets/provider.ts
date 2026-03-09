import {
  type CrazyRoosterAssetProvider,
} from "../config/CrazyRoosterGameConfig.ts";

import { getProviderPackStatus } from "../../app/assets/providerPackRegistry";

export const resolveAssetProvider = (): CrazyRoosterAssetProvider =>
  getProviderPackStatus().effectiveProvider;

export const resolveProviderSymbolRoot = (
  provider = resolveAssetProvider(),
): string => `/providers/${provider}/symbols`;

export const resolveProviderSymbolUrl = (
  symbolId: number,
  provider = resolveAssetProvider(),
): string => `${resolveProviderSymbolRoot(provider)}/symbol-${symbolId}.svg`;

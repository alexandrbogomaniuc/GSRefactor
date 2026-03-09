import { type CrazyRoosterAssetProvider } from "../../game/config/CrazyRoosterGameConfig";

import { getProviderPackStatus } from "./providerPackRegistry";

export const resolveCrazyRoosterAssetProvider = (): CrazyRoosterAssetProvider =>
  getProviderPackStatus().effectiveProvider;

export const resolveCrazyRoosterAssetRoot = (): string =>
  `/providers/${resolveCrazyRoosterAssetProvider()}/symbols`;

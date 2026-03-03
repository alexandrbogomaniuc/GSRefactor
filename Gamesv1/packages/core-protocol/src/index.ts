export * from "./IGameTransport.ts";
export * from "./SpinProfiling.ts";
export * from "./schemas.ts";
export * from "./http/GsHttpRuntimeTransport.ts";
export * from "./ws/GsWsTransport.ts";

import {
  type IGameTransport,
  type LegacyGameTransportAdapter,
  type GameInitConfig,
} from "./IGameTransport.ts";
import { GsHttpRuntimeTransport } from "./http/GsHttpRuntimeTransport.ts";
import { GsWsTransport } from "./ws/GsWsTransport.ts";

const HTTP_RUNTIME_MODES = new Set<string>(["GS_HTTP_RUNTIME", "HTTP_GS", "EXTGAME"]);
const WS_RUNTIME_MODES = new Set<string>(["WS", "WS_LEGACY"]);

export function createTransport(config: GameInitConfig): IGameTransport {
  if (HTTP_RUNTIME_MODES.has(config.mode)) {
    return new GsHttpRuntimeTransport();
  }

  if (WS_RUNTIME_MODES.has(config.mode)) {
    throw new Error(
      "WS transport is legacy/experimental and outside canonical surface. Use createLegacyTransport().",
    );
  }

  throw new Error(`Unsupported transport mode requested: ${config.mode}`);
}

export function createLegacyTransport(
  config: GameInitConfig,
): LegacyGameTransportAdapter {
  if (!WS_RUNTIME_MODES.has(config.mode)) {
    throw new Error(
      `Legacy transport requires WS mode (received: ${config.mode}).`,
    );
  }
  return new GsWsTransport();
}

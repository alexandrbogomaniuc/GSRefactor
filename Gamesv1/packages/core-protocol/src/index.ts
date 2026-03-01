export * from "./IGameTransport.ts";
export * from "./SpinProfiling.ts";
export * from "./schemas.ts";
export * from "./http/GsHttpRuntimeTransport.ts";

import { IGameTransport, GameInitConfig } from "./IGameTransport.ts";
import { GsHttpRuntimeTransport } from "./http/GsHttpRuntimeTransport.ts";
import { GsWsTransport } from "./ws/GsWsTransport.ts";

export function createTransport(config: GameInitConfig): IGameTransport {
  if (config.mode === "WS" || config.mode === "WS_LEGACY") {
    return new GsWsTransport();
  }

  if (
    config.mode === "GS_HTTP_RUNTIME" ||
    config.mode === "HTTP_GS" ||
    config.mode === "EXTGAME"
  ) {
    return new GsHttpRuntimeTransport();
  }

  throw new Error(`Unsupported transport mode requested: ${config.mode}`);
}

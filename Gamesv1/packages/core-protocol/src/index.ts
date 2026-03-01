export * from "./IGameTransport";
export * from "./SpinProfiling";
export * from "./schemas";
export * from "./http/GsHttpRuntimeTransport";

import { IGameTransport, GameInitConfig } from "./IGameTransport";
import { GsHttpRuntimeTransport } from "./http/GsHttpRuntimeTransport";
import { GsWsTransport } from "./ws/GsWsTransport";

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

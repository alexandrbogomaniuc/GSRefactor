export * from "./IGameTransport";
export * from "./SpinProfiling";
export * from "./schemas";

import { IGameTransport, GameInitConfig } from "./IGameTransport";
import { ExtGameTransport } from "./http/ExtGameTransport";
import { GsWsTransport } from "./ws/GsWsTransport";

export function createTransport(config: GameInitConfig): IGameTransport {
  if (config.mode === "WS") {
    return new GsWsTransport();
  }

  if (config.mode === "EXTGAME") {
    return new ExtGameTransport();
  }

  throw new Error(`Unsupported transport mode requested: ${config.mode}`);
}

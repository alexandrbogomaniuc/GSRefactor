import { IGameTransport, GameInitConfig } from './IGameTransport';
import { GsWsTransport } from './ws/GsWsTransport';
import { ExtGameTransport } from './http/ExtGameTransport';

export * from './IGameTransport';

/**
 * Factory function strictly bridging upper application logic matching into either Abs v1 WS or Extgame HTTP.
 */
export function createTransport(config: GameInitConfig): IGameTransport {
    if (config.mode === 'WS') {
        return new GsWsTransport();
    } else if (config.mode === 'EXTGAME') {
        return new ExtGameTransport();
    }

    throw new Error(`Unsupported transport mode requested: ${config.mode}`);
}

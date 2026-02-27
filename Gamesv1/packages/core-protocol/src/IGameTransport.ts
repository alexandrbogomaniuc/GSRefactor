export type TransportEvent = 'ready' | 'balance' | 'error' | 'disconnect';

export interface GameInitConfig {
    token: string;
    gameId: string;
    bankId?: string;
    sessionId?: string;
    baseUrl: string; // ws:// or http:// depending on mode
    mode: "WS" | "EXTGAME";
}

export interface IGameTransport {
    /** Connects to the server, authenticates, and initializes the game state. */
    connect(config: GameInitConfig): Promise<void>;

    /** Terminates the active connection. */
    disconnect(): void;

    /** 
     * Initiates a deterministic spin.
     * @param betAmount The value to wager.
     * @param operationId A unique UUID for financial idempotency.
     */
    spin(betAmount: number, operationId: string): Promise<any>;

    /**
     * Settles the spin/round with the origin backend so balance changes are physically recognized.
     * @param operationId The exact same UUID used during the initial spin request.
     */
    settle(operationId: string): Promise<any>;

    /** Registers an event listener for normalized engine callbacks (e.g. balance updates) */
    on(event: TransportEvent, callback: (...args: any[]) => void): void;
}

import { IGameTransport, GameInitConfig, TransportEvent } from '../IGameTransport';

export class ExtGameTransport implements IGameTransport {
    private config!: GameInitConfig;
    private listeners: Map<string, Function[]> = new Map();

    // ExtGame relies heavily on tracking the literal JSON blob from the server
    private gameState: any = null;
    private lastAction: string = "Enter";

    public async connect(config: GameInitConfig): Promise<void> {
        this.config = config;

        try {
            const response = await fetch(`${this.config.baseUrl}/Enter`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.config.token}`
                },
                body: JSON.stringify({ gameId: this.config.gameId })
            });

            if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);

            const data = await response.json();

            // Critical ExtGame architecture: We MUST save the server's tracking state
            if (data.gameState) this.gameState = data.gameState;
            this.lastAction = data.lastAction || "Enter";

            if (data.balance !== undefined) {
                this.emit('balance', data.balance);
            }

            this.emit('ready', data);
        } catch (e) {
            this.emit('error', e);
            throw e;
        }
    }

    public disconnect(): void {
        this.emit('disconnect');
    }

    public async spin(betAmount: number, operationId: string): Promise<any> {
        return this.processTransaction("spin", betAmount, operationId);
    }

    public async settle(operationId: string): Promise<any> {
        return this.processTransaction("settle", 0, operationId);
    }

    private async processTransaction(action: string, betAmount: number, operationId: string) {
        try {
            const response = await fetch(`${this.config.baseUrl}/processTransaction`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.config.token}`
                },
                body: JSON.stringify({
                    operationId: operationId,
                    action: action,
                    betAmount: betAmount,
                    lastAction: this.lastAction,     // Strict ECHO
                    gameState: this.gameState        // Strict ECHO
                })
            });

            if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);

            const data = await response.json();

            // Overwrite state sequentially
            if (data.gameState) this.gameState = data.gameState;
            this.lastAction = action;

            if (data.balance !== undefined) {
                this.emit('balance', data.balance);
            }

            // Built-in End Round Hook Detection
            if (data.endRoundSignature || data.roundFinishedHelper) {
                // Allows upper game logics to safely conclude visually mapped evaluations
            }

            return data;
        } catch (e) {
            this.emit('error', e);
            throw e;
        }
    }

    public on(event: TransportEvent, callback: (...args: any[]) => void): void {
        if (!this.listeners.has(event)) this.listeners.set(event, []);
        this.listeners.get(event)?.push(callback);
    }

    private emit(event: TransportEvent, ...args: any[]): void {
        this.listeners.get(event)?.forEach(cb => cb(...args));
    }
}

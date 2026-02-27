import { GSWebSocketClient } from '../network/GSWebSocketClient';
import { v4 as uuidv4 } from 'uuid';

export type SlotState = 'INIT' | 'READY' | 'RESERVED' | 'EVALUATING' | 'SETTLED';

export interface SpinConfig {
    betAmount: number;
}

export class SlotEngine {
    private client: GSWebSocketClient;
    private _state: SlotState = 'INIT';
    private currentOperationId: string | null = null;

    // View Integration Hooks
    public onStateChange: (newState: SlotState) => void = () => { };
    public onBalanceUpdate: (newBalance: number) => void = () => { };
    public onFreeRoundsUpdate: (remaining: number) => void = () => { };
    public onSpinResult: (winAmount: number, grid: number[][]) => void = () => { };
    public onRoundComplete: () => void = () => { };
    public onError: (errorMsg: string) => void = () => { };

    constructor(networkClient: GSWebSocketClient) {
        this.client = networkClient;
        this.bindNetworkEvents();
    }

    public get state(): SlotState {
        return this._state;
    }

    private setState(newState: SlotState) {
        console.log(`[SlotEngine] State Transition: ${this._state} -> ${newState}`);
        this._state = newState;
        this.onStateChange(newState);
    }

    private bindNetworkEvents() {
        this.client.onReady = () => {
            this.setState('READY');
        };

        this.client.onDisconnect = () => {
            // Strictly handle drops. If we were spinning, state stays RESERVED.
            // On reconnect, we need a SESSION_SYNC.
            if (this._state !== 'RESERVED') {
                this.setState('INIT');
            }
            // A real implementation would trigger an automatic reconnect loop here.
            console.warn("[SlotEngine] Network disconnected!");
        };

        this.client.onMessage = (type, payload) => {
            switch (type) {
                case 'BET_ACCEPTED':
                    if (this._state !== 'RESERVED') {
                        console.warn("[SlotEngine] Received BET_ACCEPTED but not in RESERVED state. Ignoring.");
                        return;
                    }
                    this.handleBetAccepted(payload);
                    break;
                case 'BET_REJECTED':
                    this.handleBetRejected(payload);
                    break;
                case 'SETTLE_ACCEPTED':
                    if (this._state !== 'EVALUATING') {
                        console.warn("[SlotEngine] Received SETTLE_ACCEPTED but not in EVALUATING state.");
                    }
                    this.setState('SETTLED');
                    break;
                case 'BALANCE_SNAPSHOT':
                    this.onBalanceUpdate(payload.balance);
                    if (payload.freeRoundsRemaining !== undefined) {
                        this.onFreeRoundsUpdate(payload.freeRoundsRemaining);
                    }
                    // After settling and getting balance, game is fully reset.
                    if (this._state === 'SETTLED') {
                        this.setState('READY');
                        this.onRoundComplete();
                    }
                    break;
                case 'SESSION_SYNC':
                    if (payload.freeRoundsRemaining !== undefined) {
                        this.onFreeRoundsUpdate(payload.freeRoundsRemaining);
                    }
                    this.handleSessionSync(payload);
                    break;
            }
        };
    }

    public play(config: SpinConfig) {
        if (this._state !== 'READY' && this._state !== 'SETTLED') {
            const err = "Cannot Spin: Game is not READY.";
            console.error(`[SlotEngine] ${err}`);
            this.onError(err);
            return;
        }

        // CRITICAL: Financial Idempotency
        // Generate a new unique operationId for this exact spin intent.
        this.currentOperationId = uuidv4();
        this.setState('RESERVED');

        console.log(`[SlotEngine] Initiating Spin (opId: ${this.currentOperationId}, bet: ${config.betAmount})`);
        this.client.sendMessage('BET_REQUEST', { betAmount: config.betAmount }, this.currentOperationId);
    }

    private handleBetAccepted(payload: any) {
        console.log("[SlotEngine] Spin Result Confirmed by Orchestrator.");
        this.setState('EVALUATING');

        // Pass the deterministic outcome to the View renderer
        this.onSpinResult(payload.totalWin, payload.resultGrid);
    }

    private handleBetRejected(payload: any) {
        console.error("[SlotEngine] Bet Rejected:", payload.reason);
        // Clear operation and revert to ready
        this.currentOperationId = null;
        this.setState('READY');
        this.onError(`Bet Rejected: ${payload.reason}`);
    }

    // Called by the View layer when all visual animations and win celebrations are complete
    public animationsComplete() {
        if (this._state !== 'EVALUATING') {
            console.warn("[SlotEngine] Warning: Visuals completed but Engine is not EVALUATING.");
            return;
        }

        if (!this.currentOperationId) {
            console.error("[SlotEngine] Fatal: Lost operationId during EVALUATING state!");
            return;
        }

        console.log(`[SlotEngine] Visuals done. Sending Settle (opId: ${this.currentOperationId})`);

        // Settle uses the EXACT SAME operationId as the Bet.
        this.client.sendMessage('SETTLE_REQUEST', {}, this.currentOperationId);
    }

    private handleSessionSync(payload: any) {
        console.log("[SlotEngine] Resyncing state after network recovery...", payload);
        // E.g., if reconnecting and find out the server already settled the spin:
        if (payload.lastKnownState === 'SETTLED') {
            this.setState('READY');
            this.onBalanceUpdate(payload.balance);
        } else if (payload.lastKnownState === 'RESERVED') {
            // Re-apply the deterministic result as if BET_ACCEPTED just fired
            this.handleBetAccepted(payload.gameResult);
        }
    }
}

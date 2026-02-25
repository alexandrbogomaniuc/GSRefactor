import { GSWebSocketClient } from '../network/GSWebSocketClient';
import { v4 as uuidv4 } from 'uuid';

export type SlotState = 'INIT' | 'IDLE' | 'SPINNING' | 'REEL_RESOLVE' | 'MINING_PHASE' | 'PAYOUT' | 'SETTLED';

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

    // The new 2-layer payload from the server script
    public onSpinResult: (winAmount: number, slotResult: number[][], miningScript: any) => void = () => { };

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
        if (this._state === newState) return;
        console.log(`[SlotEngine] State Transition: ${this._state} -> ${newState}`);
        this._state = newState;
        this.onStateChange(newState);
    }

    private bindNetworkEvents() {
        this.client.onReady = () => {
            this.setState('IDLE');
        };

        this.client.onDisconnect = () => {
            if (this._state !== 'SPINNING' && this._state !== 'REEL_RESOLVE' && this._state !== 'MINING_PHASE') {
                this.setState('INIT');
            }
            console.warn("[SlotEngine] Network disconnected!");
        };

        this.client.onMessage = (type, payload) => {
            switch (type) {
                case 'BET_ACCEPTED':
                    if (this._state !== 'SPINNING') {
                        console.warn("[SlotEngine] Received BET_ACCEPTED but not in SPINNING state. Ignoring.");
                        return;
                    }
                    this.handleBetAccepted(payload);
                    break;
                case 'BET_REJECTED':
                    this.handleBetRejected(payload);
                    break;
                case 'SETTLE_ACCEPTED':
                    if (this._state !== 'PAYOUT') {
                        console.warn("[SlotEngine] Received SETTLE_ACCEPTED but not in PAYOUT state.");
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
                        this.setState('IDLE');
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
        if (this._state !== 'IDLE' && this._state !== 'SETTLED') {
            const err = `Cannot Spin: Game is not IDLE. Current state: ${this._state}`;
            console.error(`[SlotEngine] ${err}`);
            this.onError(err);
            return;
        }

        this.currentOperationId = uuidv4();
        this.setState('SPINNING');

        console.log(`[SlotEngine] Initiating Spin (opId: ${this.currentOperationId}, bet: ${config.betAmount})`);
        this.client.sendMessage('BET_REQUEST', { betAmount: config.betAmount }, this.currentOperationId);
    }

    private handleBetAccepted(payload: any) {
        console.log("[SlotEngine] Spin Result Confirmed by Orchestrator.");
        this.setState('REEL_RESOLVE');

        // Pass the deterministic outcome to the View renderer
        // payload should now contain { totalWin, slotResult, miningScript }
        this.onSpinResult(payload.totalWin, payload.slotResult, payload.miningScript);
    }

    private handleBetRejected(payload: any) {
        console.error("[SlotEngine] Bet Rejected:", payload.reason);
        this.currentOperationId = null;
        this.setState('IDLE');
        this.onError(`Bet Rejected: ${payload.reason}`);
    }

    /**
     * UI calls this when the Top 5x3 Reels have stopped spinning.
     * Transitions state to MINING_PHASE.
     */
    public reelsResolved() {
        if (this._state === 'REEL_RESOLVE') {
            setTimeout(() => {
                this.setState('MINING_PHASE');
            }, 500);
        }
    }

    /**
     * UI calls this when the entire async Mining queue (drops, hits, gravity, chests) is totally finished.
     */
    public miningComplete() {
        if (this._state === 'MINING_PHASE') {
            this.setState('PAYOUT');

            if (!this.currentOperationId) {
                console.error("[SlotEngine] Fatal: Lost operationId during PAYOUT state!");
                return;
            }

            console.log(`[SlotEngine] Mining visuals done. Sending Settle (opId: ${this.currentOperationId})`);
            this.client.sendMessage('SETTLE_REQUEST', {}, this.currentOperationId);
        }
    }

    private handleSessionSync(payload: any) {
        console.log("[SlotEngine] Resyncing state after network recovery...", payload);
        if (payload.lastKnownState === 'SETTLED') {
            this.setState('IDLE');
            this.onBalanceUpdate(payload.balance);
        } else if (payload.lastKnownState === 'RESERVED' || payload.lastKnownState === 'SPINNING') {
            this.handleBetAccepted(payload.gameResult);
        }
    }
}

/**
 * Strict enumeration of the Slot Round Lifecycle stages.
 */
export enum RoundState {
    IDLE = 'IDLE',                      // Initial state, no session
    ENTERING = 'ENTERING',              // Handshake / Enter request in flight
    READY = 'READY',                    // Session active, awaiting player bet
    SPINNING = 'SPINNING',              // Bet sent, awaiting math result from server
    AWAITING_RESULT = 'AWAITING_RESULT', // Data received, but min-spin-time animations still running
    PRESENTING_WIN = 'PRESENTING_WIN',   // Visualizing wins / celebrate
    IN_FEATURE = 'IN_FEATURE',           // Free spins or Bonus game active
    ROUND_END = 'ROUND_END'              // Round concluding, cleaning up state
}

export interface RoundStateContext {
    state: RoundState;
    lastState: RoundState;
    betAmount: number;
    totalWin: number;
    isTurbo: boolean;
    operationId: string | null;
}

/**
 * RoundStateMachine coordinates the interaction between the network transport,
 * compliance rules (timings), and operator integrations (postMessage).
 */
export class RoundStateMachine {
    private current: RoundState = RoundState.IDLE;
    private context: RoundStateContext = {
        state: RoundState.IDLE,
        lastState: RoundState.IDLE,
        betAmount: 0,
        totalWin: 0,
        isTurbo: false,
        operationId: null
    };

    private onRoundEndedHook: (() => void) | null = null;

    // Callbacks for the visual engine
    public onStateTransition: (from: RoundState, to: RoundState) => void = () => { };

    constructor(onRoundEndedHook: (() => void) | null = null) {
        this.onRoundEndedHook = onRoundEndedHook;
    }

    public get state(): RoundState {
        return this.current;
    }

    /**
     * Validates and executes a state transition.
     * Guaranteed to follow the mandated lifecycle sequence.
     */
    public transition(to: RoundState): void {
        if (this.current === to) return;

        // Strict guard: Verify transition is legal
        if (!this.isTransitionLegal(this.current, to)) {
            console.error(`[RoundStateMachine] ILLEGAL TRANSITION: ${this.current} -> ${to}`);
            return;
        }

        const from = this.current;
        this.current = to;
        this.context.lastState = from;
        this.context.state = to;

        console.log(`[RoundStateMachine] ⚙️ ${from} -> ${to}`);

        this.handleStateEntry(to);
        this.onStateTransition(from, to);
    }

    private isTransitionLegal(from: RoundState, to: RoundState): boolean {
        const allowed: Record<RoundState, RoundState[]> = {
            [RoundState.IDLE]: [RoundState.ENTERING],
            [RoundState.ENTERING]: [RoundState.READY, RoundState.IDLE],
            [RoundState.READY]: [RoundState.SPINNING, RoundState.IDLE],
            [RoundState.SPINNING]: [RoundState.AWAITING_RESULT, RoundState.PRESENTING_WIN, RoundState.IDLE],
            [RoundState.AWAITING_RESULT]: [RoundState.PRESENTING_WIN, RoundState.IN_FEATURE, RoundState.ROUND_END],
            [RoundState.PRESENTING_WIN]: [RoundState.IN_FEATURE, RoundState.ROUND_END, RoundState.READY],
            [RoundState.IN_FEATURE]: [RoundState.PRESENTING_WIN, RoundState.ROUND_END],
            [RoundState.ROUND_END]: [RoundState.READY, RoundState.IDLE]
        };

        return allowed[from]?.includes(to) || false;
    }

    private handleStateEntry(state: RoundState): void {
        switch (state) {
            case RoundState.ROUND_END:
                // Round-end signal hook for integration layers outside core runtime state.
                this.onRoundEndedHook?.();
                break;
            case RoundState.READY:
                // Reset per-round context
                this.context.totalWin = 0;
                break;
        }
    }

    /**
     * Triggered when the LAST server response for the round is received.
     */
    public onFinalServerResponse(): void {
        // No-op in canonical runtime path; hook retained for optional integration layers.
    }

    public setTurbo(enabled: boolean): void {
        this.context.isTurbo = enabled;
    }

    public updateContext(patch: Partial<RoundStateContext>): void {
        this.context = { ...this.context, ...patch };
    }
}

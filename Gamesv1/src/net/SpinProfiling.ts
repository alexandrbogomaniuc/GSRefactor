export interface SpinStats {
    SPINREQTIME: number;
    CMD: string;
    SPINANMTIME: number;
}

export class SpinProfiler {
    private enabled: boolean;
    private lastStats: SpinStats | null = null;

    private reqStart: number = 0;
    private reqEnd: number = 0;
    private animStart: number = 0;

    constructor(enabled: boolean = false) {
        this.enabled = enabled;
    }

    public setEnabled(enabled: boolean): void {
        this.enabled = enabled;
    }

    /** Called exactly when the network payload is built and sent */
    public markRequestStart(): void {
        if (!this.enabled) return;
        this.reqStart = Date.now();
    }

    /** Called exactly when the network response is received and evaluated */
    public markRequestEnd(): void {
        if (!this.enabled) return;
        this.reqEnd = Date.now();
    }

    /** Called exactly when the reels start physically spinning on-screen */
    public markAnimationStart(): void {
        if (!this.enabled) return;
        this.animStart = Date.now();
    }

    /** 
     * Called exactly when the final visual win celebrations finish 
     * before the client returns to the Idle/Ready state 
     */
    public markAnimationEnd(commandName: string = 'PLACEBET'): void {
        if (!this.enabled) return;

        const animEnd = Date.now();

        this.lastStats = {
            SPINREQTIME: Math.max(0, this.reqEnd - this.reqStart),
            CMD: commandName,
            SPINANMTIME: Math.max(0, animEnd - this.animStart)
        };
    }

    /** 
     * Injects the PRECSPINSTAT object into an outgoing payload, effectively reporting 
     * the performance profile of the PREVIOUS spin on the CURRENT spin request.
     */
    public applyToPayload(payload: any): any {
        if (!this.enabled || !this.lastStats) {
            return payload; // Return untouched if disabled or no historical data
        }

        return {
            ...payload,
            PRECSPINSTAT: { ...this.lastStats }
        };
    }

    /** Useful when cleaning up state on hard disconnects or exits */
    public clear(): void {
        this.lastStats = null;
    }
}

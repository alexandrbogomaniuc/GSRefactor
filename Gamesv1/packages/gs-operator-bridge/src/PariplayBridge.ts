export type OutgoingPariplayMessage =
    | 'onAppFrameReady'
    | 'currencyStatus'
    | 'gameDataLoaded'
    | 'gameReady'
    | 'roundStart'
    | 'roundStarted'
    | 'ticketReceived'
    | 'roundEnded'
    | 'balance'
    | 'help'
    | 'history'
    | 'quit'
    | 'lobby'
    | 'cashier';

export interface PariplayConfig {
    enabled: boolean;
}

export class PariplayBridge {
    private enabled: boolean;

    // Public hooks for the Game Engine / Tween Controllers to bind to
    public onPauseRequested: () => void = () => { };
    public onResumeRequested: () => void = () => { };
    public onStopAutoBetRequested: () => void = () => { };

    constructor(config: PariplayConfig) {
        this.enabled = config.enabled;
        this.bindIncomingEvents();
    }

    /**
     * Helper to determine if we run in a framed environment
     */
    private isEmbedded(): boolean {
        if (typeof window === 'undefined') return false;
        return window.parent !== window;
    }

    /**
     * Emits a normalized message to the operator iFrame
     */
    public send(action: OutgoingPariplayMessage, payload?: any): void {
        if (!this.enabled || !this.isEmbedded()) return;

        // Pariplay often expects strings for simple commands, or objects with an 'action'
        const message = payload ? { action, ...payload } : action;

        try {
            window.parent.postMessage(message, '*');
            console.debug(`[Pariplay Bridge] Sent:`, message);
        } catch (e) {
            console.error(`[Pariplay Bridge] Failed to postMessage:`, e);
        }
    }

    private bindIncomingEvents() {
        if (typeof window === 'undefined') return;

        window.addEventListener('message', (event: MessageEvent) => {
            if (!this.enabled) return;

            const data = event.data;
            if (!data) return;

            // Handle Pariplay string literals
            const msgType = typeof data === 'string' ? data : data.action || data.type;

            switch (msgType) {
                case 'pause_pp':
                    console.log("[Pariplay Bridge] 🛑 Received 'pause_pp'. Yielding visual execution.");
                    this.onPauseRequested();
                    break;
                case 'resume_pp':
                    console.log("[Pariplay Bridge] ▶️ Received 'resume_pp'. Restoring visual execution.");
                    this.onResumeRequested();
                    break;
                case 'stopAutoBet_pp':
                    console.log("[Pariplay Bridge] ✋ Received 'stopAutoBet_pp'. Enqueueing autoplay exit.");
                    this.onStopAutoBetRequested();
                    break;
                default:
                    // Ignore unknown operator frames (e.g. injected third-party extensions)
                    break;
            }
        });
    }
}

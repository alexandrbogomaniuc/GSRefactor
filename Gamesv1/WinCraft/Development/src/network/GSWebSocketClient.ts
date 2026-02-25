import { v4 as uuidv4 } from 'uuid';

export interface GSConfig {
    wssUrl: string;
    authToken: string;
    sessionId: string;
    bankId: string;
    gameId: string;
}

export interface GSMessageEnvelope {
    version: string;
    type: string;
    traceId: string;
    sessionId: string;
    bankId: string;
    gameId: string;
    operationId: string;
    timestamp: string;
    seq: number;
    payload: any;
}

export class GSWebSocketClient {
    private ws: WebSocket | null = null;
    private config: GSConfig;
    private currentSeq: number = 0;

    // Callbacks for the game engine
    public onReady: () => void = () => { };
    public onMessage: (type: string, payload: any) => void = () => { };
    public onError: (err: any) => void = () => { };
    public onDisconnect: () => void = () => { };

    constructor(config: GSConfig) {
        this.config = config;
    }

    public connect(): void {
        console.log(`[GS Network] Connecting to ${this.config.wssUrl} with subprotocol abs.gs.v1...`);
        // The Abs v1 protocol explicitly requires the subprotocol for version negotiation
        this.ws = new WebSocket(this.config.wssUrl, "abs.gs.v1");

        this.ws.onopen = this.handleOpen.bind(this);
        this.ws.onmessage = this.handleMessage.bind(this);
        this.ws.onerror = this.handleError.bind(this);
        this.ws.onclose = this.handleClose.bind(this);
    }

    private handleOpen(): void {
        console.log("[GS Network] Connected. Sending Authentication Handshake (GAME_READY)...");
        this.sendMessage("GAME_READY", { token: this.config.authToken });
    }

    private handleMessage(event: MessageEvent): void {
        try {
            const envelope = JSON.parse(event.data) as GSMessageEnvelope;

            // Handle Heartbeat internally
            if (envelope.type === "PING") {
                this.sendMessage("PONG", {});
                return;
            }

            if (envelope.type === "SESSION_ACCEPTED") {
                console.log("[GS Network] Session accepted by GS Orchestrator.");
                this.onReady();
                return;
            }

            if (envelope.type === "ERROR") {
                console.error("[GS Network] Received Terminal GS Error:", envelope.payload);
                this.onError(envelope.payload);
                return;
            }

            // Route standard game messages to the engine (BET_ACCEPTED, SETTLE_ACCEPTED, BALANCE_SNAPSHOT)
            this.onMessage(envelope.type, envelope.payload);

        } catch (e) {
            console.error("[GS Network] Expected JSON envelope, failed to parse payload:", event.data);
            this.onError("INVALID_PAYLOAD_SCHEMA");
        }
    }

    private handleError(error: Event): void {
        console.error("[GS Network] WebSocket Transport Error:", error);
        this.onError("TRANSPORT_ERROR");
    }

    private handleClose(event: CloseEvent): void {
        console.warn(`[GS Network] Disconnected (Code: ${event.code}, Clean: ${event.wasClean})`);
        this.ws = null;
        this.onDisconnect();
    }

    /**
     * Packages and sends a strict Canonical Message Envelope as required by the PRD and Abs v1 spec.
     */
    public sendMessage(type: string, payload: any = {}, operationId: string = ""): void {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.error(`[GS Network] Cannot send ${type}, socket is not OPEN.`);
            return;
        }

        this.currentSeq++;

        const envelope: GSMessageEnvelope = {
            version: "1.0",
            type: type,
            traceId: uuidv4(), // Unique per network hop
            sessionId: this.config.sessionId,
            bankId: this.config.bankId,
            gameId: this.config.gameId,
            operationId: operationId, // Crucial for Financial Idempotency
            timestamp: new Date().toISOString(),
            seq: this.currentSeq,
            payload: payload
        };

        this.ws.send(JSON.stringify(envelope));
    }

    public disconnect(): void {
        if (this.ws) {
            this.ws.close();
        }
    }
}

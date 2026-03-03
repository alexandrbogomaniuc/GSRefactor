import {
    type BootstrapRequest,
    type BootstrapResponse,
    type CloseGameRequest,
    type CloseGameResponse,
    type FeatureActionRequest,
    type FeatureActionResponse,
    type GameInitConfig,
    type HistoryQuery,
    type HistoryRequest,
    type HistoryResponse,
    type LegacyGameTransportAdapter,
    type OpenGameRequest,
    type OpenGameResponse,
  type PlayRoundRequest,
  type PlayRoundResponse,
    type ResumeGameRequest,
    type ResumeGameResponse,
    type TransportEvent,
} from "../IGameTransport.ts";

// Legacy/experimental abs.gs.v1 transport only.
// Not canonical for slot-browser-v1 browser runtime.

// A lightweight browser-compatible UUID generation mock
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

const emptyEnvelope = (
    sessionId: string,
    requestCounter: number,
    stateVersion: number,
): OpenGameResponse => ({
    ok: true,
    requestId: `ws-${Date.now()}`,
    sessionId,
    requestCounter,
    stateVersion,
    wallet: {
        balanceMinor: 0,
        previousBalanceMinor: 0,
        currencyCode: "EUR",
        truncateCents: false,
        delayedWalletMessagePending: false,
    },
    round: {
        roundId: null,
        status: "NONE",
        betMinor: 0,
        winMinor: 0,
        netEffectMinor: 0,
        outcomeHash: "ws-legacy",
    },
    feature: {
        mode: "NONE",
        remainingActions: 0,
        nextAllowedActions: [],
        featureContext: {},
    },
    presentationPayload: {
        featureMode: "NONE",
        reelStops: [],
        symbolGrid: [],
        uiMessages: [],
        animationCues: [],
        audioCues: [],
        counters: [],
        labels: {},
    },
    restore: {
        hasUnfinishedRound: false,
        unfinishedRoundId: null,
        resumeStateVersion: stateVersion,
        opaqueRestorePayload: null,
    },
    idempotency: {
        isDuplicate: false,
        duplicateOfRequestId: null,
        replaySafe: true,
    },
    retry: {
        clientMayRetrySameKey: false,
        clientMustIncrementCounterOnNewAction: true,
    },
});

export class GsWsTransport implements LegacyGameTransportAdapter {
    private ws: WebSocket | null = null;
    private config!: GameInitConfig;
    private seq: number = 0;
    private listeners: Map<string, Function[]> = new Map();
    private pendingRequests: Map<string, { resolve: Function, reject: Function }> = new Map();

    public async connect(config: GameInitConfig): Promise<void> {
        this.config = config;
        return new Promise((resolve, reject) => {
            if (typeof WebSocket === 'undefined') {
                return reject(new Error("WebSocket not supported in this environment"));
            }

            this.ws = new WebSocket(config.baseUrl, "abs.gs.v1");

            this.ws.onopen = () => {
                this.sendEnvelope("GAME_READY", { token: config.token });
            };

            this.ws.onmessage = (event) => {
                try {
                    const env = JSON.parse(event.data);
                    this.handleMessage(env);

                    if (env.type === 'SESSION_ACCEPTED' || env.type === 'SESSION_SYNC') {
                        resolve();
                    }
                } catch (e) {
                    this.emit('error', 'INVALID_PAYLOAD_FORMAT');
                }
            };

            this.ws.onerror = (e) => {
                this.emit('error', 'WS_TRANSPORT_ERROR');
                reject(e);
            };

            this.ws.onclose = () => {
                this.ws = null;
                this.emit('disconnect');
            };
        });
    }

    public disconnect(): void {
        this.ws?.close();
    }

    public async spin(betAmount: number, operationId: string): Promise<any> {
        return new Promise((resolve, reject) => {
            this.pendingRequests.set(operationId, { resolve, reject });
            this.sendEnvelope("BET_REQUEST", { betAmount }, operationId);
        });
    }

    public async settle(operationId: string): Promise<any> {
        return new Promise((resolve, reject) => {
            this.pendingRequests.set(`settle_${operationId}`, { resolve, reject });
            this.sendEnvelope("SETTLE_REQUEST", {}, operationId);
        });
    }

    public on(event: TransportEvent, callback: (...args: any[]) => void): void {
        if (!this.listeners.has(event)) this.listeners.set(event, []);
        this.listeners.get(event)?.push(callback);
    }

    private emit(event: TransportEvent, ...args: any[]): void {
        this.listeners.get(event)?.forEach(cb => cb(...args));
    }

    private sendEnvelope(type: string, payload: any, operationId: string = ""): void {
        if (!this.ws || this.ws.readyState !== 1) return; // 1 = OPEN

        this.seq++;
        const envelope = {
            version: "1.0",
            type: type,
            traceId: uuidv4(),
            sessionId: this.config.sessionId || "",
            bankId: this.config.bankId || "",
            gameId: this.config.gameId || "",
            operationId: operationId,
            timestamp: new Date().toISOString(),
            seq: this.seq,
            payload: payload
        };
        this.ws.send(JSON.stringify(envelope));
    }

    private handleMessage(env: any) {
        if (env.type === 'PING') {
            this.sendEnvelope('PONG', {});
            return;
        }

        if (env.type === 'BALANCE_SNAPSHOT') {
            this.emit('balance', env.payload.balance);
            return;
        }

        if (env.type === 'SESSION_ACCEPTED' || env.type === 'SESSION_SYNC') {
            if (env.payload?.balance !== undefined) this.emit('balance', env.payload.balance);
            this.emit('ready', env.payload);
            return;
        }

        if (env.type === 'BET_ACCEPTED') {
            const req = this.pendingRequests.get(env.operationId);
            if (req) {
                req.resolve(env.payload);
                this.pendingRequests.delete(env.operationId);
            }
            return;
        }

        if (env.type === 'BET_REJECTED' || env.type === 'ERROR') {
            const req = this.pendingRequests.get(env.operationId);
            if (req) {
                req.reject(env.payload);
                this.pendingRequests.delete(env.operationId);
            } else {
                this.emit('error', env.payload);
            }
            return;
        }

        if (env.type === 'SETTLE_ACCEPTED') {
            const req = this.pendingRequests.get(`settle_${env.operationId}`);
            if (req) {
                req.resolve(env.payload);
                this.pendingRequests.delete(`settle_${env.operationId}`);
            }
            return;
        }
    }

    public async bootstrap(config: GameInitConfig, request: BootstrapRequest): Promise<BootstrapResponse> {
        await this.connect({
            ...config,
            sessionId: request.sessionId ?? config.sessionId,
        });

        return {
            contractVersion: "slot-bootstrap-v1",
            session: {
                sessionId: this.config.sessionId ?? "",
                requestCounter: this.seq,
                stateVersion: this.seq,
            },
            context: {
                gameId: this.config.gameId,
                bankId: this.config.bankId,
                playerId: this.config.playerId ?? null,
                language: this.config.language ?? "en",
            },
            assets: {
                assetBaseUrl: this.config.baseUrl,
                clientVersion: "legacy-ws",
                clientPackageVersion: "legacy-ws",
                assetBundleHash: "legacy-ws",
            },
            runtime: {
                mathPackageVersion: String(request?.bootstrapRef?.mathPackageVersion ?? "legacy"),
                rtpModelId: "legacy",
                engineContractVersion: "slot-runtime-v1",
            },
            policies: {},
            integrity: {
                configIssuedAtUtc: new Date().toISOString(),
                configId: request.bootstrapRef.configId,
                configHash: "legacy-ws",
            },
        };
    }

    public async opengame(request: OpenGameRequest): Promise<OpenGameResponse> {
        if (!this.config) {
            throw new Error("WS legacy transport requires bootstrap/connect before openGame.");
        }

        await this.connect({
            ...this.config,
            sessionId: request.sessionId,
            gameId: String(this.config.gameId),
        });

        return emptyEnvelope(request.sessionId, this.seq, this.seq);
    }

    public async playround(request: PlayRoundRequest): Promise<PlayRoundResponse> {
        const operationId = request.clientOperationId ?? uuidv4();
        const bet = await this.spin(request.selectedBet.totalBetMinor, operationId);
        const collect = await this.settle(operationId);
        const response = emptyEnvelope(request.sessionId, this.seq, request.currentStateVersion);
        response.wallet.balanceMinor = Number(collect?.balance ?? bet?.balance ?? 0);
        response.round = {
            roundId: String(bet?.roundId ?? null),
            status: "FINAL",
            betMinor: request.selectedBet.totalBetMinor,
            winMinor: Number(bet?.totalWin ?? 0),
            netEffectMinor: Number(bet?.totalWin ?? 0) - request.selectedBet.totalBetMinor,
            outcomeHash: "ws-legacy",
        };
        response.presentationPayload = {
            ...response.presentationPayload,
            labels: { source: "ws-legacy", ...(bet ?? {}) },
        };
        response.idempotency = {
            isDuplicate: false,
            duplicateOfRequestId: null,
            replaySafe: true,
        };
        return response;
    }

    public async featureaction(_request: FeatureActionRequest): Promise<FeatureActionResponse> {
        throw new Error("WS legacy transport does not implement featureAction.");
    }

    public async resumegame(request: ResumeGameRequest): Promise<ResumeGameResponse> {
        return this.opengame({
            contractVersion: request?.contractVersion ?? "slot-browser-v1",
            sessionId: request?.sessionId ?? this.config?.sessionId ?? "",
            requestCounter: request?.requestCounter ?? this.seq + 1,
            currentStateVersion: request?.currentStateVersion ?? this.seq,
            clientOperationId: request?.clientOperationId ?? `ws-resume-${Date.now()}`,
            idempotencyKey: request?.idempotencyKey ?? `ws-resume-${Date.now()}`,
            bootstrapRef: request?.bootstrapRef ?? {
                configId: "legacy",
                clientPackageVersion: "legacy",
                mathPackageVersion: "legacy",
            },
            selectedBet: null,
            selectedFeatureChoice: null,
        });
    }

    public async closegame(request: CloseGameRequest): Promise<CloseGameResponse> {
        this.disconnect();
        const response = emptyEnvelope(
            request.sessionId,
            request.requestCounter,
            request.currentStateVersion,
        );
        response.idempotency = {
            isDuplicate: false,
            duplicateOfRequestId: null,
            replaySafe: true,
        };
        return response;
    }

    public async gethistory(request: HistoryRequest): Promise<HistoryResponse> {
        const response = emptyEnvelope(request.sessionId, request.requestCounter, this.seq);
        response.feature = {
            mode: "HISTORY",
            remainingActions: 0,
            nextAllowedActions: [],
            featureContext: {
                history: [],
                historyQuery: request.historyQuery ?? ({
                    fromRoundId: null,
                    limit: 20,
                    includeFeatureDetails: true,
                } as HistoryQuery),
            },
        };
        return response;
    }

    // Transitional aliases for pre-refactor call sites.
    public async openGame(request: OpenGameRequest): Promise<OpenGameResponse> {
        return this.opengame(request);
    }

    public async playRound(request: PlayRoundRequest): Promise<PlayRoundResponse> {
        return this.playround(request);
    }

    public async featureAction(request: FeatureActionRequest): Promise<FeatureActionResponse> {
        return this.featureaction(request);
    }

    public async resumeGame(request: ResumeGameRequest): Promise<ResumeGameResponse> {
        return this.resumegame(request);
    }

    public async closeGame(request: CloseGameRequest): Promise<CloseGameResponse> {
        return this.closegame(request);
    }

    public async getHistory(request: HistoryRequest): Promise<HistoryResponse> {
        return this.gethistory(request);
    }
}

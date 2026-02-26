import { IGameTransport, GameInitConfig, TransportEvent } from '../IGameTransport';

// A lightweight browser-compatible UUID generation mock
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

export class GsWsTransport implements IGameTransport {
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
}

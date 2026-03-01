import { WebSocketServer, WebSocket } from 'ws';
import { randomUUID } from 'node:crypto';

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

export class GsMockServer {
    private wss: WebSocketServer;
    private serverSeq: number = 0;
    private processedOperations: Map<string, any> = new Map();

    constructor(port: number = 6001) {
        this.wss = new WebSocketServer({ port });
        this.setup();
        console.log(`🤖 [GS Mock Server] Listening on ws://localhost:${port}`);
    }

    private setup() {
        this.wss.on('connection', (ws: WebSocket) => {
            console.log('🔌 Client connected to Mock GS');

            ws.on('message', (data: Buffer) => {
                try {
                    const envelope = JSON.parse(data.toString()) as GSMessageEnvelope;
                    this.handleRequest(ws, envelope);
                } catch (e) {
                    console.error('❌ Failed to parse message:', data.toString());
                }
            });

            ws.on('close', () => console.log('🔌 Client disconnected'));
        });
    }

    private handleRequest(ws: WebSocket, env: GSMessageEnvelope) {
        this.serverSeq++;

        // Idempotency Check: If we already saw this operationId, return the cached result
        if (env.type === 'BET_REQUEST' && env.operationId && this.processedOperations.has(env.operationId)) {
            console.log(`♻️  [Idempotency] Resending cached result for opId: ${env.operationId}`);
            this.sendResponse(ws, env, "BET_ACCEPTED", this.processedOperations.get(env.operationId));
            return;
        }

        switch (env.type) {
            case 'GAME_READY':
                this.sendResponse(ws, env, "SESSION_ACCEPTED", {
                    balance: 1000.00,
                    currencyCode: "USD",
                    rcInterval: 3600
                });
                break;

            case 'BET_REQUEST':
                this.handleBet(ws, env);
                break;

            case 'SETTLE_REQUEST':
                this.sendResponse(ws, env, "SETTLE_ACCEPTED", {});
                break;

            case 'SYNC_REQUEST':
                this.sendResponse(ws, env, "SESSION_SYNC", { lastKnownState: "RESERVED" });
                break;

            case 'PING':
                this.sendResponse(ws, env, "PONG", {});
                break;
        }
    }

    private handleBet(ws: WebSocket, env: GSMessageEnvelope) {
        const payload = env.payload;

        // Scenario-based logic can be injected here. For now, basic mock:
        if (payload.betAmount > 5000) {
            this.sendResponse(ws, env, "BET_REJECTED", { reason: "INSUFFICIENT_FUNDS" });
            return;
        }

        const result = {
            totalBet: payload.betAmount,
            totalWin: Math.random() > 0.5 ? payload.betAmount * 5 : 0,
            resultGrid: [[1, 1, 1], [2, 2, 2], [3, 3, 3]]
        };

        if (env.operationId) {
            this.processedOperations.set(env.operationId, result);
        }

        this.sendResponse(ws, env, "BET_ACCEPTED", result);

        // Handle specific scenario for delayed balance
        if (env.operationId === 'op-delayed-balance') {
            setTimeout(() => {
                this.sendResponse(ws, env, "BALANCE", { balance: 999.00 });
            }, 100);
        }
    }

    private sendResponse(ws: WebSocket, env: GSMessageEnvelope, type: string, payload: any) {
        const response: GSMessageEnvelope = {
            version: "1.0",
            type: type,
            traceId: env.traceId || randomUUID(),
            sessionId: env.sessionId || "mock-session",
            bankId: env.bankId || "mock-bank",
            gameId: env.gameId || "mock-game",
            operationId: env.operationId || "",
            timestamp: new Date().toISOString(),
            seq: this.serverSeq,
            payload: payload
        };
        ws.send(JSON.stringify(response));
    }

    public close() {
        this.wss.close();
    }
}

// If run directly
if (process.argv[1].includes('GsMockServer')) {
    new GsMockServer();
}

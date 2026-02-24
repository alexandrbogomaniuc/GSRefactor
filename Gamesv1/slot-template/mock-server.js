import { WebSocketServer, WebSocket } from 'ws';
import { v4 as uuidv4 } from 'uuid';

const wss = new WebSocketServer({ port: 6001 });

console.log('🤖 [GS Mock Websocket Server] Running on ws://localhost:6001');



wss.on('connection', function connection(ws) {
    console.log('🔌 Client connected');
    let clientSeq = 0;

    ws.on('error', console.error);

    ws.on('message', function message(data) {
        try {
            const envelope = JSON.parse(data.toString());
            console.log(`📥 Received: [${envelope.type}] seq:${envelope.seq} opId:${envelope.operationId}`);
            clientSeq++;

            // Global generic response envelope wrapper
            const sendResponse = (type, payload) => {
                const response = {
                    version: "1.0",
                    type: type,
                    traceId: envelope.traceId, // reflect trace
                    sessionId: envelope.sessionId,
                    bankId: envelope.bankId,
                    gameId: envelope.gameId,
                    operationId: envelope.operationId,
                    timestamp: new Date().toISOString(),
                    seq: clientSeq,
                    payload: payload
                };
                console.log(`📤 Sending: [${type}]`);
                ws.send(JSON.stringify(response));
            };

            // Route standard abs.gs.v1 protocol messages
            if (envelope.type === 'GAME_READY') {
                sendResponse('SESSION_ACCEPTED', {
                    balance: 1000.50,
                    currencyCode: "USD",
                    rcInterval: 3600 // 1 hr reality check
                });

                // Immediately send an initial balance snapshot to populate the UI
                setTimeout(() => {
                    sendResponse('BALANCE_SNAPSHOT', {
                        balance: 1000.50,
                        currencyCode: "USD"
                    });
                }, 100);
            } else if (envelope.type === 'PONG') {
                // Ignore pong
            } else if (envelope.type === 'BET_REQUEST') {
                // Simulate processing latency
                setTimeout(() => {
                    const winAmount = Math.random() > 0.7 ? 50 : 0; // 30% hit rate
                    sendResponse('BET_ACCEPTED', {
                        totalBet: envelope.payload.betAmount || 0.10,
                        totalWin: winAmount,
                        resultGrid: [
                            [1, 2, 3],
                            [4, 5, 2],
                            [1, 1, 1]
                        ], // Dummy Grid
                    });
                }, 300);
            } else if (envelope.type === 'SETTLE_REQUEST') {
                // Simulate settling
                sendResponse('SETTLE_ACCEPTED', {});

                // Immediately send balance snapshot
                sendResponse('BALANCE_SNAPSHOT', { balance: 1050.50 });
            }

        } catch (e) {
            console.error('❌ Failed to parse message', data.toString());
        }
    });

    // Send a Heartbeat to client every 30s
    const pingInterval = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
            clientSeq++;
            ws.send(JSON.stringify({
                version: "1.0", type: "PING", traceId: uuidv4(),
                sessionId: "", bankId: "", gameId: "", operationId: "",
                timestamp: new Date().toISOString(), seq: clientSeq, payload: {}
            }));
        }
    }, 30000);

    ws.on('close', () => {
        console.log('🔌 Client disconnected');
        clearInterval(pingInterval);
    });
});

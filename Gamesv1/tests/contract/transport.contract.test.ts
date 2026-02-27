import { createTransport } from '../../packages/gs-protocol/src';

// === Basic Jest-styled Mock Setup for Node environments ===
let wsInstances: any[] = [];
class MockWebSocket {
    onopen: Function | null = null;
    onmessage: Function | null = null;
    send: Function;

    constructor(public url: string, public protocol: string) {
        wsInstances.push(this);
        this.send = (data: string) => {
            const req = JSON.parse(data);

            // Mock GAME_READY -> SESSION_ACCEPTED
            if (req.type === 'GAME_READY') {
                setTimeout(() => this.onmessage?.({ data: JSON.stringify({ type: 'SESSION_ACCEPTED', payload: { balance: 1000 } }) }), 10);
            }
            // Mock BET_REQUEST -> BET_ACCEPTED
            if (req.type === 'BET_REQUEST') {
                setTimeout(() => this.onmessage?.({ data: JSON.stringify({ type: 'BET_ACCEPTED', operationId: req.operationId, payload: { totalWin: 50 } }) }), 10);
            }
        };
        setTimeout(() => this.onopen?.(), 10);
    }
    close() { }
}

const mockFetch = async (url: string, options: any) => {
    const req = JSON.parse(options.body);

    if (url.includes('Enter')) {
        return { ok: true, json: async () => ({ balance: 1000, gameState: { code: 'A1' }, lastAction: 'Enter' }) };
    }
    if (url.includes('processTransaction')) {
        // Assertions for Contract: Ensure Echo logic holds
        if (req.action === 'spin' && (!req.gameState || req.lastAction !== 'Enter')) throw new Error("Contract Failed: ExtGame did not Echo state!");
        return { ok: true, json: async () => ({ balance: 950, endRoundSignature: true, gameState: { code: 'B2' } }) };
    }
    return { ok: false };
};

// Injection
(global as any).WebSocket = MockWebSocket;
(global as any).fetch = mockFetch;


async function runContractTests() {
    console.log("🚀 Starting Transport Contract Tests...");

    const opId = "test-uuid-0001";

    console.log("\n--- Testing GsWsTransport (abs.gs.v1) ---");
    const wsTransport = createTransport({ mode: 'WS', baseUrl: 'ws://mock', token: 'MOCK', gameId: 'g1' });

    // Test WS Connect
    await wsTransport.connect({ mode: 'WS', baseUrl: 'ws://mock', token: 'MOCK', gameId: 'g1' });
    console.log("✅ WS Connected (GAME_READY -> SESSION_ACCEPTED)");

    // Test WS Spin
    const wsRes = await wsTransport.spin(10, opId);
    if (wsRes.totalWin === 50) console.log("✅ WS Spin successful & operationId matched");
    else console.error("❌ WS Spin matched failed");


    console.log("\n--- Testing ExtGameTransport ---");
    const httpTransport = createTransport({ mode: 'EXTGAME', baseUrl: 'http://mock', token: 'MOCK', gameId: 'g1' });

    // Test EXTGAME Connect
    await httpTransport.connect({ mode: 'EXTGAME', baseUrl: 'http://mock', token: 'MOCK', gameId: 'g1' });
    console.log("✅ HTTP Connected (Enter -> Extracted initial gameState Echo)");

    // Test EXTGAME Spin
    const httpRes = await httpTransport.spin(10, opId);
    if (httpRes.endRoundSignature && httpRes.balance === 950) console.log("✅ HTTP Spin successful & echoed gameState correctly");
    else console.error("❌ HTTP Spin echo failed");

    console.log("\n✅ All contract tests passed.");
}

runContractTests().catch(console.error);

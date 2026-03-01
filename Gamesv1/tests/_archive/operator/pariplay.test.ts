import { PariplayBridge } from '../../src/operator/PariplayBridge';

async function runPariplayBridgeTests() {
    console.log("🚀 Starting Pariplay Bridge Integration Tests...");

    // Mocking the DOM environment 
    const mockPostMessageInbox: any[] = [];
    let mockEventListenerCallback: ((event: any) => void) | null = null;
    let mockParentCalled = false;

    (global as any).window = {
        parent: {
            postMessage: (msg: any, targetOrigin: string) => {
                mockParentCalled = true;
                mockPostMessageInbox.push({ msg, targetOrigin });
            }
        },
        addEventListener: (event: string, callback: any) => {
            if (event === 'message') {
                mockEventListenerCallback = callback;
            }
        }
    };
    (global.window as any).window = global.window; // to spoof window.parent !== window check


    let passed = 0;
    let failed = 0;

    const test = (name: string, fn: () => void) => {
        try {
            fn();
            console.log(`✅ ${name}`);
            passed++;
        } catch (e: any) {
            console.error(`❌ ${name}`);
            console.error(`   ${e.message || e}`);
            failed++;
        }
    };


    test("1. Bridge Installs Message Listeners upon Instantiation", () => {
        const bridge = new PariplayBridge({ enabled: true });
        if (!mockEventListenerCallback) throw new Error("DOM Message EventListener was not bound.");
    });


    test("2. Bridge Rejects postMessage Transmission if disabled", () => {
        const disabledBridge = new PariplayBridge({ enabled: false });
        // Reset counters
        mockPostMessageInbox.length = 0;
        mockParentCalled = false;

        disabledBridge.send("gameDataLoaded");

        if (mockParentCalled) throw new Error("Bridge incorrectly blasted out to the Outer Window when manually disabled via config.");
        if (mockPostMessageInbox.length > 0) throw new Error("Inbox corrupted with phantom payloads.");
    });


    test("3. Bridge correctly dispatches mapped Outbound Payloads", () => {
        const bridge = new PariplayBridge({ enabled: true });
        mockPostMessageInbox.length = 0;

        // No parameters
        bridge.send("onAppFrameReady");
        if (mockPostMessageInbox.length !== 1 || mockPostMessageInbox[0].msg !== "onAppFrameReady") {
            throw new Error(`Failed strict string emit. Inbox dump: ${JSON.stringify(mockPostMessageInbox)}`);
        }

        // Complex parameter mapped object
        bridge.send("balance", { balance: 1040.50, currency: "USD" });
        if (mockPostMessageInbox[1].msg.action !== "balance") throw new Error("Object payload missing 'action'");
        if (mockPostMessageInbox[1].msg.balance !== 1040.50) throw new Error("Object payload missing value mappings");
    });


    test("4. Bridge correctly intercepts Operator Timelines (Pause/Resume/AutoBet)", () => {
        const bridge = new PariplayBridge({ enabled: true });
        let pausedCount = 0;
        let resumedCount = 0;
        let stoppedAutoCount = 0;

        bridge.onPauseRequested = () => pausedCount++;
        bridge.onResumeRequested = () => resumedCount++;
        bridge.onStopAutoBetRequested = () => stoppedAutoCount++;

        // Inject fake browser DOM 'message' events from outer operator iframe
        mockEventListenerCallback!({ data: "pause_pp" });
        mockEventListenerCallback!({ data: { type: "resume_pp" } }); // Validate complex object parsing
        mockEventListenerCallback!({ data: "stopAutoBet_pp" });
        mockEventListenerCallback!({ data: "unknown_hook_ignored" });

        if (pausedCount !== 1) throw new Error(`Pause event failing to execute downstream Timeline. Expect 1, got ${pausedCount}`);
        if (resumedCount !== 1) throw new Error(`Resume event failing to execute downstream. Expect 1, got ${resumedCount}`);
        if (stoppedAutoCount !== 1) throw new Error(`Auto stop event failing to execute downstream. Expect 1, got ${stoppedAutoCount}`);
    });


    console.log(`\n🎉 Pariplay Tests completed: ${passed} passed, ${failed} failed.`);
    if (failed > 0) process.exit(1);
}

runPariplayBridgeTests().catch(console.error);

import { RoundStateMachine, RoundState } from '../../src/game/RoundStateMachine';

// Mock Pariplay Bridge to capture postMessage events
class MockBridge {
    public sentMessages: string[] = [];
    public send(type: string) {
        this.sentMessages.push(type);
    }
}

async function runRoundStateMachineTests() {
    console.log("🚀 Starting Round State Machine Replay Tests...");

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

    /**
     * Scenario: A normal successful spin with a medium win.
     * Expected: IDLE -> ENTERING -> READY -> SPINNING -> AWAITING_RESULT -> PRESENTING_WIN -> ROUND_END -> READY
     */
    test("Normal Spin Sequence with Operator Hooks", () => {
        const bridge = new MockBridge() as any;
        const rsm = new RoundStateMachine(bridge);
        const transitions: RoundState[] = [];

        rsm.onStateTransition = (_, to) => transitions.push(to);

        // 1. App Launch
        rsm.transition(RoundState.ENTERING);
        rsm.transition(RoundState.READY);

        // 2. User Clicks Spin
        rsm.transition(RoundState.SPINNING);

        // 3. Server Response Arrives (Last response for the round)
        rsm.onFinalServerResponse();
        rsm.transition(RoundState.AWAITING_RESULT);

        // 4. Visuals (Min Spin Time ends)
        rsm.transition(RoundState.PRESENTING_WIN);

        // 5. Animations done
        rsm.transition(RoundState.ROUND_END);

        // 6. Ready for next bet
        rsm.transition(RoundState.READY);

        // Verify State Sequence
        const expectedStates = [
            RoundState.ENTERING, RoundState.READY, RoundState.SPINNING,
            RoundState.AWAITING_RESULT, RoundState.PRESENTING_WIN,
            RoundState.ROUND_END, RoundState.READY
        ];

        expectedStates.forEach((state, i) => {
            if (transitions[i] !== state) throw new Error(`Sequence mismatch at index ${i}: Expected ${state}, got ${transitions[i]}`);
        });

        // Verify Operator Event Ordering
        if (bridge.sentMessages[0] !== 'ticketReceived') throw new Error("Order error: ticketReceived must be first.");
        if (bridge.sentMessages[1] !== 'roundEnded') throw new Error("Order error: roundEnded must be second.");
    });

    /**
     * Scenario: Illegal transition attempt.
     */
    test("Illegal Transition Prevention", () => {
        const rsm = new RoundStateMachine(null);
        rsm.transition(RoundState.ENTERING);
        rsm.transition(RoundState.READY);

        const startState = rsm.state;
        // Directly to ROUND_END without spinning is illegal
        rsm.transition(RoundState.ROUND_END);

        if (rsm.state !== startState) throw new Error("State machine allowed an illegal transition!");
    });

    console.log(`\n🎉 Round State Machine Tests completed: ${passed} passed, ${failed} failed.`);
    if (failed > 0) process.exit(1);
}

runRoundStateMachineTests().catch(console.error);

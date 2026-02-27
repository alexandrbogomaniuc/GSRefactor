import { SpinProfiler } from '../../src/net/SpinProfiling';

async function runSpinProfilingTests() {
    console.log("🚀 Starting Spin Profiling Unit Tests...");

    let passed = 0;
    let failed = 0;

    const test = (name: string, fn: () => void | Promise<void>) => {
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

    /** Sleep utility to mock wall-clock time passing */
    const delay = (ms: number) => new Promise(res => setTimeout(res, ms));

    await test("1. Disabled profiler does not inject profiling objects", () => {
        const profiler = new SpinProfiler(false);
        profiler.markRequestStart();
        profiler.markRequestEnd();
        profiler.markAnimationStart();
        profiler.markAnimationEnd();

        const basePayload = { betAmount: 10 };
        const mutatedPayload = profiler.applyToPayload(basePayload);

        if (mutatedPayload.PRECSPINSTAT) {
            throw new Error("Payload got injected with PRECSPINSTAT when disabled.");
        }
    });

    await test("2. Enabled profiler accurately catches latencies and appends PRECSPINSTAT payload", async () => {
        const profiler = new SpinProfiler(true);

        // Mock a 20ms spin network request (Wager #1)
        profiler.markRequestStart();
        await delay(20);
        profiler.markRequestEnd();

        // Mock a 100ms visual timeline duration playback
        profiler.markAnimationStart();
        await delay(100);
        profiler.markAnimationEnd("PLACEBET");

        // The next Spin triggers (Wager #2)
        const basePayload = { betAmount: 15 };
        const mutatedPayload = profiler.applyToPayload(basePayload);

        if (!mutatedPayload.PRECSPINSTAT) {
            throw new Error("Payload did not get injected with PRECSPINSTAT.");
        }

        const stats = mutatedPayload.PRECSPINSTAT;
        if (stats.CMD !== "PLACEBET") {
            throw new Error(`CMD was not PLACEBET, got: ${stats.CMD}`);
        }

        // We allow some flexibility for setTimeout precision (~10ms)
        if (stats.SPINREQTIME < 15 || stats.SPINREQTIME > 60) {
            throw new Error(`SPINREQTIME measurement wildly misaligned: ${stats.SPINREQTIME}ms`);
        }
        if (stats.SPINANMTIME < 90 || stats.SPINANMTIME > 160) {
            throw new Error(`SPINANMTIME visually measured wildly misaligned: ${stats.SPINANMTIME}ms`);
        }
    });

    await test("3. Clear() perfectly strips out past history", () => {
        const profiler = new SpinProfiler(true);
        profiler.markRequestStart();
        profiler.markRequestEnd();
        profiler.markAnimationStart();
        profiler.markAnimationEnd();

        profiler.clear();

        const basePayload = { betAmount: 10 };
        const mutatedPayload = profiler.applyToPayload(basePayload);

        if (mutatedPayload.PRECSPINSTAT) {
            throw new Error("Payload injected despite being explicitly cleared");
        }
    });

    console.log(`\n🎉 Spin Profiling Tests completed: ${passed} passed, ${failed} failed.`);
    if (failed > 0) process.exit(1);
}

runSpinProfilingTests().catch(console.error);

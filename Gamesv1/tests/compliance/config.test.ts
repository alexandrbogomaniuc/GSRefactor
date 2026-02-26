import { resolveConfig, assertCompliance, ConfigurationLayer, DefaultBankProperties } from '../../src/compliance/ComplianceConfig';
import { DefaultFeatureFlags } from '../../src/compliance/FeatureFlags';

async function runComplianceTests() {
    console.log("🚀 Starting Compliance Config Tests...");

    let passed = 0;
    let failed = 0;

    const test = (name: string, fn: () => void) => {
        try {
            fn();
            console.log(`✅ ${name}`);
            passed++;
        } catch (e: any) {
            console.error(`❌ ${name}`);
            console.error(`   ${e.message}`);
            failed++;
        }
    };

    test("Resolves default fallback values accurately", () => {
        const config = resolveConfig();
        if (config.flags.minReelsSpinningTimeSecs !== DefaultFeatureFlags.minReelsSpinningTimeSecs) {
            throw new Error("Default min spin time mismatch.");
        }
        if (config.bank.currencyCode !== DefaultBankProperties.currencyCode) {
            throw new Error("Default bank value mismatch.");
        }
    });

    test("Overrides stack explicitly by precedence (Cert > Licensee > Launch > Bank)", () => {
        const certConfig: ConfigurationLayer = { flags: { minReelsSpinningTimeSecs: 3.0 } };
        const licenseeConfig: ConfigurationLayer = { flags: { autoplayAllowed: false } };
        const launchParams: ConfigurationLayer = { bank: { currencyCode: "EUR" } };
        const bankOverrides = { fractionDigits: 0 };

        const config = resolveConfig(certConfig, licenseeConfig, launchParams, bankOverrides);

        if (config.flags.minReelsSpinningTimeSecs !== 3.0) throw new Error("Cert config failed override");
        if (config.flags.autoplayAllowed !== false) throw new Error("Licensee config failed override");
        if (config.bank.currencyCode !== "EUR") throw new Error("Launch param bank failed override");
        if (config.bank.fractionDigits !== 0) throw new Error("Bank override failed");
    });

    test("Assert Compliance throws correctly on fraction/truncation conflict", () => {
        let threw = false;
        try {
            resolveConfig({}, {}, {}, { fractionDigits: 2, truncateCents: true });
        } catch (e: any) {
            if (e.message.includes("`truncateCents` is true, but `fractionDigits` is > 0")) threw = true;
        }
        if (!threw) throw new Error("Did not throw on fraction exception");
    });

    test("Assert Compliance throws correctly on bad turboplay multipliers", () => {
        let threw = false;
        try {
            resolveConfig({}, {}, { flags: { turboplayAllowed: true, turboplaySpeedMultiplier: 1.0 } });
        } catch (e: any) {
            if (e.message.includes("speed multiplier is strictly non-accelerated at 1")) threw = true;
        }
        if (!threw) throw new Error("Did not throw on turboplay speed limits");
    });

    test("Assert Compliance min bounds on spin timer", () => {
        let threw = false;
        try {
            resolveConfig({ flags: { minReelsSpinningTimeSecs: -1 } });
        } catch (e: any) {
            if (e.message.includes("cannot be negative")) threw = true;
        }
        if (!threw) throw new Error("Did not throw on negative spin time");
    });

    console.log(`\n🎉 Tests completed: ${passed} passed, ${failed} failed.`);
    if (failed > 0) process.exit(1);
}

runComplianceTests().catch(console.error);

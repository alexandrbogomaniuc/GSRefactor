import assert from "node:assert/strict";

import { DefaultResolvedRuntimeConfig } from "../../packages/core-compliance/src/ResolvedRuntimeConfig.ts";
import {
  FeatureModuleManager,
} from "../../games/premium-slot/src/game/features/FeatureModuleManager.ts";
import type { RoundPresentationModel } from "../../games/premium-slot/src/app/runtime/RuntimeOutcomeMapper.ts";

const makeRound = (overrides: Partial<RoundPresentationModel> = {}): RoundPresentationModel => ({
  roundId: "round-1",
  winAmount: 100,
  slotIndex: 0,
  reels: {
    stopColumns: [
      [0, 1, 2],
      [1, 2, 3],
      [2, 3, 4],
      [3, 4, 5],
      [4, 5, 6],
    ],
  },
  featureOverlays: [],
  counters: {},
  messages: [],
  soundCues: [],
  animationCues: [],
  serverState: {},
  ...overrides,
});

let passed = 0;
let failed = 0;

const test = (name: string, fn: () => void) => {
  try {
    fn();
    console.log(`PASS ${name}`);
    passed += 1;
  } catch (error) {
    failed += 1;
    console.error(`FAIL ${name}`);
    console.error(error);
  }
};

test("free-spins module produces overlay when counter present", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.freeSpins = true;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      counters: { freeSpinsRemaining: 7 },
    }),
  );

  assert.ok(frame.overlays.some((overlay) => overlay.type === "free-spins"));
  assert.ok(frame.messages.some((message) => message.includes("FREE SPINS")));
});

test("buy feature hidden when cash bonus forbids buy", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = true;
  config.capabilities.features.buyFeatureForCashBonus = false;
  config.capabilities.features.buyFeatureDisabledForCashBonus = true;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      serverState: { cashBonusMode: true, buyFeatureAvailable: true },
    }),
  );

  assert.equal(frame.controlVisibility.buyFeature, false);
  assert.ok(frame.messages.some((message) => message.includes("DISABLED FOR CASH BONUS")));
});

test("jackpot module emits cues when triggered", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.jackpotHooks = true;
  config.jackpotHooks.enabled = true;
  config.jackpotHooks.source = "gs";

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      counters: { jackpotLevel: 3 },
      serverState: { jackpotTriggered: true },
    }),
  );

  assert.ok(frame.soundCues.includes("jackpot-stinger"));
  assert.ok(frame.animationCues.includes("jackpot-overlay"));
  assert.ok(frame.overlays.some((overlay) => overlay.type === "jackpot"));
});

console.log(`\nFeature module tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

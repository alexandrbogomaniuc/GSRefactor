import assert from "node:assert/strict";

import { DefaultResolvedRuntimeConfig } from "../../packages/core-compliance/src/ResolvedRuntimeConfig.ts";
import {
  FeatureModuleManager,
  resolvePremiumHudVisibility,
  type RoundPresentationModel,
} from "@gamesv1/ui-kit/shell";

const makeRound = (
  overrides: Partial<RoundPresentationModel> = {},
): RoundPresentationModel => ({
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
  symbolGrid: [
    [0, 1, 2, 3, 4],
    [1, 2, 3, 4, 5],
    [2, 3, 4, 5, 6],
  ],
  counters: {},
  messages: [],
  soundCues: [],
  animationCues: [],
  labels: {},
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
  assert.ok(frame.enabledModuleIds.includes("free-spins"));
  assert.ok(frame.activeModuleIds.includes("free-spins"));
});

test("buy feature enabled in normal mode when allowed and available", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = true;
  config.capabilities.features.buyFeatureForCashBonus = false;
  config.capabilities.features.buyFeatureDisabledForCashBonus = true;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      counters: { cashBonusMode: false, buyFeatureAvailable: true },
    }),
  );

  assert.equal(frame.controlVisibility.buyFeature, true);
  assert.ok(frame.overlays.some((overlay) => overlay.type === "buy-feature"));
  assert.ok(frame.enabledModuleIds.includes("buy-feature"));
  assert.ok(frame.activeModuleIds.includes("buy-feature"));
});

test("cash-bonus-only buy feature is enabled when cash bonus mode is active", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = false;
  config.capabilities.features.buyFeatureForCashBonus = true;
  config.capabilities.features.buyFeatureDisabledForCashBonus = false;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      counters: { cashBonusMode: true, buyFeatureAvailable: true },
    }),
  );

  assert.equal(frame.controlVisibility.buyFeature, true);
  assert.ok(frame.overlays.some((overlay) => overlay.type === "buy-feature"));
  assert.ok(frame.enabledModuleIds.includes("buy-feature"));
  assert.ok(frame.activeModuleIds.includes("buy-feature"));
});

test("buy feature hidden when cash bonus forbids buy", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = true;
  config.capabilities.features.buyFeatureForCashBonus = false;
  config.capabilities.features.buyFeatureDisabledForCashBonus = true;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      counters: { cashBonusMode: true, buyFeatureAvailable: true },
    }),
  );

  assert.equal(frame.controlVisibility.buyFeature, false);
  assert.ok(
    frame.messages.some((message) =>
      message.includes("DISABLED FOR CASH BONUS"),
    ),
  );
  assert.ok(frame.enabledModuleIds.includes("buy-feature"));
  assert.ok(frame.activeModuleIds.includes("buy-feature"));
});

test("HUD buy-feature visibility is consistent with buy-feature capability policy", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = false;
  config.capabilities.features.buyFeatureForCashBonus = true;
  config.capabilities.features.buyFeatureDisabledForCashBonus = false;

  const hudVisibility = resolvePremiumHudVisibility(config);
  assert.equal(hudVisibility.controls.buyFeature, true);

  const manager = new FeatureModuleManager(config);
  const nonCashBonusRound = manager.resolve(
    makeRound({
      counters: { cashBonusMode: false, buyFeatureAvailable: true },
    }),
  );
  const cashBonusRound = manager.resolve(
    makeRound({
      counters: { cashBonusMode: true, buyFeatureAvailable: true },
    }),
  );

  assert.equal(nonCashBonusRound.controlVisibility.buyFeature, false);
  assert.equal(cashBonusRound.controlVisibility.buyFeature, true);
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
      labels: { jackpotTriggered: "true" },
    }),
  );

  assert.ok(frame.soundCues.includes("jackpot-stinger"));
  assert.ok(frame.animationCues.includes("jackpot-overlay"));
  assert.ok(frame.overlays.some((overlay) => overlay.type === "jackpot"));
  assert.ok(frame.enabledModuleIds.includes("jackpot-hooks"));
  assert.ok(frame.activeModuleIds.includes("jackpot-hooks"));
});

test("activeModuleIds tracks current-round active modules, not all enabled modules", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.freeSpins = true;
  config.capabilities.features.buyFeature = true;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve(
    makeRound({
      counters: {
        freeSpinsRemaining: 0,
        buyFeatureAvailable: false,
      },
      labels: {
        freeSpinsActive: "false",
      },
    }),
  );

  assert.ok(frame.enabledModuleIds.includes("free-spins"));
  assert.ok(frame.enabledModuleIds.includes("buy-feature"));
  assert.equal(frame.activeModuleIds.includes("free-spins"), false);
  assert.ok(frame.activeModuleIds.includes("buy-feature"));
});

console.log(`\nFeature module tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

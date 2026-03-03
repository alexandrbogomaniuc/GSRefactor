import assert from "node:assert/strict";

import { DefaultResolvedRuntimeConfig } from "../../packages/core-compliance/src/ResolvedRuntimeConfig.ts";
import { hasRowGaps } from "../../packages/pixi-layout/src/index.ts";
import { computeHudLayout } from "../../packages/ui-kit/src/layout/HudLayout.ts";
import { FeatureModuleManager } from "../../packages/ui-kit/src/shell/features/FeatureModuleManager.ts";
import { mapPlayRoundToPresentation } from "../../packages/ui-kit/src/shell/presentation/PremiumPresentationMapper.ts";
import { resolvePremiumHudVisibility } from "../../packages/ui-kit/src/shell/hud/PremiumHudPolicy.ts";
import type { PlayRoundResponse } from "../../packages/core-protocol/src/IGameTransport.ts";

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

const baseHudItems = [
  { id: "spin", width: 210, height: 100 },
  { id: "turbo", width: 190, height: 84 },
  { id: "autoplay", width: 190, height: 84 },
  { id: "buyFeature", width: 190, height: 84 },
  { id: "sound", width: 190, height: 84 },
  { id: "settings", width: 190, height: 84 },
  { id: "history", width: 190, height: 84 },
] as const;

const defaultLayout = {
  reelCount: 5,
  rowCount: 3,
  symbolModulo: 12,
} as const;

const toLayoutItems = (visibility: Record<(typeof baseHudItems)[number]["id"], boolean>) =>
  baseHudItems.map((item) => ({
    ...item,
    visible: visibility[item.id],
  }));

const assertGaplessLayout = (
  visibleItems: Array<{ id: string; width: number; height: number; visible: boolean }>,
) => {
  const { layout } = computeHudLayout(visibleItems, {
    width: 844,
    height: 390,
    orientation: "landscape",
    safeArea: { top: 0, right: 44, bottom: 21, left: 44 },
  });

  assert.equal(hasRowGaps(layout), false);
  return layout;
};

test("hidden controls collapse without layout gaps", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.turbo.allowed = false;
  config.capabilities.features.buyFeature = false;
  config.capabilities.features.buyFeatureForCashBonus = false;
  config.capabilities.features.inGameHistory = false;

  const visibility = resolvePremiumHudVisibility(config);
  const visibleItems = toLayoutItems(visibility.controls);
  const layout = assertGaplessLayout(visibleItems);

  const visibleControlCount = Object.values(visibility.controls).filter(Boolean).length;
  assert.equal(layout.items.length, visibleControlCount);
});

test("all controls visible layout has no gaps", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.turbo.allowed = true;
  config.capabilities.features.autoplay = true;
  config.capabilities.features.buyFeature = true;
  config.capabilities.sound.showToggle = true;
  config.capabilities.features.inGameHistory = true;
  config.capabilities.history.enabled = true;

  const visibility = resolvePremiumHudVisibility(config);
  const layout = assertGaplessLayout(toLayoutItems(visibility.controls));
  assert.equal(layout.items.length, 7);
});

test("minimal controls visible layout has no gaps", () => {
  const minimalControls = {
    spin: true,
    turbo: false,
    autoplay: false,
    buyFeature: false,
    sound: false,
    settings: false,
    history: false,
  } as const;

  const layout = assertGaplessLayout(toLayoutItems(minimalControls));
  assert.equal(layout.items.length, 1);
  assert.equal(layout.items[0].id, "spin");
});

test("feature modules reflect free-spins and buy-feature policy", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.freeSpins = true;
  config.capabilities.features.buyFeature = true;
  config.capabilities.features.buyFeatureForCashBonus = false;
  config.capabilities.features.buyFeatureDisabledForCashBonus = true;

  const manager = new FeatureModuleManager(config);
  const frame = manager.resolve({
    roundId: "round-10",
    winAmount: 200,
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
    counters: {
      freeSpinsRemaining: 4,
      cashBonusMode: true,
      buyFeatureAvailable: true,
    },
    messages: [],
    soundCues: [],
    animationCues: [],
    labels: {},
  });

  assert.ok(frame.overlays.some((overlay) => overlay.type === "free-spins"));
  assert.equal(frame.controlVisibility.buyFeature, false);
});

test("buy-feature visibility stays consistent between HUD policy and module output", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = false;
  config.capabilities.features.buyFeatureForCashBonus = true;
  config.capabilities.features.buyFeatureDisabledForCashBonus = false;

  const hudVisibility = resolvePremiumHudVisibility(config);
  assert.equal(hudVisibility.controls.buyFeature, true);

  const manager = new FeatureModuleManager(config);
  const cashBonusRound = manager.resolve({
    roundId: "cash-bonus-round",
    winAmount: 0,
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
    counters: {
      cashBonusMode: true,
      buyFeatureAvailable: true,
    },
    messages: [],
    soundCues: [],
    animationCues: [],
    labels: {},
  });

  assert.equal(cashBonusRound.controlVisibility.buyFeature, true);
  assert.ok(cashBonusRound.activeModuleIds.includes("buy-feature"));
});

test("dynamic buy-feature visibility toggles by round state without layout gaps", () => {
  const config = structuredClone(DefaultResolvedRuntimeConfig);
  config.capabilities.features.buyFeature = true;
  config.capabilities.features.buyFeatureForCashBonus = false;
  config.capabilities.features.buyFeatureDisabledForCashBonus = true;

  const manager = new FeatureModuleManager(config);

  const availableRound = manager.resolve({
    roundId: "buy-available",
    winAmount: 0,
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
    counters: {
      cashBonusMode: false,
      buyFeatureAvailable: true,
    },
    messages: [],
    soundCues: [],
    animationCues: [],
    labels: {},
  });

  const disabledRound = manager.resolve({
    roundId: "buy-disabled",
    winAmount: 0,
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
    counters: {
      cashBonusMode: true,
      buyFeatureAvailable: true,
    },
    messages: [],
    soundCues: [],
    animationCues: [],
    labels: {},
  });

  assert.equal(availableRound.controlVisibility.buyFeature, true);
  assert.equal(disabledRound.controlVisibility.buyFeature, false);

  const baseVisibility = resolvePremiumHudVisibility(config);
  const enabledLayout = assertGaplessLayout(
    toLayoutItems({
      ...baseVisibility.controls,
      buyFeature: availableRound.controlVisibility.buyFeature ?? false,
    }),
  );
  const disabledLayout = assertGaplessLayout(
    toLayoutItems({
      ...baseVisibility.controls,
      buyFeature: disabledRound.controlVisibility.buyFeature ?? false,
    }),
  );

  assert.equal(enabledLayout.items.length, disabledLayout.items.length + 1);
});

test("presentation mapper ignores engine-private fields", () => {
  const response: PlayRoundResponse = {
    ok: true,
    requestId: "req-9",
    sessionId: "sid-9",
    requestCounter: 9,
    stateVersion: 9,
    wallet: {
      balanceMinor: 1000,
      currencyCode: "EUR",
    },
    round: {
      roundId: "round-9",
      winAmountMinor: 250,
      totalBetMinor: 100,
      status: "SETTLED",
    },
    feature: null,
    presentationPayload: {
      reelStops: [
        [0, 1, 2],
        [1, 2, 3],
        [2, 3, 4],
        [3, 4, 5],
        [4, 5, 6],
      ],
      symbolGrid: [
        [0, 1, 2, 3, 4],
        [1, 2, 3, 4, 5],
        [2, 3, 4, 5, 6],
      ],
      uiMessages: ["ROUND COMPLETE"],
      audioCues: ["jackpot-stinger"],
      animationCues: ["reel-stop"],
      counters: {},
      labels: {
        jackpotTriggered: "false",
      },
      serverAudit: {
        rngTraceRef: "hidden",
      },
    },
    restore: null,
    idempotency: null,
    retry: null,
  };

  const mapped = mapPlayRoundToPresentation(response, defaultLayout);

  assert.equal(mapped.roundId, "round-9");
  assert.equal(mapped.messages[0], "ROUND COMPLETE");
  assert.equal(Object.prototype.hasOwnProperty.call(mapped, "serverAudit"), false);
});

console.log(`\nPremium shell smoke tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

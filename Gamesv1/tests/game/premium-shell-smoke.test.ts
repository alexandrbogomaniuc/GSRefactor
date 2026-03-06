import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

import { DefaultResolvedRuntimeConfig } from "../../packages/core-compliance/src/ResolvedRuntimeConfig.ts";
import { hasRowGaps } from "../../packages/pixi-layout/src/index.ts";
import { computeHudLayout } from "@gamesv1/ui-kit/layout";
import {
  applyAudioCue,
  createAudioCueRegistry,
  FeatureModuleManager,
  mergePremiumHudVisibility,
  mapPlayRoundToPresentation,
  resolvePremiumHudVisibility,
  resolveShellThemeTokens,
  resolveWinSymbolsFromReels,
  RoundActionBuilder,
} from "@gamesv1/ui-kit/shell";
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

const toLayoutItems = (
  visibility: Record<(typeof baseHudItems)[number]["id"], boolean>,
) =>
  baseHudItems.map((item) => ({
    ...item,
    visible: visibility[item.id],
  }));

const assertGaplessLayout = (
  visibleItems: Array<{
    id: string;
    width: number;
    height: number;
    visible: boolean;
  }>,
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

  const visibleControlCount = Object.values(visibility.controls).filter(
    Boolean,
  ).length;
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
    toLayoutItems(
      mergePremiumHudVisibility(
        baseVisibility,
        availableRound.controlVisibility,
      ).controls,
    ),
  );
  const disabledLayout = assertGaplessLayout(
    toLayoutItems(
      mergePremiumHudVisibility(baseVisibility, disabledRound.controlVisibility)
        .controls,
    ),
  );

  assert.equal(enabledLayout.items.length, disabledLayout.items.length + 1);
});

test("generic dynamic visibility supports turbo/autoplay/history updates", () => {
  const baseVisibility = resolvePremiumHudVisibility(
    DefaultResolvedRuntimeConfig,
  );
  const mergedVisibility = mergePremiumHudVisibility(baseVisibility, {
    buyFeature: false,
    turbo: false,
    autoplay: false,
    history: false,
  });

  const layout = assertGaplessLayout(toLayoutItems(mergedVisibility.controls));
  assert.equal(mergedVisibility.controls.buyFeature, false);
  assert.equal(mergedVisibility.controls.turbo, false);
  assert.equal(mergedVisibility.controls.autoplay, false);
  assert.equal(mergedVisibility.controls.history, false);
  assert.equal(layout.items.length, 3);
});

test("round action builder removes screen-local bet and buy-price assumptions", () => {
  const builder = new RoundActionBuilder({
    bet: {
      lineCount: 25,
      multiplier: 2,
    },
    buyFeature: {
      priceMinor: 450,
    },
  });

  const spinBet = builder.buildSpinBet(500);
  assert.equal(spinBet.lines, 25);
  assert.equal(spinBet.multiplier, 2);
  assert.equal(spinBet.totalBetMinor, 500);

  const buyAction = builder.buildBuyFeatureAction({ totalBetMinor: 500 });
  assert.equal(buyAction.selectedBet.lines, 25);
  assert.equal(buyAction.selectedFeatureChoice.priceMinor, 450);

  const neutralBuilder = new RoundActionBuilder();
  const neutralBuyAction = neutralBuilder.buildBuyFeatureAction({
    totalBetMinor: 200,
  });
  assert.equal(neutralBuyAction.selectedFeatureChoice.priceMinor, 0);
  assert.equal(neutralBuyAction.selectedBet.lines, 1);
  assert.equal(neutralBuyAction.selectedBet.multiplier, 1);

  const themedBuilder = new RoundActionBuilder(
    resolveShellThemeTokens().roundActions,
  );
  const themedBet = themedBuilder.buildSpinBet(200);
  assert.equal(themedBet.lines, 20);
  assert.equal(themedBet.multiplier, 1);
});

test("win target resolver uses configurable layout constraints", () => {
  const r0 = [
    { id: "r0-top" },
    { id: "r0-mid" },
    { id: "r0-bottom" },
  ] as never[];
  const r1 = [
    { id: "r1-top" },
    { id: "r1-mid" },
    { id: "r1-bottom" },
  ] as never[];
  const r2 = [
    { id: "r2-top" },
    { id: "r2-mid" },
    { id: "r2-bottom" },
  ] as never[];
  const reels = [
    { getVisibleSymbols: () => r0 },
    { getVisibleSymbols: () => r1 },
    { getVisibleSymbols: () => r2 },
  ];

  const symbols = resolveWinSymbolsFromReels(reels, {
    reelCount: 3,
    rowCount: 3,
    highlightRowIndex: 2,
    highlightReelIndexes: [0, 2],
  });

  assert.equal(symbols.length, 2);
  assert.equal(symbols[0], r0[2]);
  assert.equal(symbols[1], r2[2]);
});

test("audio cue execution is delegated to shared registry", () => {
  const registry = createAudioCueRegistry({
    overrides: {
      "jackpot-stinger": [
        { type: "sfx", assetKey: "theme/sfx-jp", volume: 0.6 },
      ],
      "mute-bgm": [{ type: "bgmVolume", volume: 0.15 }],
    },
  });

  const played: Array<{ key: string; volume: number }> = [];
  const bgm: number[] = [];

  applyAudioCue(
    "jackpot-stinger",
    {
      playSfx: (assetKey, options) =>
        played.push({ key: assetKey, volume: options.volume }),
      setBgmVolume: (volume) => bgm.push(volume),
      isSoundEnabled: () => true,
    },
    registry,
  );

  applyAudioCue(
    "mute-bgm",
    {
      playSfx: (assetKey, options) =>
        played.push({ key: assetKey, volume: options.volume }),
      setBgmVolume: (volume) => bgm.push(volume),
      isSoundEnabled: () => true,
    },
    registry,
  );

  const mainScreenSource = readFileSync(
    "games/premium-slot/src/app/screens/main/MainScreen.ts",
    "utf8",
  );

  assert.deepEqual(played, [{ key: "theme/sfx-jp", volume: 0.6 }]);
  assert.deepEqual(bgm, [0.15]);
  assert.equal(mainScreenSource.includes("applyAudioCue("), true);
  assert.equal(mainScreenSource.includes('cue === "jackpot-stinger"'), false);
  assert.equal(mainScreenSource.includes('cue === "mute-bgm"'), false);
});

test("theme token foundation resolves overrides and query hooks", () => {
  const query = new URLSearchParams(
    "theme=neon&skin=night&vfxIntensity=low&hudPanelAlpha=0.85",
  );
  const theme = resolveShellThemeTokens({
    runtimeConfig: DefaultResolvedRuntimeConfig,
    queryParams: query,
    overrides: {
      winPresentation: {
        tierLabels: { mega: "EPIC WIN" },
      },
    },
  });

  assert.equal(theme.metadata.themeId, "neon");
  assert.equal(theme.metadata.skinId, "night");
  assert.equal(theme.vfx.intensity, "low");
  assert.equal(theme.hud.panelAlpha, 0.85);
  assert.equal(theme.winPresentation.tierLabels.mega, "EPIC WIN");
});

test("tier style hooks are consumed by premium screen integration", () => {
  const mainScreenSource = readFileSync(
    "games/premium-slot/src/app/screens/main/MainScreen.ts",
    "utf8",
  );

  assert.equal(mainScreenSource.includes("resolveTierStyleHook"), true);
  assert.equal(
    mainScreenSource.includes("this.winCounter.showWin(amountMinor, title"),
    true,
  );
  assert.equal(
    mainScreenSource.includes("this.winHighlight.showWin(symbols"),
    true,
  );
  assert.equal(
    mainScreenSource.includes("shellTheme.winPresentation.tierStyleHooks"),
    true,
  );
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
  assert.equal(
    Object.prototype.hasOwnProperty.call(mapped, "serverAudit"),
    false,
  );
});

console.log(`\nPremium shell smoke tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

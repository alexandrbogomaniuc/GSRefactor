import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

import { resolveConfig, resolveConfigWithMetadata } from "../../packages/core-compliance/src/config/ConfigResolver.ts";
import type { ConfigResolverInput } from "../../packages/core-compliance/src/config/RuntimeConfigSchema.ts";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const fixtureDir = path.join(__dirname, "fixtures", "config-layers");

const loadFixture = <T>(name: string): T => {
  const filePath = path.join(fixtureDir, `${name}.json`);
  return JSON.parse(readFileSync(filePath, "utf8")) as T;
};

const createInput = (): ConfigResolverInput => ({
  templateDefaults: loadFixture("templateDefaults"),
  bankProperties: loadFixture("bankProperties"),
  gameOverrides: loadFixture("gameOverrides"),
  currencyOverrides: loadFixture("currencyOverrides"),
  launchParams: loadFixture("launchParams"),
  devMode: false,
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

test("applies precedence template < bank < game < currency < launch", () => {
  const resolved = resolveConfig(createInput());

  assert.equal(resolved.currencyCode, "USD");
  assert.equal(resolved.minBet, 50);
  assert.equal(resolved.maxBet, 5000);
  assert.equal(resolved.defaultBet, 200);
  assert.equal(resolved.maxExposure, 100000);

  assert.equal(resolved.betConfig.mode, "dynamic");
  if (resolved.betConfig.mode === "dynamic") {
    assert.equal(resolved.betConfig.dynamicBetConstraints.step, 50);
  }

  assert.equal(resolved.turboplay.allowed, true);
  assert.equal(resolved.turboplay.speedId, "turbo-x3");
  assert.equal(resolved.turboplay.preferred, true);

  assert.equal(resolved.soundDefaults.masterVolume, 0.5);
  assert.equal(resolved.localization.defaultLang, "en-US");
  assert.equal(resolved.localization.localizedTitleKey, "game.title.runtime");
  assert.equal(resolved.localization.showMissingLocalizationError, true);
  assert.equal(resolved.localization.contentPath, "/cdn/content");
  assert.equal(resolved.localization.customTranslationsEnabled, true);
  assert.equal(resolved.history.url, "/history?scope=player");
  assert.equal(resolved.history.openInSameWindow, true);
});

test("falls back to template bet ladder when currency override is missing", () => {
  const input = createInput();
  input.bankProperties = {
    currencyCode: "GBP",
    minBet: 10,
    maxBet: 500,
    defaultBet: 100,
  };
  input.launchParams = { currencyCode: "GBP" };

  const resolved = resolveConfig(input);
  assert.equal(resolved.betConfig.mode, "ladder");
  assert.equal(resolved.defaultBet, 100);
});

test("logs diff entries per overriding layer", () => {
  const { diffLog } = resolveConfigWithMetadata({
    ...createInput(),
    devMode: true,
  });

  const minBetOverride = diffLog.find(
    (entry) => entry.key === "minBet" && entry.layer === "bankProperties",
  );
  assert.ok(minBetOverride);

  const defaultBetOverride = diffLog.find(
    (entry) => entry.key === "defaultBet" && entry.layer === "launchParams",
  );
  assert.ok(defaultBetOverride);
});

test("applies legacy GL_DEFAULT_BET fallback when launch defaultBet is absent", () => {
  const input = createInput();
  input.launchParams = {
    currencyCode: "USD",
    GL_DEFAULT_BET: 275,
  };

  const { config, diffLog, warnings } = resolveConfigWithMetadata(input);
  assert.equal(config.defaultBet, 275);
  assert.ok(
    diffLog.some((entry) => entry.layer === "launchParams.legacy.GL_DEFAULT_BET"),
  );
  assert.ok(
    warnings.some((warning) => warning.key === "GL_DEFAULT_BET"),
  );
});

test("surfaces unsupported config keys in warnings", () => {
  const input = createInput() as ConfigResolverInput & {
    launchParams: ConfigResolverInput["launchParams"] & {
      UNKNOWN_FLAG?: boolean;
    };
  };
  input.launchParams.UNKNOWN_FLAG = true;

  const { warnings } = resolveConfigWithMetadata(input);
  assert.ok(
    warnings.some((warning) => warning.key === "UNKNOWN_FLAG"),
  );
});

test("maps legacy capability aliases into canonical phase-1 fields", () => {
  const input = createInput();
  (input.launchParams as Record<string, unknown>) = {
    USE_JP_NOTIFICATION: 1,
    content_path: "/cdn/custom-locales",
    spinProfilingEnabled: true,
    delayedWalletMessages: "true",
    BUY_FEATURE_DISABLED_FOR_CASH_BONUS: true,
    FRB: true,
    OFRB: false,
    jackpotHooksEnabled: true,
  };

  const resolved = resolveConfig(input);
  assert.equal(resolved.localization.serverNotificationsEnabled, true);
  assert.equal(resolved.localization.contentPath, "/cdn/custom-locales");
  assert.equal(resolved.capabilities.spinProfiling.enabled, true);
  assert.equal(resolved.walletDisplayPolicy.delayedWalletMessages, true);
  assert.equal(resolved.capabilities.features.buyFeatureDisabledForCashBonus, true);
  assert.equal(resolved.capabilities.features.frb, true);
  assert.equal(resolved.capabilities.features.ofrb, false);
  assert.equal(resolved.capabilities.features.jackpotHooks, true);
  assert.equal(resolved.jackpotHooks.enabled, true);
});

test("maps GS policy groups into runtime fields and capabilities", () => {
  const input = createInput();
  input.launchParams = {
    animationPolicy: {
      forcedSpinStopEnabled: true,
      forcedSkipWinPresentation: true,
      minReelSpinTimeMs: { normal: 2100, turbo: 1300 },
      autoplayMinDelayMs: 275,
      lowPerformanceMode: false,
      spinProfilingEnabled: true,
    },
    soundPolicy: {
      soundModeByDefault: "muted",
      showToggle: true,
      masterVolume: 0.4,
      bgmVolume: 0.2,
      sfxVolume: 0.3,
    },
    localizationPolicy: {
      defaultLanguage: "de",
      localizedTitleKey: "game.title.de",
      localizedTitle: "Premium Slot DE",
      showMissingLocalizationError: true,
      contentPath: "/cdn/de",
      customTranslationsEnabled: true,
      serverNotificationsEnabled: true,
    },
    walletDisplayPolicy: {
      showBalance: true,
      showCurrencyCode: true,
      showDelayedIndicator: true,
      delayedWalletMessages: true,
    },
    featurePolicy: {
      autoplay: true,
      buyFeature: true,
      buyFeatureForCashBonus: false,
      buyFeatureDisabledForCashBonus: true,
      freeSpins: true,
      respin: true,
      holdAndWin: true,
      inGameHistory: true,
      holidayMode: false,
      customSkins: true,
      frb: true,
      ofrb: false,
      jackpotHooksEnabled: true,
    },
    sessionUiPolicy: {
      showSessionTimer: true,
      showRealityCheckBanner: true,
      closeButtonPolicy: "confirm",
    },
  };

  const resolved = resolveConfig(input);
  assert.equal(resolved.minReelSpinTime.normalMs, 2100);
  assert.equal(resolved.minReelSpinTime.turboMs, 1300);
  assert.equal(resolved.capabilities.spinProfiling.enabled, true);
  assert.equal(resolved.soundDefaults.modeByDefault, "muted");
  assert.equal(resolved.localization.defaultLang, "de");
  assert.equal(resolved.localization.localizedTitle, "Premium Slot DE");
  assert.equal(resolved.capabilities.walletMessaging.delayedWalletMessages, true);
  assert.equal(resolved.capabilities.features.jackpotHooks, true);
  assert.equal(resolved.jackpotHooks.enabled, true);
  assert.equal(resolved.sessionUi.showSessionTimer, true);
});

test("resolves final maxBet with min(GL_MAX_BET, exposureDerivedMaxBet)", () => {
  const input = createInput();
  input.launchParams = {
    maxBet: 5000,
    defaultBet: 1200,
    GL_MAX_BET: 1000,
    exposureDerivedMaxBet: 750,
  };

  const resolved = resolveConfig(input);
  assert.equal(resolved.maxBet, 750);
  assert.equal(resolved.defaultBet, 750);
});

test("throws on invalid constraints", () => {
  const input = createInput();
  input.bankProperties.minBet = 6000;
  input.bankProperties.maxBet = 5000;

  assert.throws(() => resolveConfig(input), /minBet cannot be greater than maxBet/);
});

test("clamps maxBet when maxBet exceeds exposure-derived cap", () => {
  const input = createInput();
  input.bankProperties.maxBet = 6000;
  input.bankProperties.maxExposure = 5000;

  const resolved = resolveConfig(input);
  assert.equal(resolved.maxBet, 5000);
});

test("throws when history URL uses javascript scheme", () => {
  const input = createInput();
  input.launchParams = {
    history: {
      enabled: true,
      url: "javascript:alert(1)",
      openInSameWindow: true,
    },
  };

  assert.throws(() => resolveConfig(input), /history URL cannot use javascript/);
});

console.log(`\nConfigResolver tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}


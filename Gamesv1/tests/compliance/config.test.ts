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

test("throws on invalid constraints", () => {
  const input = createInput();
  input.bankProperties.minBet = 6000;
  input.bankProperties.maxBet = 5000;

  assert.throws(() => resolveConfig(input), /minBet cannot be greater than maxBet/);
});

test("throws when maxBet exceeds maxExposure", () => {
  const input = createInput();
  input.bankProperties.maxBet = 6000;
  input.bankProperties.maxExposure = 5000;

  assert.throws(() => resolveConfig(input), /maxBet cannot exceed maxExposure/);
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


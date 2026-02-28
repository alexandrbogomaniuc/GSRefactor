import assert from "node:assert/strict";

import {
  AnimationPolicyEngine,
  createAnimationPolicy,
} from "../../packages/core-compliance/src/animation/AnimationPolicy.ts";

const createEngine = (overrides?: {
  forcedSkipWinPresentation?: boolean;
  lowPerformanceMode?: boolean;
}) => {
  const policy = createAnimationPolicy({
    runtimeConfig: {
      turboplay: {
        allowed: true,
        speedId: "turbo-x3",
        preferred: false,
      },
      minReelSpinTime: {
        normalMs: 2000,
        turboMs: 1200,
      },
    },
    forcedSkipWinPresentation: overrides?.forcedSkipWinPresentation,
    lowPerformanceMode: overrides?.lowPerformanceMode,
    bigWinThresholds: {
      bigMultiplier: 8,
      hugeMultiplier: 20,
      megaMultiplier: 40,
    },
    autoplay: {
      minDelayBetweenSpinStartsMs: 250,
      delayAfterSkipMs: 120,
      delayAfterWinByTierMs: {
        none: 300,
        big: 900,
        huge: 1200,
        mega: 1800,
      },
    },
  });

  return new AnimationPolicyEngine(policy);
};

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

test("resolves normal spin timing without turbo", () => {
  const engine = createEngine();
  const timing = engine.resolveSpinTiming(false);

  assert.equal(timing.turboEnabled, false);
  assert.equal(timing.minSpinMs, 2000);
  assert.equal(timing.speedMultiplier, 1);
});

test("resolves turbo timing with speed profile", () => {
  const engine = createEngine();
  const timing = engine.resolveSpinTiming(true);

  assert.equal(timing.turboEnabled, true);
  assert.equal(timing.speed_id, "turbo-x3");
  assert.equal(timing.speedMultiplier, 3);
  assert.equal(timing.minSpinMs, 1200);
});

test("classifies win tiers by multiplier thresholds", () => {
  const engine = createEngine();

  assert.equal(engine.classifyWinByMultiplier(2), "none");
  assert.equal(engine.classifyWinByMultiplier(8), "big");
  assert.equal(engine.classifyWinByMultiplier(20), "huge");
  assert.equal(engine.classifyWinByMultiplier(40), "mega");
});

test("forced skip yields zero presentation duration", () => {
  const engine = createEngine({ forcedSkipWinPresentation: true });
  assert.equal(engine.shouldAllowForcedSkip(), true);
  assert.equal(engine.getWinPresentationDurationMs("mega", false), 0);
});

test("low performance mode disables heavy win fx", () => {
  const engine = createEngine({ lowPerformanceMode: true });
  assert.equal(engine.shouldPlayHeavyWinFx("none"), false);
  assert.equal(engine.shouldPlayHeavyWinFx("mega"), false);
});

test("autoplay delay respects timing contract", () => {
  const engine = createEngine();

  assert.equal(engine.getAutoplayDelayMs("none", false), 300);
  assert.equal(engine.getAutoplayDelayMs("big", false), 900);
  // skip delay is clamped to minDelayBetweenSpinStartsMs
  assert.equal(engine.getAutoplayDelayMs("mega", true), 250);
});

console.log(`\nAnimationPolicy tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}
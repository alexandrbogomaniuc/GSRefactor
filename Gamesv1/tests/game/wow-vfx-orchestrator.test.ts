import assert from "node:assert/strict";

import {
  AnimationPolicyEngine,
  createAnimationPolicy,
} from "../../packages/core-compliance/src/animation/AnimationPolicy.ts";
import { DefaultResolvedRuntimeConfig } from "../../packages/core-compliance/src/ResolvedRuntimeConfig.ts";
import {
  WowVfxOrchestrator,
  type PresentationWinTier,
} from "../../packages/ui-kit/src/shell/vfx/index.ts";

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

const makeHarness = (input?: {
  lowPerformanceMode?: boolean;
  forcedSkipWinPresentation?: boolean;
}) => {
  const runtimeConfig = structuredClone(DefaultResolvedRuntimeConfig);
  const animationPolicy = new AnimationPolicyEngine(
    createAnimationPolicy({
      runtimeConfig,
      lowPerformanceMode: input?.lowPerformanceMode,
      forcedSkipWinPresentation: input?.forcedSkipWinPresentation,
    }),
  );

  const events: string[] = [];

  const orchestrator = new WowVfxOrchestrator(animationPolicy, {
    onAudioCue: (cue) => events.push(`audio:${cue}`),
    onAnimationCue: (cue) => events.push(`anim:${cue}`),
    showWinCounter: (_amount, title, tier) => events.push(`counter:${title}:${tier}`),
    hideWinCounter: () => events.push("counter:hide"),
    showHeavyWinFx: (_symbols, tier) => events.push(`heavy:${tier}`),
    clearHeavyWinFx: () => events.push("heavy:clear"),
    playCoinBurst: (_origin, tier) => events.push(`burst:${tier}`),
  });

  return {
    orchestrator,
    events,
  };
};

const makeInput = (winAmountMinor: number, animationCues: string[] = []) => ({
  winAmountMinor,
  defaultBetMinor: 20,
  winSymbols: [] as never[],
  soundCues: ["jackpot-stinger"],
  animationCues,
});

const assertTier = (actual: PresentationWinTier, expected: PresentationWinTier) => {
  assert.equal(actual, expected);
};

test("maps zero/small/big/huge/mega presentation tiers", () => {
  const { orchestrator } = makeHarness();

  const zero = orchestrator.startWinPresentation(makeInput(0));
  assertTier(zero.tier, "none");
  assert.equal(zero.hasWinPresentation, false);
  assert.equal(zero.durationMs, 0);

  assertTier(orchestrator.startWinPresentation(makeInput(40)).tier, "normal");
  assertTier(orchestrator.startWinPresentation(makeInput(200)).tier, "big");
  assertTier(orchestrator.startWinPresentation(makeInput(500)).tier, "huge");
  assertTier(orchestrator.startWinPresentation(makeInput(1000)).tier, "mega");
});

test("orchestrates audio, animation, heavy fx, and win counter", () => {
  const { orchestrator, events } = makeHarness();

  const state = orchestrator.startWinPresentation(makeInput(1000, ["jackpot-overlay"]));

  assert.equal(state.heavyFxPlayed, true);
  assert.equal(state.hasWinPresentation, true);
  assert.ok(events.includes("audio:jackpot-stinger"));
  assert.ok(events.includes("anim:jackpot-overlay"));
  assert.ok(events.includes("heavy:mega"));
  assert.ok(events.includes("burst:mega"));
  assert.ok(events.includes("counter:MEGA WIN:mega"));

  orchestrator.finishWinPresentation();
  assert.ok(events.includes("counter:hide"));
  assert.ok(events.includes("heavy:clear"));
});

test("low-performance mode suppresses heavy win fx", () => {
  const { orchestrator, events } = makeHarness({ lowPerformanceMode: true });

  const state = orchestrator.startWinPresentation(makeInput(1000));

  assert.equal(state.heavyFxPlayed, false);
  assert.equal(state.hasWinPresentation, true);
  assert.ok(events.includes("heavy:clear"));
  assert.equal(events.includes("heavy:mega"), false);
  assert.equal(events.includes("burst:mega"), false);
});

test("force-skip animation cue returns zero duration", () => {
  const { orchestrator } = makeHarness();

  const state = orchestrator.startWinPresentation(
    makeInput(200, ["force-skip-presentation"]),
  );

  assert.equal(state.forcedSkip, true);
  assert.equal(state.durationMs, 0);
  assert.equal(state.heavyFxPlayed, false);
});

test("zero-win rounds do not show win counter or heavy effects", () => {
  const { orchestrator, events } = makeHarness();

  const state = orchestrator.startWinPresentation(makeInput(0, ["reel-stop"]));

  assert.equal(state.tier, "none");
  assert.equal(state.hasWinPresentation, false);
  assert.equal(
    events.some((entry) => /^counter:(WIN|BIG WIN|HUGE WIN|MEGA WIN):/.test(entry)),
    false,
  );
  assert.equal(events.includes("heavy:big"), false);
  assert.equal(events.includes("heavy:huge"), false);
  assert.equal(events.includes("heavy:mega"), false);
  assert.ok(events.includes("heavy:clear"));
  assert.equal(events.some((entry) => entry.startsWith("burst:")), false);
});

console.log(`\nWOW/VFX tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

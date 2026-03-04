import assert from "node:assert/strict";

import { hasRowGaps } from "../../packages/pixi-layout/src/index.ts";
import { computeHudLayout } from "../../packages/ui-kit/src/layout/HudLayout.ts";

const baseItems = [
  { id: "spin", width: 210, height: 100, visible: true },
  { id: "turbo", width: 190, height: 84, visible: true },
  { id: "autoplay", width: 190, height: 84, visible: true },
  { id: "buyFeature", width: 190, height: 84, visible: true },
  { id: "sound", width: 190, height: 84, visible: true },
  { id: "settings", width: 190, height: 84, visible: true },
  { id: "history", width: 190, height: 84, visible: true },
];

const matrix = [
  {
    name: "phone-portrait",
    width: 390,
    height: 844,
    safeArea: { top: 44, right: 0, bottom: 34, left: 0 },
  },
  {
    name: "phone-landscape",
    width: 844,
    height: 390,
    safeArea: { top: 0, right: 44, bottom: 21, left: 44 },
  },
  {
    name: "tablet-portrait",
    width: 768,
    height: 1024,
    safeArea: { top: 24, right: 0, bottom: 20, left: 0 },
  },
  {
    name: "tablet-landscape",
    width: 1024,
    height: 768,
    safeArea: { top: 0, right: 24, bottom: 20, left: 24 },
  },
  { name: "desktop", width: 1440, height: 900, safeArea: { top: 0, right: 0, bottom: 0, left: 0 } },
  {
    name: "ultrawide",
    width: 2560,
    height: 1080,
    safeArea: { top: 0, right: 0, bottom: 0, left: 0 },
  },
] as const;

const visibilityScenarios = [
  {
    name: "all-controls-visible",
    resolver: (_itemId: string) => true,
    expectedCount: 7,
  },
  {
    name: "minimal-controls-visible",
    resolver: (itemId: string) => itemId === "spin",
    expectedCount: 1,
  },
  {
    name: "dynamic-buy-feature-only",
    resolver: (itemId: string) => itemId !== "buyFeature",
    expectedCount: 6,
  },
  {
    name: "dynamic-turbo-history-autoplay-hidden",
    resolver: (itemId: string) =>
      itemId !== "turbo" && itemId !== "autoplay" && itemId !== "history",
    expectedCount: 4,
  },
  {
    name: "mixed-hidden-no-gaps",
    resolver: (itemId: string) => itemId !== "buyFeature" && itemId !== "history",
    expectedCount: 5,
  },
] as const;

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

const buildLayout = (
  width: number,
  height: number,
  safeArea: { top: number; right: number; bottom: number; left: number },
  resolver: (itemId: string) => boolean,
) =>
  computeHudLayout(
    baseItems.map((item) => ({
      ...item,
      visible: resolver(item.id),
    })),
    {
      width,
      height,
      orientation: height >= width ? "portrait" : "landscape",
      safeArea,
    },
  ).layout;

for (const scenario of matrix) {
  for (const visibilityScenario of visibilityScenarios) {
    test(`layout matrix ${scenario.name} ${visibilityScenario.name}`, () => {
      const layout = buildLayout(
        scenario.width,
        scenario.height,
        scenario.safeArea,
        visibilityScenario.resolver,
      );

      assert.equal(layout.items.length, visibilityScenario.expectedCount);
      assert.equal(hasRowGaps(layout), false);

      for (const item of layout.items) {
        assert.ok(item.x >= scenario.safeArea.left);
        assert.ok(item.x + item.width <= scenario.width - scenario.safeArea.right + 1);
        assert.ok(item.y >= 0);
      }
    });
  }

  test(`layout matrix ${scenario.name} dynamic-buy-feature-visibility`, () => {
    const withBuyFeature = buildLayout(
      scenario.width,
      scenario.height,
      scenario.safeArea,
      (_itemId) => true,
    );

    const withoutBuyFeature = buildLayout(
      scenario.width,
      scenario.height,
      scenario.safeArea,
      (itemId) => itemId !== "buyFeature",
    );

    assert.equal(withBuyFeature.items.length, withoutBuyFeature.items.length + 1);
    assert.equal(hasRowGaps(withBuyFeature), false);
    assert.equal(hasRowGaps(withoutBuyFeature), false);
  });
}

console.log(`\nLayout matrix tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

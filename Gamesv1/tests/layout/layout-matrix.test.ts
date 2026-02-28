import assert from "node:assert/strict";

import { hasRowGaps } from "../../packages/pixi-layout/src/index.ts";
import { computeHudLayout } from "../../packages/ui-kit/src/layout/HudLayout.ts";

const baseItems = [
  { id: "spin", width: 210, height: 100, visible: true },
  { id: "turbo", width: 190, height: 84, visible: true },
  { id: "autoplay", width: 190, height: 84, visible: true },
  { id: "buybonus", width: 190, height: 84, visible: true },
  { id: "pause", width: 68, height: 68, visible: true },
  { id: "settings", width: 68, height: 68, visible: true },
];

const matrix = [
  { name: "phone-portrait", width: 390, height: 844, safeArea: { top: 44, right: 0, bottom: 34, left: 0 } },
  { name: "phone-landscape", width: 844, height: 390, safeArea: { top: 0, right: 44, bottom: 21, left: 44 } },
  { name: "tablet-portrait", width: 768, height: 1024, safeArea: { top: 24, right: 0, bottom: 20, left: 0 } },
  { name: "tablet-landscape", width: 1024, height: 768, safeArea: { top: 0, right: 24, bottom: 20, left: 24 } },
  { name: "desktop", width: 1440, height: 900, safeArea: { top: 0, right: 0, bottom: 0, left: 0 } },
  { name: "ultrawide", width: 2560, height: 1080, safeArea: { top: 0, right: 0, bottom: 0, left: 0 } },
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

for (const scenario of matrix) {
  test(`layout matrix ${scenario.name}`, () => {
    const visibleItems = baseItems.map((item) => ({
      ...item,
      visible: item.id !== "buybonus" && item.id !== "autoplay",
    }));

    const { layout } = computeHudLayout(visibleItems, {
      width: scenario.width,
      height: scenario.height,
      orientation: scenario.height >= scenario.width ? "portrait" : "landscape",
      safeArea: scenario.safeArea,
    });

    assert.equal(layout.items.length, 4);
    assert.equal(hasRowGaps(layout), false);

    for (const item of layout.items) {
      assert.ok(item.x >= scenario.safeArea.left);
      assert.ok(item.x + item.width <= scenario.width - scenario.safeArea.right + 1);
      assert.ok(item.y >= 0);
    }
  });
}

console.log(`\nLayout matrix tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}
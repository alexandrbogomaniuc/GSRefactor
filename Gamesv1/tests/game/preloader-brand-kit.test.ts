import assert from "node:assert/strict";

import {
  DefaultShellThemeTokens,
  resolveShellThemeTokens,
} from "@gamesv1/ui-kit";
import { resolvePremiumSlotBrandKit } from "../../games/premium-slot/src/app/theme/brandKit.ts";

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

test("shell theme tokens provide strict defaults", () => {
  const theme = resolveShellThemeTokens({
    overrides: {
      brand: {
        displayName: "Aurora Vault",
      },
    },
  });

  assert.equal(theme.brand.displayName, "Aurora Vault");
  assert.equal(theme.preloader.style, DefaultShellThemeTokens.preloader.style);
  assert.equal(
    theme.preloader.heroFx,
    DefaultShellThemeTokens.preloader.heroFx,
  );
  assert.equal(
    theme.preloader.vfxIntensity,
    DefaultShellThemeTokens.preloader.vfxIntensity,
  );
});

test("shell theme tokens reject invalid inputs", () => {
  assert.throws(
    () =>
      resolveShellThemeTokens({
        overrides: {
          brand: {
            displayName: "",
          },
        },
      }),
    /Too small/,
  );

  assert.throws(
    () =>
      resolveShellThemeTokens({
        overrides: {
          brand: {
            displayName: "Broken",
            primaryColor: "hotpink",
          },
        },
      }),
    /Expected #RRGGBB or #RRGGBBAA/,
  );
});

test("premium-slot brand presets expose two visibly distinct preloader kits", () => {
  const brandA = resolvePremiumSlotBrandKit("A");
  const brandB = resolvePremiumSlotBrandKit("B");

  assert.equal(brandA.brand.displayName, "Aurora Vault");
  assert.equal(brandA.preloader.style, "wow");
  assert.equal(brandA.preloader.heroFx, "energyRing");
  assert.ok(brandA.brand.logoAssetKey);

  assert.equal(brandB.brand.displayName, "Neon Harbor");
  assert.equal(brandB.preloader.style, "minimal");
  assert.equal(brandB.preloader.heroFx, "slotSweep");
  assert.ok(brandB.brand.logoUrl?.startsWith("data:image/svg+xml"));
});

console.log(`\nPreloader brand-kit tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

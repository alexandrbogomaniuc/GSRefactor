import {
  resolveShellThemeTokens,
  type ShellThemeTokens,
} from "@gamesv1/ui-kit/shell";
import type { ResolvedConfig } from "@gamesv1/core-compliance";

import {
  getProviderPackStatus,
  resolveProviderWordmarkUrl,
} from "../assets/providerPackRegistry.ts";
import {
  CRAZY_ROOSTER_BRAND,
  CRAZY_ROOSTER_BRAND_NAME,
  CRAZY_ROOSTER_BUY_TIERS,
  CRAZY_ROOSTER_LAYOUT,
  CRAZY_ROOSTER_PAYLINES,
} from "../../game/config/CrazyRoosterGameConfig.ts";

export const resolveCrazyRoosterBrandKit = (
  brandParam: string | null,
  runtimeConfig?: ResolvedConfig,
  queryParams = new URLSearchParams(window.location.search),
): ShellThemeTokens =>
  resolveShellThemeTokens({
    runtimeConfig,
    queryParams,
    overrides: {
      metadata: {
        themeId: CRAZY_ROOSTER_BRAND.themeId,
        skinId: `provider-${getProviderPackStatus().effectiveProvider}`,
      },
      brand: {
        displayName: brandParam?.trim() || CRAZY_ROOSTER_BRAND_NAME,
        logoUrl: resolveProviderWordmarkUrl(),
        primaryColor: CRAZY_ROOSTER_BRAND.primaryColor,
        accentColor: CRAZY_ROOSTER_BRAND.accentColor,
      },
      hud: {
        visualStyle: "premium-default",
        panelAlpha: 0.96,
        metricAccentColor: 0xffffff,
        controlSkinHooks: {
          spin: "emphasis",
          buyFeature: "emphasis",
          autoplay: "muted",
          turbo: "muted",
        },
      },
      preloader: {
        style: "wow",
        heroFx: "energyRing",
        vfxIntensity: 0.88,
        audioStingerCue: "preloader-stinger",
      },
      roundActions: {
        bet: {
          lineCount: CRAZY_ROOSTER_PAYLINES.length,
          multiplier: 1,
          minCoinValueMinor: 1,
        },
        buyFeature: {
          featureType: "BUY_FEATURE",
          action: "CONFIRM",
          priceMultiplier: CRAZY_ROOSTER_BUY_TIERS[0].priceMultiplier,
          payloadDefaults: {
            source: "crazy-rooster",
            tierId: CRAZY_ROOSTER_BUY_TIERS[0].id,
          },
        },
      },
      winPresentation: {
        tierLabels: {
          big: "BIG WIN",
          huge: "HUGE WIN",
          mega: "MEGA WIN",
        },
        tierStyleHooks: {
          big: "subtle",
          huge: "neon",
          mega: "intense",
        },
      },
      winTargets: {
        reelCount: CRAZY_ROOSTER_LAYOUT.reelCount,
        rowCount: CRAZY_ROOSTER_LAYOUT.rowCount,
        highlightRowIndex: 1,
        highlightReelIndexes: [0, 1, 2],
      },
      vfx: {
        intensity: "normal",
        heavyFxEnabled: true,
        coinBurstEnabled: true,
      },
    },
  });

export const CRAZY_ROOSTER_TITLE = CRAZY_ROOSTER_BRAND.displayName;

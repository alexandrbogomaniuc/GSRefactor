import {
  resolveShellThemeTokens,
  type ShellThemeTokens,
} from "../../../../../packages/ui-kit/src/shell/theme/ShellThemeTokens.ts";

import { AppAssetKeys } from "../assets/assetKeys.ts";

const createInlineLogoUrl = (label: string, primaryColor: string, accentColor: string): string => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 120">
      <defs>
        <linearGradient id="brand-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${primaryColor}" />
          <stop offset="100%" stop-color="${accentColor}" />
        </linearGradient>
      </defs>
      <rect x="6" y="6" width="308" height="108" rx="28" fill="#0f1620" stroke="url(#brand-gradient)" stroke-width="10" />
      <circle cx="62" cy="60" r="26" fill="url(#brand-gradient)" opacity="0.95" />
      <text x="110" y="73" font-family="Trebuchet MS, sans-serif" font-size="42" font-weight="800" fill="url(#brand-gradient)">${label}</text>
    </svg>
  `.trim();

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
};

const brandThemes: Record<string, ShellThemeTokens> = {
  A: resolveShellThemeTokens({
    brand: {
      displayName: "Aurora Vault",
      logoAssetKey: AppAssetKeys.LOGO_PRELOAD,
      primaryColor: "#F3C35C",
      accentColor: "#FF4F8E",
    },
    preloader: {
      style: "wow",
      heroFx: "energyRing",
      vfxIntensity: 0.88,
      audioStingerCue: "preloader-stinger",
    },
  }),
  B: resolveShellThemeTokens({
    brand: {
      displayName: "Neon Harbor",
      logoUrl: createInlineLogoUrl("NH", "#6AE8FF", "#6BFFB4"),
      primaryColor: "#6AE8FF",
      accentColor: "#6BFFB4",
    },
    preloader: {
      style: "minimal",
      heroFx: "slotSweep",
      vfxIntensity: 0.34,
    },
  }),
};

export const resolvePremiumSlotBrandKit = (brandParam: string | null): ShellThemeTokens => {
  const normalized = brandParam?.trim().toUpperCase();

  if (!normalized) {
    return brandThemes.A;
  }

  if (brandThemes[normalized]) {
    return brandThemes[normalized];
  }

  return resolveShellThemeTokens({
    brand: {
      ...brandThemes.A.brand,
      displayName: brandParam!.trim(),
    },
    preloader: brandThemes.A.preloader,
  });
};

import { UiAssetKeys } from "../../assets.ts";

export interface SfxAudioCueAction {
  type: "sfx";
  assetKey: string;
  volume?: number;
  respectSoundEnabled?: boolean;
}

export interface BgmVolumeAudioCueAction {
  type: "bgmVolume";
  volume: number;
}

export type AudioCueAction = SfxAudioCueAction | BgmVolumeAudioCueAction;

export type AudioCueRegistry = Record<string, AudioCueAction[]>;

export interface AudioCueRegistryBuildOptions {
  overrides?: Partial<AudioCueRegistry>;
  themedOverrides?: Record<string, Partial<AudioCueRegistry>>;
  themeId?: string;
  skinId?: string;
}

export interface AudioCueRuntime {
  playSfx: (assetKey: string, options: { volume: number }) => void;
  setBgmVolume: (volume: number) => void;
  isSoundEnabled?: () => boolean;
}

export const DefaultAudioCueRegistry: AudioCueRegistry = {
  "jackpot-stinger": [
    {
      type: "sfx",
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 1,
      respectSoundEnabled: true,
    },
  ],
  "mute-bgm": [
    {
      type: "bgmVolume",
      volume: 0,
    },
  ],
  "preloader-stinger": [
    {
      type: "sfx",
      assetKey: UiAssetKeys.SFX_HOVER,
      volume: 0.9,
      respectSoundEnabled: true,
    },
  ],
};

const hasBuildOptionsShape = (
  value: Partial<AudioCueRegistry> | AudioCueRegistryBuildOptions,
): value is AudioCueRegistryBuildOptions =>
  typeof value === "object" &&
  value !== null &&
  ("overrides" in value ||
    "themedOverrides" in value ||
    "themeId" in value ||
    "skinId" in value);

const mergeRegistry = (
  base: AudioCueRegistry,
  patch: Partial<AudioCueRegistry> | undefined,
): AudioCueRegistry => {
  if (!patch) {
    return base;
  }

  const merged: AudioCueRegistry = { ...base };
  for (const [cue, actions] of Object.entries(patch)) {
    if (!actions) {
      continue;
    }
    merged[cue] = actions;
  }
  return merged;
};

export const createAudioCueRegistry = (
  options: Partial<AudioCueRegistry> | AudioCueRegistryBuildOptions = {},
): AudioCueRegistry => {
  const buildOptions = hasBuildOptionsShape(options) ? options : { overrides: options };
  const themeKeys = [
    buildOptions.themeId,
    buildOptions.themeId && buildOptions.skinId
      ? `${buildOptions.themeId}:${buildOptions.skinId}`
      : undefined,
    buildOptions.skinId,
  ].filter((value): value is string => typeof value === "string" && value.length > 0);

  let merged: AudioCueRegistry = { ...DefaultAudioCueRegistry };
  for (const key of themeKeys) {
    merged = mergeRegistry(merged, buildOptions.themedOverrides?.[key]);
  }

  merged = mergeRegistry(merged, buildOptions.overrides);
  return merged;
};

export const resolveAudioCueActions = (
  cue: string,
  registry: AudioCueRegistry = DefaultAudioCueRegistry,
): AudioCueAction[] => registry[cue] ?? [];

export const applyAudioCue = (
  cue: string,
  runtime: AudioCueRuntime,
  registry: AudioCueRegistry = DefaultAudioCueRegistry,
): AudioCueAction[] => {
  const actions = resolveAudioCueActions(cue, registry);

  for (const action of actions) {
    if (action.type === "sfx") {
      if (action.respectSoundEnabled && runtime.isSoundEnabled?.() === false) {
        continue;
      }
      runtime.playSfx(action.assetKey, { volume: action.volume ?? 1 });
      continue;
    }

    runtime.setBgmVolume(action.volume);
  }

  return actions;
};

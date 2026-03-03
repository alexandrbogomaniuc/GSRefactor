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
};

export const createAudioCueRegistry = (
  overrides: Partial<AudioCueRegistry> = {},
): AudioCueRegistry => {
  const merged: AudioCueRegistry = { ...DefaultAudioCueRegistry };

  for (const [cue, actions] of Object.entries(overrides)) {
    if (!actions) {
      continue;
    }
    merged[cue] = actions;
  }

  return merged;
};

export const resolveAudioCueActions = (
  cue: string,
  registry: AudioCueRegistry = DefaultAudioCueRegistry,
): AudioCueAction[] => registry[cue] ?? [];

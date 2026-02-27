export interface FeatureFlags {
    turboplayAllowed: boolean;
    turboplaySpeedMultiplier: number; // e.g., 2.0x
    autoplayAllowed: boolean;
    forcedSpinStopAllowed: boolean;
    buyFeatureDisabled: boolean;
    spinProfilingEnabled: boolean;
    postMessageEnabled: boolean; // POST_MESSAGE_TO_OPENER
    minReelsSpinningTimeSecs: number; // usual_flow_min_reels_spinning_time_in_secs
}

export const DefaultFeatureFlags: FeatureFlags = {
    turboplayAllowed: true,
    turboplaySpeedMultiplier: 2.0,
    autoplayAllowed: true,
    forcedSpinStopAllowed: true,
    buyFeatureDisabled: false,
    spinProfilingEnabled: false,
    postMessageEnabled: true,
    minReelsSpinningTimeSecs: 1.5,
};

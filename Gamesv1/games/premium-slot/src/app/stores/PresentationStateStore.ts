export interface PresentationState {
  isSpinning: boolean;
  isPresentingWin: boolean;
  turboSelected: boolean;
  soundEnabled: boolean;
  statusText: string;
  lastWinAmount: number;
  lastMessages: string[];
  activeFeatureModules: string[];
}

let state: PresentationState = {
  isSpinning: false,
  isPresentingWin: false,
  turboSelected: false,
  soundEnabled: true,
  statusText: "",
  lastWinAmount: 0,
  lastMessages: [],
  activeFeatureModules: [],
};

export const PresentationStateStore = {
  set(next: PresentationState): void {
    state = { ...next };
  },

  patch(patch: Partial<PresentationState>): void {
    state = { ...state, ...patch };
  },

  get(): PresentationState {
    return { ...state };
  },

  reset(): void {
    state = {
      isSpinning: false,
      isPresentingWin: false,
      turboSelected: false,
      soundEnabled: true,
      statusText: "",
      lastWinAmount: 0,
      lastMessages: [],
      activeFeatureModules: [],
    };
  },
};

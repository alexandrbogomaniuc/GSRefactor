export interface PresentationState {
  isSpinning: boolean;
  isPresentingWin: boolean;
  turboSelected: boolean;
  statusText: string;
  lastWinAmount: number;
}

let state: PresentationState = {
  isSpinning: false,
  isPresentingWin: false,
  turboSelected: false,
  statusText: "",
  lastWinAmount: 0,
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
      statusText: "",
      lastWinAmount: 0,
    };
  },
};

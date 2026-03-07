import type { BootstrapRef } from "@gamesv1/core-protocol";

// Canonical mutating runtime envelope store.
// This store intentionally excludes bootstrap config/policy/auth context.
export interface SessionRuntimeState {
  sessionId: string;
  balance: number;
  requestCounter: number;
  stateVersion: number;
  bootstrapRef: BootstrapRef;
  resumeRef?: string | Record<string, unknown>;
  unfinishedRound?: unknown;
  lastRoundId?: string;
}

let state: SessionRuntimeState | null = null;

const assertState = (): SessionRuntimeState => {
  if (!state) {
    throw new Error("SessionRuntimeStore is not initialized.");
  }
  return state;
};

export const SessionRuntimeStore = {
  set(next: SessionRuntimeState): void {
    state = { ...next };
    console.log("[SessionRuntimeStore] Snapshot updated.", {
      sessionId: state.sessionId,
      requestCounter: state.requestCounter,
      balance: state.balance,
    });
  },

  patch(patch: Partial<SessionRuntimeState>): void {
    const current = assertState();
    state = { ...current, ...patch };
  },

  clear(): void {
    state = null;
  },

  get(): SessionRuntimeState {
    return assertState();
  },

  getSnapshot(): SessionRuntimeState | null {
    return state ? { ...state } : null;
  },
};

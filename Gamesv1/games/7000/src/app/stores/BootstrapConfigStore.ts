import type { BootstrapResponse } from "@gamesv1/core-protocol";

type JsonRecord = Record<string, unknown>;

const isRecord = (value: unknown): value is JsonRecord =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asRecord = (value: unknown): JsonRecord => (isRecord(value) ? value : {});
const cloneRecord = (value: JsonRecord): JsonRecord =>
  JSON.parse(JSON.stringify(value)) as JsonRecord;
const asString = (value: unknown): string | undefined =>
  typeof value === "string" && value.length > 0 ? value : undefined;
const asNumber = (value: unknown): number | undefined =>
  typeof value === "number" && Number.isFinite(value) ? value : undefined;

export interface BootstrapSessionState {
  sessionId: string;
  requestCounter: number;
  stateVersion: number;
}

export interface BootstrapConfigState {
  contractVersion: "slot-bootstrap-v1";
  session: BootstrapSessionState;
  context: JsonRecord;
  assets: JsonRecord;
  runtime: JsonRecord;
  policies: JsonRecord;
  integrity: JsonRecord;
}

let state: BootstrapConfigState | null = null;

const assertState = (): BootstrapConfigState => {
  if (!state) {
    throw new Error("BootstrapConfigStore is not initialized.");
  }
  return state;
};

export const BootstrapConfigStore = {
  set(next: BootstrapConfigState): void {
    state = { ...next };
  },

  hydrateFromBootstrap(snapshot: BootstrapResponse): void {
    const session = asRecord(snapshot.session);
    const sessionId = asString(session.sessionId);
    const requestCounter = asNumber(session.requestCounter);
    const stateVersion = asNumber(session.stateVersion);

    if (!sessionId || requestCounter === undefined || stateVersion === undefined) {
      throw new Error(
        "BootstrapConfigStore requires bootstrap.session to include sessionId/requestCounter/stateVersion.",
      );
    }

    state = {
      contractVersion: snapshot.contractVersion,
      session: {
        sessionId,
        requestCounter,
        stateVersion,
      },
      context: cloneRecord(asRecord(snapshot.context)),
      assets: cloneRecord(asRecord(snapshot.assets)),
      runtime: cloneRecord(asRecord(snapshot.runtime)),
      policies: cloneRecord(asRecord(snapshot.policies)),
      integrity: cloneRecord(asRecord(snapshot.integrity)),
    };
  },

  get(): BootstrapConfigState {
    return assertState();
  },

  getSnapshot(): BootstrapConfigState | null {
    return state ? { ...state } : null;
  },

  clear(): void {
    state = null;
  },
};

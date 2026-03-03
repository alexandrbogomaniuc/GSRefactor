import {
  createTransport,
  type BootstrapRef,
  type BootstrapResponse,
  type FeatureActionResponse,
  type GameInitConfig,
  type HistoryQuery,
  type IGameTransport,
  type OpenGameResponse,
  type PlayRoundResponse,
  type SelectedBet,
  type SelectedFeatureChoice,
} from "@gamesv1/core-protocol";

import { BootstrapConfigStore } from "../stores/BootstrapConfigStore";
import { SessionRuntimeStore } from "../stores/SessionRuntimeStore";

const CONTRACT_VERSION = "slot-browser-v1";
const DEFAULT_API_BASE = "http://127.0.0.1:6400";
const DEFAULT_GAME_ID = 10;

type JsonRecord = Record<string, unknown>;

const isRecord = (value: unknown): value is JsonRecord =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asRecord = (value: unknown): JsonRecord => (isRecord(value) ? value : {});

const asString = (value: unknown): string | undefined =>
  typeof value === "string" && value.length > 0 ? value : undefined;

const asNumber = (value: unknown): number | undefined =>
  typeof value === "number" && Number.isFinite(value) ? value : undefined;

const cloneRecord = (value: JsonRecord): JsonRecord =>
  JSON.parse(JSON.stringify(value)) as JsonRecord;

const asBootstrapRef = (value: unknown): BootstrapRef | undefined => {
  if (isRecord(value)) {
    const configId = asString(value.configId);
    const clientPackageVersion = asString(value.clientPackageVersion);
    const mathPackageVersion = asString(value.mathPackageVersion);
    if (!configId || !clientPackageVersion) {
      return undefined;
    }
    return {
      configId,
      clientPackageVersion,
      ...(mathPackageVersion ? { mathPackageVersion } : {}),
    };
  }
  return undefined;
};

const asResumeRef = (value: unknown): string | JsonRecord | undefined => {
  if (isRecord(value)) {
    return cloneRecord(value);
  }
  return asString(value);
};

const readUrlNumber = (value: string | null): number | undefined => {
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
};

const extractBootstrapRefFromRuntimeEnvelope = (
  envelope: OpenGameResponse,
): BootstrapRef | undefined => {
  const payload = asRecord(envelope.presentationPayload);
  return asBootstrapRef(payload.bootstrapRef);
};

const extractResumeRefFromRuntimeEnvelope = (
  envelope: OpenGameResponse,
): string | JsonRecord | undefined => {
  const restore = asRecord(envelope.restore);
  return asResumeRef(restore.resumeRef);
};

const extractWalletBalanceMinor = (envelope: OpenGameResponse): number => {
  const wallet = asRecord(envelope.wallet);
  return asNumber(wallet.balanceMinor) ?? 0;
};

const extractRoundId = (envelope: PlayRoundResponse): string | undefined => {
  const round = asRecord(envelope.round);
  return asString(round.roundId);
};

const extractHistoryItems = (envelope: OpenGameResponse): Array<Record<string, unknown>> => {
  const feature = asRecord(envelope.feature);
  const history = feature.history;
  if (!Array.isArray(history)) {
    return [];
  }
  return history.filter(
    (entry): entry is Record<string, unknown> => typeof entry === "object" && entry !== null,
  );
};

const extractUnfinishedRound = (envelope: OpenGameResponse): unknown => {
  const restore = asRecord(envelope.restore);
  if (restore.hasUnfinishedRound === true) {
    return restore;
  }
  return undefined;
};

const extractBootstrapSessionId = (snapshot: BootstrapResponse): string | undefined =>
  asString(asRecord(snapshot.session).sessionId);

const extractBootstrapRequestCounter = (snapshot: BootstrapResponse): number =>
  asNumber(asRecord(snapshot.session).requestCounter) ?? 0;

const extractBootstrapStateVersion = (snapshot: BootstrapResponse): number =>
  asNumber(asRecord(snapshot.session).stateVersion) ?? 0;

const extractBootstrapBalanceMinor = (_snapshot: BootstrapResponse): number =>
  0;

const extractBootstrapRefFromBootstrap = (
  _snapshot: BootstrapResponse,
): BootstrapRef | undefined =>
  undefined;

const extractResumeRefFromBootstrap = (
  snapshot: BootstrapResponse,
): string | JsonRecord | undefined =>
  asResumeRef(asRecord(asRecord(snapshot.runtime).unfinishedRound).resumeRef);

const readLaunchParams = () => {
  const params = new URLSearchParams(window.location.search);
  const sessionId =
    params.get("sid") ??
    params.get("SID") ??
    params.get("sessionId") ??
    params.get("SESSIONID") ??
    "";

  const bankId = readUrlNumber(params.get("bankId") ?? params.get("BANKID"));
  const gameId =
    readUrlNumber(
      params.get("gameIdNumeric") ??
        params.get("GAMEIDNUMERIC") ??
        params.get("gameId") ??
        params.get("GAMEID"),
    ) ?? DEFAULT_GAME_ID;

  const apiBase =
    params.get("ngsApiUrl") ??
    params.get("NGSAPIURL") ??
    import.meta.env.VITE_NGS_API_BASE ??
    DEFAULT_API_BASE;

  return {
    sessionId,
    bankId,
    gameId,
    apiBase,
    gsInternalBaseUrl:
      params.get("gsInternalBaseUrl") ?? params.get("GSINTERNALBASEURL") ?? undefined,
    token: params.get("token") ?? params.get("TOKEN") ?? undefined,
    playerId: params.get("playerId") ?? params.get("PLAYERID") ?? undefined,
    language: params.get("lang") ?? undefined,
    bootstrapRef:
      asBootstrapRef(params.get("bootstrapRef")) ??
      asBootstrapRef(params.get("BOOTSTRAPREF")),
    launchParams: Object.fromEntries(params.entries()),
  };
};

export interface FeatureActionOptions {
  payload?: Record<string, unknown>;
  selectedBet?: SelectedBet;
  selectedFeatureChoice?: SelectedFeatureChoice;
  featureType?: SelectedFeatureChoice["featureType"];
  action?: SelectedFeatureChoice["action"];
  priceMinor?: number;
}

export interface BootstrapFlowSnapshot {
  bootstrap: BootstrapResponse;
  opengame: OpenGameResponse;
}

export class GsRuntimeClient {
  private transport: IGameTransport | null = null;
  private config: GameInitConfig | null = null;
  private static readonly DEFAULT_HISTORY_QUERY: HistoryQuery = {
    fromRoundId: null,
    limit: 20,
    includeFeatureDetails: true,
  };

  public async bootstrap(): Promise<BootstrapFlowSnapshot> {
    const launch = readLaunchParams();
    if (!launch.sessionId) {
      throw new Error("Missing sessionId launch parameter. Cannot open GS runtime session.");
    }

    const transport = createTransport({
      mode: "GS_HTTP_RUNTIME",
      baseUrl: launch.apiBase,
      token: launch.token,
      bankId: launch.bankId,
      playerId: launch.playerId,
      gameId: launch.gameId,
      sessionId: launch.sessionId,
      gsInternalBaseUrl: launch.gsInternalBaseUrl,
      language: launch.language,
      internalClientCode: CONTRACT_VERSION,
    });

    this.transport = transport;
    this.config = {
      mode: "GS_HTTP_RUNTIME",
      baseUrl: launch.apiBase,
      token: launch.token,
      bankId: launch.bankId,
      playerId: launch.playerId,
      gameId: launch.gameId,
      sessionId: launch.sessionId,
      gsInternalBaseUrl: launch.gsInternalBaseUrl,
      language: launch.language,
      internalClientCode: CONTRACT_VERSION,
    };

    const bootstrapSnapshot = await transport.bootstrap(this.config, {
      contractVersion: CONTRACT_VERSION,
      sessionId: launch.sessionId,
      bootstrapRef:
        launch.bootstrapRef ?? {
          configId: `cfg-${launch.sessionId}`,
          clientPackageVersion: "client-pkg-dev",
        },
    });

    BootstrapConfigStore.hydrateFromBootstrap(bootstrapSnapshot);

    const bootstrapSessionId = extractBootstrapSessionId(bootstrapSnapshot) ?? launch.sessionId;
    const bootstrapRef =
      extractBootstrapRefFromBootstrap(bootstrapSnapshot) ??
      launch.bootstrapRef ??
      {
        configId: `cfg-${bootstrapSessionId}`,
        clientPackageVersion: "client-pkg-dev",
        mathPackageVersion:
          asString(asRecord(bootstrapSnapshot.runtime).mathPackageVersion) ??
          "math-dev",
      };
    const bootstrapRequestCounter = extractBootstrapRequestCounter(bootstrapSnapshot);
    const bootstrapStateVersion = extractBootstrapStateVersion(bootstrapSnapshot);
    const openCounter = bootstrapRequestCounter + 1;
    const openOperationId = `opengame-${openCounter}`;

    const openSnapshot = await transport.opengame({
      contractVersion: CONTRACT_VERSION,
      sessionId: bootstrapSessionId,
      requestCounter: openCounter,
      currentStateVersion: bootstrapStateVersion,
      bootstrapRef,
      selectedBet: null,
      selectedFeatureChoice: null,
      idempotencyKey: openOperationId,
      clientOperationId: openOperationId,
    });

    this.applyOpenSnapshot(openSnapshot, {
      fallbackSessionId: bootstrapSessionId,
      fallbackBalanceMinor: extractBootstrapBalanceMinor(bootstrapSnapshot),
      fallbackRequestCounter: bootstrapRequestCounter,
      fallbackStateVersion: bootstrapStateVersion,
      fallbackBootstrapRef: bootstrapRef,
      fallbackResumeRef: extractResumeRefFromBootstrap(bootstrapSnapshot),
    });

    return {
      bootstrap: bootstrapSnapshot,
      opengame: openSnapshot,
    };
  }

  public async resumegame(): Promise<OpenGameResponse> {
    const transport = this.assertTransport();
    const session = SessionRuntimeStore.get();
    const nextCounter = session.requestCounter + 1;
    const operationId = `resumegame-${nextCounter}`;

    const opened = await transport.resumegame({
      contractVersion: CONTRACT_VERSION,
      sessionId: session.sessionId,
      bootstrapRef: session.bootstrapRef,
      resumeRef: session.resumeRef,
      selectedBet: null,
      selectedFeatureChoice: null,
      requestCounter: nextCounter,
      currentStateVersion: session.stateVersion,
      idempotencyKey: operationId,
      clientOperationId: operationId,
    });

    this.applyOpenSnapshot(opened, {
      fallbackSessionId: session.sessionId,
      fallbackBalanceMinor: session.balance,
      fallbackRequestCounter: session.requestCounter,
      fallbackStateVersion: session.stateVersion,
      fallbackBootstrapRef: session.bootstrapRef,
      fallbackResumeRef: session.resumeRef,
    });

    return opened;
  }

  public async playround(
    selectedBet: SelectedBet,
    _selectedFeatureChoice?: SelectedFeatureChoice,
  ): Promise<PlayRoundResponse> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.get();
    const nextCounter = snapshot.requestCounter + 1;
    const operationKey = `playround-${nextCounter}`;

    const result = await transport.playround({
      contractVersion: CONTRACT_VERSION,
      sessionId: snapshot.sessionId,
      selectedBet,
      selectedFeatureChoice: null,
      bootstrapRef: snapshot.bootstrapRef,
      requestCounter: nextCounter,
      clientOperationId: operationKey,
      idempotencyKey: operationKey,
      currentStateVersion: snapshot.stateVersion,
    });

    SessionRuntimeStore.patch({
      balance: extractWalletBalanceMinor(result),
      requestCounter: result.requestCounter,
      stateVersion: result.stateVersion,
      bootstrapRef:
        extractBootstrapRefFromRuntimeEnvelope(result) ?? snapshot.bootstrapRef,
      resumeRef: extractResumeRefFromRuntimeEnvelope(result) ?? snapshot.resumeRef,
      lastRoundId: extractRoundId(result),
    });

    return result;
  }

  public async gethistory(
    historyQuery: HistoryQuery = GsRuntimeClient.DEFAULT_HISTORY_QUERY,
  ): Promise<Array<Record<string, unknown>>> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.get();

    const response = await transport.gethistory({
      contractVersion: CONTRACT_VERSION,
      sessionId: snapshot.sessionId,
      requestCounter: snapshot.requestCounter,
      historyQuery,
    });

    return extractHistoryItems(response);
  }

  public async featureaction(
    action: string,
    options: FeatureActionOptions = {},
  ): Promise<FeatureActionResponse> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.get();
    const nextCounter = snapshot.requestCounter + 1;
    const operationKey = `featureaction-${action}-${nextCounter}`;

    const response = await transport.featureaction({
      contractVersion: CONTRACT_VERSION,
      sessionId: snapshot.sessionId,
      selectedBet: options.selectedBet ?? null,
      selectedFeatureChoice:
        options.selectedFeatureChoice ?? {
          featureType: options.featureType ?? "BUY_FEATURE",
          action: options.action ?? "CONFIRM",
          priceMinor: options.priceMinor ?? 0,
          payload: options.payload ?? {},
        },
      bootstrapRef: snapshot.bootstrapRef,
      requestCounter: nextCounter,
      clientOperationId: operationKey,
      idempotencyKey: operationKey,
      currentStateVersion: snapshot.stateVersion,
    });

    SessionRuntimeStore.patch({
      balance: extractWalletBalanceMinor(response),
      requestCounter: response.requestCounter,
      stateVersion: response.stateVersion,
      bootstrapRef:
        extractBootstrapRefFromRuntimeEnvelope(response) ?? snapshot.bootstrapRef,
      resumeRef: extractResumeRefFromRuntimeEnvelope(response) ?? snapshot.resumeRef,
    });

    return response;
  }

  public async close(closeReason = "client-close"): Promise<void> {
    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.getSnapshot();
    if (!snapshot?.sessionId) {
      SessionRuntimeStore.clear();
      BootstrapConfigStore.clear();
      return;
    }

    const nextCounter = snapshot.requestCounter + 1;
    const operationId = `closegame-${nextCounter}`;

    await transport.closegame({
      contractVersion: CONTRACT_VERSION,
      sessionId: snapshot.sessionId,
      closeReason,
      bootstrapRef: snapshot.bootstrapRef,
      selectedBet: null,
      selectedFeatureChoice: null,
      requestCounter: nextCounter,
      currentStateVersion: snapshot.stateVersion,
      idempotencyKey: operationId,
      clientOperationId: operationId,
    });

    SessionRuntimeStore.clear();
    BootstrapConfigStore.clear();
  }

  public getTransport(): IGameTransport {
    return this.assertTransport();
  }

  private applyOpenSnapshot(
    opened: OpenGameResponse,
    fallback: {
      fallbackSessionId: string;
      fallbackBalanceMinor: number;
      fallbackRequestCounter: number;
      fallbackStateVersion: number;
      fallbackBootstrapRef: BootstrapRef;
      fallbackResumeRef?: string | JsonRecord;
    },
  ): void {
    const wallet = asRecord(opened.wallet);
    const balanceMinor = asNumber(wallet.balanceMinor);

    SessionRuntimeStore.set({
      sessionId: opened.sessionId || fallback.fallbackSessionId,
      balance: balanceMinor ?? fallback.fallbackBalanceMinor,
      requestCounter: opened.requestCounter ?? fallback.fallbackRequestCounter,
      stateVersion: opened.stateVersion ?? fallback.fallbackStateVersion,
      bootstrapRef:
        extractBootstrapRefFromRuntimeEnvelope(opened) ?? fallback.fallbackBootstrapRef,
      resumeRef:
        extractResumeRefFromRuntimeEnvelope(opened) ?? fallback.fallbackResumeRef,
      unfinishedRound: extractUnfinishedRound(opened),
      lastRoundId: undefined,
    });
  }

  private assertTransport(): IGameTransport {
    if (!this.transport || !this.config) {
      throw new Error("GsRuntimeClient not bootstrapped.");
    }
    return this.transport;
  }
}

export const gsRuntimeClient = new GsRuntimeClient();

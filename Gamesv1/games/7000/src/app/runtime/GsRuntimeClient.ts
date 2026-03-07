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
import { crazyRoosterDemoRuntime } from "./demoRuntime";

const CONTRACT_VERSION = "slot-browser-v1";
const DEFAULT_API_BASE = "http://127.0.0.1:6400";
const DEFAULT_GAME_ID = 7000;

type JsonRecord = Record<string, unknown>;

const isRecord = (value: unknown): value is JsonRecord =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asRecord = (value: unknown): JsonRecord => (isRecord(value) ? value : {});

const asString = (value: unknown): string | undefined =>
  typeof value === "string" && value.length > 0 ? value : undefined;

const asNumber = (value: unknown): number | undefined =>
  typeof value === "number" && Number.isFinite(value) ? value : undefined;

const asBootstrapRef = (value: unknown): BootstrapRef | undefined => {
  if (!isRecord(value)) {
    return undefined;
  }

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
};

const readUrlNumber = (value: string | null): number | undefined => {
  if (!value) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
};

const extractWalletBalanceMinor = (response: OpenGameResponse | PlayRoundResponse): number => {
  const wallet = asRecord(response.wallet);
  return asNumber(wallet.balanceMinor) ?? 0;
};

const extractBootstrapSessionId = (snapshot: BootstrapResponse): string | undefined =>
  asString(asRecord(snapshot.session).sessionId);

const extractBootstrapRequestCounter = (snapshot: BootstrapResponse): number =>
  asNumber(asRecord(snapshot.session).requestCounter) ?? 0;

const extractBootstrapStateVersion = (snapshot: BootstrapResponse): number =>
  asNumber(asRecord(snapshot.session).stateVersion) ?? 0;

const extractBootstrapRefFromEnvelope = (
  response: OpenGameResponse | PlayRoundResponse,
): BootstrapRef | undefined =>
  asBootstrapRef(asRecord(asRecord(response.presentationPayload).bootstrapRef));

const extractResumeRefFromEnvelope = (
  response: OpenGameResponse | PlayRoundResponse,
): string | JsonRecord | undefined => {
  const resumeRef = asRecord(asRecord(response.restore).resumeRef);
  if (Object.keys(resumeRef).length > 0) {
    return resumeRef;
  }

  const raw = asString(asRecord(response.restore).resumeRef);
  return raw;
};

const readLaunchParams = () => {
  const params = new URLSearchParams(window.location.search);
  const sessionId =
    params.get("sid") ??
    params.get("SID") ??
    params.get("sessionId") ??
    params.get("SESSIONID") ??
    "";

  return {
    sessionId,
    bankId: readUrlNumber(params.get("bankId") ?? params.get("BANKID")),
    gameId:
      readUrlNumber(
        params.get("gameIdNumeric") ??
          params.get("GAMEIDNUMERIC") ??
          params.get("gameId") ??
          params.get("GAMEID"),
      ) ?? DEFAULT_GAME_ID,
    apiBase:
      params.get("ngsApiUrl") ??
      params.get("NGSAPIURL") ??
      import.meta.env.VITE_NGS_API_BASE ??
      DEFAULT_API_BASE,
    gsInternalBaseUrl:
      params.get("gsInternalBaseUrl") ?? params.get("GSINTERNALBASEURL") ?? undefined,
    token: params.get("token") ?? params.get("TOKEN") ?? undefined,
    playerId: params.get("playerId") ?? params.get("PLAYERID") ?? undefined,
    language: params.get("lang") ?? undefined,
    bootstrapRef:
      asBootstrapRef(params.get("bootstrapRef")) ??
      asBootstrapRef(params.get("BOOTSTRAPREF")),
    demoRequested:
      params.get("allowDevFallback") === "1" ||
      params.get("devConfig") === "1" ||
      params.has("proofState"),
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
  private demoMode = false;

  private static readonly DEFAULT_HISTORY_QUERY: HistoryQuery = {
    fromRoundId: null,
    limit: 20,
    includeFeatureDetails: true,
  };

  public async bootstrap(): Promise<BootstrapFlowSnapshot> {
    const launch = readLaunchParams();

    if (!launch.sessionId && launch.demoRequested) {
      return this.bootstrapDemo();
    }

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
    this.demoMode = false;
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

    try {
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
      const bootstrapRef = launch.bootstrapRef ?? {
        configId: `cfg-${bootstrapSessionId}`,
        clientPackageVersion: "client-pkg-dev",
        mathPackageVersion:
          asString(asRecord(bootstrapSnapshot.runtime).mathPackageVersion) ?? "math-dev",
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
        fallbackBalanceMinor: 0,
        fallbackRequestCounter: bootstrapRequestCounter,
        fallbackStateVersion: bootstrapStateVersion,
        fallbackBootstrapRef: bootstrapRef,
      });

      return {
        bootstrap: bootstrapSnapshot,
        opengame: openSnapshot,
      };
    } catch (error) {
      if (!launch.demoRequested) {
        throw error;
      }

      console.warn("[GsRuntimeClient] Falling back to demo runtime after GS bootstrap failure.", error);
      return this.bootstrapDemo();
    }
  }

  public async playround(
    selectedBet: SelectedBet,
    _selectedFeatureChoice?: SelectedFeatureChoice,
  ): Promise<PlayRoundResponse> {
    if (this.demoMode) {
      const response = crazyRoosterDemoRuntime.playround(selectedBet);
      this.patchSessionFromRound(response);
      return response;
    }

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

    this.patchSessionFromRound(result);
    return result;
  }

  public async gethistory(
    historyQuery: HistoryQuery = GsRuntimeClient.DEFAULT_HISTORY_QUERY,
  ): Promise<Array<Record<string, unknown>>> {
    if (this.demoMode) {
      return crazyRoosterDemoRuntime.gethistory(historyQuery);
    }

    const transport = this.assertTransport();
    const snapshot = SessionRuntimeStore.get();
    const response = await transport.gethistory({
      contractVersion: CONTRACT_VERSION,
      sessionId: snapshot.sessionId,
      requestCounter: snapshot.requestCounter,
      historyQuery,
    });

    const history = asRecord(response.feature).history;
    return Array.isArray(history) ? (history as Array<Record<string, unknown>>) : [];
  }

  public async featureaction(
    action: string,
    options: FeatureActionOptions = {},
  ): Promise<FeatureActionResponse> {
    if (this.demoMode) {
      const response = crazyRoosterDemoRuntime.featureaction(
        options.selectedBet ?? null,
        options.selectedFeatureChoice ?? {
          featureType: options.featureType ?? "BUY_FEATURE",
          action: options.action ?? "CONFIRM",
          priceMinor: options.priceMinor ?? 0,
          payload: options.payload ?? {},
        },
      );
      this.patchSessionFromRound(response as PlayRoundResponse);
      return response;
    }

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

    this.patchSessionFromRound(response as PlayRoundResponse);
    return response;
  }

  public async close(closeReason = "client-close"): Promise<void> {
    if (this.demoMode) {
      crazyRoosterDemoRuntime.close();
      this.demoMode = false;
      SessionRuntimeStore.clear();
      BootstrapConfigStore.clear();
      return;
    }

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

  private bootstrapDemo(): BootstrapFlowSnapshot {
    this.demoMode = true;
    this.transport = null;
    this.config = null;

    const snapshot = crazyRoosterDemoRuntime.bootstrap();
    BootstrapConfigStore.hydrateFromBootstrap(snapshot.bootstrap);
    this.applyOpenSnapshot(snapshot.opengame, {
      fallbackSessionId: "demo-session-7000",
      fallbackBalanceMinor: extractWalletBalanceMinor(snapshot.opengame),
      fallbackRequestCounter: extractBootstrapRequestCounter(snapshot.bootstrap),
      fallbackStateVersion: extractBootstrapStateVersion(snapshot.bootstrap),
      fallbackBootstrapRef:
        extractBootstrapRefFromEnvelope(snapshot.opengame) ?? {
          configId: "demo-crazy-rooster",
          clientPackageVersion: "demo-shell-7000",
          mathPackageVersion: "demo-math-7000",
        },
    });
    return snapshot;
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
    SessionRuntimeStore.set({
      sessionId: opened.sessionId || fallback.fallbackSessionId,
      balance: extractWalletBalanceMinor(opened) || fallback.fallbackBalanceMinor,
      requestCounter: opened.requestCounter ?? fallback.fallbackRequestCounter,
      stateVersion: opened.stateVersion ?? fallback.fallbackStateVersion,
      bootstrapRef:
        extractBootstrapRefFromEnvelope(opened) ?? fallback.fallbackBootstrapRef,
      resumeRef:
        extractResumeRefFromEnvelope(opened) ?? fallback.fallbackResumeRef,
      unfinishedRound: undefined,
      lastRoundId: undefined,
    });
  }

  private patchSessionFromRound(response: PlayRoundResponse): void {
    const snapshot = SessionRuntimeStore.get();
    SessionRuntimeStore.patch({
      balance: extractWalletBalanceMinor(response),
      requestCounter: response.requestCounter,
      stateVersion: response.stateVersion,
      bootstrapRef:
        extractBootstrapRefFromEnvelope(response) ?? snapshot.bootstrapRef,
      resumeRef: extractResumeRefFromEnvelope(response) ?? snapshot.resumeRef,
      lastRoundId: asString(asRecord(response.round).roundId),
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

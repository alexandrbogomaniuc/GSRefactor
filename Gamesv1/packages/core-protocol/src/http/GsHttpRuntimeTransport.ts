import {
  type BootstrapRequest,
  type CloseGameRequest,
  type FeatureActionRequest,
  type FeatureActionResponse,
  type GameInitConfig,
  type HistoryRequest,
  type HistoryResponse,
  type IGameTransport,
  type OpenGameRequest,
  type OpenGameResponse,
  type PlayRoundRequest,
  type PlayRoundResponse,
  type RequestMetadata,
  type ResumeGameRequest,
  type TransportEvent,
} from "../IGameTransport";

type ErrorEnvelope = {
  error?: {
    code?: string;
    message?: string;
  };
};

type PlaceBetResponse = {
  roundId: string;
  balance?: number;
  requestCounter?: number;
  currentStateVersion?: string;
  math?: unknown;
} & ErrorEnvelope;

type CollectResponse = {
  roundId: string;
  balance: number;
  winAmount?: number;
  requestCounter?: number;
  currentStateVersion?: string;
} & ErrorEnvelope;

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

export class GsHttpRuntimeTransport implements IGameTransport {
  private config: GameInitConfig | null = null;
  private listeners: Map<TransportEvent, Array<(...args: any[]) => void>> = new Map();
  private sessionId = "";
  private requestCounter = 0;
  private currentStateVersion: string | undefined;
  private operationSequence = 0;

  public async bootstrap(
    config: GameInitConfig,
    request?: BootstrapRequest,
  ): Promise<OpenGameResponse> {
    this.config = { ...config };
    const sessionId = request?.sessionId ?? config.sessionId ?? "";
    if (!sessionId) {
      throw new Error("GS runtime bootstrap requires sessionId.");
    }

    return this.openGame({
      sessionId,
      bankId: config.bankId ? Number(config.bankId) : undefined,
      playerId: config.playerId,
      gameId: Number(config.gameId),
      gsInternalBaseUrl: config.gsInternalBaseUrl,
      language: config.language,
      internalClientCode: config.internalClientCode,
    });
  }

  public async openGame(request: OpenGameRequest): Promise<OpenGameResponse> {
    this.assertConfig();

    const response = await this.postJson<OpenGameRequest, OpenGameResponse>(
      "/v1/opengame",
      request,
    );

    this.sessionId = response.sessionId;
    this.requestCounter = response.requestCounter;
    this.currentStateVersion = response.currentStateVersion;

    this.emit("balance", response.balance);
    this.emit("ready", response);

    return response;
  }

  public async playRound(request: PlayRoundRequest): Promise<PlayRoundResponse> {
    this.assertConfig();
    this.assertSession();

    const placeRequestCounter = this.resolveRequestCounter(request.metadata);
    const clientOperationId =
      request.metadata?.clientOperationId ?? this.nextOperationId("placebet");
    const idempotencyKey =
      request.metadata?.idempotencyKey ?? `idem:${clientOperationId}`;

    const placeBody: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter: placeRequestCounter,
      clientOperationId,
      bets: [{ betType: request.betType, betAmount: request.betAmount }],
    };
    if (request.metadata?.currentStateVersion ?? this.currentStateVersion) {
      placeBody.currentStateVersion =
        request.metadata?.currentStateVersion ?? this.currentStateVersion;
    }

    const place = await this.postJson<Record<string, unknown>, PlaceBetResponse>(
      "/v1/placebet",
      placeBody,
      {
        idempotencyKey,
        clientOperationId,
      },
    );

    const collectRequestCounter =
      place.requestCounter ?? placeRequestCounter + 1;
    const collectClientOperationId = `${clientOperationId}:collect`;
    const collectIdempotencyKey = `${idempotencyKey}:collect`;

    const collectBody: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter: collectRequestCounter,
      roundId: place.roundId,
      clientOperationId: collectClientOperationId,
    };
    if (request.metadata?.currentStateVersion ?? this.currentStateVersion) {
      collectBody.currentStateVersion =
        request.metadata?.currentStateVersion ?? this.currentStateVersion;
    }

    const collect = await this.postJson<Record<string, unknown>, CollectResponse>(
      "/v1/collect",
      collectBody,
      {
        idempotencyKey: collectIdempotencyKey,
        clientOperationId: collectClientOperationId,
      },
    );

    this.requestCounter = collect.requestCounter ?? collectRequestCounter;
    this.currentStateVersion =
      collect.currentStateVersion ??
      place.currentStateVersion ??
      this.currentStateVersion;

    const presentationPayload = {
      source: "gs-runtime-v1",
      roundId: place.roundId,
      betAmount: request.betAmount,
      betType: request.betType,
      math: place.math ?? null,
      winAmount: collect.winAmount ?? 0,
      balance: collect.balance,
    };

    const response: PlayRoundResponse = {
      roundId: place.roundId,
      balance: collect.balance,
      winAmount: collect.winAmount ?? 0,
      requestCounter: this.requestCounter,
      currentStateVersion: this.currentStateVersion,
      presentationPayload,
      rawPlaceBet: place,
      rawCollect: collect,
    };

    this.emit("balance", response.balance);
    this.emit("round", response);
    return response;
  }

  public async featureAction(
    request: FeatureActionRequest,
  ): Promise<FeatureActionResponse> {
    this.assertConfig();
    this.assertSession();

    const requestCounter = this.resolveRequestCounter(request.metadata);
    const clientOperationId =
      request.metadata?.clientOperationId ?? this.nextOperationId(request.action);
    const idempotencyKey =
      request.metadata?.idempotencyKey ?? `idem:${clientOperationId}`;

    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      action: request.action,
      payload: request.payload ?? {},
      clientOperationId,
    };
    if (request.metadata?.currentStateVersion ?? this.currentStateVersion) {
      body.currentStateVersion =
        request.metadata?.currentStateVersion ?? this.currentStateVersion;
    }

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/featureaction",
      body,
      {
        idempotencyKey,
        clientOperationId,
      },
    );

    this.requestCounter =
      (typeof raw.requestCounter === "number" ? raw.requestCounter : requestCounter);
    this.currentStateVersion =
      (typeof raw.currentStateVersion === "string"
        ? raw.currentStateVersion
        : this.currentStateVersion);

    const response: FeatureActionResponse = {
      requestCounter: this.requestCounter,
      currentStateVersion: this.currentStateVersion,
      presentationPayload: raw.presentationPayload,
      raw,
    };

    this.emit("round", response);
    return response;
  }

  public async resumeGame(request?: ResumeGameRequest): Promise<OpenGameResponse> {
    const sessionId = request?.sessionId ?? this.sessionId ?? this.config?.sessionId;
    if (!sessionId) {
      throw new Error("resumeGame requires an existing sessionId.");
    }

    return this.openGame({
      sessionId,
      bankId: this.config?.bankId ? Number(this.config.bankId) : undefined,
      playerId: this.config?.playerId,
      gameId: this.config?.gameId ? Number(this.config.gameId) : undefined,
      gsInternalBaseUrl: this.config?.gsInternalBaseUrl,
      language: this.config?.language,
      internalClientCode: this.config?.internalClientCode,
    });
  }

  public async closeGame(request?: CloseGameRequest): Promise<void> {
    this.assertConfig();
    if (!this.sessionId) return;

    const requestCounter = this.resolveRequestCounter(request?.metadata);
    const clientOperationId =
      request?.metadata?.clientOperationId ?? this.nextOperationId("closegame");

    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      reason: request?.reason ?? "client-close",
      clientOperationId,
    };
    if (request?.metadata?.currentStateVersion ?? this.currentStateVersion) {
      body.currentStateVersion =
        request?.metadata?.currentStateVersion ?? this.currentStateVersion;
    }

    try {
      await this.postJson<Record<string, unknown>, Record<string, unknown>>(
        "/v1/closegame",
        body,
        {
          idempotencyKey:
            request?.metadata?.idempotencyKey ?? `idem:${clientOperationId}`,
          clientOperationId,
        },
      );
    } catch (error) {
      const message = String(error);
      if (!message.includes("404")) {
        throw error;
      }
    } finally {
      this.disconnect();
    }
  }

  public async readHistory(request?: HistoryRequest): Promise<HistoryResponse> {
    this.assertConfig();
    this.assertSession();

    const requestCounter = this.resolveRequestCounter(request?.metadata);
    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      pageNumber: request?.pageNumber ?? 0,
    };
    if (request?.metadata?.currentStateVersion ?? this.currentStateVersion) {
      body.currentStateVersion =
        request?.metadata?.currentStateVersion ?? this.currentStateVersion;
    }

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/readhistory",
      body,
    );

    this.requestCounter =
      (typeof raw.requestCounter === "number" ? raw.requestCounter : requestCounter);
    this.currentStateVersion =
      (typeof raw.currentStateVersion === "string"
        ? raw.currentStateVersion
        : this.currentStateVersion);

    const history = Array.isArray(raw.history)
      ? raw.history.filter((item): item is Record<string, unknown> => isRecord(item))
      : [];

    const response: HistoryResponse = {
      requestCounter: this.requestCounter,
      currentStateVersion: this.currentStateVersion,
      history,
      raw,
    };

    this.emit("history", response);
    return response;
  }

  public disconnect(): void {
    this.sessionId = "";
    this.emit("disconnect");
  }

  public on(event: TransportEvent, callback: (...args: any[]) => void): void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(callback);
  }

  // Deprecated legacy API compatibility.
  public async connect(config: GameInitConfig): Promise<void> {
    await this.bootstrap(config, {
      sessionId: config.sessionId ?? "legacy-session",
    });
  }

  public async spin(betAmount: number, operationId: string): Promise<any> {
    return this.playRound({
      betAmount,
      betType: "DEFAULT",
      metadata: {
        clientOperationId: operationId,
      },
    });
  }

  public async settle(_operationId: string): Promise<any> {
    throw new Error(
      "settle() is deprecated for GS HTTP runtime transport; use playRound().",
    );
  }

  private resolveRequestCounter(metadata?: RequestMetadata): number {
    if (typeof metadata?.requestCounter === "number") {
      return metadata.requestCounter;
    }
    return this.requestCounter + 1;
  }

  private nextOperationId(prefix: string): string {
    this.operationSequence += 1;
    return `${prefix}-${Date.now()}-${this.operationSequence}`;
  }

  private assertConfig(): void {
    this.requireConfig();
  }

  private assertSession(): void {
    if (!this.sessionId) {
      throw new Error("No active GS session. Call openGame()/resumeGame() first.");
    }
  }

  private emit(event: TransportEvent, ...args: any[]): void {
    const listeners = this.listeners.get(event);
    if (!listeners) return;
    for (const listener of listeners) {
      listener(...args);
    }
  }

  private async postJson<TReq, TRes>(
    path: string,
    body: TReq,
    metadata?: {
      idempotencyKey?: string;
      clientOperationId?: string;
    },
  ): Promise<TRes> {
    const config = this.requireConfig();
    const base = config.baseUrl.replace(/\/+$/, "");
    const url = `${base}${path}`;
    const headers: Record<string, string> = {
      "content-type": "application/json",
    };

    if (config.token) {
      headers.authorization = `Bearer ${config.token}`;
    }
    if (metadata?.idempotencyKey) {
      headers["x-idempotency-key"] = metadata.idempotencyKey;
    }
    if (metadata?.clientOperationId) {
      headers["x-client-operation-id"] = metadata.clientOperationId;
    }

    const response = await fetch(url, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    });

    const rawData = (await response.json().catch(() => ({}))) as TRes & ErrorEnvelope;
    if (!response.ok) {
      const message =
        rawData.error?.message ??
        `GS runtime request failed (${response.status}) at ${path}`;
      throw new Error(`${response.status}: ${message}`);
    }

    return rawData as TRes;
  }

  private requireConfig(): GameInitConfig {
    if (!this.config) {
      throw new Error("GS runtime transport not bootstrapped.");
    }
    return this.config;
  }
}

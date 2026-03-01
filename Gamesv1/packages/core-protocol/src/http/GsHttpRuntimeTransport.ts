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
} from "../IGameTransport.ts";

type ErrorEnvelope = {
  error?: {
    code?: string;
    message?: string;
  };
};

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asString = (value: unknown): string | undefined =>
  typeof value === "string" && value.length > 0 ? value : undefined;

const asNumber = (value: unknown): number | undefined =>
  typeof value === "number" && Number.isFinite(value) ? value : undefined;

const asHistoryList = (value: unknown): Array<Record<string, unknown>> =>
  Array.isArray(value)
    ? value.filter((item): item is Record<string, unknown> => isRecord(item))
    : [];

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

    const sessionId = request?.sessionId ?? config.sessionId;
    if (!sessionId) {
      throw new Error("GS runtime bootstrap requires sessionId.");
    }

    const body: Record<string, unknown> = {
      sessionId,
      gameId: request?.gameId ?? (config.gameId ? Number(config.gameId) : undefined),
      bankId: request?.bankId ?? (config.bankId ? Number(config.bankId) : undefined),
      playerId: request?.playerId ?? config.playerId,
      language: request?.language ?? config.language,
      launchParams: request?.launchParams ?? {},
    };

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/bootstrap",
      body,
    );

    return this.applySessionSnapshot(raw);
  }

  public async openGame(request: OpenGameRequest): Promise<OpenGameResponse> {
    this.assertConfig();

    const raw = await this.postJson<OpenGameRequest, Record<string, unknown>>(
      "/v1/opengame",
      request,
    );

    return this.applySessionSnapshot(raw);
  }

  public async playRound(request: PlayRoundRequest): Promise<PlayRoundResponse> {
    this.assertConfig();
    this.assertSession();

    const requestCounter = this.resolveRequestCounter(request.metadata);
    const clientOperationId =
      request.metadata?.clientOperationId ?? this.nextOperationId("playround");
    const idempotencyKey =
      request.metadata?.idempotencyKey ?? `idem:${clientOperationId}`;

    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      clientOperationId,
      idempotencyKey,
      currentStateVersion:
        request.metadata?.currentStateVersion ?? this.currentStateVersion,
      betAmount: request.betAmount,
      betType: request.betType,
      roundInput: request.roundInput ?? {},
    };

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/playround",
      body,
      {
        idempotencyKey,
        clientOperationId,
      },
    );

    const nextRequestCounter =
      asNumber(raw.requestCounter) ??
      asNumber(isRecord(raw.session) ? raw.session.requestCounter : undefined) ??
      requestCounter;

    this.requestCounter = nextRequestCounter;
    this.currentStateVersion =
      asString(raw.currentStateVersion) ??
      asString(isRecord(raw.session) ? raw.session.currentStateVersion : undefined) ??
      this.currentStateVersion;

    const balance =
      asNumber(raw.balance) ??
      asNumber(isRecord(raw.wallet) ? raw.wallet.balance : undefined) ??
      0;

    const winAmount =
      asNumber(raw.winAmount) ??
      asNumber(isRecord(raw.round) ? raw.round.winAmount : undefined) ??
      0;

    const roundId =
      asString(raw.roundId) ??
      asString(isRecord(raw.round) ? raw.round.roundId : undefined) ??
      `${clientOperationId}:round`;

    const response: PlayRoundResponse = {
      roundId,
      balance,
      winAmount,
      requestCounter: this.requestCounter,
      currentStateVersion: this.currentStateVersion,
      presentationPayload: raw.presentationPayload,
      raw,
    };

    this.emit("balance", balance);
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
      request.metadata?.clientOperationId ?? this.nextOperationId(`feature-${request.action}`);
    const idempotencyKey =
      request.metadata?.idempotencyKey ?? `idem:${clientOperationId}`;

    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      action: request.action,
      payload: request.payload ?? {},
      clientOperationId,
      idempotencyKey,
      currentStateVersion:
        request.metadata?.currentStateVersion ?? this.currentStateVersion,
    };

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/featureaction",
      body,
      {
        idempotencyKey,
        clientOperationId,
      },
    );

    this.requestCounter = asNumber(raw.requestCounter) ?? requestCounter;
    this.currentStateVersion =
      asString(raw.currentStateVersion) ?? this.currentStateVersion;

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
    this.assertConfig();

    const sessionId = request?.sessionId ?? this.sessionId ?? this.config?.sessionId;
    if (!sessionId) {
      throw new Error("resumeGame requires an existing sessionId.");
    }

    const metadata = request?.metadata;
    const requestCounter = this.resolveRequestCounter(metadata);

    const body: Record<string, unknown> = {
      sessionId,
      requestCounter,
      clientOperationId:
        metadata?.clientOperationId ?? this.nextOperationId("resumegame"),
      idempotencyKey:
        metadata?.idempotencyKey ?? `idem:${metadata?.clientOperationId ?? "resumegame"}`,
      currentStateVersion: metadata?.currentStateVersion ?? this.currentStateVersion,
    };

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/resumegame",
      body,
      {
        idempotencyKey: asString(body.idempotencyKey),
        clientOperationId: asString(body.clientOperationId),
      },
    );

    return this.applySessionSnapshot(raw);
  }

  public async closeGame(request?: CloseGameRequest): Promise<void> {
    this.assertConfig();
    if (!this.sessionId) return;

    const requestCounter = this.resolveRequestCounter(request?.metadata);
    const clientOperationId =
      request?.metadata?.clientOperationId ?? this.nextOperationId("closegame");
    const idempotencyKey =
      request?.metadata?.idempotencyKey ?? `idem:${clientOperationId}`;

    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      reason: request?.reason ?? "client-close",
      clientOperationId,
      idempotencyKey,
      currentStateVersion:
        request?.metadata?.currentStateVersion ?? this.currentStateVersion,
    };

    try {
      await this.postJson<Record<string, unknown>, Record<string, unknown>>(
        "/v1/closegame",
        body,
        {
          idempotencyKey,
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

  public async getHistory(request?: HistoryRequest): Promise<HistoryResponse> {
    this.assertConfig();
    this.assertSession();

    const requestCounter = this.resolveRequestCounter(request?.metadata);

    const body: Record<string, unknown> = {
      sessionId: this.sessionId,
      requestCounter,
      pageNumber: request?.pageNumber ?? 0,
      currentStateVersion:
        request?.metadata?.currentStateVersion ?? this.currentStateVersion,
      clientOperationId:
        request?.metadata?.clientOperationId ?? this.nextOperationId("gethistory"),
      idempotencyKey:
        request?.metadata?.idempotencyKey ?? undefined,
    };

    const raw = await this.postJson<Record<string, unknown>, Record<string, unknown>>(
      "/v1/gethistory",
      body,
    );

    this.requestCounter = asNumber(raw.requestCounter) ?? requestCounter;
    this.currentStateVersion =
      asString(raw.currentStateVersion) ?? this.currentStateVersion;

    const response: HistoryResponse = {
      requestCounter: this.requestCounter,
      currentStateVersion: this.currentStateVersion,
      history: asHistoryList(raw.history),
      raw,
    };

    this.emit("history", response);
    return response;
  }

  // Deprecated alias kept for old call sites.
  public async readHistory(request?: HistoryRequest): Promise<HistoryResponse> {
    return this.getHistory(request);
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
      gameId: config.gameId ? Number(config.gameId) : undefined,
      bankId: config.bankId ? Number(config.bankId) : undefined,
      playerId: config.playerId,
      language: config.language,
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
    throw new Error("settle() is deprecated; use playRound().");
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
      throw new Error("No active GS session. Call bootstrap()/openGame()/resumeGame() first.");
    }
  }

  private emit(event: TransportEvent, ...args: any[]): void {
    const listeners = this.listeners.get(event);
    if (!listeners) return;
    for (const listener of listeners) {
      listener(...args);
    }
  }

  private applySessionSnapshot(raw: Record<string, unknown>): OpenGameResponse {
    const session = isRecord(raw.session) ? raw.session : {};
    const wallet = isRecord(raw.wallet) ? raw.wallet : {};

    const sessionId =
      asString(raw.sessionId) ??
      asString(session.sessionId) ??
      this.sessionId;

    const requestCounter =
      asNumber(raw.requestCounter) ??
      asNumber(session.requestCounter) ??
      this.requestCounter;

    const currentStateVersion =
      asString(raw.currentStateVersion) ??
      asString(session.currentStateVersion) ??
      this.currentStateVersion;

    const balance =
      asNumber(raw.balance) ??
      asNumber(wallet.balance) ??
      0;

    this.sessionId = sessionId;
    this.requestCounter = requestCounter;
    this.currentStateVersion = currentStateVersion;

    const response: OpenGameResponse = {
      sessionId,
      balance,
      requestCounter,
      currencyCode:
        asString(raw.currencyCode) ??
        asString(wallet.currencyCode),
      minBet: asNumber(raw.minBet),
      maxBet: asNumber(raw.maxBet),
      currentStateVersion,
      unresolvedRoundState: raw.unresolvedRoundState ?? raw.restore,
      presentationPayload: raw.presentationPayload,
      runtimeConfig: isRecord(raw.runtimeConfig) ? raw.runtimeConfig : undefined,
      capabilities: isRecord(raw.capabilities) ? raw.capabilities : undefined,
      wallet,
      session,
      restore: isRecord(raw.restore) ? raw.restore : undefined,
    };

    this.emit("balance", response.balance);
    this.emit("ready", response);

    return response;
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

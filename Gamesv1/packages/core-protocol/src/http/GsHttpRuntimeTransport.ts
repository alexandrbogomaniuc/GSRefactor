import {
  type BootstrapRequest,
  type BootstrapResponse,
  type CloseGameRequest,
  type CloseGameResponse,
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
  type ResumeGameRequest,
  type ResumeGameResponse,
  type RuntimeEnvelopeResponse,
  type TransportEvent,
} from "../IGameTransport.ts";

type ErrorEnvelope = {
  error?: {
    code?: string;
    message?: string;
  };
};

type JsonRecord = Record<string, unknown>;

const CONTRACT_HEADER_VALUE = "slot-browser-v1";
const SLOT_PREFIX = "/slot/v1";

const isRecord = (value: unknown): value is JsonRecord =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asRecord = (value: unknown): JsonRecord => (isRecord(value) ? value : {});

const asString = (value: unknown): string | undefined =>
  typeof value === "string" && value.length > 0 ? value : undefined;

const asInteger = (value: unknown): number | undefined =>
  typeof value === "number" && Number.isFinite(value) ? Math.trunc(value) : undefined;

const asBoolean = (value: unknown): boolean | undefined =>
  typeof value === "boolean" ? value : undefined;

const randomRequestId = (): string => {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }
  return `req-${Date.now()}-${Math.random().toString(16).slice(2)}`;
};

const parseRuntimeEnvelope = (raw: unknown): RuntimeEnvelopeResponse => {
  const record = asRecord(raw);

  const requestId = asString(record.requestId);
  const sessionId = asString(record.sessionId);
  const requestCounter = asInteger(record.requestCounter);
  const stateVersion = asInteger(record.stateVersion);

  if (!requestId || !sessionId) {
    throw new Error("GS runtime response missing requestId/sessionId.");
  }
  if (requestCounter === undefined || stateVersion === undefined) {
    throw new Error("GS runtime response missing numeric requestCounter/stateVersion.");
  }
  if (asBoolean(record.ok) !== true) {
    throw new Error("GS runtime response must include ok=true.");
  }

  const wallet = asRecord(record.wallet);
  const round = asRecord(record.round);
  const feature = asRecord(record.feature);
  const presentationPayload = asRecord(record.presentationPayload);
  const restore = asRecord(record.restore);
  const idempotency = asRecord(record.idempotency);
  const retry = asRecord(record.retry);
  const history = isRecord(record.history) ? asRecord(record.history) : undefined;

  if (
    Object.keys(wallet).length === 0 ||
    Object.keys(round).length === 0 ||
    Object.keys(feature).length === 0 ||
    Object.keys(presentationPayload).length === 0 ||
    Object.keys(restore).length === 0 ||
    Object.keys(idempotency).length === 0 ||
    Object.keys(retry).length === 0
  ) {
    throw new Error("GS runtime response missing required envelope objects.");
  }

  return {
    ok: true,
    requestId,
    sessionId,
    requestCounter,
    stateVersion,
    wallet,
    round,
    feature,
    presentationPayload,
    restore,
    idempotency,
    retry,
    ...(history ? { history } : {}),
  };
};

const parseBootstrapResponse = (raw: unknown): BootstrapResponse => {
  const record = asRecord(raw);
  const contractVersion = asString(record.contractVersion);
  if (contractVersion !== "slot-bootstrap-v1") {
    throw new Error("GS bootstrap response missing contractVersion=slot-bootstrap-v1.");
  }

  const session = asRecord(record.session);
  const context = asRecord(record.context);
  const assets = asRecord(record.assets);
  const runtime = asRecord(record.runtime);
  const policies = asRecord(record.policies);
  const integrity = asRecord(record.integrity);

  const sessionId = asString(session.sessionId);
  const requestCounter = asInteger(session.requestCounter);
  const stateVersion = asInteger(session.stateVersion);

  if (!sessionId || requestCounter === undefined || stateVersion === undefined) {
    throw new Error(
      "GS bootstrap response session must include sessionId/requestCounter/stateVersion.",
    );
  }
  if (Object.keys(context).length === 0) {
    throw new Error("GS bootstrap response missing context object.");
  }
  if (Object.keys(assets).length === 0) {
    throw new Error("GS bootstrap response missing assets object.");
  }
  if (Object.keys(runtime).length === 0) {
    throw new Error("GS bootstrap response missing runtime object.");
  }
  if (Object.keys(policies).length === 0) {
    throw new Error("GS bootstrap response missing policies object.");
  }
  if (Object.keys(integrity).length === 0) {
    throw new Error("GS bootstrap response missing integrity object.");
  }

  return {
    contractVersion: "slot-bootstrap-v1",
    session: {
      sessionId,
      requestCounter,
      stateVersion,
    },
    context,
    assets,
    runtime,
    policies,
    integrity,
  };
};

export class GsHttpRuntimeTransport implements IGameTransport {
  private config: GameInitConfig | null = null;
  private listeners: Map<TransportEvent, Array<(...args: any[]) => void>> = new Map();

  private sessionId = "";
  private requestCounter = 0;
  private stateVersion = 0;

  public async bootstrap(
    config: GameInitConfig,
    request: BootstrapRequest,
  ): Promise<BootstrapResponse> {
    this.config = { ...config };

    if (!request.sessionId || !request.bootstrapRef) {
      throw new Error("bootstrap requires sessionId and bootstrapRef.");
    }

    const raw = await this.postJson<BootstrapRequest, JsonRecord>(
      `${SLOT_PREFIX}/bootstrap`,
      request,
      {
        sessionId: request.sessionId,
      },
    );

    const bootstrap = parseBootstrapResponse(raw);
    this.sessionId = asString(asRecord(bootstrap.session).sessionId) ?? request.sessionId;
    this.requestCounter =
      asInteger(asRecord(bootstrap.session).requestCounter) ?? this.requestCounter;
    this.stateVersion = asInteger(asRecord(bootstrap.session).stateVersion) ?? this.stateVersion;

    this.emit("ready", bootstrap);
    return bootstrap;
  }

  public async opengame(request: OpenGameRequest): Promise<OpenGameResponse> {
    this.requireConfig();
    this.assertSession();
    this.assertRequestSession(request.sessionId);

    const raw = await this.postJson<OpenGameRequest, JsonRecord>(
      `${SLOT_PREFIX}/opengame`,
      request,
      {
        sessionId: request.sessionId,
        clientOperationId: request.clientOperationId,
        idempotencyKey: request.idempotencyKey,
      },
    );

    const envelope = parseRuntimeEnvelope(raw);
    this.applyEnvelope(envelope);

    this.emit("ready", envelope);
    return envelope;
  }

  public async playround(request: PlayRoundRequest): Promise<PlayRoundResponse> {
    this.requireConfig();
    this.assertSession();
    this.assertRequestSession(request.sessionId);

    const raw = await this.postJson<PlayRoundRequest, JsonRecord>(
      `${SLOT_PREFIX}/playround`,
      request,
      {
        sessionId: request.sessionId,
        clientOperationId: request.clientOperationId,
        idempotencyKey: request.idempotencyKey,
      },
    );

    const envelope = parseRuntimeEnvelope(raw);
    this.applyEnvelope(envelope);

    this.emit("round", envelope);
    return envelope;
  }

  public async featureaction(request: FeatureActionRequest): Promise<FeatureActionResponse> {
    this.requireConfig();
    this.assertSession();
    this.assertRequestSession(request.sessionId);

    const raw = await this.postJson<FeatureActionRequest, JsonRecord>(
      `${SLOT_PREFIX}/featureaction`,
      request,
      {
        sessionId: request.sessionId,
        clientOperationId: request.clientOperationId,
        idempotencyKey: request.idempotencyKey,
      },
    );

    const envelope = parseRuntimeEnvelope(raw);
    this.applyEnvelope(envelope);

    this.emit("round", envelope);
    return envelope;
  }

  public async resumegame(request: ResumeGameRequest): Promise<ResumeGameResponse> {
    this.requireConfig();
    this.assertSession();
    this.assertRequestSession(request.sessionId);

    const raw = await this.postJson<ResumeGameRequest, JsonRecord>(
      `${SLOT_PREFIX}/resumegame`,
      request,
      {
        sessionId: request.sessionId,
        clientOperationId: request.clientOperationId,
        idempotencyKey: request.idempotencyKey,
      },
    );

    const envelope = parseRuntimeEnvelope(raw);
    this.applyEnvelope(envelope);

    this.emit("ready", envelope);
    return envelope;
  }

  public async closegame(request: CloseGameRequest): Promise<CloseGameResponse> {
    this.requireConfig();
    this.assertSession();
    this.assertRequestSession(request.sessionId);

    try {
      const raw = await this.postJson<CloseGameRequest, JsonRecord>(
        `${SLOT_PREFIX}/closegame`,
        request,
        {
          sessionId: request.sessionId,
          clientOperationId: request.clientOperationId,
          idempotencyKey: request.idempotencyKey,
        },
      );

      const envelope = parseRuntimeEnvelope(raw);
      this.applyEnvelope(envelope);
      return envelope;
    } finally {
      this.disconnect();
    }
  }

  public async gethistory(request: HistoryRequest): Promise<HistoryResponse> {
    this.requireConfig();
    this.assertSession();
    this.assertRequestSession(request.sessionId);

    const raw = await this.postJson<HistoryRequest, JsonRecord>(
      `${SLOT_PREFIX}/gethistory`,
      request,
      {
        sessionId: request.sessionId,
      },
    );

    const envelope = parseRuntimeEnvelope(raw);

    if (envelope.requestCounter !== request.requestCounter) {
      throw new Error(
        `gethistory must be read-only: response requestCounter (${envelope.requestCounter}) does not match request (${request.requestCounter}).`,
      );
    }
    if (this.stateVersion > 0 && envelope.stateVersion !== this.stateVersion) {
      throw new Error(
        `gethistory must be read-only: response stateVersion (${envelope.stateVersion}) does not match active stateVersion (${this.stateVersion}).`,
      );
    }
    if (!isRecord(envelope.history) || Object.keys(envelope.history).length === 0) {
      throw new Error("gethistory must include history payload.");
    }

    this.emit("history", envelope);
    return envelope;
  }

  public disconnect(): void {
    this.sessionId = "";
    this.requestCounter = 0;
    this.stateVersion = 0;
    this.emit("disconnect");
  }

  public on(event: TransportEvent, callback: (...args: any[]) => void): void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(callback);
  }

  private applyEnvelope(envelope: RuntimeEnvelopeResponse): void {
    this.sessionId = envelope.sessionId;
    this.requestCounter = envelope.requestCounter;
    this.stateVersion = envelope.stateVersion;
  }

  private assertSession(): void {
    if (!this.sessionId) {
      throw new Error(
        "No active GS session. Call bootstrap()/opengame()/resumegame() first.",
      );
    }
  }

  private assertRequestSession(sessionId: string): void {
    if (!sessionId) {
      throw new Error("sessionId is required for GS runtime operation.");
    }
    if (this.sessionId && sessionId !== this.sessionId) {
      throw new Error(
        `sessionId mismatch for GS runtime operation (active=${this.sessionId}, request=${sessionId}).`,
      );
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
    metadata: {
      sessionId: string;
      idempotencyKey?: string;
      clientOperationId?: string;
    },
  ): Promise<TRes> {
    const config = this.requireConfig();
    const base = config.baseUrl.replace(/\/+$/, "");
    const url = `${base}${path}`;

    const headers: Record<string, string> = {
      "content-type": "application/json",
      "X-GS-Client-Contract": config.internalClientCode ?? CONTRACT_HEADER_VALUE,
      "X-Request-Id": randomRequestId(),
      "X-Session-Id": metadata.sessionId,
    };

    if (config.token) {
      headers.authorization = `Bearer ${config.token}`;
    }
    if (metadata.idempotencyKey) {
      headers["X-Idempotency-Key"] = metadata.idempotencyKey;
    }
    if (metadata.clientOperationId) {
      headers["X-Client-Operation-Id"] = metadata.clientOperationId;
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

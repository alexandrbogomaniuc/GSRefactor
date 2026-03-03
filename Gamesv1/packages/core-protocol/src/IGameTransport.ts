export type TransportEvent =
  | "ready"
  | "balance"
  | "round"
  | "history"
  | "error"
  | "disconnect";

export type TransportMode =
  | "GS_HTTP_RUNTIME"
  | "HTTP_GS"
  | "EXTGAME"
  | "WS_LEGACY"
  | "WS";

export interface GameInitConfig {
  token?: string;
  gameId: string | number;
  bankId?: string | number;
  playerId?: string;
  sessionId?: string;
  baseUrl: string;
  mode: TransportMode;
  gsInternalBaseUrl?: string;
  language?: string;
  internalClientCode?: string;
}

export interface BootstrapRef {
  configId: string;
  clientPackageVersion: string;
  mathPackageVersion?: string;
}

export interface SelectedBet {
  coinValueMinor: number;
  lines: number;
  multiplier: number;
  totalBetMinor: number;
}

export interface SelectedFeatureChoice {
  featureType: "BUY_FEATURE" | "HOLD_AND_WIN" | "RESPIN" | "FREE_SPINS";
  action: "PICK" | "CONFIRM" | "COLLECT" | "CONTINUE";
  priceMinor: number;
  payload: Record<string, unknown>;
}

export interface HistoryQuery {
  fromRoundId: string | null;
  limit: number;
  includeFeatureDetails: boolean;
}

export interface BootstrapRequest {
  contractVersion: string;
  sessionId: string;
  bootstrapRef: BootstrapRef;
}

export interface OpenGameRequest {
  contractVersion: string;
  sessionId: string;
  requestCounter: number;
  currentStateVersion: number;
  idempotencyKey: string;
  clientOperationId: string;
  bootstrapRef: BootstrapRef;
  selectedBet: null;
  selectedFeatureChoice: null;
}

export interface PlayRoundRequest {
  contractVersion: string;
  sessionId: string;
  requestCounter: number;
  currentStateVersion: number;
  idempotencyKey: string;
  clientOperationId: string;
  bootstrapRef: BootstrapRef;
  selectedBet: SelectedBet;
  selectedFeatureChoice: null;
}

export interface FeatureActionRequest {
  contractVersion: string;
  sessionId: string;
  requestCounter: number;
  currentStateVersion: number;
  idempotencyKey: string;
  clientOperationId: string;
  bootstrapRef: BootstrapRef;
  selectedBet: SelectedBet | null;
  selectedFeatureChoice: SelectedFeatureChoice;
}

export interface ResumeGameRequest {
  contractVersion: string;
  sessionId: string;
  requestCounter: number;
  currentStateVersion: number;
  idempotencyKey: string;
  clientOperationId: string;
  bootstrapRef: BootstrapRef;
  selectedBet: null;
  selectedFeatureChoice: null;
  resumeRef?: Record<string, unknown> | string;
}

export interface CloseGameRequest {
  contractVersion: string;
  sessionId: string;
  requestCounter: number;
  currentStateVersion: number;
  idempotencyKey: string;
  clientOperationId: string;
  bootstrapRef: BootstrapRef;
  selectedBet: null;
  selectedFeatureChoice: null;
  closeReason: string;
}

export interface HistoryRequest {
  contractVersion: string;
  sessionId: string;
  requestCounter: number;
  historyQuery: HistoryQuery;
}

export interface RuntimeEnvelopeResponse {
  ok: boolean;
  requestId: string;
  sessionId: string;
  requestCounter: number;
  stateVersion: number;
  wallet: Record<string, unknown>;
  round: Record<string, unknown>;
  feature: Record<string, unknown>;
  presentationPayload: Record<string, unknown>;
  restore: Record<string, unknown>;
  idempotency: Record<string, unknown>;
  retry: Record<string, unknown>;
  history?: Record<string, unknown>;
}

export interface BootstrapResponse {
  contractVersion: "slot-bootstrap-v1";
  session: {
    sessionId: string;
    requestCounter: number;
    stateVersion: number;
  };
  context: Record<string, unknown>;
  assets: Record<string, unknown>;
  runtime: Record<string, unknown>;
  policies: Record<string, unknown>;
  integrity: Record<string, unknown>;
}

export type OpenGameResponse = RuntimeEnvelopeResponse;
export type PlayRoundResponse = RuntimeEnvelopeResponse;
export type FeatureActionResponse = RuntimeEnvelopeResponse;
export type ResumeGameResponse = RuntimeEnvelopeResponse;
export type CloseGameResponse = RuntimeEnvelopeResponse;
export type HistoryResponse = RuntimeEnvelopeResponse;

export interface IGameTransport {
  bootstrap(config: GameInitConfig, request: BootstrapRequest): Promise<BootstrapResponse>;
  opengame(request: OpenGameRequest): Promise<OpenGameResponse>;
  playround(request: PlayRoundRequest): Promise<PlayRoundResponse>;
  featureaction(request: FeatureActionRequest): Promise<FeatureActionResponse>;
  resumegame(request: ResumeGameRequest): Promise<ResumeGameResponse>;
  closegame(request: CloseGameRequest): Promise<CloseGameResponse>;
  gethistory(request: HistoryRequest): Promise<HistoryResponse>;
  disconnect(): void;
  on(event: TransportEvent, callback: (...args: any[]) => void): void;
}

// Legacy/experimental compatibility API kept outside canonical transport surface.
export interface LegacyGameTransportAdapter extends IGameTransport {
  openGame(request: OpenGameRequest): Promise<OpenGameResponse>;
  playRound(request: PlayRoundRequest): Promise<PlayRoundResponse>;
  featureAction(request: FeatureActionRequest): Promise<FeatureActionResponse>;
  resumeGame(request: ResumeGameRequest): Promise<ResumeGameResponse>;
  closeGame(request: CloseGameRequest): Promise<CloseGameResponse>;
  getHistory(request: HistoryRequest): Promise<HistoryResponse>;
  connect(config: GameInitConfig): Promise<void>;
  spin(betAmount: number, operationId: string): Promise<any>;
  settle(operationId: string): Promise<any>;
}

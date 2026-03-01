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

export interface RequestMetadata {
  requestCounter?: number;
  idempotencyKey?: string;
  clientOperationId?: string;
  currentStateVersion?: string;
}

export interface BootstrapRequest {
  sessionId?: string;
}

export interface OpenGameRequest {
  sessionId: string;
  bankId?: number;
  playerId?: string;
  gameId?: number;
  gsInternalBaseUrl?: string;
  language?: string;
  internalClientCode?: string;
}

export interface OpenGameResponse {
  sessionId: string;
  balance: number;
  requestCounter: number;
  minBet?: number;
  maxBet?: number;
  currentStateVersion?: string;
  unresolvedRoundState?: unknown;
  presentationPayload?: unknown;
  runtimeConfig?: Record<string, unknown>;
}

export interface PlayRoundRequest {
  betAmount: number;
  betType: string;
  metadata?: RequestMetadata;
}

export interface PlayRoundResponse {
  roundId: string;
  balance: number;
  winAmount: number;
  requestCounter: number;
  currentStateVersion?: string;
  presentationPayload?: unknown;
  rawPlaceBet: unknown;
  rawCollect: unknown;
}

export interface FeatureActionRequest {
  action: string;
  payload?: Record<string, unknown>;
  metadata?: RequestMetadata;
}

export interface FeatureActionResponse {
  requestCounter: number;
  currentStateVersion?: string;
  presentationPayload?: unknown;
  raw: unknown;
}

export interface ResumeGameRequest {
  sessionId?: string;
  metadata?: RequestMetadata;
}

export interface CloseGameRequest {
  reason?: string;
  metadata?: RequestMetadata;
}

export interface HistoryRequest {
  pageNumber?: number;
  metadata?: RequestMetadata;
}

export interface HistoryResponse {
  requestCounter: number;
  currentStateVersion?: string;
  history: Array<Record<string, unknown>>;
  raw: unknown;
}

export interface IGameTransport {
  bootstrap(config: GameInitConfig, request?: BootstrapRequest): Promise<OpenGameResponse>;
  openGame(request: OpenGameRequest): Promise<OpenGameResponse>;
  playRound(request: PlayRoundRequest): Promise<PlayRoundResponse>;
  featureAction(request: FeatureActionRequest): Promise<FeatureActionResponse>;
  resumeGame(request?: ResumeGameRequest): Promise<OpenGameResponse>;
  closeGame(request?: CloseGameRequest): Promise<void>;
  readHistory(request?: HistoryRequest): Promise<HistoryResponse>;
  disconnect(): void;
  on(event: TransportEvent, callback: (...args: any[]) => void): void;

  // Legacy compatibility surface (deprecated).
  connect?(config: GameInitConfig): Promise<void>;
  spin?(betAmount: number, operationId: string): Promise<any>;
  settle?(operationId: string): Promise<any>;
}

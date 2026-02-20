import Fastify, { FastifyReply } from "fastify";
import cors from "@fastify/cors";
import { createHash } from "node:crypto";

type Currency = {
  code: string;
  prefix: string;
  suffix: string;
  grouping: string;
  decimal: string;
  precision: number;
  denomination: number;
};

type SessionState = {
  sessionId: string;
  balance: number;
  requestCounter: number;
  currency: Currency;
  playerId: string;
  bankId: number;
  gsInternalBaseUrl: string | null;
  gameId: number;
};

type RoundState = {
  roundId: string;
  sessionId: string;
  requestCounter: number;
  betAmount: number;
  winAmount: number;
  betType: string;
  createdAt: string;
  collected: boolean;
  slot: number;
  walletOperationId: string;
};

type ApiError = {
  code: string;
  message: string;
  details?: Record<string, unknown>;
  traceId?: string;
};

type ParsedGsError = {
  status: number | null;
  code?: string;
  message?: string;
};

type OpenGameRequest = {
  sessionId: string;
  bankId?: number;
  playerId?: string;
  gsInternalBaseUrl?: string;
  gameId?: number;
  language?: string;
  internalClientCode?: string;
};

type PlaceBetRequest = {
  sessionId: string;
  requestCounter: number;
  clientOperationId?: string;
  bets: Array<{ betType: string; betAmount: number }>;
};

type CollectRequest = {
  sessionId: string;
  requestCounter: number;
  roundId: string;
  clientOperationId?: string;
};

type ReadHistoryRequest = {
  sessionId: string;
  requestCounter: number;
  pageNumber?: number;
};

type GsSessionValidateResponse = {
  ok: boolean;
  playerId: string;
  bankId: number;
  balance: number;
  currency: Currency;
};

type GsWalletReserveResponse = {
  ok: boolean;
  walletOperationId: string;
  balance?: number;
};

type GsWalletSettleResponse = {
  ok: boolean;
  balance?: number;
};

type RiskLevel = "low" | "medium" | "high";

type ResolvedBetConfig = {
  lines: number;
  risk: RiskLevel;
  multipliers: number[];
  weights: number[];
  weightTotal: number;
};

const APP_VERSION = "0.3.2";
const CONTRACT_VERSION = "v1";
const port = Number(process.env.PORT ?? "6400");
const GS_INTERNAL_BASE_URL = process.env.GS_INTERNAL_BASE_URL;
const GS_INTERNAL_TIMEOUT_MS = Number(process.env.GS_INTERNAL_TIMEOUT_MS ?? "3000");
const RNG_SALT = process.env.NGS_RNG_SALT ?? "change-me-before-production";

const app = Fastify({ logger: true });

const sessions = new Map<string, SessionState>();
const rounds = new Map<string, RoundState>();
const idempotentResponses = new Map<string, Record<string, unknown>>();

const defaultCurrency: Currency = {
  code: "USD",
  prefix: "$",
  suffix: "",
  grouping: ",",
  decimal: ".",
  precision: 1,
  denomination: 1
};

const RISK_LEVELS = ["low", "medium", "high"] as const;
const DEFAULT_LINES = 12;
const DEFAULT_RISK: RiskLevel = "medium";
const POSSIBLE_LINES = [10, 11, 12, 13] as const;

const PAYOUT_TABLES: Record<number, Record<RiskLevel, number[]>> = {
  8: {
    low: [2.5, 2.1, 1.2, 1, 0.4, 1, 1.2, 2.1, 2.5],
    medium: [12.2, 3, 1.2, 0.7, 0.4, 0.7, 1.2, 3, 12.2],
    high: [28, 4, 1.4, 0.3, 0.2, 0.3, 1.4, 4, 28]
  },
  9: {
    low: [3.5, 2.5, 1.5, 1, 0.65, 0.65, 1, 1.5, 2.5, 3.5],
    medium: [13.5, 4.1, 1.7, 1, 0.4, 0.4, 1, 1.7, 4.1, 13.5],
    high: [43, 6.95, 1.8, 0.6, 0.2, 0.2, 0.6, 1.8, 6.95, 43]
  },
  10: {
    low: [6.5, 3, 1.4, 1.1, 1, 0.4, 1, 1.1, 1.4, 3, 6.5],
    medium: [20, 4.65, 2, 1.5, 0.5, 0.4, 0.5, 1.5, 2, 4.65, 20],
    high: [72, 10, 2.75, 0.9, 0.3, 0.2, 0.3, 0.9, 2.75, 10, 72]
  },
  11: {
    low: [8.5, 3.4, 2.1, 1.3, 1, 0.6, 0.6, 1, 1.3, 2.1, 3.4, 8.5],
    medium: [24, 6.2, 3, 1.8, 0.6, 0.5, 0.5, 0.6, 1.8, 3, 6.2, 24],
    high: [108, 13.2, 5, 1.4, 0.4, 0.2, 0.2, 0.4, 1.4, 5, 13.2, 108]
  },
  12: {
    low: [9, 3.4, 2, 1.2, 1.1, 1, 0.4, 1, 1.1, 1.2, 2, 3.4, 9],
    medium: [30, 11, 4, 2.1, 1.1, 0.5, 0.3, 0.5, 1.1, 2.1, 4, 11, 30],
    high: [150, 21, 8, 2, 0.7, 0.2, 0.2, 0.2, 0.7, 2, 8, 21, 150]
  },
  13: {
    low: [9, 4.5, 3.2, 2, 1.2, 0.9, 0.6, 0.6, 0.9, 1.2, 2, 3.2, 4.5, 9],
    medium: [42, 12, 6.3, 3, 1.3, 0.6, 0.4, 0.4, 0.6, 1.3, 3, 6.3, 12, 42],
    high: [240, 35, 10, 4, 1, 0.2, 0.2, 0.2, 0.2, 1, 4, 10, 35, 240]
  },
  14: {
    low: [8, 4, 2, 1.45, 1.2, 1.1, 1, 0.4, 1, 1.1, 1.2, 1.45, 2, 4, 8],
    medium: [58, 15.5, 7.2, 4.1, 1.6, 1, 0.5, 0.2, 0.5, 1, 1.6, 4.1, 7.2, 15.5, 58],
    high: [400, 55, 16, 5.2, 1.8, 0.3, 0.2, 0.2, 0.2, 0.3, 1.8, 5.2, 16, 55, 400]
  },
  15: {
    low: [15, 8, 3.2, 2, 1.6, 1.1, 1, 0.6, 0.6, 1, 1.1, 1.6, 2, 3.2, 8, 15],
    medium: [88, 18, 11.2, 5.2, 3, 1.1, 0.5, 0.3, 0.3, 0.5, 1.1, 3, 5.2, 11.2, 18, 88],
    high: [620, 80, 25, 7.5, 3, 0.5, 0.2, 0.2, 0.2, 0.2, 0.5, 3, 7.5, 25, 80, 620]
  },
  16: {
    low: [16, 8.5, 2.2, 1.45, 1.2, 1.2, 1.1, 1, 0.4, 1, 1.1, 1.2, 1.2, 1.45, 2.2, 8.5, 16],
    medium: [108, 38, 9.2, 5.1, 3, 1.3, 1, 0.5, 0.3, 0.5, 1, 1.3, 3, 5.1, 9.2, 38, 108],
    high: [888, 112, 26, 8, 4, 2, 0.2, 0.2, 0.2, 0.2, 0.2, 2, 4, 8, 26, 112, 888]
  }
};

const PASCALE_WEIGHTS = new Map<number, number[]>();

function fail(reply: FastifyReply, status: number, error: ApiError) {
  return reply.code(status).send({ error });
}

function assertCounter(session: SessionState, incoming: number): void {
  const expected = session.requestCounter + 1;
  if (incoming !== expected) {
    throw new Error(`INVALID_REQUEST_COUNTER expected=${expected} got=${incoming}`);
  }
  session.requestCounter = incoming;
}

function normalizeRisk(value?: string): RiskLevel {
  const normalized = String(value ?? "").trim().toLowerCase();
  if (normalized === "low" || normalized === "medium" || normalized === "high") {
    return normalized;
  }
  return DEFAULT_RISK;
}

function normalizeLines(value?: number): number {
  if (
    typeof value === "number" &&
    Number.isInteger(value) &&
    POSSIBLE_LINES.includes(value as (typeof POSSIBLE_LINES)[number]) &&
    PAYOUT_TABLES[value]
  ) {
    return value;
  }
  return DEFAULT_LINES;
}

function parseBetType(betType?: string): { risk: RiskLevel; lines: number } {
  const raw = String(betType ?? "").trim();
  const patternA = raw.match(/(low|medium|high)[_:\- ]?(\d{1,2})?/i);
  if (patternA) {
    return {
      risk: normalizeRisk(patternA[1]),
      lines: normalizeLines(patternA[2] ? Number(patternA[2]) : undefined)
    };
  }

  const patternB = raw.match(/(\d{1,2})[_:\- ]?(low|medium|high)/i);
  if (patternB) {
    return {
      risk: normalizeRisk(patternB[2]),
      lines: normalizeLines(Number(patternB[1]))
    };
  }

  return { risk: DEFAULT_RISK, lines: DEFAULT_LINES };
}

function pascalWeights(lines: number): number[] {
  const cached = PASCALE_WEIGHTS.get(lines);
  if (cached) {
    return cached;
  }

  const values: number[] = [1];
  let coeff = 1;
  for (let k = 1; k <= lines; k += 1) {
    coeff = (coeff * (lines - k + 1)) / k;
    values.push(Math.round(coeff));
  }
  PASCALE_WEIGHTS.set(lines, values);
  return values;
}

function resolveBetConfig(betType?: string): ResolvedBetConfig {
  const parsed = parseBetType(betType);
  const lines = normalizeLines(parsed.lines);
  const risk = normalizeRisk(parsed.risk);
  const multipliers = PAYOUT_TABLES[lines]?.[risk] ?? PAYOUT_TABLES[DEFAULT_LINES][DEFAULT_RISK];
  const weights = pascalWeights(lines);
  const weightTotal = weights.reduce((sum, weight) => sum + weight, 0);

  return { lines, risk, multipliers, weights, weightTotal };
}

function deterministicSlot(seed: string, weights: number[], weightTotal: number): number {
  const hex = createHash("sha256").update(`${seed}:${RNG_SALT}`).digest("hex");
  const n = parseInt(hex.slice(0, 8), 16) % weightTotal;
  let cumulative = 0;
  for (let slot = 0; slot < weights.length; slot += 1) {
    cumulative += weights[slot] ?? 0;
    if (n < cumulative) {
      return slot;
    }
  }
  return Math.floor(weights.length / 2);
}

function buildRoundId(sessionId: string, rc: number): string {
  const base = `${sessionId}:${rc}:${Date.now()}`;
  return createHash("sha1").update(base).digest("hex").slice(0, 16);
}

function deterministicOutcome(
  seed: string,
  betType?: string
): { slot: number; multiplier: number; lines: number; risk: RiskLevel; multipliers: number[] } {
  const config = resolveBetConfig(betType);
  const slot = deterministicSlot(seed, config.weights, config.weightTotal);
  return {
    slot,
    multiplier: config.multipliers[slot] ?? config.multipliers[Math.floor(config.multipliers.length / 2)] ?? 0,
    lines: config.lines,
    risk: config.risk,
    multipliers: config.multipliers
  };
}

function idempotencyKey(sessionId: string, action: string, clientOperationId?: string): string | null {
  if (!clientOperationId) return null;
  return `${sessionId}:${action}:${clientOperationId}`;
}

function resolveGsInternalBaseUrl(value?: string): string | null {
  const source = value?.trim() || GS_INTERNAL_BASE_URL?.trim();
  if (!source) {
    return null;
  }
  return source.replace(/\/+$/, "");
}

function isGsTimeoutError(error: unknown): boolean {
  return error instanceof Error && error.message.includes("GS_INTERNAL_TIMEOUT");
}

function parseGsError(error: unknown): ParsedGsError {
  if (!(error instanceof Error)) {
    return { status: null };
  }

  const statusMatch = error.message.match(/status=(\d+)/);
  const bodyMatch = error.message.match(/body=(\{.*\})$/);
  const status = statusMatch ? Number(statusMatch[1]) : null;

  if (!bodyMatch) {
    return { status };
  }

  try {
    const parsed = JSON.parse(bodyMatch[1]) as { error?: { code?: string; message?: string } };
    return {
      status,
      code: parsed.error?.code,
      message: parsed.error?.message
    };
  } catch {
    return { status };
  }
}

function extractExpectedSessionId(message?: string): string | null {
  if (!message) {
    return null;
  }
  const match = message.match(/expected:([^)]+)\)/);
  return match ? match[1] : null;
}

async function gsPost<TReq extends Record<string, unknown>, TRes>(
  gsInternalBaseUrl: string | null,
  path: string,
  payload: TReq,
  headers?: Record<string, string>
): Promise<TRes> {
  if (!gsInternalBaseUrl) {
    throw new Error("GS_INTERNAL_BASE_URL is not configured");
  }

  const requestHeaders: Record<string, string> = {
    "content-type": "application/json",
    "x-ngs-contract": CONTRACT_VERSION
  };
  if (headers) {
    for (const [key, value] of Object.entries(headers)) {
      if (value) {
        requestHeaders[key] = value;
      }
    }
  }

  const timeoutController = new AbortController();
  const timeout = setTimeout(() => timeoutController.abort(), GS_INTERNAL_TIMEOUT_MS);
  let response: Response;
  try {
    response = await fetch(`${gsInternalBaseUrl}${path}`, {
      method: "POST",
      headers: requestHeaders,
      body: JSON.stringify(payload),
      signal: timeoutController.signal
    });
  } catch (error) {
    if (error instanceof Error && error.name === "AbortError") {
      throw new Error(`GS_INTERNAL_TIMEOUT timeoutMs=${GS_INTERNAL_TIMEOUT_MS} path=${path}`);
    }
    throw error;
  } finally {
    clearTimeout(timeout);
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`GS_INTERNAL_ERROR status=${response.status} body=${text}`);
  }

  return (await response.json()) as TRes;
}

async function gsSessionValidate(payload: {
  gsInternalBaseUrl: string | null;
  sessionId: string;
  bankId: number;
  playerId?: string;
}): Promise<GsSessionValidateResponse> {
  if (!payload.gsInternalBaseUrl) {
    return {
      ok: true,
      playerId: payload.playerId ?? payload.sessionId,
      bankId: payload.bankId,
      balance: 100000,
      currency: defaultCurrency
    };
  }
  const requestId = `${payload.sessionId}:sv:${Date.now()}`;
  const requestPayload: { sessionId: string; bankId: number; playerId?: string } = {
    sessionId: payload.sessionId,
    bankId: payload.bankId
  };
  if (payload.playerId && payload.playerId.trim().length > 0) {
    requestPayload.playerId = payload.playerId.trim();
  }
  return gsPost(
    payload.gsInternalBaseUrl,
    "/gs-internal/newgames/v1/session/validate",
    requestPayload,
    {
      "x-request-id": requestId,
      "x-session-id": payload.sessionId
    }
  );
}

async function gsWalletReserve(payload: {
  gsInternalBaseUrl: string | null;
  sessionId: string;
  gameId: number;
  roundId: string;
  betAmount: number;
  requestCounter: number;
  clientOperationId?: string;
}): Promise<GsWalletReserveResponse> {
  if (!payload.gsInternalBaseUrl) {
    return { ok: true, walletOperationId: `mock-${payload.roundId}` };
  }
  const idempotencyKey = payload.clientOperationId ?? `${payload.sessionId}:${payload.roundId}:reserve:${payload.requestCounter}`;
  const requestId = `${payload.sessionId}:reserve:${payload.requestCounter}:${Date.now()}`;
  return gsPost(
    payload.gsInternalBaseUrl,
    "/gs-internal/newgames/v1/wallet/reserve",
    {
      sessionId: payload.sessionId,
      gameId: payload.gameId,
      roundId: payload.roundId,
      betAmount: payload.betAmount,
      requestCounter: payload.requestCounter,
      clientOperationId: payload.clientOperationId
    },
    {
      "x-request-id": requestId,
      "x-session-id": payload.sessionId,
      "x-idempotency-key": idempotencyKey
    }
  );
}

async function gsWalletSettle(payload: {
  gsInternalBaseUrl: string | null;
  sessionId: string;
  gameId: number;
  roundId: string;
  walletOperationId: string;
  winAmount: number;
  requestCounter: number;
  clientOperationId?: string;
}): Promise<GsWalletSettleResponse> {
  if (!payload.gsInternalBaseUrl) {
    return { ok: true };
  }
  const idempotencyKey = payload.clientOperationId ?? `${payload.sessionId}:${payload.roundId}:settle:${payload.requestCounter}`;
  const requestId = `${payload.sessionId}:settle:${payload.requestCounter}:${Date.now()}`;
  return gsPost(
    payload.gsInternalBaseUrl,
    "/gs-internal/newgames/v1/wallet/settle",
    {
      sessionId: payload.sessionId,
      gameId: payload.gameId,
      roundId: payload.roundId,
      walletOperationId: payload.walletOperationId,
      winAmount: payload.winAmount,
      requestCounter: payload.requestCounter,
      clientOperationId: payload.clientOperationId
    },
    {
      "x-request-id": requestId,
      "x-session-id": payload.sessionId,
      "x-idempotency-key": idempotencyKey
    }
  );
}

async function gsHistoryWrite(payload: {
  gsInternalBaseUrl: string | null;
  sessionId: string;
  roundId: string;
  eventType: string;
  data: Record<string, unknown>;
}): Promise<{ ok: boolean }> {
  if (!payload.gsInternalBaseUrl) {
    return { ok: true };
  }
  const requestId = `${payload.sessionId}:history:${payload.roundId}:${Date.now()}`;
  return gsPost(
    payload.gsInternalBaseUrl,
    "/gs-internal/newgames/v1/history/write",
    {
      sessionId: payload.sessionId,
      roundId: payload.roundId,
      eventType: payload.eventType,
      data: payload.data
    },
    {
      "x-request-id": requestId,
      "x-session-id": payload.sessionId
    }
  );
}

await app.register(cors, { origin: true, credentials: true });

app.get("/healthz", async () => {
  return {
    status: "ok",
    service: "new-games-server",
    version: APP_VERSION,
    contract: CONTRACT_VERSION,
    ts: new Date().toISOString()
  };
});

app.get("/v1/contract", async () => {
  return {
    contract: CONTRACT_VERSION,
    defaultGsInternalBaseUrl: GS_INTERNAL_BASE_URL ?? null,
    endpoints: ["/v1/opengame", "/v1/placebet", "/v1/collect", "/v1/readhistory"]
  };
});

app.post("/v1/opengame", async (request, reply) => {
  const body = request.body as OpenGameRequest;

  if (!body?.sessionId) {
    return fail(reply, 400, { code: "BAD_REQUEST", message: "sessionId is required" });
  }

  const bankId = body.bankId ?? 6274;
  const gameId = body.gameId ?? 10;
  const playerId = body.playerId?.trim();
  const gsInternalBaseUrl = resolveGsInternalBaseUrl(body.gsInternalBaseUrl);
  let effectiveSessionId = body.sessionId;

  const existing = sessions.get(effectiveSessionId);
  if (existing) {
    const defaultConfig = resolveBetConfig(`${DEFAULT_RISK}:${DEFAULT_LINES}`);
    return {
      method: "opengame",
      sessionId: existing.sessionId,
      playerId: existing.playerId,
      bankId: existing.bankId,
      balance: existing.balance,
      currency: existing.currency,
      requestCounter: existing.requestCounter,
      gsInternalBaseUrl: existing.gsInternalBaseUrl,
      gameId: existing.gameId,
      minBet: 100,
      maxBet: 10000,
      defaultBet: 100,
      multipliers: defaultConfig.multipliers,
      possibleLines: POSSIBLE_LINES,
      risks: RISK_LEVELS,
      defaultLines: DEFAULT_LINES,
      defaultRisk: DEFAULT_RISK
    };
  }

  let validated: GsSessionValidateResponse;
  try {
    validated = await gsSessionValidate({ gsInternalBaseUrl, sessionId: effectiveSessionId, bankId, playerId });
  } catch (error) {
    const parsed = parseGsError(error);
    const expectedSessionId = extractExpectedSessionId(parsed.message);
    const isSessionMismatch = Boolean(expectedSessionId) && parsed.message?.includes("Mismatch sessionId");

    if (isSessionMismatch && expectedSessionId && expectedSessionId !== effectiveSessionId) {
      request.log.warn(
        { sessionId: effectiveSessionId, expectedSessionId, traceId: request.id },
        "retrying GS session validation with expected session id"
      );

      effectiveSessionId = expectedSessionId;
      try {
        validated = await gsSessionValidate({ gsInternalBaseUrl, sessionId: effectiveSessionId, bankId, playerId });
      } catch (retryError) {
        request.log.error(retryError);
        return fail(reply, 401, {
          code: "INVALID_SESSION",
          message: "Session is stale or expired. Relaunch the game to get a fresh session.",
          details: { expectedSessionId: effectiveSessionId },
          traceId: request.id
        });
      }
    } else if (isGsTimeoutError(error)) {
      request.log.error(error);
      return fail(reply, 502, {
        code: "GS_SESSION_VALIDATE_FAILED",
        message: "GS session validation timed out",
        details: { reason: "timeout", timeoutMs: GS_INTERNAL_TIMEOUT_MS },
        traceId: request.id
      });
    } else if (parsed.status === 400 || parsed.status === 401 || parsed.status === 403) {
      request.log.error(error);
      return fail(reply, 401, {
        code: "INVALID_SESSION",
        message: "Session is stale or expired. Relaunch the game to get a fresh session.",
        traceId: request.id
      });
    } else {
      request.log.error(error);
      return fail(reply, 502, {
        code: "GS_SESSION_VALIDATE_FAILED",
        message: "GS session validation request failed",
        traceId: request.id
      });
    }
  }

  if (!validated.ok) {
    return fail(reply, 401, {
      code: "INVALID_SESSION",
      message: "session is not valid in GS",
      traceId: request.id
    });
  }

  const session: SessionState = {
    sessionId: effectiveSessionId,
    playerId: validated.playerId,
    bankId: validated.bankId,
    balance: validated.balance,
    requestCounter: 0,
    currency: validated.currency,
    gsInternalBaseUrl,
    gameId
  };
  sessions.set(effectiveSessionId, session);

  const defaultConfig = resolveBetConfig(`${DEFAULT_RISK}:${DEFAULT_LINES}`);
  return {
    method: "opengame",
    sessionId: session.sessionId,
    playerId: session.playerId,
    bankId: session.bankId,
    balance: session.balance,
    currency: session.currency,
    requestCounter: session.requestCounter,
    gsInternalBaseUrl: session.gsInternalBaseUrl,
    gameId: session.gameId,
    minBet: 100,
    maxBet: 10000,
    defaultBet: 100,
    multipliers: defaultConfig.multipliers,
    possibleLines: POSSIBLE_LINES,
    risks: RISK_LEVELS,
    defaultLines: DEFAULT_LINES,
    defaultRisk: DEFAULT_RISK
  };
});

app.post("/v1/placebet", async (request, reply) => {
  const body = request.body as PlaceBetRequest;
  const session = sessions.get(body?.sessionId);

  if (!session) {
    return fail(reply, 404, { code: "INVALID_SESSION", message: "session is not found" });
  }
  if (!body?.bets?.length) {
    return fail(reply, 400, { code: "BAD_REQUEST", message: "bets are required" });
  }

  const key = idempotencyKey(body.sessionId, "placebet", body.clientOperationId);
  if (key && idempotentResponses.has(key)) {
    return idempotentResponses.get(key);
  }

  try {
    assertCounter(session, body.requestCounter);
  } catch (error) {
    return fail(reply, 409, {
      code: "INVALID_REQUEST_COUNTER",
      message: String(error),
      traceId: request.id
    });
  }

  const bet = body.bets[0];
  if (bet.betAmount <= 0) {
    return fail(reply, 400, { code: "BAD_REQUEST", message: "betAmount must be > 0" });
  }
  if (session.balance < bet.betAmount) {
    return fail(reply, 409, { code: "INSUFFICIENT_FUNDS", message: "not enough balance" });
  }

  const roundId = buildRoundId(session.sessionId, body.requestCounter);

  let reserve: GsWalletReserveResponse;
  try {
    reserve = await gsWalletReserve({
      gsInternalBaseUrl: session.gsInternalBaseUrl,
      sessionId: session.sessionId,
      gameId: session.gameId,
      roundId,
      betAmount: bet.betAmount,
      requestCounter: body.requestCounter,
      clientOperationId: body.clientOperationId
    });
  } catch (error) {
    request.log.error(error);
    return fail(reply, 502, {
      code: "GS_WALLET_RESERVE_FAILED",
      message: isGsTimeoutError(error) ? "GS wallet reserve timed out" : "GS wallet reserve request failed",
      details: isGsTimeoutError(error) ? { reason: "timeout", timeoutMs: GS_INTERNAL_TIMEOUT_MS } : undefined,
      traceId: request.id
    });
  }

  if (!reserve.ok) {
    return fail(reply, 409, { code: "WALLET_RESERVE_REJECTED", message: "GS reserve rejected" });
  }

  const outcome = deterministicOutcome(`${session.sessionId}:${roundId}:${bet.betType}`, bet.betType);
  const winAmount = Math.round(bet.betAmount * outcome.multiplier);
  const slot = outcome.slot;

  if (typeof reserve.balance === "number") {
    session.balance = reserve.balance;
  } else {
    session.balance -= bet.betAmount;
  }

  const round: RoundState = {
    roundId,
    sessionId: session.sessionId,
    requestCounter: body.requestCounter,
    betAmount: bet.betAmount,
    winAmount,
    betType: bet.betType,
    createdAt: new Date().toISOString(),
    collected: false,
    slot,
    walletOperationId: reserve.walletOperationId
  };
  rounds.set(roundId, round);

  void gsHistoryWrite({
    gsInternalBaseUrl: session.gsInternalBaseUrl,
    sessionId: session.sessionId,
    roundId,
    eventType: "BET_PLACED",
    data: {
      betType: bet.betType,
      betAmount: bet.betAmount,
      winAmount,
      multiplier: outcome.multiplier,
      slot,
      lines: outcome.lines,
      risk: outcome.risk
    }
  }).catch((error) => request.log.warn(error));

  const response = {
    method: "placebet",
    sessionId: session.sessionId,
    requestCounter: body.requestCounter,
    roundId,
    balance: session.balance,
    roundEnded: false,
    math: {
      bets: [{ betType: bet.betType, betAmount: bet.betAmount, winAmount }],
      totalBetAmount: bet.betAmount,
      totalWinAmount: winAmount,
      details: {
        ballInfo: [{ position: slot * 1024, slot }],
        config: {
          lines: outcome.lines,
          risk: outcome.risk,
          multipliers: outcome.multipliers
        }
      },
      nextAction: ["COLLECT"]
    }
  };

  if (key) {
    idempotentResponses.set(key, response);
  }

  return response;
});

app.post("/v1/collect", async (request, reply) => {
  const body = request.body as CollectRequest;
  const session = sessions.get(body?.sessionId);
  const round = rounds.get(body?.roundId);

  if (!session) {
    return fail(reply, 404, { code: "INVALID_SESSION", message: "session is not found" });
  }
  if (!round || round.sessionId !== body.sessionId) {
    return fail(reply, 404, { code: "ROUND_NOT_FOUND", message: "round is not found" });
  }

  const key = idempotencyKey(body.sessionId, "collect", body.clientOperationId);
  if (key && idempotentResponses.has(key)) {
    return idempotentResponses.get(key);
  }

  try {
    assertCounter(session, body.requestCounter);
  } catch (error) {
    return fail(reply, 409, {
      code: "INVALID_REQUEST_COUNTER",
      message: String(error),
      traceId: request.id
    });
  }

  if (!round.collected) {
    let settled: GsWalletSettleResponse;
    try {
      settled = await gsWalletSettle({
        gsInternalBaseUrl: session.gsInternalBaseUrl,
        sessionId: session.sessionId,
        gameId: session.gameId,
        roundId: round.roundId,
        walletOperationId: round.walletOperationId,
        winAmount: round.winAmount,
        requestCounter: body.requestCounter,
        clientOperationId: body.clientOperationId
      });
      if (!settled.ok) {
        return fail(reply, 409, {
          code: "WALLET_SETTLE_REJECTED",
          message: "GS settle rejected",
          traceId: request.id
        });
      }
    } catch (error) {
      request.log.error(error);
      return fail(reply, 502, {
        code: "GS_WALLET_SETTLE_FAILED",
        message: isGsTimeoutError(error) ? "GS wallet settle timed out" : "GS wallet settle request failed",
        details: isGsTimeoutError(error) ? { reason: "timeout", timeoutMs: GS_INTERNAL_TIMEOUT_MS } : undefined,
        traceId: request.id
      });
    }

    if (typeof settled.balance === "number") {
      session.balance = settled.balance;
    } else {
      session.balance += round.winAmount;
    }
    round.collected = true;

    void gsHistoryWrite({
      gsInternalBaseUrl: session.gsInternalBaseUrl,
      sessionId: session.sessionId,
      roundId: round.roundId,
      eventType: "ROUND_COLLECTED",
      data: {
        betAmount: round.betAmount,
        winAmount: round.winAmount,
        balance: session.balance
      }
    }).catch((error) => request.log.warn(error));
  }

  const response = {
    method: "collect",
    sessionId: session.sessionId,
    requestCounter: body.requestCounter,
    balance: session.balance,
    winAmount: round.winAmount,
    roundId: round.roundId
  };

  if (key) {
    idempotentResponses.set(key, response);
  }

  return response;
});

app.post("/v1/readhistory", async (request, reply) => {
  const body = request.body as ReadHistoryRequest;
  const session = sessions.get(body?.sessionId);
  if (!session) {
    return fail(reply, 404, { code: "INVALID_SESSION", message: "session is not found" });
  }

  try {
    assertCounter(session, body.requestCounter);
  } catch (error) {
    return fail(reply, 409, {
      code: "INVALID_REQUEST_COUNTER",
      message: String(error),
      traceId: request.id
    });
  }

  const history = [...rounds.values()]
    .filter((r) => r.sessionId === body.sessionId)
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
    .slice(0, 50)
    .map((r) => ({
      created: r.createdAt,
      roundUuid: r.roundId,
      sumBetAmount: r.betAmount,
      sumWinAmount: r.winAmount,
      betType: r.betType,
      collected: r.collected
    }));

  return {
    method: "readhistory",
    sessionId: session.sessionId,
    requestCounter: body.requestCounter,
    pageNumber: body.pageNumber ?? 0,
    nextPage: false,
    history
  };
});

// Contract-compatible mock GS internal endpoints for local integration tests.
app.post("/gs-internal/newgames/v1/session/validate", async (request) => {
  const body = request.body as { sessionId: string; bankId: number; playerId: string };
  return {
    ok: true,
    sessionId: body.sessionId,
    playerId: body.playerId,
    bankId: body.bankId,
    balance: 100000,
    currency: defaultCurrency
  };
});

app.post("/gs-internal/newgames/v1/wallet/reserve", async (request) => {
  const body = request.body as { roundId: string };
  return { ok: true, walletOperationId: `mock-${body.roundId}` };
});

app.post("/gs-internal/newgames/v1/wallet/settle", async () => ({ ok: true }));
app.post("/gs-internal/newgames/v1/history/write", async () => ({ ok: true }));

app.listen({ port, host: "0.0.0.0" }).catch((error) => {
  app.log.error(error);
  process.exit(1);
});

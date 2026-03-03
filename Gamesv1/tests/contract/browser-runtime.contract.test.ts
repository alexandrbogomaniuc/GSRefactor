import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

import Ajv2020, { type ValidateFunction } from "ajv/dist/2020.js";

import { GsHttpRuntimeTransport } from "../../packages/core-protocol/src/http/GsHttpRuntimeTransport.ts";

type JsonRecord = Record<string, unknown>;

type FixtureEndpoint = {
  method: string;
  path: string;
  readOnly: boolean;
};

type RequestFixture = {
  fixtureVersion: string;
  name: string;
  endpoint: FixtureEndpoint;
  requestHeaders: Record<string, string>;
  requestBody: JsonRecord;
};

type ResponseFixture = {
  fixtureVersion: string;
  name: string;
  endpoint: FixtureEndpoint;
  response: {
    httpStatus: number;
    body: JsonRecord;
  };
};

type RuntimeEnvelope = {
  ok: true;
  requestId: string;
  sessionId: string;
  requestCounter: number;
  stateVersion: number;
  wallet: JsonRecord;
  round: JsonRecord;
  feature: JsonRecord;
  presentationPayload: JsonRecord;
  restore: JsonRecord;
  idempotency: JsonRecord;
  retry: JsonRecord;
};

type RuntimeState = {
  sessionId: string;
  requestCounter: number;
  stateVersion: number;
  balanceMinor: number;
  history: Array<Record<string, unknown>>;
  idempotencyCache: Map<string, RuntimeEnvelope>;
};

type RequestLogEntry = {
  path: string;
  headers: Record<string, string>;
  body: JsonRecord;
};

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const fixturesDir = path.resolve(__dirname, "../../docs/gs/fixtures");
const schemasDir = path.resolve(__dirname, "../../docs/gs/schemas");

const readJson = <T>(absolutePath: string): T => {
  const raw = fs.readFileSync(absolutePath, "utf8");
  const withoutBom = raw.charCodeAt(0) === 0xfeff ? raw.slice(1) : raw;
  return JSON.parse(withoutBom) as T;
};

const asRecord = (value: unknown): JsonRecord =>
  typeof value === "object" && value !== null && !Array.isArray(value)
    ? (value as JsonRecord)
    : {};

const asString = (value: unknown): string => {
  if (typeof value === "string") return value;
  throw new Error(`Expected string, got ${String(value)}`);
};

const asNumber = (value: unknown): number => {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }
  throw new Error(`Expected numeric value, got ${String(value)}`);
};

const normalizeHeaders = (headers: HeadersInit | undefined): Record<string, string> => {
  if (!headers) return {};
  if (headers instanceof Headers) return Object.fromEntries(headers.entries());
  if (Array.isArray(headers)) {
    return Object.fromEntries(headers.map(([key, value]) => [String(key).toLowerCase(), String(value)]));
  }
  return Object.fromEntries(
    Object.entries(headers).map(([key, value]) => [key.toLowerCase(), String(value)]),
  );
};

const mkResponse = (status: number, body: Record<string, unknown>): Response =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });

const ajv = new Ajv2020({
  allErrors: true,
  strict: false,
});

const compileSchema = (schemaFile: string): ValidateFunction => {
  const schema = readJson<JsonRecord>(path.join(schemasDir, schemaFile));
  return ajv.compile(schema);
};

const validateBootstrapRequest = compileSchema("bootstrap.request.schema.json");
const validateBootstrapResponse = compileSchema("bootstrap.response.schema.json");
const validateOpenGameRequest = compileSchema("opengame.request.schema.json");
const validateOpenGameResponse = compileSchema("opengame.response.schema.json");
const validatePlayRoundRequest = compileSchema("playround.request.schema.json");
const validatePlayRoundResponse = compileSchema("playround.response.schema.json");
const validateFeatureActionRequest = compileSchema("featureaction.request.schema.json");
const validateFeatureActionResponse = compileSchema("featureaction.response.schema.json");
const validateResumeGameRequest = compileSchema("resumegame.request.schema.json");
const validateResumeGameResponse = compileSchema("resumegame.response.schema.json");
const validateGetHistoryRequest = compileSchema("gethistory.request.schema.json");
const validateGetHistoryResponse = compileSchema("gethistory.response.schema.json");
const validateCloseGameRequest = compileSchema("closegame.request.schema.json");
const validateCloseGameResponse = compileSchema("closegame.response.schema.json");

const assertValid = (label: string, validator: ValidateFunction, payload: unknown): void => {
  if (validator(payload)) return;
  const issues = (validator.errors ?? [])
    .map((err) => `${err.instancePath || "/"} ${err.message ?? "invalid"}`)
    .join("; ");
  throw new Error(`${label} schema validation failed: ${issues}`);
};

const readRequestFixture = (name: string): RequestFixture =>
  readJson<RequestFixture>(path.join(fixturesDir, `${name}.request.json`));

const readResponseFixture = (name: string): ResponseFixture =>
  readJson<ResponseFixture>(path.join(fixturesDir, `${name}.response.json`));

const bootstrapRequestFixture = readRequestFixture("bootstrap");
const bootstrapResponseFixture = readResponseFixture("bootstrap");
const openGameRequestFixture = readRequestFixture("opengame");
const openGameResponseFixture = readResponseFixture("opengame");
const playRoundRequestFixture = readRequestFixture("playround");
const playRoundResponseFixture = readResponseFixture("playround");
const playRoundDuplicateResponseFixture =
  readResponseFixture("playround.duplicate");
const featureActionRequestFixture = readRequestFixture("featureaction");
const featureActionResponseFixture = readResponseFixture("featureaction");
const resumeGameRequestFixture = readRequestFixture("resumegame");
const resumeGameResponseFixture = readResponseFixture("resumegame");
const getHistoryRequestFixture = readRequestFixture("gethistory");
const getHistoryResponseFixture = readResponseFixture("gethistory");
const closeGameRequestFixture = readRequestFixture("closegame");
const closeGameResponseFixture = readResponseFixture("closegame");

for (const fixture of [
  bootstrapRequestFixture,
  openGameRequestFixture,
  playRoundRequestFixture,
  featureActionRequestFixture,
  resumeGameRequestFixture,
  getHistoryRequestFixture,
  closeGameRequestFixture,
]) {
  assert.equal(fixture.endpoint.path, `/slot/v1/${fixture.name.split(".")[0]}`);
}

assertValid("bootstrap.request", validateBootstrapRequest, bootstrapRequestFixture.requestBody);
assertValid(
  "bootstrap.response",
  validateBootstrapResponse,
  bootstrapResponseFixture.response.body,
);
assertValid("opengame.request", validateOpenGameRequest, openGameRequestFixture.requestBody);
assertValid("opengame.response", validateOpenGameResponse, openGameResponseFixture.response.body);
assertValid("playround.request", validatePlayRoundRequest, playRoundRequestFixture.requestBody);
assertValid("playround.response", validatePlayRoundResponse, playRoundResponseFixture.response.body);
assertValid("playround.duplicate.response", validatePlayRoundResponse, playRoundDuplicateResponseFixture.response.body);
assertValid(
  "featureaction.request",
  validateFeatureActionRequest,
  featureActionRequestFixture.requestBody,
);
assertValid(
  "featureaction.response",
  validateFeatureActionResponse,
  featureActionResponseFixture.response.body,
);
assertValid("resumegame.request", validateResumeGameRequest, resumeGameRequestFixture.requestBody);
assertValid("resumegame.response", validateResumeGameResponse, resumeGameResponseFixture.response.body);
assertValid("gethistory.request", validateGetHistoryRequest, getHistoryRequestFixture.requestBody);
assertValid("gethistory.response", validateGetHistoryResponse, getHistoryResponseFixture.response.body);
assertValid("closegame.request", validateCloseGameRequest, closeGameRequestFixture.requestBody);
assertValid("closegame.response", validateCloseGameResponse, closeGameResponseFixture.response.body);

const bootstrapSession = asRecord(bootstrapResponseFixture.response.body.session);
const state: RuntimeState = {
  sessionId: asString(bootstrapSession.sessionId),
  requestCounter: asNumber(bootstrapSession.requestCounter),
  stateVersion: asNumber(bootstrapSession.stateVersion),
  balanceMinor: 100000,
  history: [],
  idempotencyCache: new Map(),
};

const requestLog: RequestLogEntry[] = [];
let forceHistoryStateDrift = false;

const cloneEnvelope = (envelope: RuntimeEnvelope): RuntimeEnvelope =>
  structuredClone(envelope) as RuntimeEnvelope;

const assertMonotonic = (pathName: string, next: number, current: number): Response | null => {
  if (next <= current) {
    return mkResponse(409, {
      error: {
        code: "INVALID_REQUEST_COUNTER",
        message: `${pathName}: requestCounter must be monotonic (current=${current}, got=${next})`,
      },
    });
  }
  return null;
};

(globalThis as { fetch: typeof fetch }).fetch = (async (
  input: RequestInfo | URL,
  init?: RequestInit,
): Promise<Response> => {
  const url = String(input);
  const pathName = new URL(url).pathname;
  const body = init?.body ? (JSON.parse(String(init.body)) as JsonRecord) : {};
  const headers = normalizeHeaders(init?.headers);

  requestLog.push({ path: pathName, headers, body });

  if (pathName === "/slot/v1/bootstrap") {
    assertValid("bootstrap.request", validateBootstrapRequest, body);
    if (headers["x-idempotency-key"] || headers["x-client-operation-id"]) {
      return mkResponse(400, {
        error: { code: "IDEMPOTENCY_KEY_REUSE", message: "bootstrap is read-only" },
      });
    }
    return mkResponse(
      bootstrapResponseFixture.response.httpStatus,
      bootstrapResponseFixture.response.body,
    );
  }

  if (pathName === "/slot/v1/opengame") {
    assertValid("opengame.request", validateOpenGameRequest, body);
    if (!headers["x-idempotency-key"] || !headers["x-client-operation-id"]) {
      return mkResponse(400, {
        error: { code: "IDEMPOTENCY_KEY_REUSE", message: "missing mutating headers" },
      });
    }

    const monotonicError = assertMonotonic(pathName, asNumber(body.requestCounter), state.requestCounter);
    if (monotonicError) return monotonicError;

    state.requestCounter = asNumber(body.requestCounter);
    state.stateVersion = Math.max(state.stateVersion + 1, asNumber(body.currentStateVersion) + 1);

    const response = structuredClone(openGameResponseFixture.response.body) as JsonRecord;
    response.requestCounter = state.requestCounter;
    response.stateVersion = state.stateVersion;
    response.sessionId = state.sessionId;
    asRecord(response.wallet).balanceMinor = state.balanceMinor;

    assertValid("opengame.response", validateOpenGameResponse, response);
    return mkResponse(openGameResponseFixture.response.httpStatus, response);
  }

  if (pathName === "/slot/v1/playround") {
    assertValid("playround.request", validatePlayRoundRequest, body);
    if (!headers["x-idempotency-key"] || !headers["x-client-operation-id"]) {
      return mkResponse(400, {
        error: { code: "IDEMPOTENCY_KEY_REUSE", message: "missing mutating headers" },
      });
    }

    const idempotencyKey = asString(body.idempotencyKey);
    const cached = state.idempotencyCache.get(idempotencyKey);
    if (cached) {
      const duplicate = structuredClone(
        playRoundDuplicateResponseFixture.response.body,
      ) as JsonRecord;
      duplicate.requestCounter = cached.requestCounter;
      duplicate.stateVersion = cached.stateVersion;
      duplicate.sessionId = cached.sessionId;
      duplicate.wallet = cached.wallet;
      duplicate.round = cached.round;
      duplicate.feature = cached.feature;
      duplicate.presentationPayload = cached.presentationPayload;
      duplicate.restore = cached.restore;

      assertValid("playround.duplicate.response", validatePlayRoundResponse, duplicate);
      return mkResponse(200, duplicate);
    }

    const monotonicError = assertMonotonic(pathName, asNumber(body.requestCounter), state.requestCounter);
    if (monotonicError) return monotonicError;

    state.requestCounter = asNumber(body.requestCounter);
    state.stateVersion = Math.max(state.stateVersion + 1, asNumber(body.currentStateVersion) + 1);

    const response = structuredClone(playRoundResponseFixture.response.body) as JsonRecord;
    const selectedBet = asRecord(body.selectedBet);
    const round = asRecord(response.round);
    const wallet = asRecord(response.wallet);
    const winMinor = asNumber(round.winMinor ?? 0);

    state.balanceMinor = state.balanceMinor - asNumber(selectedBet.totalBetMinor) + winMinor;

    response.requestCounter = state.requestCounter;
    response.stateVersion = state.stateVersion;
    response.sessionId = state.sessionId;
    wallet.balanceMinor = state.balanceMinor;
    wallet.previousBalanceMinor = state.balanceMinor + asNumber(selectedBet.totalBetMinor) - winMinor;
    round.roundId = `R-${String(state.requestCounter).padStart(3, "0")}`;
    round.betMinor = asNumber(selectedBet.totalBetMinor);
    round.netEffectMinor = winMinor - asNumber(selectedBet.totalBetMinor);
    asRecord(response.idempotency).isDuplicate = false;
    asRecord(response.idempotency).duplicateOfRequestId = null;

    assertValid("playround.response", validatePlayRoundResponse, response);
    state.idempotencyCache.set(idempotencyKey, response as RuntimeEnvelope);

    return mkResponse(playRoundResponseFixture.response.httpStatus, response);
  }

  if (pathName === "/slot/v1/featureaction") {
    assertValid("featureaction.request", validateFeatureActionRequest, body);
    const monotonicError = assertMonotonic(pathName, asNumber(body.requestCounter), state.requestCounter);
    if (monotonicError) return monotonicError;

    state.requestCounter = asNumber(body.requestCounter);
    state.stateVersion = Math.max(state.stateVersion + 1, asNumber(body.currentStateVersion) + 1);

    const response = structuredClone(featureActionResponseFixture.response.body) as JsonRecord;
    response.requestCounter = state.requestCounter;
    response.stateVersion = state.stateVersion;
    response.sessionId = state.sessionId;
    asRecord(response.wallet).balanceMinor = state.balanceMinor;

    assertValid("featureaction.response", validateFeatureActionResponse, response);
    return mkResponse(featureActionResponseFixture.response.httpStatus, response);
  }

  if (pathName === "/slot/v1/resumegame") {
    assertValid("resumegame.request", validateResumeGameRequest, body);
    const monotonicError = assertMonotonic(pathName, asNumber(body.requestCounter), state.requestCounter);
    if (monotonicError) return monotonicError;

    state.requestCounter = asNumber(body.requestCounter);
    state.stateVersion = Math.max(state.stateVersion + 1, asNumber(body.currentStateVersion) + 1);

    const response = structuredClone(resumeGameResponseFixture.response.body) as JsonRecord;
    response.requestCounter = state.requestCounter;
    response.stateVersion = state.stateVersion;
    response.sessionId = state.sessionId;
    asRecord(response.wallet).balanceMinor = state.balanceMinor;

    assertValid("resumegame.response", validateResumeGameResponse, response);
    return mkResponse(resumeGameResponseFixture.response.httpStatus, response);
  }

  if (pathName === "/slot/v1/gethistory") {
    assertValid("gethistory.request", validateGetHistoryRequest, body);
    if (headers["x-idempotency-key"] || headers["x-client-operation-id"]) {
      return mkResponse(400, {
        error: { code: "IDEMPOTENCY_KEY_REUSE", message: "gethistory is read-only" },
      });
    }
    if (asNumber(body.requestCounter) !== state.requestCounter) {
      return mkResponse(409, {
        error: { code: "INVALID_REQUEST_COUNTER", message: "history must reuse accepted counter" },
      });
    }

    const response = structuredClone(getHistoryResponseFixture.response.body) as JsonRecord;
    response.requestCounter = forceHistoryStateDrift
      ? state.requestCounter + 1
      : state.requestCounter;
    response.stateVersion = forceHistoryStateDrift
      ? state.stateVersion + 1
      : state.stateVersion;
    response.sessionId = state.sessionId;
    asRecord(response.wallet).balanceMinor = state.balanceMinor;
    asRecord(response.feature).featureContext = {
      ...asRecord(asRecord(response.feature).featureContext),
      historyQuery: body.historyQuery,
    };

    assertValid("gethistory.response", validateGetHistoryResponse, response);
    return mkResponse(getHistoryResponseFixture.response.httpStatus, response);
  }

  if (pathName === "/slot/v1/closegame") {
    assertValid("closegame.request", validateCloseGameRequest, body);
    const monotonicError = assertMonotonic(pathName, asNumber(body.requestCounter), state.requestCounter);
    if (monotonicError) return monotonicError;

    state.requestCounter = asNumber(body.requestCounter);
    state.stateVersion = Math.max(state.stateVersion + 1, asNumber(body.currentStateVersion) + 1);

    const response = structuredClone(closeGameResponseFixture.response.body) as JsonRecord;
    response.requestCounter = state.requestCounter;
    response.stateVersion = state.stateVersion;
    response.sessionId = state.sessionId;

    assertValid("closegame.response", validateCloseGameResponse, response);
    return mkResponse(closeGameResponseFixture.response.httpStatus, response);
  }

  return mkResponse(404, { error: { code: "NOT_FOUND", message: `No route for ${pathName}` } });
}) as typeof fetch;

const transport = new GsHttpRuntimeTransport();

let passed = 0;
let failed = 0;

const test = async (name: string, fn: () => Promise<void>) => {
  try {
    await fn();
    console.log(`PASS ${name}`);
    passed += 1;
  } catch (error) {
    console.error(`FAIL ${name}`);
    console.error(error);
    failed += 1;
  }
};

const assertCommonHeaders = (entry: RequestLogEntry) => {
  assert.equal(entry.headers["x-gs-client-contract"], "slot-browser-v1");
  assert.ok(entry.headers["x-request-id"]);
  assert.ok(entry.headers["x-session-id"]);
};

await test("bootstrap is read-only and keeps bootstrap contract separate", async () => {
  const result = await transport.bootstrap(
    {
      mode: "GS_HTTP_RUNTIME",
      baseUrl: "http://mock-runtime",
      gameId: 10045,
      bankId: 6274,
      playerId: "P-9001",
      sessionId: "SID-123",
      language: "en",
      internalClientCode: "slot-browser-v1",
    },
    bootstrapRequestFixture.requestBody as any,
  );

  assertValid("bootstrap.result", validateBootstrapResponse, result);
  assert.equal(result.contractVersion, "slot-bootstrap-v1");

  const last = requestLog.at(-1)!;
  assert.equal(last.path, "/slot/v1/bootstrap");
  assertCommonHeaders(last);
  assert.equal(last.headers["x-idempotency-key"], undefined);
  assert.equal(last.headers["x-client-operation-id"], undefined);
});

await test("opengame uses canonical mutating headers and envelope", async () => {
  const result = await transport.opengame(openGameRequestFixture.requestBody as any);
  assertValid("opengame.result", validateOpenGameResponse, result);

  const last = requestLog.at(-1)!;
  assert.equal(last.path, "/slot/v1/opengame");
  assertCommonHeaders(last);
  assert.equal(last.headers["x-idempotency-key"], asString(openGameRequestFixture.requestBody.idempotencyKey));
  assert.equal(last.headers["x-client-operation-id"], asString(openGameRequestFixture.requestBody.clientOperationId));
});

await test("playround validates exact selectedBet wire shape", async () => {
  const result = await transport.playround(playRoundRequestFixture.requestBody as any);
  assertValid("playround.result", validatePlayRoundResponse, result);
  assert.equal(typeof asNumber(asRecord(result.round).betMinor), "number");
  assert.equal(typeof asNumber(asRecord(result.wallet).balanceMinor), "number");
});

await test("duplicate playround is idempotent", async () => {
  const base = playRoundRequestFixture.requestBody;
  const first = await transport.playround({
    ...base,
    requestCounter: 44,
    idempotencyKey: "idem-play-dup-44",
    clientOperationId: "client-op-dup-44",
  } as any);
  const second = await transport.playround({
    ...base,
    requestCounter: 44,
    idempotencyKey: "idem-play-dup-44",
    clientOperationId: "client-op-dup-44",
  } as any);

  assertValid("playround.first", validatePlayRoundResponse, first);
  assertValid("playround.second", validatePlayRoundResponse, second);
  assert.equal(asRecord(second.idempotency).isDuplicate, true);
});

await test("featureaction advances sequencing", async () => {
  const result = await transport.featureaction({
    ...(featureActionRequestFixture.requestBody as any),
    requestCounter: state.requestCounter + 1,
    currentStateVersion: state.stateVersion,
    idempotencyKey: `idem-feature-${state.requestCounter + 1}`,
    clientOperationId: `client-op-feature-${state.requestCounter + 1}`,
  });
  assertValid("featureaction.result", validateFeatureActionResponse, result);
});

await test("resumegame returns restore payload", async () => {
  const result = await transport.resumegame({
    ...(resumeGameRequestFixture.requestBody as any),
    requestCounter: state.requestCounter + 1,
    currentStateVersion: state.stateVersion,
    idempotencyKey: `idem-resume-${state.requestCounter + 1}`,
    clientOperationId: `client-op-resume-${state.requestCounter + 1}`,
  });
  assertValid("resumegame.result", validateResumeGameResponse, result);
  assert.ok(asRecord(result.restore).opaqueRestorePayload !== undefined);
});

await test("gethistory is read-only and reuses accepted counters", async () => {
  const result = await transport.gethistory({
    ...(getHistoryRequestFixture.requestBody as any),
    requestCounter: state.requestCounter,
  });
  assertValid("gethistory.result", validateGetHistoryResponse, result);

  const last = requestLog.at(-1)!;
  assert.equal(last.path, "/slot/v1/gethistory");
  assert.equal(last.headers["x-idempotency-key"], undefined);
  assert.equal(last.headers["x-client-operation-id"], undefined);
});

await test("gethistory rejects state-advancing response envelope", async () => {
  forceHistoryStateDrift = true;
  try {
    await assert.rejects(
      () =>
        transport.gethistory({
          ...(getHistoryRequestFixture.requestBody as any),
          requestCounter: state.requestCounter,
        }),
      /gethistory must be read-only/,
    );
  } finally {
    forceHistoryStateDrift = false;
  }
});

await test("non-monotonic requestCounter is rejected", async () => {
  await assert.rejects(
    () =>
      transport.playround({
        ...(playRoundRequestFixture.requestBody as any),
        requestCounter: 43,
        idempotencyKey: "idem-stale-043",
        clientOperationId: "client-op-stale-043",
      }),
    /requestCounter must be monotonic/,
  );
});

await test("closegame requires idempotency headers and closes session", async () => {
  const closeRequest = {
    ...(closeGameRequestFixture.requestBody as any),
    requestCounter: state.requestCounter + 1,
    currentStateVersion: state.stateVersion,
    idempotencyKey: `idem-close-${state.requestCounter + 1}`,
    clientOperationId: `client-op-close-${state.requestCounter + 1}`,
  };
  const result = await transport.closegame({
    ...closeRequest,
  });
  assertValid("closegame.result", validateCloseGameResponse, result);

  const last = requestLog.at(-1)!;
  assert.equal(last.path, "/slot/v1/closegame");
  assert.equal(last.headers["x-idempotency-key"], asString(closeRequest.idempotencyKey));
  assert.equal(last.headers["x-client-operation-id"], asString(closeRequest.clientOperationId));
});

console.log(`\nBrowser runtime contract tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

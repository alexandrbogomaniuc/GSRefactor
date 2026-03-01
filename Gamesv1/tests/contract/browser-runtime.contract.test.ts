import assert from "node:assert/strict";

import { GsHttpRuntimeTransport } from "../../packages/core-protocol/src/http/GsHttpRuntimeTransport.ts";

type RuntimeState = {
  sessionId: string;
  balance: number;
  requestCounter: number;
  currentStateVersion: string;
  restorePayload: Record<string, unknown> | null;
  history: Array<Record<string, unknown>>;
  idempotencyCache: Map<string, Record<string, unknown>>;
};

const state: RuntimeState = {
  sessionId: "session-123",
  balance: 10000,
  requestCounter: 0,
  currentStateVersion: "v0",
  restorePayload: {
    roundId: "restore-round-1",
    stage: "WAITING_PRESENTATION",
  },
  history: [],
  idempotencyCache: new Map(),
};

const mkResponse = (status: number, body: Record<string, unknown>): Response =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });

const asRecord = (value: unknown): Record<string, unknown> =>
  (typeof value === "object" && value !== null ? (value as Record<string, unknown>) : {});

(globalThis as { fetch: typeof fetch }).fetch = (async (
  input: RequestInfo | URL,
  init?: RequestInit,
): Promise<Response> => {
  const url = String(input);
  const path = new URL(url).pathname;
  const body = init?.body ? (JSON.parse(String(init.body)) as Record<string, unknown>) : {};

  if (path === "/v1/bootstrap") {
    state.requestCounter = 1;
    state.currentStateVersion = "v1";
    return mkResponse(200, {
      session: {
        sessionId: state.sessionId,
        requestCounter: state.requestCounter,
        currentStateVersion: state.currentStateVersion,
      },
      wallet: {
        balance: state.balance,
        currencyCode: "EUR",
      },
      runtimeConfig: {
        currencyCode: "EUR",
        minBet: 10,
        maxBet: 200,
        maxExposure: 100000,
        defaultBet: 20,
      },
      capabilities: {
        turbo: {
          allowed: true,
          speedId: "turbo-x2",
          preferred: false,
        },
      },
      restore: state.restorePayload,
    });
  }

  if (path === "/v1/opengame") {
    state.requestCounter += 1;
    state.currentStateVersion = `v${state.requestCounter}`;
    return mkResponse(200, {
      sessionId: state.sessionId,
      balance: state.balance,
      requestCounter: state.requestCounter,
      currentStateVersion: state.currentStateVersion,
      runtimeConfig: {
        defaultBet: 20,
      },
    });
  }

  if (path === "/v1/playround") {
    const requestCounter = Number(body.requestCounter);
    const idempotencyKey = String(body.idempotencyKey ?? "");

    if (!Number.isFinite(requestCounter)) {
      return mkResponse(400, { error: { code: "GS_SEQUENCE_INVALID", message: "missing requestCounter" } });
    }

    if (idempotencyKey && state.idempotencyCache.has(idempotencyKey)) {
      return mkResponse(200, state.idempotencyCache.get(idempotencyKey)!);
    }

    state.requestCounter = requestCounter;
    state.currentStateVersion = `v${state.requestCounter}`;

    const betAmount = Number(body.betAmount ?? 0);
    const winAmount = Math.max(0, Math.round(betAmount * 1.2));
    state.balance = state.balance - betAmount + winAmount;

    const result = {
      roundId: `round-${state.requestCounter}`,
      requestCounter: state.requestCounter,
      currentStateVersion: state.currentStateVersion,
      balance: state.balance,
      winAmount,
      presentationPayload: {
        slotIndex: 3,
        reelStopColumns: [
          [0, 1, 2],
          [1, 2, 3],
          [2, 3, 4],
          [3, 4, 5],
          [4, 5, 6],
        ],
        winAmount,
      },
    };

    if (idempotencyKey) {
      state.idempotencyCache.set(idempotencyKey, result);
    }

    state.history.unshift({
      roundId: result.roundId,
      winAmount,
      balance: state.balance,
      requestCounter: state.requestCounter,
    });

    return mkResponse(200, result);
  }

  if (path === "/v1/featureaction") {
    const requestCounter = Number(body.requestCounter);
    state.requestCounter = requestCounter;
    state.currentStateVersion = `v${state.requestCounter}`;

    return mkResponse(200, {
      requestCounter: state.requestCounter,
      currentStateVersion: state.currentStateVersion,
      presentationPayload: {
        action: body.action,
        accepted: true,
      },
    });
  }

  if (path === "/v1/resumegame") {
    state.requestCounter = Number(body.requestCounter ?? state.requestCounter + 1);
    state.currentStateVersion = `v${state.requestCounter}`;

    return mkResponse(200, {
      sessionId: state.sessionId,
      balance: state.balance,
      requestCounter: state.requestCounter,
      currentStateVersion: state.currentStateVersion,
      unresolvedRoundState: state.restorePayload,
    });
  }

  if (path === "/v1/gethistory") {
    state.requestCounter = Number(body.requestCounter ?? state.requestCounter + 1);
    state.currentStateVersion = `v${state.requestCounter}`;

    return mkResponse(200, {
      requestCounter: state.requestCounter,
      currentStateVersion: state.currentStateVersion,
      history: state.history,
    });
  }

  if (path === "/v1/closegame") {
    return mkResponse(200, { ok: true });
  }

  return mkResponse(404, { error: { code: "NOT_FOUND", message: `No route for ${path}` } });
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

await test("bootstrap returns runtime/session/wallet truth", async () => {
  const result = await transport.bootstrap(
    {
      mode: "GS_HTTP_RUNTIME",
      baseUrl: "http://mock-runtime",
      gameId: 10,
      sessionId: state.sessionId,
    },
    { sessionId: state.sessionId },
  );

  assert.equal(result.sessionId, state.sessionId);
  assert.equal(result.requestCounter, 1);
  assert.equal(result.balance, 10000);
  assert.ok(asRecord(result.runtimeConfig).defaultBet);
});

await test("opengame updates sequencing snapshot", async () => {
  const result = await transport.openGame({
    sessionId: state.sessionId,
    gameId: 10,
  });

  assert.equal(result.sessionId, state.sessionId);
  assert.equal(result.requestCounter, 2);
  assert.equal(result.currentStateVersion, "v2");
});

await test("playround consumes requestCounter/idempotency and returns payload", async () => {
  const result = await transport.playRound({
    betAmount: 100,
    betType: "DEFAULT",
    metadata: {
      requestCounter: 3,
      idempotencyKey: "idem-round-3",
      clientOperationId: "op-round-3",
      currentStateVersion: "v2",
    },
  });

  assert.equal(result.requestCounter, 3);
  assert.equal(result.currentStateVersion, "v3");
  assert.ok(asRecord(result.presentationPayload).reelStopColumns);
});

await test("idempotency duplicate returns same round result", async () => {
  const first = await transport.playRound({
    betAmount: 50,
    betType: "DEFAULT",
    metadata: {
      requestCounter: 4,
      idempotencyKey: "idem-dup-4",
      clientOperationId: "op-dup-4",
      currentStateVersion: "v3",
    },
  });

  const second = await transport.playRound({
    betAmount: 50,
    betType: "DEFAULT",
    metadata: {
      requestCounter: 4,
      idempotencyKey: "idem-dup-4",
      clientOperationId: "op-dup-4",
      currentStateVersion: "v3",
    },
  });

  assert.equal(first.roundId, second.roundId);
  assert.equal(first.requestCounter, second.requestCounter);
  assert.equal(first.balance, second.balance);
});

await test("featureaction updates requestCounter/currentStateVersion", async () => {
  const result = await transport.featureAction({
    action: "toggle-autoplay",
    payload: { enabled: true },
    metadata: {
      requestCounter: 5,
      idempotencyKey: "idem-feature-5",
      clientOperationId: "op-feature-5",
      currentStateVersion: "v4",
    },
  });

  assert.equal(result.requestCounter, 5);
  assert.equal(result.currentStateVersion, "v5");
  assert.equal(asRecord(result.presentationPayload).accepted, true);
});

await test("resumegame returns restore payload", async () => {
  const result = await transport.resumeGame({
    sessionId: state.sessionId,
    metadata: {
      requestCounter: 6,
      idempotencyKey: "idem-resume-6",
      clientOperationId: "op-resume-6",
      currentStateVersion: "v5",
    },
  });

  assert.equal(result.requestCounter, 6);
  assert.ok(result.unresolvedRoundState);
});

await test("gethistory returns entries and sequencing", async () => {
  const result = await transport.getHistory({
    pageNumber: 0,
    metadata: {
      requestCounter: 7,
      currentStateVersion: "v6",
    },
  });

  assert.equal(result.requestCounter, 7);
  assert.ok(Array.isArray(result.history));
  assert.ok(result.history.length >= 1);
});

await test("closegame succeeds", async () => {
  await transport.closeGame({
    reason: "test-close",
    metadata: {
      requestCounter: 8,
      currentStateVersion: "v7",
    },
  });
});

console.log(`\nBrowser runtime contract tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

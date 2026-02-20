import test from "node:test";
import assert from "node:assert/strict";
import { postJson, startGsStub, startNgsServer, type StartedGsStub, type StartedNgsServer } from "./test-helpers.ts";

let ngsServer: StartedNgsServer | null = null;
let gsStub: StartedGsStub | null = null;

test.afterEach(async () => {
  if (ngsServer) {
    await ngsServer.stop();
    ngsServer = null;
  }
  if (gsStub) {
    await gsStub.stop();
    gsStub = null;
  }
});

test("opengame fails with GS session validation timeout", async () => {
  gsStub = await startGsStub({ validateDelayMs: 350 });
  ngsServer = await startNgsServer({ GS_INTERNAL_TIMEOUT_MS: "80" });

  const response = await postJson<{ error: { code: string; message: string; details?: { reason?: string } } }>(
    ngsServer.baseUrl,
    "/v1/opengame",
    {
      sessionId: `session-timeout-open-${Date.now()}`,
      bankId: 6274,
      playerId: "player-timeout-open",
      gameId: 10,
      gsInternalBaseUrl: gsStub.baseUrl
    }
  );

  assert.equal(response.status, 502);
  assert.equal(response.data.error.code, "GS_SESSION_VALIDATE_FAILED");
  assert.match(response.data.error.message, /timed out/i);
  assert.equal(response.data.error.details?.reason, "timeout");
});

test("placebet fails with GS reserve timeout", async () => {
  gsStub = await startGsStub({ reserveDelayMs: 350 });
  ngsServer = await startNgsServer({ GS_INTERNAL_TIMEOUT_MS: "80" });

  const sessionId = `session-timeout-reserve-${Date.now()}`;
  const opened = await postJson<{ requestCounter: number }>(ngsServer.baseUrl, "/v1/opengame", {
    sessionId,
    gameId: 10,
    gsInternalBaseUrl: gsStub.baseUrl
  });
  assert.equal(opened.status, 200);
  assert.equal(opened.data.requestCounter, 0);

  const place = await postJson<{ error: { code: string; message: string; details?: { reason?: string } } }>(
    ngsServer.baseUrl,
    "/v1/placebet",
    {
      sessionId,
      requestCounter: 1,
      bets: [{ betType: "MEDIUM:16", betAmount: 100 }]
    }
  );

  assert.equal(place.status, 502);
  assert.equal(place.data.error.code, "GS_WALLET_RESERVE_FAILED");
  assert.match(place.data.error.message, /timed out/i);
  assert.equal(place.data.error.details?.reason, "timeout");
});

test("collect fails with GS settle timeout", async () => {
  gsStub = await startGsStub({ settleDelayMs: 350 });
  ngsServer = await startNgsServer({ GS_INTERNAL_TIMEOUT_MS: "80" });

  const sessionId = `session-timeout-settle-${Date.now()}`;
  const opened = await postJson<{ requestCounter: number }>(ngsServer.baseUrl, "/v1/opengame", {
    sessionId,
    gameId: 10,
    gsInternalBaseUrl: gsStub.baseUrl
  });
  assert.equal(opened.status, 200);

  const place = await postJson<{ roundId: string }>(ngsServer.baseUrl, "/v1/placebet", {
    sessionId,
    requestCounter: 1,
    bets: [{ betType: "MEDIUM:16", betAmount: 100 }]
  });
  assert.equal(place.status, 200);

  const collect = await postJson<{ error: { code: string; message: string; details?: { reason?: string } } }>(
    ngsServer.baseUrl,
    "/v1/collect",
    {
      sessionId,
      requestCounter: 2,
      roundId: place.data.roundId
    }
  );

  assert.equal(collect.status, 502);
  assert.equal(collect.data.error.code, "GS_WALLET_SETTLE_FAILED");
  assert.match(collect.data.error.message, /timed out/i);
  assert.equal(collect.data.error.details?.reason, "timeout");
});

test("reconnect preserves session state and counter", async () => {
  ngsServer = await startNgsServer();

  const sessionId = `session-reconnect-${Date.now()}`;

  const open1 = await postJson<{ requestCounter: number; balance: number }>(ngsServer.baseUrl, "/v1/opengame", {
    sessionId,
    gameId: 10
  });
  assert.equal(open1.status, 200);
  assert.equal(open1.data.requestCounter, 0);

  const place = await postJson<{ roundId: string; balance: number }>(ngsServer.baseUrl, "/v1/placebet", {
    sessionId,
    requestCounter: 1,
    bets: [{ betType: "MEDIUM:16", betAmount: 100 }]
  });
  assert.equal(place.status, 200);

  const openReconnect = await postJson<{ requestCounter: number; balance: number }>(ngsServer.baseUrl, "/v1/opengame", {
    sessionId,
    gameId: 10
  });
  assert.equal(openReconnect.status, 200);
  assert.equal(openReconnect.data.requestCounter, 1);
  assert.equal(openReconnect.data.balance, place.data.balance);

  const collect = await postJson<{ requestCounter: number; roundId: string }>(ngsServer.baseUrl, "/v1/collect", {
    sessionId,
    requestCounter: 2,
    roundId: place.data.roundId
  });
  assert.equal(collect.status, 200);
  assert.equal(collect.data.requestCounter, 2);
});

test("opengame recovers from stale session id mismatch by retrying expected session id", async () => {
  const expectedSessionId = `session-expected-${Date.now()}`;
  gsStub = await startGsStub({ validateExpectedSessionId: expectedSessionId });
  ngsServer = await startNgsServer();

  const open = await postJson<{ sessionId: string; requestCounter: number; balance: number }>(
    ngsServer.baseUrl,
    "/v1/opengame",
    {
      sessionId: `session-stale-${Date.now()}`,
      gameId: 10,
      gsInternalBaseUrl: gsStub.baseUrl
    }
  );

  assert.equal(open.status, 200);
  assert.equal(open.data.sessionId, expectedSessionId);
  assert.equal(open.data.requestCounter, 0);
  assert.ok(open.data.balance > 0);

  const place = await postJson<{ requestCounter: number }>(ngsServer.baseUrl, "/v1/placebet", {
    sessionId: open.data.sessionId,
    requestCounter: 1,
    bets: [{ betType: "MEDIUM:16", betAmount: 100 }]
  });

  assert.equal(place.status, 200);
  assert.equal(place.data.requestCounter, 1);
});

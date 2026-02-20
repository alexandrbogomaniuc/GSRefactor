import test from "node:test";
import assert from "node:assert/strict";
import { postJson, startNgsServer, type StartedNgsServer } from "./test-helpers.ts";

let ngsServer: StartedNgsServer | null = null;

test.before(async () => {
  ngsServer = await startNgsServer();
});

test.after(async () => {
  if (!ngsServer) {
    return;
  }
  await ngsServer.stop();
});

test("opengame -> placebet -> collect -> readhistory happy path with idempotency", async () => {
  const sessionId = `session-happy-${Date.now()}`;

  const open = await postJson<{ requestCounter: number; balance: number }>(ngsServer!.baseUrl, "/v1/opengame", {
    sessionId,
    bankId: 6274,
    playerId: sessionId,
    gameId: 10
  });
  assert.equal(open.status, 200);
  assert.equal(open.data.requestCounter, 0);
  assert.equal(open.data.balance, 100000);

  const placePayload = {
    sessionId,
    requestCounter: 1,
    clientOperationId: "place-op-1",
    bets: [{ betType: "MEDIUM:16", betAmount: 100 }]
  };

  const place = await postJson<{
    roundId: string;
    requestCounter: number;
    balance: number;
    math: { totalBetAmount: number; totalWinAmount: number };
  }>(ngsServer!.baseUrl, "/v1/placebet", placePayload);

  assert.equal(place.status, 200);
  assert.equal(place.data.requestCounter, 1);
  assert.ok(place.data.roundId.length > 0);
  assert.equal(place.data.math.totalBetAmount, 100);
  assert.ok(place.data.math.totalWinAmount >= 0);

  const placeDuplicate = await postJson<typeof place.data>(ngsServer!.baseUrl, "/v1/placebet", placePayload);
  assert.equal(placeDuplicate.status, 200);
  assert.deepEqual(placeDuplicate.data, place.data);

  const collectPayload = {
    sessionId,
    requestCounter: 2,
    roundId: place.data.roundId,
    clientOperationId: "collect-op-1"
  };

  const collect = await postJson<{ requestCounter: number; roundId: string; winAmount: number }>(
    ngsServer!.baseUrl,
    "/v1/collect",
    collectPayload
  );
  assert.equal(collect.status, 200);
  assert.equal(collect.data.requestCounter, 2);
  assert.equal(collect.data.roundId, place.data.roundId);
  assert.ok(collect.data.winAmount >= 0);

  const collectDuplicate = await postJson<typeof collect.data>(ngsServer!.baseUrl, "/v1/collect", collectPayload);
  assert.equal(collectDuplicate.status, 200);
  assert.deepEqual(collectDuplicate.data, collect.data);

  const history = await postJson<{ requestCounter: number; history: Array<{ roundUuid: string; collected: boolean }> }>(
    ngsServer!.baseUrl,
    "/v1/readhistory",
    { sessionId, requestCounter: 3, pageNumber: 0 }
  );
  assert.equal(history.status, 200);
  assert.equal(history.data.requestCounter, 3);
  assert.ok(history.data.history.length >= 1);
  assert.equal(history.data.history[0]?.roundUuid, place.data.roundId);
  assert.equal(history.data.history[0]?.collected, true);
});

test("requestCounter validation rejects out-of-order requests", async () => {
  const sessionId = `session-counter-${Date.now()}`;

  const open = await postJson<{ requestCounter: number }>(ngsServer!.baseUrl, "/v1/opengame", { sessionId, gameId: 10 });
  assert.equal(open.status, 200);
  assert.equal(open.data.requestCounter, 0);

  const placeInvalid = await postJson<{ error: { code: string } }>(ngsServer!.baseUrl, "/v1/placebet", {
    sessionId,
    requestCounter: 2,
    bets: [{ betType: "MEDIUM:16", betAmount: 100 }]
  });

  assert.equal(placeInvalid.status, 409);
  assert.equal(placeInvalid.data.error.code, "INVALID_REQUEST_COUNTER");
});

test("placebet rejects insufficient balance", async () => {
  const sessionId = `session-funds-${Date.now()}`;

  const open = await postJson<{ requestCounter: number }>(ngsServer!.baseUrl, "/v1/opengame", { sessionId, gameId: 10 });
  assert.equal(open.status, 200);

  const placeInvalid = await postJson<{ error: { code: string } }>(ngsServer!.baseUrl, "/v1/placebet", {
    sessionId,
    requestCounter: 1,
    bets: [{ betType: "MEDIUM:16", betAmount: 100001 }]
  });

  assert.equal(placeInvalid.status, 409);
  assert.equal(placeInvalid.data.error.code, "INSUFFICIENT_FUNDS");
});

test("betType risk/lines changes slot range and payout table selection", async () => {
  const sessionId = `session-lines-risk-${Date.now()}`;

  const open = await postJson<{ requestCounter: number }>(ngsServer!.baseUrl, "/v1/opengame", { sessionId, gameId: 10 });
  assert.equal(open.status, 200);

  const low10 = await postJson<{
    roundId: string;
    requestCounter: number;
    math: {
      totalWinAmount: number;
      details: { ballInfo: Array<{ slot: number }> };
    };
  }>(ngsServer!.baseUrl, "/v1/placebet", {
    sessionId,
    requestCounter: 1,
    bets: [{ betType: "LOW:10", betAmount: 100 }]
  });
  assert.equal(low10.status, 200);
  const low10Slot = low10.data.math.details.ballInfo[0]?.slot ?? -1;
  assert.ok(low10Slot >= 0 && low10Slot <= 10);
  assert.ok([40, 100, 110, 140, 300, 650].includes(low10.data.math.totalWinAmount));

  const low8Collect = await postJson<{ requestCounter: number }>(ngsServer!.baseUrl, "/v1/collect", {
    sessionId,
    requestCounter: 2,
    roundId: low10.data.roundId
  });
  assert.equal(low8Collect.status, 200);

  const high13 = await postJson<{
    requestCounter: number;
    math: {
      totalWinAmount: number;
      details: { ballInfo: Array<{ slot: number }> };
    };
  }>(ngsServer!.baseUrl, "/v1/placebet", {
    sessionId,
    requestCounter: 3,
    bets: [{ betType: "HIGH:13", betAmount: 100 }]
  });
  assert.equal(high13.status, 200);
  const high13Slot = high13.data.math.details.ballInfo[0]?.slot ?? -1;
  assert.ok(high13Slot >= 0 && high13Slot <= 13);
  assert.ok([20, 100, 400, 1000, 3500, 24000].includes(high13.data.math.totalWinAmount));
});

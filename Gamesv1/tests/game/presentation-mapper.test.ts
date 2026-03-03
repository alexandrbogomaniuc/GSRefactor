import assert from "node:assert/strict";

import { mapPlayRoundToPresentation } from "../../games/premium-slot/src/app/runtime/RuntimeOutcomeMapper.ts";
import type { PlayRoundResponse } from "../../packages/core-protocol/src/IGameTransport.ts";

const makeRound = (presentationPayload: unknown): PlayRoundResponse => ({
  ok: true,
  requestId: "req-round-42",
  sessionId: "sid-demo-001",
  requestCounter: 10,
  stateVersion: 100,
  wallet: {
    balanceMinor: 900,
    currencyCode: "EUR",
  },
  round: {
    roundId: "round-42",
    winAmountMinor: 200,
    totalBetMinor: 100,
    status: "SETTLED",
  },
  feature: null,
  presentationPayload,
  restore: null,
  idempotency: null,
  retry: null,
});

let passed = 0;
let failed = 0;

const test = (name: string, fn: () => void) => {
  try {
    fn();
    console.log(`PASS ${name}`);
    passed += 1;
  } catch (error) {
    failed += 1;
    console.error(`FAIL ${name}`);
    console.error(error);
  }
};

test("maps canonical payload fields into presentation model", () => {
  const mapped = mapPlayRoundToPresentation(
    makeRound({
      reelStops: [
        [0, 1, 2],
        [1, 2, 3],
        [2, 3, 4],
        [3, 4, 5],
        [4, 5, 6],
      ],
      symbolGrid: [
        [0, 1, 2, 3, 4],
        [1, 2, 3, 4, 5],
        [2, 3, 4, 5, 6],
      ],
      counters: { freeSpinsRemaining: 3 },
      uiMessages: ["FREE SPINS ACTIVE"],
      audioCues: ["jackpot-stinger"],
      animationCues: ["free-spins-pulse"],
      labels: { jackpotTriggered: "true" },
    }),
  );

  assert.equal(mapped.roundId, "round-42");
  assert.equal(mapped.reels.stopColumns.length, 5);
  assert.equal(mapped.counters.freeSpinsRemaining, 3);
  assert.equal(mapped.messages[0], "FREE SPINS ACTIVE");
  assert.equal(mapped.soundCues[0], "jackpot-stinger");
  assert.equal(mapped.labels.jackpotTriggered, "true");
});

test("throws on missing reel stops", () => {
  assert.throws(
    () => mapPlayRoundToPresentation(makeRound({ uiMessages: ["invalid"] })),
    /Invalid presentationPayload/,
  );
});

console.log(`\nPresentation mapper tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

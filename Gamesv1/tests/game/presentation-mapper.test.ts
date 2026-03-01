import assert from "node:assert/strict";

import { mapPlayRoundToPresentation } from "../../games/premium-slot/src/app/runtime/RuntimeOutcomeMapper.ts";
import type { PlayRoundResponse } from "../../packages/core-protocol/src/IGameTransport.ts";

const makeRound = (presentationPayload: unknown): PlayRoundResponse => ({
  roundId: "round-42",
  balance: 900,
  winAmount: 200,
  requestCounter: 10,
  currentStateVersion: "v10",
  presentationPayload,
  raw: {},
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
      reelStopColumns: [
        [0, 1, 2],
        [1, 2, 3],
        [2, 3, 4],
        [3, 4, 5],
        [4, 5, 6],
      ],
      featureOverlays: [{ id: "ov1", type: "free-spins", label: "FS 3", visible: true }],
      counters: { freeSpinsRemaining: 3 },
      messages: ["FREE SPINS ACTIVE"],
      soundCues: ["jackpot-stinger"],
      animationCues: ["free-spins-pulse"],
      serverState: { freeSpinsActive: true },
    }),
  );

  assert.equal(mapped.roundId, "round-42");
  assert.equal(mapped.reels.stopColumns.length, 5);
  assert.equal(mapped.counters.freeSpinsRemaining, 3);
  assert.equal(mapped.messages[0], "FREE SPINS ACTIVE");
  assert.equal(mapped.soundCues[0], "jackpot-stinger");
});

test("throws on missing reel stop columns", () => {
  assert.throws(
    () => mapPlayRoundToPresentation(makeRound({ messages: ["invalid"] })),
    /Invalid presentationPayload/,
  );
});

console.log(`\nPresentation mapper tests: ${passed} passed, ${failed} failed.`);
if (failed > 0) {
  process.exit(1);
}

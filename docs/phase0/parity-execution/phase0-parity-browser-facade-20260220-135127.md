# Phase 0 Parity Execution Report (Browser facade probe)

- Timestamp (UTC): 2026-02-20 13:51:06
- Probe mode: browser fetch from `http://localhost:18080` (refactor static facade)
- Host context: `localhost` (casino/subcasino resolution valid)
- Method: Chrome DevTools `evaluate_script` fetch calls with contract regex checks

| Test ID | Flow | Status | HTTP | Evidence |
|---|---|---|---|---|
| P0-LA-01 | Launch | PASS_CONTRACT | 200 | `/cwstartgamev2.do?bankId=271&gameId=838&mode=real&token=test_user_271&lang=en` |
| P0-LA-02 | LaunchInvalidParams | PASS_CONTRACT | 200 | `/cwstartgamev2.do?bankId=999999&gameId=838&mode=real&token=invalid_token&lang=en` |
| P0-LA-03 | LaunchAlias | PASS_CONTRACT | 200 | `/startgame?bankId=271&gameId=838&mode=real&token=test_user_271&lang=en` |
| P0-WA-01 | Wager | PASS_CONTRACT | 200 | `/bscheck.do?bankId=271&extBonusId=1&hash=3c630d44b9fa5a1f94d4c88296000da9` |
| P0-WA-00 | WagerInvalidParams | PASS_CONTRACT | 200 | `/bscheck.do?bankId=999999&extBonusId=1&hash=deadbeef` |
| P0-SE-01 | Settle | PASS_CONTRACT | 200 | `/bsaward.do?bankId=271&amount=10000&games=all&hash=23a43b861aab6123175fc1c02e3fb38b&extBonusId=1&gameIds=838&userId=test_user_271&expDate=22.03.2026&type=Deposit&multiplier=2` |
| P0-SE-00 | SettleInvalidParams | PASS_CONTRACT | 200 | `/bsaward.do?bankId=999999&amount=10000&games=all&hash=deadbeef&extBonusId=1&gameIds=838&userId=invalid_user&expDate=31.12.2099&type=Deposit&multiplier=2` |

## Notes
- Clean alias `/startgame` is validated on facade path (`localhost:18080`) and returns parity-equivalent launch page.
- Direct GS core endpoint (`refactor-gs:8080`) still expects `.do` routing; use `/startgame.do` there for compatibility.

# Bank Configuration Comparison

## Current Configuration (Production)

| Setting | Bank 6274 (MMC) | Bank 6275 (MQC) |
|---------|----------------|----------------|
| **Bank ID** | 6274 | 6275 |
| **Description** | www.maxquest.com_MaxCoins | www.maxquest.com_QuestCoins |
| **Currency** | MMC (MaxCoins) | MQC (QuestCoins) |
| **Sub Casino ID** | 507 | 507 |
| **Auth URL** | `https://wallet.mqbase.com/authenticate` | `https://wallet.mqbase.com/authenticate` |
| **Balance URL** | `https://wallet.mqbase.com/balance` | `https://wallet.mqbase.com/balance` |
| **Bet URL** | `https://wallet.mqbase.com/betResult` | `https://wallet.mqbase.com/betResult` |
| **Refund URL** | `https://wallet.mqbase.com/refundBet` | `https://wallet.mqbase.com/refundBet` |
| **Auth Pass** | huecTlCT1OPSE0k4 | huecTlCT1OPSE0k4 |
| **Wallet Manager** | CanexCWClient | CanexCWClient |

---

## New Configuration (Local Testing)

| Setting | Bank 6274 (MMC) | Bank 6275 (MQC) |
|---------|----------------|----------------|
| **Bank ID** | 6274 | 6275 |
| **Description** | www.maxquest.com_MaxCoins | www.maxquest.com_QuestCoins |
| **Currency** | MMC (MaxCoins) | MQC (QuestCoins) |
| **Sub Casino ID** | 507 | 507 |
| **Auth URL** | `http://host.docker.internal:8000/bav/authenticate` ⚠️ | `http://host.docker.internal:8000/bav/authenticate` ⚠️ |
| **Balance URL** | `http://host.docker.internal:8000/bav/balance` ⚠️ | `http://host.docker.internal:8000/bav/balance` ⚠️ |
| **Bet URL** | `http://host.docker.internal:8000/bav/betResult` ⚠️ | `http://host.docker.internal:8000/bav/betResult` ⚠️ |
| **Refund URL** | `http://host.docker.internal:8000/bav/refundBet` ⚠️ | `http://host.docker.internal:8000/bav/refundBet` ⚠️ |
| **Auth Pass** | huecTlCT1OPSE0k4 | huecTlCT1OPSE0k4 |
| **Wallet Manager** | CanexCWClient | CanexCWClient |

⚠️ = Changed from production

---

## Key Properties (Both Banks)

| Property | Value |
|----------|-------|
| **Wallet Manager Class** | `com.dgphoenix.casino.payment.wallet.CommonWalletManager` |
| **Request Client Class** | `com.dgphoenix.casino.payment.wallet.client.v4.CanexCWClient` |
| **Auth Required** | `false` |
| **Persist Bets** | `true` |
| **Persist Wallet Ops** | `false` |
| **Persist Game Sessions** | `true` |
| **Is Enabled** | `true` |

---

## Coin Denominations (Both Banks)

| ID | Value |
|----|-------|
| 13 | 1 |
| 11 | 2 |
| 30 | 3 |
| 33 | 4 |
| 12 | 5 |
| 1 | 10 |
| 14 | 15 |
| 15 | 20 |
| 2 | 25 |
| 3 | 50 |
| 4 | 100 |

---

## Limits (Both Banks)

| Setting | Value |
|---------|-------|
| **Limit ID** | 1 |
| **Min Value** | 100 |
| **Max Value** | 10000 |

---

## Changes Required

### URLs to Update (4 per bank):
1. `COMMON_WALLET_AUTH_URL`
2. `COMMON_WALLET_BALANCE_URL`
3. `COMMON_WALLET_WAGER_URL`
4. `COMMON_WALLET_REFUND_URL`

### From (Production):
```
https://wallet.mqbase.com/*
```

### To (Local):
```
http://host.docker.internal:8000/bav/*
```

---

## Additional Notable Settings

| Property | Bank 6274 | Bank 6275 | Notes |
|----------|-----------|-----------|-------|
| `SEND_CURRENCY_SYMBOL` | `true` | `false` | Different! |
| `USE_LANG_PARAM_AS_GAME_TITLE_LANG` | Not set | `false` | Bank 6275 only |
| `KEY_MRGREN_LOGIN` | Not set | `null` | Bank 6275 only |
| `DEVELOPMENT_VERSION` | `true` | `true` | Both in dev mode |
| `IN_TEST_MODE` | `false` | `false` | Both not in test |
| `STUB_MODE` | `false` | `false` | Real wallet calls |

---

## Casino Side Endpoints (Target)

| Endpoint | URL | Method |
|----------|-----|--------|
| **Authenticate** | `http://localhost:8000/bav/authenticate` | GET |
| **Balance** | `http://localhost:8000/bav/balance` | GET |
| **Bet Result** | `http://localhost:8000/bav/betResult` | GET |
| **Refund Bet** | `http://localhost:8000/bav/refundBet` | GET |

All endpoints expect:
- `token` parameter (session token)
- `hash` parameter (MD5 hash for validation)
- `PASS_KEY` = `huecTlCT1OPSE0k4`

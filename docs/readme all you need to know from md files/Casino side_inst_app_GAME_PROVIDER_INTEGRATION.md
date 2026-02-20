# Game Provider ↔ Casino Side Integration Setup

## Step 1: Update Cassandra - Banks Configuration

Connect to Cassandra and update wallet URLs for banks 6274 and 6275:

```cql
-- Connect to Cassandra (from Game Provider Docker container)
docker exec -it gp3-gs-1 cqlsh c1.gsmp.lan

-- Use the correct keyspace
USE RCasinoKS;

-- View current banks configuration
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet, bank_url_refund_bet 
FROM banks 
WHERE bank_id IN (6274, 6275);

-- Update Bank 6274 (MaxCoins → USD) wallet URLs
UPDATE banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6274;

-- Update Bank 6275 (QuestCoins → VND) wallet URLs  
UPDATE banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6275;

-- Verify the changes
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet, bank_url_refund_bet 
FROM banks 
WHERE bank_id IN (6274, 6275);
```

## Step 2: Create Session Token in MySQL

```sql
-- Create session token for userId=8 (test_user)
INSERT INTO sessions (userId, token, session_type, provider, status, created_at, expires_at) 
VALUES (8, 'bav_game_session_001', 'game', 'bsg_bav', 'active', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY))
ON DUPLICATE KEY UPDATE 
    expires_at = DATE_ADD(NOW(), INTERVAL 30 DAY), 
    status = 'active',
    provider = 'bsg_bav';

-- Add balance for testing (optional)
UPDATE wallets 
SET balance_usd = 100000  -- $1000.00 in cents
WHERE userId = 8;

-- Verify
SELECT userId, token, session_type, provider, status, expires_at 
FROM sessions 
WHERE userId = 8 AND provider = 'bsg_bav';

SELECT userId, balance_usd, currency 
FROM wallets 
WHERE userId = 8;
```

## Step 3: Game Launch URL

**Template URL Format:**
```
http://localhost:8081/free/mp/template.jsp?bankId=6274&sessionId=bav_game_session_001&gameId=838&lang=en
```

**Parameters:**
- `bankId`: 6274 (USD/MaxCoins) or 6275 (VND/QuestCoins)
- `sessionId`: The token from MySQL sessions table
- `gameId`: 838 (Moonshot - see game_inventory.md for other games)
- `lang`: en (language)

**Alternative game IDs:**
- 838: Moonshot
- 960: Point of View
- 975: Battlegrounds

## Step 4: Expected Flow

1. **Game Launch** → Template.jsp loads
2. **Authentication Call** → Game Provider calls `http://host.docker.internal:8000/bav/authenticate`
   - With: token=bav_game_session_001, hash=calculated_md5
3. **Casino Side Response** → Returns user balance in XML
4. **Game Loads** → Player sees balance and can play
5. **Bet Placed** → Game Provider calls `/bav/betResult` with bet amount
6. **Win Occurs** → Game Provider calls `/bav/betResult` with win amount
7. **Balance Updates** → Verified via `/bav/balance` calls

## Step 5: Testing Checklist

- [ ] Cassandra banks updated
- [ ] Session token created
- [ ] Open game URL in browser
- [ ] Check Casino Side Docker logs for authenticate call
- [ ] Verify balance displays correctly
- [ ] Place a bet
- [ ] Check balance deduction
- [ ] Trigger win
- [ ] Check balance credit

## Troubleshooting

**If authentication fails:**
- Check Casino Side Docker logs: `docker logs casino_side --tail 50`
- Verify hash calculation matches PASS_KEY
- Confirm session token exists in MySQL
- Check network connectivity: Game Provider → Casino Side

**If game doesn't load:**
- Check Game Provider Docker logs
- Verify Cassandra banks configuration
- Confirm both containers can communicate via `host.docker.internal`

## Key URLs

- **Casino Side**: http://localhost:8000
- **Game Provider**: http://localhost:8081
- **BAV Endpoints**: http://localhost:8000/bav/*
- **Game Launch**: http://localhost:8081/free/mp/template.jsp?...

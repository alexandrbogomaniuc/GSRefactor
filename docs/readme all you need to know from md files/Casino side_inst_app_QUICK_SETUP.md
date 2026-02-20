# Quick Setup Commands - Copy & Paste

## Step 1: Create Session Token
```sql
-- Run in MySQL
USE apidb;

INSERT INTO sessions (userId, token, session_type, provider, status, expires_at) 
VALUES (8, 'bav_game_session_001', 'game', 'bsg_bav', 'active', DATE_ADD(NOW(), INTERVAL 30 DAY))
ON DUPLICATE KEY UPDATE expires_at = DATE_ADD(NOW(), INTERVAL 30 DAY), status = 'active', provider = 'bsg_bav';

UPDATE wallets SET balance = 1000.00 WHERE userId = 8 AND currency_code = 'USD';

SELECT 'Session:' as info, userId, token, provider, status FROM sessions WHERE userId=8 AND provider='bsg_bav';
SELECT 'Wallet:' as info, userId, balance, currency_code FROM wallets WHERE userId=8 AND currency_code='USD';
```

## Step 2: Update Cassandra
```bash
docker exec -it gp3-gs-1 cqlsh c1.gsmp.lan
```

Then run:
```cql
UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6274;

UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6275;

SELECT bank_id, bank_name, bank_url_authenticate FROM RCasinoKS.banks WHERE bank_id IN (6274, 6275);
```

## Step 3: Test Balance
```powershell
curl http://localhost:8000/bav/balance?userId=8&hash=ee005044ff8fbf17752a16c21cbd066f&bankId=6274
```

Expected: `<BALANCE>1000.00</BALANCE>` (or 100000 if in cents)

## Step 4: Launch Game
```
http://localhost:8081/free/mp/template.jsp?bankId=6274&sessionId=bav_game_session_001&gameId=838&lang=en
```

## Step 5: Monitor Logs
```powershell
docker logs casino_side -f
```

Watch for `/bav/authenticate` calls when game loads.

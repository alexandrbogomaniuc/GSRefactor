# CORRECTED: Connect to Cassandra Container

## Find the Cassandra Container Name

```bash
docker ps | findstr cassandra
```

or

```bash
docker ps | findstr c1
```

Look for container name like `gp3-c1-1` or `gp3_c1_1`

## Connect to Cassandra

Use the correct container name:

```bash
docker exec -it gp3-c1-1 cqlsh
```

or if the name is different:

```bash
docker exec -it gp3_c1_1 cqlsh
```

## Once Connected

You'll see:
```
cqlsh>
```

Then run these commands **ONE AT A TIME**:

```cql
UPDATE RCasinoKS.banks SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate' WHERE bank_id = 6274;
UPDATE RCasinoKS.banks SET bank_url_balance = 'http://host.docker.internal:8000/bav/balance' WHERE bank_id = 6274;
UPDATE RCasinoKS.banks SET bank_url_bet = 'http://host.docker.internal:8000/bav/betResult' WHERE bank_id = 6274;
UPDATE RCasinoKS.banks SET bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet' WHERE bank_id = 6274;

UPDATE RCasinoKS.banks SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate' WHERE bank_id = 6275;
UPDATE RCasinoKS.banks SET bank_url_balance = 'http://host.docker.internal:8000/bav/balance' WHERE bank_id = 6275;
UPDATE RCasinoKS.banks SET bank_url_bet = 'http://host.docker.internal:8000/bav/betResult' WHERE bank_id = 6275;
UPDATE RCasinoKS.banks SET bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet' WHERE bank_id = 6275;
```

Verify:
```cql
SELECT bank_id, bank_name, bank_url_authenticate FROM RCasinoKS.banks WHERE bank_id IN (6274, 6275);
```

Exit:
```
exit
```

## Then Launch Game

```
http://localhost:8081/free/mp/template.jsp?bankId=6274&sessionId=bav_game_session_001&gameId=838&lang=en
```

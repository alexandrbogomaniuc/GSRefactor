# 🔧 Manual Cassandra Update - Step by Step

The automated commands aren't working. Let's do it manually:

## Step 1: Connect to Cassandra

```bash
docker exec -it gp3-gs-1 cqlsh c1.gsmp.lan
```

You should see:
```
Connected to Test Cluster at c1.gsmp.lan:9042.
[cqlsh 5.0.1 | Cassandra 3.11.x | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.
cqlsh>
```

## Step 2: Check Current Banks

Copy-paste this:
```cql
SELECT bank_id, bank_name, bank_url_authenticate FROM RCasinoKS.banks WHERE bank_id = 6274;
```

Then:
```cql
SELECT bank_id, bank_name, bank_url_authenticate FROM RCasinoKS.banks WHERE bank_id = 6275;
```

## Step 3: Update Bank 6274

Copy-paste these ONE AT A TIME:

```cql
UPDATE RCasinoKS.banks SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate' WHERE bank_id = 6274;
```

```cql
UPDATE RCasinoKS.banks SET bank_url_balance = 'http://host.docker.internal:8000/bav/balance' WHERE bank_id = 6274;
```

```cql
UPDATE RCasinoKS.banks SET bank_url_bet = 'http://host.docker.internal:8000/bav/betResult' WHERE bank_id = 6274;
```

```cql
UPDATE RCasinoKS.banks SET bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet' WHERE bank_id = 6274;
```

## Step 4: Update Bank 6275

```cql
UPDATE RCasinoKS.banks SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate' WHERE bank_id = 6275;
```

```cql
UPDATE RCasinoKS.banks SET bank_url_balance = 'http://host.docker.internal:8000/bav/balance' WHERE bank_id = 6275;
```

```cql
UPDATE RCasinoKS.banks SET bank_url_bet = 'http://host.docker.internal:8000/bav/betResult' WHERE bank_id = 6275;
```

```cql
UPDATE RCasinoKS.banks SET bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet' WHERE bank_id = 6275;
```

## Step 5: Verify

```cql
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance FROM RCasinoKS.banks WHERE bank_id IN (6274, 6275);
```

You should see both banks with http://host.docker.internal:8000/bav/* URLs

## Step 6: Exit

```
exit
```

## Step 7: Retry Game Launch

Open in browser:
```
http://localhost:8081/free/mp/template.jsp?bankId=6274&sessionId=bav_game_session_001&gameId=838&lang=en
```

This time it should call Casino Side `/bav/authenticate`!

# 🎯 UPDATE CASSANDRA FOR BAV BANKS

## The Truth
- BankInfoCache.xml = **Temporary cache** (loaded at startup)
- **Real data** = Cassandra database

## Step 1: Connect to Cassandra

```powershell
docker exec -it gp3-c1-1 cqlsh
```

## Step 2: Check What Tables Exist

```sql
USE RCasinoSCKS;
DESCRIBE TABLES;
```

Look for: `bankinfocf` or similar bank table

## Step 3: See Current Banks

```sql
SELECT key FROM RCasinoSCKS.bankinfocf;
```

## Step 4: Check if Banks 6274/6275 Exist

```sql
SELECT * FROM RCasinoSCKS.bankinfocf WHERE key IN (6274, 6275);
```

## Step 5A: If They DON'T Exist - Create Them

We need to see the structure first:
```sql
DESC TABLE RCasinoSCKS.bankinfocf;
```

Then insert based on the structure.

## Step 5B: If They DO Exist - Update wallet URLs

This depends on what columns the table has. You'll need to update columns like:
- `bank_url_authenticate`
- `bank_url_balance` 
- `bank_url_bet`
- `bank_url_refund_bet`

With values:
- `http://host.docker.internal:8000/bav/authenticate`
- `http://host.docker.internal:8000/bav/balance`
- `http://host.docker.internal:8000/bav/betResult`
- `http://host.docker.internal:8000/bav/refundBet`

## Step 6: Verify

```sql
SELECT * FROM RCasinoSCKS.bankinfocf WHERE key IN (6274, 6275);
```

## Step 7: Restart Game Provider

```powershell
docker restart gp3-gs-1
```

**This will reload the cache from Cassandra!**

---

## ⚠️ IMPORTANT
The XML file is just a **startup cache**. Changes there are lost on restart. 
**Cassandra is the source of truth!**

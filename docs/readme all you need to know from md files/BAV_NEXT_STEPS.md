# BAV Game Launch - Troubleshooting & Next Steps

## Issue Summary
**Error**: `ClassNotFoundException: StandardRESTCWClient`  
**Status**: Game server restarted to reload bank configuration cache

---

## What We've Done

### ✅ 1. Identified Root Cause
- Cassandra has wrong wallet client class name
- Tries to load: `StandardRESTCWClient` ❌
- Should load: `StandartRESTCWClient` ✅ (typo in codebase)

### ✅ 2. Updated Cassandra
- Applied `FINAL_BAV_BANKS.cql` configuration
- Banks 6274 & 6275 should now have correct class name

### ✅ 3. Restarted Game Server
- **Container**: `gp3-gs-1` restarted at 10:10 UTC
- **Purpose**: Reload `BankInfoCache` from Cassandra

---

## Next Steps

### Step 1: Verify Server is Running
```powershell
docker ps --filter "name=gp3-gs-1"
```
**Expected**: Status shows "Up" (may take 30-60 seconds)

### Step 2: Check Startup Logs
```powershell
docker logs gp3-gs-1 --tail 100
```
**Look for**:
- ✅ `BankInfoCache` loaded successfully
- ✅ Bank 6274/6275 loaded
- ❌ Any `ClassNotFoundException` errors

### Step 3: Test Game Launch
```
http://localhost:8081/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=<TOKEN>&lang=en
```

### Step 4: Monitor Logs During Test
```powershell
docker logs gp3-gs-1 --follow
```

---

## Expected Outcomes

### ✅ Success Scenario
- No `ClassNotFoundException` in logs
- Wallet client instantiates: `StandartRESTCWClient`
- Call to Casino Side `/bav/authenticate` endpoint
- Either successful authentication OR authentication error (which is progress!)

### ❌ Still Failing Scenario
If still getting `ClassNotFoundException: StandardRESTCWClient`:

**Cause**: Cassandra update didn't save or cache still has old data

**Solution**:
1. Verify Cassandra has correct data:
```cql
docker exec -it gp3-c1-1 cqlsh
SELECT key FROM RCasinoSCKS.bankinfocf WHERE key = 6274;
-- Check the jcn column contains "StandartRESTCWClient"
```

2. If Cassandra is correct but cache is wrong:
   - Check `BankInfoCache` loading logic
   - Verify cache expiration settings
   - May need to manually clear cache

---

## Verification Commands

```powershell
# Check if server is up
docker ps | Select-String "gp3-gs-1"

# Check recent logs
docker logs gp3-gs-1 --tail 50

# Follow logs in real-time
docker logs gp3-gs-1 --follow

# Check support tickets
# Visit: http://localhost:8081/support/logviewer.do
```

---

## Quick Cassandra Check

```powershell
# Connect to Cassandra
docker exec -it gp3-c1-1 cqlsh

# Check if update is saved
SELECT key FROM RCasinoSCKS.bankinfocf WHERE key IN (6274, 6275);
```

The `jcn` column should contain:
```json
"COMMON_WALLET_REQUEST_CLIENT_CLASS":"com.dgphoenix.casino.payment.wallet.client.v4.StandartRESTCWClient"
```

**Not**:
```json
"COMMON_WALLET_REQUEST_CLIENT_CLASS":"com.dgphoenix.casino.payment.wallet.client.v4.StandardRESTCWClient"
```

---

**Status**: Waiting for game server to complete startup  
**Next**: Test game launch and check logs

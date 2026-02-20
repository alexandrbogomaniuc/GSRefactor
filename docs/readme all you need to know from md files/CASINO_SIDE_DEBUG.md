# Casino Side BAV Integration - Next Steps

## 🎉 SUCCESS SO FAR

### ✅ Game Provider Fixed
- ✅ Cassandra updated with correct wallet client class (`StandartRESTCWClient`)
- ✅ Game server restarted and cache reloaded
- ✅ Wallet client instantiates successfully
- ✅ HTTP request sent to Casino Side `/bav/authenticate`

**Request Details**:
```
URL: http://host.docker.internal:8000/bav/authenticate
Parameters:
- apipassword: huecTlCT1OPSE0k4
- bankId: 6274
- clientType: FLASH
- hash: 8a3e093772f3e8c793a091b1d58cceda
- token: <TOKEN>
```

---

## ❌ Current Issue: Casino Side Returns HTTP 500

**Error**: `Invalid response code: 500, Internal Server Error`

---

## Root Cause Analysis

### Casino Side Endpoint Expects

Looking at `authenticate.py` (lines 21-27), the endpoint signature is:

```python
@router.get("/authenticate")
async def authenticate(
    request: Request,
    token: str = Query(...),
    hash: str = Query(...),
    bankId: int | None = Query(None),
    clientType: str | None = Query(None),
    db: Session = Depends(get_db),
):
```

**Key Issues**:
1. ⚠️ **Missing `apipassword` parameter** - The endpoint does NOT accept `apipassword`
2. ⚠️ **Different authentication flow** - It uses `BAV_PASS_KEY` from bank settings (line 44)

### Game Provider Sends

```
apipassword=huecTlCT1OPSE0k4&bankId=6274&clientType=FLASH&hash=8a3e093772f3e8c793a091b1d58cceda&token=<TOKEN>
```

---

## Problem

The Casino Side endpoint does NOT expect or use the `apipassword` parameter that the Game Provider is sending. Instead, it:

1. Gets `bankId` from query string
2. Looks up bank settings: `bank = get_bank_settings(bank_id)`
3. Uses `bank.BAV_PASS_KEY` to validate hash
4. Validates: `MD5(token + BAV_PASS_KEY) == hash`

**This means**:
- The `apipassword` parameter is being sent but IGNORED
- The endpoint might be failing because it can't find the bank settings or there's a database connection issue

---

## Required Actions

### 1. ✅ Check if Casino Side is Running

```powershell
# Check container status
docker ps | Select-String "casino"

# If not running, start it
cd "Casino side\inst_app"
python -m uvicorn igw.app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 2. Check Casino Side Logs

```powershell
# If running in Docker
docker logs casino_side --tail 50 --follow

# If running locally
# Check terminal where uvicorn is running
```

### 3. Verify Database Connection

The authentication endpoint queries:
- `UserSession` table (lines 66-76)
- `Player` table (line 82)

**Check**:
- Is the Casino Side database accessible?
- Does the `UserSession` table exist?
- Is there a session for the test token?

### 4. Test Token Generation

Currently, the Game Provider is sending `<TOKEN>` (placeholder). You need a **valid JWT token** that:
- Contains `uid` or `sub` field
- Is signed with the correct secret
- Has a corresponding `UserSession` in the database

---

## Next Immediate Steps

1. **Start Casino Side if not running**
2. **Check logs for actual error**
3. **Generate a valid test token** using the Casino Side token generation endpoint
4. **Create a game session** in the database before testing
5. **Verify bank settings** are configured correctly

---

## Expected Flow

```
1. Game Provider generates token via Casino Side API
2. Token is stored in UserSession table with:
   - userId
   - token value
   - session_type = "game"
   - provider = "bsg_bav"
   - status = "active"
   
3. Game launch URL includes this token
4. Game Provider calls /bav/authenticate
5. Casino Side:
   - Verifies hash matches MD5(token + BAV_PASS_KEY)
   - Looks up UserSession by token
   - Returns user info + balance
```

---

**Status**: Casino Side needs to be running and accessible. Check if the server is up and review logs for the actual 500 error cause.

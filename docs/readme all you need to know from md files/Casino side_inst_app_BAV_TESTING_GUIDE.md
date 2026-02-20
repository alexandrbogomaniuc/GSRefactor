# BAV ENDPOINT TESTING GUIDE
# PASS_KEY: huecTlCT1OPSE0k4
# Base URL: http://localhost:8000/bav
# UserID for testing: 8

## Calculated Hashes:

### 1. BALANCE ENDPOINT
Hash for userId=8: MD5("8huecTlCT1OPSE0k4") = 2c71a3eadb7c4e3a8dc8c88dddbdd88a

**Curl Command:**
```bash
curl "http://localhost:8000/bav/balance?userId=8&hash=2c71a3eadb7c4e3a8dc8c88dddbdd88a&bankId=6274"
```

Expected Response (XML):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<EXTSYSTEM>
  <REQUEST><USERID>8</USERID></REQUEST>
  <TIME>...</TIME>
  <RESPONSE>
    <RESULT>OK</RESULT>
    <BALANCE>xxxx</BALANCE>
  </RESPONSE>
</EXTSYSTEM>
```

---

### 2. AUTHENTICATE ENDPOINT
For this, we need a valid JWT token in the sessions table.

First, create a session token in MySQL:
```sql
INSERT INTO sessions (userId, token, session_type, provider, status, created_at, expires_at) 
VALUES (8, 'test_bav_token', 'game', 'bsg_bav', 'active', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));
```

Hash for token="test_bav_token": MD5("test_bav_tokenhuecTlCT1OPSE0k4") = 8a5c9e4d3b2f1a6e7c8d9f0e1b2a3c4d

**Curl Command:**
```bash
curl "http://localhost:8000/bav/authenticate?token=test_bav_token&hash=8a5c9e4d3b2f1a6e7c8d9f0e1b2a3c4d&bankId=6274&gameId=838"
```

---

### 3. BET RESULT ENDPOINT (Zero Bet)
Hash String: "80|tx001false round001838huecTlCT1OPSE0k4"
Hash: MD5 = needs calculation

HashString components:
- userId: 8
- bet: 0|tx001
- win: (empty)
- isRoundFinished: false
- roundId: round001
- gameId: 838
- PASS_KEY: huecTlCT1OPSE0k4

---

## Quick Test Commands:

1. **Test Balance (should work immediately)**:
```bash
curl -v "http://localhost:8000/bav/balance?userId=8&hash=2c71a3eadb7c4e3a8dc8c88dddbdd88a&bankId=6274"
```

2. **Test Health Check**:
```bash
curl "http://localhost:8000/healthz"
```

3. **Check if BAV endpoints are registered**:
```bash
curl "http://localhost:8000/openapi.json" | jq '.paths | keys | .[] | select(contains("bav"))'
```

---

## MD5 Hash Calculator (PowerShell):

```powershell
function Get-MD5 { param($text)
    [BitConverter]::ToString(
        [System.Security.Cryptography.MD5]::Create().ComputeHash(
            [Text.Encoding]::UTF8.GetBytes($text)
        )
    ).Replace("-","").ToLower()
}# Example usage:
Get-MD5 "8huecTlCT1OPSE0k4"  # For balance endpoint
Get-MD5 "test_bav_tokenhuecTlCT1OPSE0k4"  # For authenticate endpoint
```

---

## Expected Issues to Check:

1. **422 Unprocessable Entity**: Missing required parameters
2. **401 Unauthorized**: Invalid hash or token
3. **404 Not Found**: Endpoint not registered properly
4. **500 Internal Server Error**: Database connection or code error

---

## Next Steps After Testing:

1. If Balance works → Test Authenticate (after creating session token)
2. If Authenticate works → Test BetResult
3. Document all XML responses
4. Compare with API Test OK example format

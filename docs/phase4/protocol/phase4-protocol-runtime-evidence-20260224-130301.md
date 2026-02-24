# Phase 4 Protocol Runtime Evidence (20260224-130301 UTC)

- bankId: 6275
- transport: host
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- allowMissingRuntime: false
- runtime_readiness: PASS
- parity_check: PASS
- wallet_shadow_probe: FAIL
- json_security_probe: FAIL

## Runtime Readiness Output
```text
Phase 4 Runtime Readiness
  transport: host
  protocol: 127.0.0.1:18078
  gs:       127.0.0.1:18081
PASS protocol endpoint reachable
PASS gs endpoint reachable
READY: runtime checks passed
```

## Parity Check Output
```text
PARITY_OK bankId=6275 endpoint=/wallet/reserve
JSON/XML parity check passed for bank 6275 (POST /wallet/reserve) transport=host
```

## Wallet Shadow Probe Output
```text
FAIL: could not auto-resolve sessionId from startgame launch
Launch headers:
HTTP/1.1 200 OK
X-Trace-Id: 35cf7e6b-4da0-4cba-ba9f-135ce2e46415
Set-Cookie: JSESSIONID=node017n6ve50bqk7k82m1hcb9px6i2.node0;Path=/
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: text/html;charset=utf-8
Content-Length: 1091
Server: Jetty(9.4.18.v20190429)

Launch body snippet:
<html>
<head>
    <title>Error</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Expires" content="Tue, 01 Jan 1980 1:00:00 GMT">
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="Pragma" content="no-cache">

    <style>
        
        body {background-color: white; color: black;}
        #center {color: black;}
    </style>

    <script>
        function hidescrollbar() {
            var agent = navigator.userAgent;
            if (agent.indexOf("MSIE") != -1) {
                document.body.scroll = "no";
            } else {
                document.documentElement.style.overflow = 'hidden';
            }
        }

        function sendRedirect(url) {
            window.location = url;
        }
    </script>

</head>
<body>
<script>
    var submitBtnClicked = false;


    hidescrollbar();
</script><br/><br/>
<div id="center" align=center>
            <div>Bank is incorrect</div>
```

## JSON Security Probe Output
```text
HTTP POST normalize failed: 403
{"allowed":false,"httpStatus":403,"settings":{"bankId":"6275","protocolMode":"JSON","jsonSecurity":{"hash":{"enabled":true,"headerName":"Hash","algorithm":"HMAC-SHA256","enforcementMode":"ENFORCE","secretRef":"ref://vault/bank-6275/json-hmac-key-v1","exemptEndpoints":["/api/v1/account/info","/api/v1/balance","/api/v1/bonus/cash/check","/api/v1/bonus/frb/check","/api/v1/viewers","/api/v1/feeds"],"getHashRules":{"/api/v1/bonus/cash/history":["bankId","userId","timeZone"]}},"replay":{"enabled":true,"windowSeconds":300,"nonceTtlSeconds":300}},"updatedAt":"2026-02-24T13:03:02.943Z","updatedBy":"phase4-protocol-json-security-canary"},"security":{"hash":{"checked":true,"required":true,"verified":false,"reason":"secret_not_available","headerName":"Hash","enforcementMode":"ENFORCE","hashInput":null,"providedHash":null,"expectedHash":null},"replay":{"ok":true,"checked":true,"replayProtected":true,"reason":"ok"}},"canonicalRequest":{"bankId":"6275","protocolMode":"JSON","method":"POST","endpoint":"/api/bonus/cash/cancel","query":{},"payload":{"bankId":"6275","bonusId":86584571},"receivedAt":"2026-02-24T13:03:02.957Z","traceId":"","sessionId":"","operationId":""}}```

# Phase 4 Protocol Runtime Evidence (20260224-163238 UTC)

- bankId: 6275
- transport: host
- protocolBaseUrl: http://127.0.0.1:18078
- gsBaseUrl: http://127.0.0.1:18081
- subCasinoId: 507
- allowMissingRuntime: false
- runtime_readiness: PASS
- parity_check: PASS
- wallet_shadow_probe: FAIL
- json_security_probe: SKIPPED

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
X-Trace-Id: 61fc634e-9860-42b9-87f1-bdf00b19ae46
Set-Cookie: JSESSIONID=node01pkqdeytlp0a1q8hqhkfkr9t514.node0;Path=/
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: text/html;charset=utf-8
Content-Length: 1323
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
            <div>Sorry, an error occurred during login</div>
```

## JSON Security Probe Output
```text
Security probe not executed because --run-security-probe=false (default safe mode).
```

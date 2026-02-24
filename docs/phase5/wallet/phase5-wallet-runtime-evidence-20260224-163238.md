# Phase 5 Wallet Adapter Runtime Evidence (20260224-163238 UTC)

- bankId: 6275
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- walletBaseUrl: http://127.0.0.1:18075
- subCasinoId: 507
- readiness_check: PASS
- wallet_canary_probe: FAIL

## Readiness Output
```text
Phase 5 Wallet Runtime Readiness
  wallet-adapter: 127.0.0.1:18075
  gs:             127.0.0.1:18081
PASS wallet-adapter endpoint reachable
PASS gs endpoint reachable
READY: runtime checks passed
```

## Canary Output
```text
FAIL: could not auto-resolve sessionId from startgame launch
Launch headers:
HTTP/1.1 200 OK
X-Trace-Id: 6176483f-671c-4ad1-bf70-a5f47efec90d
Set-Cookie: JSESSIONID=node0uum7xtxy21qp108zm67xklkhn13.node0;Path=/
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

# Phase 5 Wallet Adapter Runtime Evidence (20260224-163038 UTC)

- bankId: 6275
- transport: host
- gsBaseUrl: http://127.0.0.1:18081
- walletBaseUrl: http://127.0.0.1:18075
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
X-Trace-Id: cb52280c-7330-4cd7-b457-c1b191f2c408
Set-Cookie: JSESSIONID=node01f1c4j8w7dfoy1vb2q808ys4gw12.node0;Path=/
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

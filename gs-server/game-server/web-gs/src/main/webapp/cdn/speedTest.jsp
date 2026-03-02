<%@ page import="com.abs.casino.common.web.BaseAction" %>
<%@ page import="com.abs.casino.system.configuration.GameServerConfiguration" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Loading...</title>
    <meta charset="UTF-8">
    <style>
        @font-face {
            font-family: 'Titillium Web';
            font-style: normal;
            font-weight: 400;
            src: url('/css/fonts/titillium-web/titillium-web-v5-latin-regular.eot'); /* IE9 Compat Modes */
            src: local('Titillium Web Regular'), local('TitilliumWeb-Regular'),
                /* IE6-IE8 */ url('/css/fonts/titillium-web/titillium-web-v5-latin-regular.eot?#iefix') format('embedded-opentype'),
                /* Super Modern Browsers */ url('/css/fonts/titillium-web/titillium-web-v5-latin-regular.woff2') format('woff2'),
                /* Modern Browsers */ url('/css/fonts/titillium-web/titillium-web-v5-latin-regular.woff') format('woff'),
                /* Safari, Android, iOS */ url('/css/fonts/titillium-web/titillium-web-v5-latin-regular.ttf') format('truetype'),
                /* Legacy iOS */ url('/css/fonts/titillium-web/titillium-web-v5-latin-regular.svg#TitilliumWeb') format('svg');
        }

        body {
            margin: 0;
            background-color: #000;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .logo-container {
            margin-bottom: -30px;
        }

        .logo-container img {
            width: 100%;
        }

        .splash-text {
            color: #f1f1f1;
            text-transform: uppercase;
            font-family: 'Titillium Web', sans-serif;
            letter-spacing: 2px;
            font-size: 14px;
            text-align: center;
        }

        .circle-container {
            position: absolute;
            top: calc(50% - 5px);
            width: 100%;
            display: inline-block;
            animation: motion 2s linear infinite;
        }

        .circle {
            width: 10px;
            height: 10px;
            background: #E9DC06;
            -moz-border-radius: 50%;
            -webkit-border-radius: 50%;
            border-radius: 50%;
            box-shadow: 0 0 10px 2px #E9DC06, 0 0 30px 5px #ffffff;
            filter: blur(2px);
        }

        .circle-glow {
            position: absolute;
            left: 0;
            top: -50%;
            transform: translate(-50%, 50%);
            width: 50%;
            height: 10px;
            background: -webkit-gradient(linear, 0 0, 100% 0, from(transparent), to(transparent), color-stop(50%, #E9DC06));
            -webkit-border-radius: 50% / 50%;
            -moz-border-radius: 50% / 50%;
            border-radius: 50% / 50%;
            filter: blur(2px);
            opacity: 0.5;
        }

        @-webkit-keyframes motion {
            0% {
                -webkit-transform: translateX(-150px) scale(0);
                transform: translateX(-150px) scale(0);
            }
            50% {
                -webkit-transform: translateX(150px) scale(1);
                transform: translateX(150px) scale(1);
            }
            100% {
                -webkit-transform: translateX(150px) scale(0);
                transform: translateX(150px) scale(0);
            }
        }

        @keyframes motion {
            0% {
                -webkit-transform: translateX(-150px) scale(0);
                transform: translateX(-150px) scale(0);
            }
            50% {
                -webkit-transform: translateX(150px) scale(1);
                transform: translateX(150px) scale(1);
            }
            100% {
                -webkit-transform: translateX(150px) scale(0);
                transform: translateX(150px) scale(0);
            }
        }

        .loader-bar {
            position: relative;
            margin: 0 auto;
            width: 300px;
            height: 2px;
        }

        .line {
            margin: 0;
            height: 2px;
            background: -webkit-gradient(linear, 0 0, 100% 0, from(transparent), to(transparent), color-stop(50%, #E9DC06));
        }

        .line-glow {
            /* fallback */
            position: absolute;
            width: 100%;
            height: 4px;
            background: -webkit-gradient(linear, 0 0, 100% 0, from(transparent), to(transparent), color-stop(50%, #E9DC06));
            filter: blur(5px);
        }
    </style>
</head>
<body>
<div class="splash-container">
    <div class="logo-container">
        <img src="/cdn/<%=GameServerConfiguration.getInstance().getBrandName().toLowerCase()%>_logo.png?v=3" alt="Logo">
    </div>

    <p class="splash-text">Your game is being loaded</p>

    <div class="loader-bar">
        <div class="line-glow"></div>
        <div class="line"></div>
        <div class="circle-container">
            <div class="circle"></div>
            <div class="circle-glow"></div>
        </div>
    </div>
</div>

<script>
    "use strict";

    function makeRequestIE(url, onComplete) {
        var xmlHttp = new XDomainRequest();

        try {
            xmlHttp.open('GET', url);
            xmlHttp.timeout = 10000;
            xmlHttp.onload = function () {
                if (xmlHttp.status === 200) {
                    onComplete(true);
                } else {
                    onComplete(false);
                }
            };
            xmlHttp.onerror = function () {
                onComplete(false);
            };
            xmlHttp.send();
        } catch (e) {
            onComplete(false);
        }
    }

    function makeRequestXmlHttp(url, onComplete) {
        var xmlHttp = new XMLHttpRequest();

        try {
            xmlHttp.open('GET', url, true);
            xmlHttp.timeout = 10000;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState !== 4) return;

                if (xmlHttp.status === 200) {
                    onComplete(true);
                } else {
                    onComplete(false);
                }
            };
            xmlHttp.send();
        } catch (e) {
            onComplete(false);
        }
    }

    function makeRequest(url, onComplete) {
        if (window.XDomainRequest) {
            makeRequestIE(url, onComplete);
        } else if (window.XMLHttpRequest) {
            makeRequestXmlHttp(url, onComplete);
        } else {
            alert("Your browser does not support AJAX!");
        }
    }

    function averageWithoutWorst(times) {
        var max = 0;
        var sum = 0;

        times.forEach(function (time) {
            if (time > max) {
                sum += max;
                max = time;
            } else {
                sum += time;
            }
        });

        return Math.round(sum / (times.length - 1));
    }

    function cdnResult(cdn) {
        return cdn.url + "=" + averageWithoutWorst(cdn.times);
    }

    function sendResults(cdnList) {
        var url = "/api/cdnHostInit?";

        cdnList.forEach(function (cdn) {
            url = url + cdnResult(cdn) + "&";
        });

        url = url + "bankId=<%=request.getParameter(BaseAction.BANK_ID_ATTRIBUTE)%>";

        makeRequest(url, function (result) {
            if (result) {
                document.location.reload(false);
            }
        });
    }

    function checkSingleCdn(cdnList, index, requestCount, completeCallback) {
        var url = (cdnList[index].url === 'DISABLED' ? '' : '//' + cdnList[index].url) +
                '/js/jquery.js?rnd=' + Math.random();

        var beginTime = new Date().getTime();

        var requestCallback = function (success) {
            var endTime = new Date().getTime();
            var delta = endTime - beginTime;
            if (!success) {
                delta = 10000;
            }
            cdnList[index].times.push(delta);

            if (cdnList[index].times.length < requestCount) {
                checkSingleCdn(cdnList, index, requestCount, completeCallback);
            } else {
                completeCallback(index);
            }
        };

        makeRequest(url, requestCallback);
    }

    function checkCdn(config) {
        var requestCount = 4;
        var cdnList = [];

        (config + ";DISABLED=DISABLED").split(';').forEach(function (v) {
            cdnList.push({
                name: v.split('=')[0],
                url: v.split('=')[1],
                times: []
            });
        });

        var singleCheckFinishedCallback = function (index) {
            if (index + 1 < cdnList.length) {
                checkSingleCdn(cdnList, index + 1, requestCount, singleCheckFinishedCallback);
            } else {
                sendResults(cdnList);
            }
        };

        checkSingleCdn(cdnList, 0, requestCount, singleCheckFinishedCallback, 1);
    }

    document.onload = checkCdn("<%=request.getAttribute("cdnCheck")%>");
</script>
</body>
</html>

<html>
<head>
    <title>Error</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Expires" content="Tue, 01 Jan 1980 1:00:00 GMT">
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="Pragma" content="no-cache">

    <%
        response.setHeader("Cache-Control", "no-cache, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Tue, 01 Jan 1980 1:00:00 GMT");
    %>

    <script language="JavaScript" type="text/javascript" src="/js/util.js"></script>
    <script language="JavaScript" type="text/javascript" src="/js/lobby.js"></script>
    <script type="text/javascript" src="/js/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="/js/jquery.appendDom.js"></script>
    <style type="text/css">
        body {background-color: #08153F; color: #FFFFFF;}
        #center {color: #FFFFFF;}
        a {color: yellow;text-decoration: underline;}
    </style>
    <script src="/js/SpryValidationTextField.js" type="text/javascript"></script>

    <script type="text/javascript">
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
<script language="JavaScript" type="text/javascript">
    var submitBtnClicked = false;
    $(document).ready(function () {
    });

    hidescrollbar();
</script>
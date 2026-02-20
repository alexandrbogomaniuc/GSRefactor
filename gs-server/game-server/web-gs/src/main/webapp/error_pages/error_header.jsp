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

    <style>
        <%
            String bgColor = "white";
            String fontColor = "black";
            if (request.getServerName().startsWith("phc-")) {
                bgColor = "black";
                fontColor = "white";
            }
        %>
        body {background-color: <%=bgColor%>; color: <%=fontColor%>;}
        #center {color: <%=fontColor%>;}
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
</script>
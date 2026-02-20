<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script>
        function getServerId() {
            return "1";
        }
        function getSessionId() {
            return "<%=request.getParameter("token")%>";
        }
        function getWebSocketUrl() {
            return "ws://localhost:8080/websocket/mplobby";
        }
    </script>
</head>
<body>

</body>
</html>

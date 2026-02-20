<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <title><bean:message key="error.history.gameHistoryNotFoundTitle"/></title>
    <style>.message { text-align: center; font: italic bold 16px/24px sans-serif; }
    body { padding-top: 100px; }</style>
</head>
<body>
<div class="message"><bean:message key="error.history.gameHistoryNotFound"/></div>
<div class="message"><bean:message
        key="error.time"/>: <%=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())%>
</div>
</body>
</html>

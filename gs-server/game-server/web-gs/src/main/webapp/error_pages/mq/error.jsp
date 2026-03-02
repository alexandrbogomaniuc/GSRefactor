<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.abs.casino.gs.GameServer" %>
<%@ page import="java.util.Date" %>
<%@ page isErrorPage="true" %>
<jsp:include page="/error_pages/error_header.jsp"/>
<br/><br/>

<%
    ThreadLog.error("MQ TEMPLATE.JSP:: Error, cause" + exception.getMessage(), exception);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    String time = formatter.format(new Date());

    String sessionid = request.getParameter("sessionid");
    int serverId = GameServer.getInstance().getServerId();
%>

<div align=center>Something went wrong! Please restart the game. If the problem persists, please contact support.</div>
<br><br>
<% if (sessionid != null && !sessionid.equals("")) { %>
<div align=center>Support ticket ID G<%=serverId%>: <%=sessionid%>
</div>
<% } %>
<div align=center>Time: <%=time%>
</div>

<br/>
<jsp:include page="/error_pages/error_header.jsp"/>

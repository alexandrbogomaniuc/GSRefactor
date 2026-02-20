<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="../error_header.jsp"/>
<%
    String lang = request.getParameter("lang");
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    String time = formatter.format(new Date());
    String sessionId = request.getParameter("sessionid");

    String header = "Something went wrong! Please restart the game. If the problem persists, please contact support.";
    String sTicketId = "Support ticket ID:";
    String sTime = "Time:";
%>

<br/><br/>

<div align=center>

    <% if ("zh-cn".equals(lang)) {

        header = "由于网络延迟，与服务器连接中断。";
        sTicketId = "技术协助:";
        sTime = "伦敦时间:";

    } %>

    <div align=center><%=header%>
    </div>
    <br><br>
    <%if (!StringUtils.isTrimmedEmpty(sessionId)) {%>
    <div align=center><%=sTicketId%> <%=sessionId%>
    </div>
    <%}%>
    <div align=center><%=sTime%> <%=time%>
    </div>

</div>
<br/><br/><br/><br/>
<jsp:include page="../error_footer.jsp"/>

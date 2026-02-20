<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="../error_header.jsp"/>
<%
    String lang = request.getParameter("lang");
    String header = "Sorry, connection to server was broken, please try again.";
%>

<% if ("zh-cn".equals(lang)) {

    header = "与服务器连接中断，请重新尝试或联系客服。";

} %>

<br/><br/>
<div id="center" align="center"><%=header%>
</div>
<br/><br/><br/><br/>

<jsp:include page="../error_footer.jsp"/>

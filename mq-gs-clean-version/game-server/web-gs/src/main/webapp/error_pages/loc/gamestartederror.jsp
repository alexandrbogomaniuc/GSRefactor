<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="../error_header.jsp"/>
<%
    String lang = request.getParameter("lang");
    String header = "Error occurred while trying to start game.";
%>

<% if ("zh-cn".equals(lang)) {

    header = "连接发生错误，请重新尝试";

} %>

<br/><br/>
<div id="center" align="center"><%=header%>
</div>
<br/><br/><br/><br/>

<jsp:include page="../error_footer.jsp"/>

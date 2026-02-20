<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="../error_header.jsp"/>
<%
    String lang = request.getParameter("lang");
    String header = "Error occurred while trying to load game.";
%>

<% if ("zh-cn".equals(lang)) {

    header = "游戏下载发生错误，请重新尝试或联系客服。";

} %>

<br/><br/>
<div id="center" align="center"><%=header%>
</div>
<br/><br/><br/><br/>

<jsp:include page="../error_footer.jsp"/>

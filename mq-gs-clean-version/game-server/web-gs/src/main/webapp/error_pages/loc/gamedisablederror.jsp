<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="../error_header.jsp"/>
<%
    String lang = request.getParameter("lang");
    String header = "Sorry, game is disabled.";
%>

<% if ("zh-cn".equals(lang)) {

    header = "本游戏暂时无法进入，请重新尝试或联系客服。";

} %>

<br/><br/>
<div id="center" align="center"><%=header%>
</div>
<br/><br/><br/><br/>

<jsp:include page="../error_footer.jsp"/>

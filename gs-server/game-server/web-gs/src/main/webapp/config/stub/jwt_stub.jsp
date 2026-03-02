<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if (StringUtils.isTrimmedEmpty(username) || StringUtils.isTrimmedEmpty(password)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    } else {
        response.getWriter().print(username.trim());
    }
%>
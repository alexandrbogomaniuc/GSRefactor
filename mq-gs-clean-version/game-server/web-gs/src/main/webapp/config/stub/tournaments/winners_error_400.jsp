<%@ page contentType="text/html;charset=UTF-8"%>

<%
    response.getWriter().print("{\n" +
            "      \"status\": \"Error\",\n" +
            "      \"message\": \"Invalid JSON\"\n" +
            "}\n");
%>
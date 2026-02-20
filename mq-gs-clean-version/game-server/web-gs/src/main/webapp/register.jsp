<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%--
  Created by IntelliJ IDEA.
  User: plastical
  Date: 06.04.2010
  Time: 17:27:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register page</title>
</head>
<body>
<a href="/index.jsp">Login</a>
<p>Registration Form</p>
<html:form action="/cwregisterstub">
    <p><label>userId:</label> <html:text property="userId" value=""/></p>
    <p><label>username:</label> <html:text property="username" value=""/></p>
    <p><label>currency:</label> <html:text property="currencyCode" value=""/></p>
    <p><html:submit/></p>
</html:form>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<html>
<head><title>Add subcasino page</title></head>
<body>
<html:form action="/support/addsubcasino">
    <b>Id</b> <html:text property="id"/><br>
    <b>Name</b> <html:text property="name"/><br>
    <html:submit value="back" property="button"/> <html:submit value="add" property="button"/>
</html:form>


</body>
</html>
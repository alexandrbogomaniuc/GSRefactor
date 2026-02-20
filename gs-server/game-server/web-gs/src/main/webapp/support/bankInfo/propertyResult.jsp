<%--
  User: rus-nura
  Date: 25.12.17
  Time: 11:48
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<html>
<head>
    <title>Bank Property Info</title>
</head>
<body>
<table width="15%">
    <caption><b>Result</b></caption>
    <tr>
        <td>Bank ID</td>
        <td><i><bean:write name="BankPropertyInfo" property="bankId"/></i></td>
    </tr>
    <tr>
        <td>Property name</td>
        <td><i><bean:write name="BankPropertyInfo" property="property"/></i></td>
    </tr>
    <tr>
        <td>Property Value</td>
        <td>
            <% String value = (String) request.getAttribute("propertyValue");
                if (value == null) { %>
            <b style="color: red">Property isn't found!</b>
            <% } else { %>
            <b style="color: green"><%=value%>
            </b>
            <% } %>
        </td>
    </tr>
</table>
<br/>
<button style="width: 15%;" onclick="document.location.href = '/support/bankInfo/propertyForm.jsp?bankId=${param.bankId}&property=${param.property}';">
    <- Back
</button>
</body>
</html>

<%--
  User: rus-nura
  Date: 25.12.17
  Time: 10:58
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<html>
<head>
    <title>Bank Property Info</title>
</head>
<body>
<b id="errors"><html:errors property="bankInfo"/></b>
<html:form action="/bankPropertyInfo" onsubmit="return fieldsChecker();" method="get">
    Bank ID:<br>
    <html:text name="BankPropertyInfo" property="bankId" size="30" value='<%=request.getParameter("bankId")%>'/><br>
    Bank property:<br>
    <html:text name="BankPropertyInfo" property="property" size="30" value='<%=request.getParameter("property")%>'/><br>
    <html:submit value="getProperty"/>
</html:form>

<script type="text/javascript">
    function fieldsChecker() {
        var bankId = (document.getElementsByName("bankId")[0]).value;
        if (isNaN(bankId) || bankId.length == 0) {
            document.getElementById("errors").innerHTML = "<b style='color: red'>Please, define a numeric value of 'Bank ID' field!</b><br>";
            return false;
        }
        if (parseInt(bankId) < 0) {
            document.getElementById("errors").innerHTML = "<b style='color: red'>Please, define a positive numeric value of 'Bank ID' field!</b><br>";
            return false;
        }
        var property = (document.getElementsByName("property")[0]).value;
        if (property.length == 0) {
            document.getElementById("errors").innerHTML = "<b style='color: red'>Please, define a value of 'Property' field!</b><br>";
            return false;
        }
        return true;
    }

    window.onload = function () {
        if ((document.getElementsByName("bankId")[0]).value == "0") {
            (document.getElementsByName("bankId")[0]).value = "";
        }
    }
</script>
</body>
</html>
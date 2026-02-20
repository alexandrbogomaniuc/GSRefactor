<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Utils</title></head>
<body>
<html:form action="/encoderutil" method="post">
    <p>
    <p>Text:</p>
    <html:textarea rows="30" cols="80" property="text"/>
    </p>

    <p>
        <html:submit property="command" value="encode">encode</html:submit>
        <html:submit property="command" value="decode">decode</html:submit>
    </p>
</html:form>

<logic:present name="result" scope="request">
    <p>
        Result: <bean:write name="result"/>
    </p>
</logic:present>

</body>
</html>
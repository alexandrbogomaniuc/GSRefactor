<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<html>
<head><title>Domain List page</title></head>
<body>

<html:form action="/support/editdomains">
    <b> Domains: </b>
    <table border="1" cellpadding="7" cellspacing="0">
        <tr>
            <td>DOMAIN</td>
            <td>REMOVE DOMAIN</td>
        </tr>
        <logic:iterate id="domain" name="DomainwlForm" property="domainList" indexId="index">
            <tr>
                <td><html:text size="50" property="domainList[${index}]"/></td>
                <td align="center"><html:multibox property="removeList" value="${index}"/></td>
            </tr>
        </logic:iterate>
    </table>
    <br>
    <b>New domains (list with ";")</b> <br>
    <html:text size="100" property="newDomains"/> <br><br>
    <html:submit value="submit" property="button"/>
    <html:submit value="back" property="button"/>

</html:form>


</body>
</html>
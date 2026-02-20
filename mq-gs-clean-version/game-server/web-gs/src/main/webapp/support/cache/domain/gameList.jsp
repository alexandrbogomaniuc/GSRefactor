<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>


<html>
<head>
    <title>Game list page</title>
    <script type="text/javascript">

        function checkAll(form, checked) {
            var el, elValue;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                if (el.name == "selectedGameList") {
                    el.checked = checked;
                }
            }
        }
    </script>
</head>
<body>
<table border="1" cellpadding="7" cellspacing="0">
    <tr>
        <td>
            <b>Add/Remove domain for many games</b>
        </td>
        <td>
            <b>Edit domain list for game</b>
        </td>
        <td>
            <b>All domains list</b>
        </td>
    </tr>
    <tr>
        <td valign="top">

            <html:form action="/support/domainformany">
                <b>Domain</b>
                <br>
                <html:text property="domainForMany" size="40"/>
                <br>
                <html:submit property="button" value="show games with domain"/>
                <html:submit property="button" value="save"/><br>
                <br>
                <input type="checkbox" onClick="checkAll(this.form, this.checked)">
                <b>Check all</b>
                <br>
                <logic:iterate id="gameBean" name="DomainwlForm" property="gameBeans">
                    <html:multibox property="selectedGameList" value="${gameBean.id}"/>
                    <bean:write name="gameBean" property="name"/>
                    <br>
                </logic:iterate>
            </html:form>
        </td>
        <td valign="top">

            <html:form action="/support/domainsbygame">
                <b>Game</b><br>
                <html:select property="selectedGameId">
                    <html:optionsCollection property="gameList"/>
                </html:select>
                <html:submit value="submit"/>
            </html:form>
        </td>
        <td valign="top">
            <logic:iterate id="domain" name="DomainwlForm" property="allDomains">
                <html:link href="/support/domainformany.do?domainForMany=${domain}&button=show">
                    <bean:write name="domain"/>
                </html:link> <br>
            </logic:iterate>
        </td>
    </tr>
</table>
</body>
</html>
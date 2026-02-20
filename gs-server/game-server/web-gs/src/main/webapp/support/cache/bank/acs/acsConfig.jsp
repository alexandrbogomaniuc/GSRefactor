<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<html>
<head>
    <title>ACS Config Page</title>

    <script type="text/javascript">


        function checkAll(form, checked, group) {
            var el;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                if (el.name.indexOf(group) == 0) {
                    el.checked = checked;
                }

                if (group == 'editableList') {
                    if (el.name.indexOf('payoutPercents') == 0 || el.name.indexOf('acsStates') == 0) {
                        el.disabled = !checked;
                    }
                }
            }
        }

        function includeGame(form, checked, gameId) {
            var el, elValue;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                if (el.name.indexOf('payoutPercents') == 0 || el.name.indexOf('acsStates') == 0) {
                    if (el.id == gameId) el.disabled = !checked;
                }
            }
        }

    </script>
</head>
<body>

<html:form action="/support/acsConfig">
    <html:hidden property="bankId"/>
    <html:submit property="button" value="back"/> <html:submit property="button" value="submit"/>
    <br><br>
    <table border="0" cellpadding="0" cellspacing="7">
        <b>For Currencies:</b> <br>

        <tr>
            <td>
                (All)
            </td>
            <logic:iterate id="currency" name="AcsForm" property="currencyDescr">
                <td>
                    <bean:write name="currency"/>
                </td>
            </logic:iterate>
        </tr>
        <tr>
            <td>
                <input type="checkbox" onClick="checkAll(this.form, this.checked, 'currencyList')">
            </td>
            <logic:iterate id="currency" name="AcsForm" property="currencyDescr">
                <td>
                    <html:multibox property="currencyList" value="${currency}"/>
                </td>
            </logic:iterate>
        </tr>
    </table>
    <br>
    <table border="1" cellpadding="0" cellspacing="5">
        <tr>
            <td><b>Include game </b> (<input type="checkbox" onClick="checkAll(this.form, this.checked, 'editableList')"> All)</td>
            <td align="center"><b>Game</b></td>
            <td align="center"><b>Payout Percent</b></td>
            <td><b>ACS Enable</b> (<input type="checkbox" onClick="checkAll(this.form, this.checked, 'acsStates')"> All)</td>
        </tr>
        <logic:iterate id="acsBean" name="AcsForm" property="acsDataBeans" indexId="index">
            <tr>
                <td align="center">
                    <html:multibox property="editableList" value="${acsBean.gameId}" onchange="includeGame(this.form, this.checked, ${acsBean.gameId})"/></td>
                <td><bean:write name="acsBean" property="gameName"/></td>
                <td><html:text name="AcsForm" property="payoutPercents[${index}]" disabled="true" styleId="${acsBean.gameId}"/></td>
                <td align="center"><html:checkbox name="AcsForm" property="acsStates[${index}]" disabled="true" styleId="${acsBean.gameId}"/></td>
            </tr>
        </logic:iterate>
    </table>

</html:form>


</body>
</html>

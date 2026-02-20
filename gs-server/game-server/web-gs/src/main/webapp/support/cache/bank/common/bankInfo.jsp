<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head><title>
    <bean:write name="BankInfoForm" property="bankId"/> :: <bean:write name="BankInfoForm" property="name"/>
</title></head>
<body>
<b>Bank info:</b> <br>
<html:form action="/support/bankSelectAction">
    <html:hidden property="bankId" value="${BankInfoForm.bankId}"/>
    <html:submit value="editProperties" property="button"/>
    <html:submit value="languagesSupport" property="button"/>
    <html:submit value="acsConfig" property="button"/>
    <%--<html:submit value="FaceBookConfig" property="button"/>--%>
</html:form>

<b>Bank name:</b> <bean:write name="BankInfoForm" property="name"/> <br>
<b>Bank id:</b> <bean:write name="BankInfoForm" property="bankId"/> <br>
<b>Bank ext id:</b> <bean:write name="BankInfoForm" property="extId"/> <br>
<b>Bank limit:</b> <bean:write name="BankInfoForm" property="limit"/> <br>
<b>Bank default currency:</b> <bean:write name="BankInfoForm" property="defaultCurrency"/> <br>
<b>Bank coins:</b><br>

<logic:iterate id="coin" name="BankInfoForm" property="coins">
    <bean:write name="coin"/>
    <br>
</logic:iterate>

<b>Select currency:</b><br>
<html:form action="/support/currencySelect">
    <html:hidden property="bankId" value="${BankInfoForm.bankId}"/>
    <html:select property="currencyCodeAndBankId">
        <html:optionsCollection name="BankInfoForm" property="currencies"/>
    </html:select>
    <br><br>
    <html:submit value="show configured games"/>

</html:form>
<html:form action="/support/awayFromBankInfo">
    <html:hidden property="bankId" value="${BankInfoForm.bankId}"/>
    <html:submit value="subcasinoSelect" property="forward"/>
    <%--<html:submit value="addGame" property="forward"/>--%>
</html:form>

<logic:equal name="BankInfoForm" property="mustShow" value="true">
    <br><br>
    <b>Configured games:</b>
    <br>
    <logic:iterate id="game" name="gameList">
        <html:link href="${game.url}">
            <bean:write name="game" property="descr"/>
        </html:link>
        <br>
    </logic:iterate>


</logic:equal>


</body>
</html>
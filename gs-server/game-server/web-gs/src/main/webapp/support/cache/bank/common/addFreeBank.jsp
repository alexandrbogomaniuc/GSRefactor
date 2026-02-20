<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<html>
<head><title>Add bank page</title>
    <script type="text/javascript">
        function trimLeft(str) {
            return str.replace(/^\s+/, '');
        }

        function trimRight(str) {
            return str.replace(/\s+$/, '');
        }

        function trim(str) {
            return trimRight(trimLeft(str));
        }

        function checkForm(form) {
            var el, elName, value, type;
            var errorList = [];

            var errorText = {
                1: "Field is not filled 'Id'",
                2: "Field is not filled 'Ext Id'",
                3: "Field is not filled 'Name'"
            }
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                elName = el.nodeName.toLowerCase();
                value = trim(el.value);
                if (elName == "input") {
                    type = el.type.toLowerCase();
                    if (type == "text") {
                        if (el.name == "id" && value == "") errorList.push(1);
                        if (el.name == "extId" && value == "") errorList.push(2);
                        if (el.name == "name" && value == "") errorList.push(3);
                    }
                }
            }
            if (!errorList.length) return true;
            var errorMsg = "Error form input!\n";
            for (i = 0; i < errorList.length; i++) {
                errorMsg += errorText[errorList[i]] + "\n";
            }
            alert(errorMsg);

            return false;
        }

        function checkAll(form, checked) {
            var el, elValue;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                if (el.name == "gameIds") {
                    el.checked = checked;
                }
            }
        }

    </script>

</head>
<body>
<html:link href="/support/newBankNSubCasino.do"> BACK </html:link>

<html:form action="/support/acceptInfoFreeBank">
    Accept fields and games from other bank
    <html:select property="copyBankId">
        <html:optionsCollection property="allBanks"/>
    </html:select>
    <html:submit value="accept"/>
</html:form> <br>

<html:form action="/support/createFreeBank" onsubmit="return checkForm(this);">

    <table border="1" align="left" cellpadding="7" cellspacing="0">
        <tr>
            <td align="center">Bank</td>
            <td align="center">Games</td>
            <td aling="center">Properties</td>
        </tr>
        <tr>
            <td valign="top">
                <b>Id</b> <html:text property="id" value=""/> <br>
                <b>Ext Id</b> <html:text property="extId" value=""/> <br>
                <b>Name</b> <html:text property="name" value=""/> <br>
                <b>Default currency</b> <html:select property="defCurCode">
                <html:optionsCollection property="allCurrencies"/>
            </html:select> <br>
                <b>Default language</b> <html:select property="defLang">
                <html:optionsCollection property="allLanguages"/>
            </html:select> <br>
                <b>Limit</b> <html:select property="limitId">
                <html:optionsCollection property="allLimits"/>
            </html:select> <br><br>
                <b>Coins:</b> <br>

                <logic:iterate id="coin" name="NewBankForm" property="allCoins">
                    <html:multibox property="coinIds" value="${coin.value}"/>
                    <bean:write name="coin" property="label"/>
                    <br>
                </logic:iterate> <br>

                <b>Currencies:</b> <br>

                <logic:iterate id="currency" name="NewBankForm" property="allCurrencies">
                    <html:multibox property="currencyCodes" value="${currency.value}"/>
                    <bean:write name="currency" property="label"/>
                    <br>
                </logic:iterate>

                <br>
                <html:submit value="create bank" property="button"/> <br>

            </td>
            <td valign="top">
                Games will be configured into<br>
                the default currency of the bank
                <br>
                <input type="checkbox" onClick="checkAll(this.form, this.checked)">
                <b>Check/Uncheck all</b> <br>
                <logic:iterate id="game" name="NewBankForm" property="games">
                    <html:multibox property="gameIds" value="${game.value}"/>
                    <bean:write name="game" property="label"/>
                    <br>
                </logic:iterate>

            </td>
            <td valign="top">
                The properties will be configured as a template bank
                <br>
                <br>
                <b>CashierURL</b><br>
                <html:text property="bpsForm.cashierUrl" size="90"/><br><br>

            </td>
        </tr>

    </table>
</html:form>

</body>
</html>
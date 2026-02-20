<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<html>
<head><title>Edit properties page</title></head>
<body>

<logic:equal value="full" name="BankPropertiesForm" property="mode">
    <html:form action="/support/bankPropEdit">
        <input type="hidden" name="mode" value="full"/>
        <input type="hidden" name="bankId" value="${BankPropertiesForm.bankId}"/>
        <html:errors/>
        <br/>
        <table>
            <tr>
                <td><html:text property="cashierUrl" size="90"/></td>
                <td><b>CashierURL</b><br/></td>
            </tr>
            <tr>
                <td><html:text property="externalBankId" size="90"/></td>
                <td><b>Extetrnal Bank Id</b><br/></td>
            </tr>

            <tr>
                <td><html:text property="externalBankIdDescription" size="90"/></td>
                <td><b>Extetrnal Bank Id Description id</b><br/></td>
            </tr>
            <tr>
                <td align="right">
                    <html:select property="defaultCurrencyCode">
                        <html:optionsCollection name="BankPropertiesForm" property="currenciesList"
                                                label="label" value="value"/>

                    </html:select>
                </td>
                <td><b>Default currency</b><br/></td>
            </tr>
            <tr>
                <td align="right">
                    <html:select property="limitId">
                        <html:optionsCollection name="BankPropertiesForm" property="limitsList"
                                                label="label" value="value"/>
                    </html:select>
                </td>
                <td><b>Limit</b><br/></td>
            </tr>
            <tr>
                <td align="right">
                    <html:select property="defaultLanguage">
                        <html:optionsCollection name="BankPropertiesForm" property="languagesList"
                                                label="label" value="value"/>
                    </html:select>
                </td>
                <td><b>Default Language</b><br/></td>
            <tr>
            <tr>
                <td><html:text property="freeGameOverRedirectUrl" size="90"/></td>
                <td><b>FreeGameOverRedirectUrl</b><br/></td>
                <br/><br/>
            </tr>
            <tr>
                <logic:iterate id="property" name="properties">
                    <td>
                        <bean:define id="info" name="property" property="value"/>
                        <logic:equal name="info" value="boolean" property="key">
                            <html:checkbox property="${info.value}" value="true"/>
                        </logic:equal>
                        <logic:equal name="info" value="string" property="key">
                            <html:text property="${info.value}" size="90"/>
                        </logic:equal>
                        <logic:equal name="info" value="numeric" property="key">
                            <html:text property="${info.value}" size="12"/>
                        </logic:equal>
                    </td>
                    <td>
                        <span>${property.key}</span>
                    </td>
                </logic:iterate>
            </tr>
        </table>
        <html:submit value="back" property="button"/>
        <html:submit value="save" property="button"/>
    </html:form>
</logic:equal>
</body>
</html>
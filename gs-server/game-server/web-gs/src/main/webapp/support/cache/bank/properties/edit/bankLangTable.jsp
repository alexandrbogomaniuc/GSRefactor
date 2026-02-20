<%--
  Created by IntelliJ IDEA.
  User: vik
  Date: 16.11.11
  Time: 10:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.configuration.messages.MessageManager" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.apache.axis.encoding.Base64" %>
<%@ page import="com.dgphoenix.casino.common.util.xml.XMLUtils" %>
<%@ page import="java.util.Set" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%
    long bankId = Long.parseLong(request.getParameter("bankId"));
    long gameId = Long.parseLong(request.getParameter("gameId"));

    Set<String> editedLangs = (HashSet<String>) request.getAttribute("editedLanguages");
    request.getSession().removeAttribute("editedLanguages");
    request.removeAttribute("editedLanguages");
    Set<String> conflictLangs = (HashSet<String>) request.getAttribute("conflictLanguages");
    request.getSession().removeAttribute("conflictLanguages");
    request.removeAttribute("conflictLanguages");
    if (editedLangs == null) {
        editedLangs = new HashSet();
    }
    if (conflictLangs == null) {
        conflictLangs = new HashSet();
    }
    request.setAttribute("supportedLangsFromLoadAction", request.getAttribute("supportedLangsFromLoadAction"));
%>

<html>
<head><title>Banks&Languages table page</title>

    <script type="text/javascript">
        function strEndWith(str, endStr) {
            var dif = str.length - endStr.length;
            if (dif < 0) return false;
            for (var i = endStr.length; i >= 0; i--) {
                if (endStr.charAt(i) != str.charAt(i + dif)) return false;
            }
            return true;
        }

        function checkAll(form, lang, checked) {
            var el, elValue;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                if (el.type == "checkbox") {
                    elValue = el.value.toLowerCase();
                    if (strEndWith(elValue, lang)) {
                        el.checked = checked;
                    }
                }
            }
        }
    </script>

</head>
<body>
<%--<jsp:useBean id="gameBean" class="com.dgphoenix.casino.support.cache.bank.edit.forms.editproperties.BeanHelper"/>--%>
<%--<jsp:setProperty name="gameBean" property="gameId" value="0"/>--%>
<%--<jsp:setProperty name="gameBean" property="bankId" value="0"/>--%>
<div align="center">
    <%=

    MessageManager.getInstance().getApplicationMessage("game.name." + BaseGameCache.getInstance().getGameNameById(bankId,
            gameId)) == null
            ?
            BaseGameCache.getInstance().getGameNameById(bankId, gameId)
            :
            MessageManager.getInstance().getApplicationMessage("game.name." +
                    BaseGameCache.getInstance().getGameNameById(
                            bankId,
                            gameId))
    %>   (id = ${BankLangForm.bankId})
</div>
<div align="center">
    <html:form action="/support/banklangcacheedit">
        <input type="hidden" name="bankId" value="<%= bankId %>">
        <input type="hidden" name="gameId" value="<%= gameId %>">
        <input type="hidden" name="supportedLangsFromLoadAction" value="<%=Base64.encode(XMLUtils.toXML(request.getAttribute("supportedLangsFromLoadAction")).getBytes()) %>">

        <div align="left">
            <html:submit value="back" property="button"/>
            <html:submit value="save" property="button"/>
        </div>
        <table border="1" align="center" cellpadding="7" cellspacing="0">
            <tr>
                <td></td>
                <logic:iterate id="lang" name="BankLangForm" property="allLanguages">
                    <logic:notEqual value="NOTSELECTED" name="lang">
                        <td>
                            <bean:write name="lang"/>
                            <input type="checkbox" onClick="checkAll(this.form,'${lang}', this.checked)">
                        </td>
                    </logic:notEqual>
                </logic:iterate>

            </tr>

            <logic:iterate id="bank" name="BankLangForm" property="allBanksBySubCasino">
                <tr>
                    <td>
                            <%--<!-- <jsp:setProperty name="gameBean" property="gameId" value="${BankLangForm.gameId}"/> -->--%>
                            <%--<jsp:setProperty name="gameBean" property="bankId" value="${bank}"/>--%>
                        <b>
                            <%=BankInfoCache.getInstance().getBankInfo(bankId)
                                    .getExternalBankIdDescription()
                            %>
                            (id = ${bank})
                        </b>
                    </td>

                    <logic:iterate id="lang" name="BankLangForm" property="allLanguages">
                        <logic:notEqual value="NOTSELECTED" name="lang">
                            <%

                                String tdStyle = "";
                                if (editedLangs.contains(bank + "+" + lang)) {
                                    tdStyle = "background: #4fe8a2";
                                }
                                if (conflictLangs.contains(bank + "+" + lang)) {
                                    tdStyle = "background: #ffb6c1";
                                }

                            %>
                            <td align="center" style="<%= tdStyle %>">
                                <html:multibox property="langSupported" value="${bank}+${lang}"/>
                            </td>
                        </logic:notEqual>
                    </logic:iterate>

                </tr>
            </logic:iterate>

        </table>

    </html:form>
</div>
</body>
</html>
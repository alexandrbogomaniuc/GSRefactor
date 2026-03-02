<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.configuration.messages.MessageManager" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="com.abs.casino.common.util.xml.XMLUtils" %>
<%@ page import="org.apache.axis.encoding.Base64" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<%@ page trimDirectiveWhitespaces="true" %>
<%
    String bankIdValue = request.getParameter("bankId");
    if (bankIdValue == null) {
        Object languageSupportForm = request.getAttribute("LanguageSupportForm");
        if (languageSupportForm != null) {
            try {
                Object value = languageSupportForm.getClass().getMethod("getBankId").invoke(languageSupportForm);
                if (value != null) {
                    bankIdValue = String.valueOf(value);
                }
            } catch (Exception ignore) {
                // keep fallback path below
            }
        }
    }
    if (bankIdValue == null) {
        response.getWriter().println("bankId is missing.");
        return;
    }

    HashSet<String> editedLangs = (HashSet<String>) request.getAttribute("editedLanguages");
    request.getSession().removeAttribute("editedLanguages");
    request.removeAttribute("editedLanguages");
    HashSet<String> conflictLangs = (HashSet<String>) request.getAttribute("conflictLanguages");
    request.getSession().removeAttribute("conflictLanguages");
    request.removeAttribute("conflictLanguages");

    HashSet<String> languageExists = (HashSet<String>) request.getAttribute("languageExists");
    request.getSession().removeAttribute("languageExists");
    request.removeAttribute("languageExists");
    HashSet<String> languageNotExists = (HashSet<String>) request.getAttribute("languageNotExists");
    request.getSession().removeAttribute("languageNotExists");
    request.removeAttribute("languageNotExists");

    HashSet<String> addGamesLanguages = (HashSet<String>) request.getAttribute("addGamesLanguages");
    request.getSession().removeAttribute("addGamesLanguages");
    request.removeAttribute("addGamesLanguages");
    HashSet<String> delGamesLanguages = (HashSet<String>) request.getAttribute("delGamesLanguages");
    request.getSession().removeAttribute("delGamesLanguages");
    request.removeAttribute("delGamesLanguages");

    if (editedLangs == null) {
        editedLangs = new HashSet();
    }
    if (conflictLangs == null) {
        conflictLangs = new HashSet();
    }

    if (languageExists == null) {
        languageExists = new HashSet();
    }
    if (languageNotExists == null) {
        languageNotExists = new HashSet();
    }

    if (addGamesLanguages == null) {
        addGamesLanguages = new HashSet();
    }
    if (delGamesLanguages == null) {
        delGamesLanguages = new HashSet();
    }
    request.setAttribute("supportedLangsFromLoadAction", request.getAttribute("supportedLangsFromLoadAction"));
    Long bankId = Long.parseLong(bankIdValue);
    Long masterBankId = null;
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
    if (bankInfo.getMasterBankId() != null && bankInfo.getMasterBankId() > 0 && !bankInfo.getMasterBankId().equals(bankId)) {
        masterBankId = bankInfo.getMasterBankId();
    }

%>
<html>
<head><title>Languages table page</title>
    <script type="text/javascript">

        window.onload = function () {
            var nodes = document.getElementById("savePanel").getElementsByTagName('*');
            for (var i = 0; i < nodes.length; i++) {
                nodes[i].disabled = false;
            }
        };

        function strEndWith(str, endStr) {
            var dif = str.length - endStr.length;
            if (dif < 0) return false;
            for (var i = endStr.length; i >= 0; i--) {
                if (endStr.charAt(i) != str.charAt(i + dif)) return false;
            }
            return true;
        }

        function getGameId(str) {
            var gameId = "";
            for (var i = 0; i < str.length; i++) {
                if (str.charAt(i) != '+') {
                    gameId = gameId + str.charAt(i);
                } else {
                    break;
                }
            }
            return gameId;
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

        function backLightAll(form, lang, checked) {
            var el, elValue, fl = false, gameId, tdId;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                if (el.type == "checkbox") {
                    elValue = el.value.toLowerCase();
                    if (strEndWith(elValue, lang)) {
                        gameId = getGameId(elValue);
                        tdId = gameId + "+" + lang;
                        var tdEl = document.getElementById(tdId);
                        if (checked) {
                            tdEl.style.backgroundColor = '#9ce4ff';
                        } else {
                            tdEl.style.backgroundColor = '#FFFFFF';
                        }
                        fl = true;
                    }
                }
            }
        }

        function checkLanguageFixAll() {
            if (confirm("Are you want fix all languages for all games?")) {
                document.getElementById("languageCheck").submit();
            }
        }
    </script>

    <style>
        .uploadFiles {
            float: left;
            margin: 15px;
            width: 420px;
        }

        .cacheSetAll {
            float: right;
            margin: 5px;
            width: 525px;
        }

        .supportTools {
            float: left;
            margin: 15px;
            width: 205px;
        }

        .divHeader {
            font-size: 14pt;
        }

        .optionEntry {
            display: block;
        }

        .optionName {
            display: inline-block;
            width: 170px;
        }

        .innerMessages {
            color: red;
            font-size: 12pt;
        }

        .tool {
            height: 100px;
        }
    </style>

</head>
<body>
<%if (masterBankId != null) {%>
<div class="innerMessages">This is slave bank, master bank: <%=masterBankId%>
</div>
<%}%>
<logic:present name="innerMessages" scope="request">
    <div class="innerMessages">
        <logic:iterate id="message" name="innerMessages" scope="request">
            <bean:write name="message"/></br>
        </logic:iterate>
    </div>
</logic:present>

<div class="uploadFiles">
    <div class="recovery tool">
        <span class="divHeader">Recovery languages:</span>
        <html:form action="/support/uploadLangs" method="post" enctype="multipart/form-data">
            <input type="hidden" name="bankId" value="${LanguageSupportForm.bankId}">
            <div class="options">
                    <span class="optionEntry">
                        <span class="optionName">Choose file: </span><html:file property="file" value="review"/>
                    </span>
            </div>
            <html:submit value="recovery"/>
            <html:hidden property="gameSetType" value="${LanguageSupportForm.gameSetType}"/>
        </html:form>
    </div>

    <div class="uploadCSV tool">
        <span class="divHeader">Import CSV file:</span>
        <html:form action="/support/uploadCSVLangs" method="post" enctype="multipart/form-data">
            <input type="hidden" name="bankId" value="${LanguageSupportForm.bankId}">
            <div class="options">
                    <span class="optionEntry">
                        <span class="optionName">Choose file: </span><html:file property="file" value="review"/>
                    </span>
                <span class="optionEntry">
                        <span class="optionName">SetPartiallyDone: </span><html:checkbox property="setPartiallyDone" value="true"/>
                    </span>
            </div>
            <html:submit value="Import CSV"/>
            <html:hidden property="gameSetType" value="${LanguageSupportForm.gameSetType}"/>
        </html:form>
    </div>
</div>

<div class="cacheSetAll">
    <span class="divHeader">Put languages in another bank:</span>
    <html:form action="/support/langcachesetall" method="post" enctype="multipart/form-data">
        <input type="hidden" name="bankId" value="${LanguageSupportForm.bankId}">
        The same languages as at
        <html:select property="selectedBank">
            <html:optionsCollection property="banksForSubcasino"/>
        </html:select> bank
        <html:submit value="putLangs" property="button"/>
        <html:hidden property="gameSetType" value="${LanguageSupportForm.gameSetType}"/>
    </html:form>
</div>

<div class="supportTools">
    <div class="tool">
        <span class="divHeader">Show filter:</span>
        <html:form action="/support/languagetable" styleId="selectGameSetType">
            <html:hidden property="bankId"/>
            <html:select property="gameSetType">
                <html:optionsCollection property="allGameSetTypes"/>
            </html:select>
            <html:submit value="show"/>
        </html:form>
    </div>

    <div class="tool">
        <span class="divHeader tool">Languages validator:</span>
        <html:form action="/support/langcachecheck" styleId="languageCheck">
            <html:submit value="checkAll" property="button"/>
            <html:button value="fixAll" property="button" onclick="checkLanguageFixAll();"/>
            <html:hidden property="bankId"/>
            <html:hidden property="gameSetType"/>
        </html:form>
    </div>
</div>

<html:form action="/support/langcacheedit" style="float:left">
    <html:hidden property="bankId"/>
    <input type="hidden" name="supportedLangsFromLoadAction" value="<%=Base64.encode(XMLUtils.toXML(request.getAttribute("supportedLangsFromLoadAction")).getBytes()) %>">

    <div align="left" id="savePanel">
        <html:submit value="back" property="button"/>
        <html:submit value="save" property="button" disabled="true"/>
        <br>
        <html:select property="gameSetType">
            <html:optionsCollection property="allGameSetTypes"/>
        </html:select>
        <html:submit value="saveToXML" property="button" disabled="true"/>
    </div>

    Default language for <b><bean:write name="LanguageSupportForm" property="bankDescr"/></b> bank
    <html:select property="defLang">
        <html:optionsCollection property="defLangList"/>
    </html:select> <br><br>
    <table border="1" align="center" cellpadding="7" cellspacing="0">

            <%--Backlight--%>
        <tr>
            <td><b>Backlight column</b></td>
            <logic:iterate id="lang" name="LanguageSupportForm" property="allLanguages">
                <logic:notEqual value="NOTSELECTED" name="lang">
                    <td align="center">
                        <input type="checkbox" onClick="backLightAll(this.form,'${lang}', this.checked)">
                    </td>
                </logic:notEqual>
            </logic:iterate>
        </tr>

            <%--Select Column--%>
        <tr>
            <td><b>Select column</b></td>
            <logic:iterate id="lang" name="LanguageSupportForm" property="allLanguages">
                <logic:notEqual value="NOTSELECTED" name="lang">
                    <td align="center">
                        <input type="checkbox" onClick="checkAll(this.form,'${lang}', this.checked)">
                    </td>
                </logic:notEqual>
            </logic:iterate>
        </tr>

            <%--Language list--%>
        <tr>
            <td><b>Language</b></td>
            <logic:iterate id="lang" name="LanguageSupportForm" property="allLanguages">
                <logic:notEqual value="NOTSELECTED" name="lang">
                    <td>
                        <font face="verdana" size="4"> [<bean:write name="lang"/>] </font>
                    </td>
                </logic:notEqual>
            </logic:iterate>
        </tr>

        <logic:iterate id="game" name="LanguageSupportForm" property="allGamesByBank">
            <tr>
                <td>
                    <b>
                        <html:link href="/support/banklang.do?gameId=${game}&bankId=${LanguageSupportForm.bankId}">
                            <%
                                Long gameId = Long.parseLong(String.valueOf(game));
                                String gameName = BaseGameCache.getInstance().getGameNameById(bankId, gameId);
                                String msg = MessageManager.getInstance().getApplicationMessage("game.name." + gameName);
                                if (msg == null) {
                                    msg = gameName;
                                }
                            %>
                            <%= msg %>
                            (id = ${game})
                        </html:link>
                    </b>
                </td>

                <logic:iterate id="lang" name="LanguageSupportForm" property="allLanguages">
                    <logic:notEqual value="NOTSELECTED" name="lang">
                        <%
                            String tdStyle = "";
                            String entry = game + "+" + lang;
                            if (addGamesLanguages.contains(entry) ||
                                    languageExists.contains(entry) ||
                                    editedLangs.contains(entry)) {
                                tdStyle = "background: #4fe8a2";
                            }
                            if (delGamesLanguages.contains(entry) ||
                                    languageNotExists.contains(entry) ||
                                    conflictLangs.contains(entry)) {
                                tdStyle = "background: #ffb6c1";
                            }
                        %>
                        <td align="center" id="${game}+${lang}" style="<%= tdStyle %>">
                            <html:multibox property="langSupported" value="${game}+${lang}"/>
                        </td>
                    </logic:notEqual>
                </logic:iterate>
            </tr>
        </logic:iterate>
    </table>
</html:form>
</body>
</html>

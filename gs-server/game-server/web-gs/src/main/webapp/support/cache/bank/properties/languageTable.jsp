<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<html>
<head><title>Languages table page</title>


    <script type="text/javascript">
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
                            tdEl.style.backgroundColor = '#ffb6c1';
                        } else {
                            tdEl.style.backgroundColor = '#FFFFFF';
                        }
                        fl = true;

                    }

                }
            }

        }
    </script>

</head>
<body>
<div align="center">
    <table WIDTH="80%">
        <tr>
            <td ALIGN=LEFT>
                <html:form action="/support/uploadLangs" method="post" enctype="multipart/form-data">
                    Choose file: <html:file property="file" value="review"/>
                    <html:submit value="recovery"/>
                </html:form>
            </td>
            <td ALIGN=RIGHT>
                <html:form action="/support/langcachesetall" method="post" enctype="multipart/form-data">
                    The same languages as at
                    <html:select property="selectedBank">
                        <html:optionsCollection property="banksForSubcasino"/>
                    </html:select> bank
                    <html:submit value="putLangs" property="button"/>
                </html:form>
            </td>
        </tr>
    </table>
    <html:form action="/support/langcacheedit">
        <html:hidden property="bankId"/>

        <div align="left">
            <html:submit value="back" property="button"/>
            <html:submit value="save" property="button"/>
            <html:submit value="saveToXML" property="button"/>
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

            <logic:iterate id="game" name="LanguageSupportForm" property="games">
                <tr>
                    <td>
                        <b>
                            <html:link href="/support/banklang.do?gameId=${game.label}&bankId=${LanguageSupportForm.bankId}">
                                ${game.value} (id = ${game.label})
                            </html:link>
                        </b>
                    </td>

                    <logic:iterate id="lang" name="LanguageSupportForm" property="allLanguages">
                        <logic:notEqual value="NOTSELECTED" name="lang">
                            <td align="center" id="${game.label}+${lang}">
                                <html:multibox property="langSupported" value="${game.label}+${lang}"/>
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

<%@ page import="static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.containsProperty" %>
<%@ page import="static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.isMandatoryProperty" %>
<%@ page import="static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.isBooleanProperty" %>
<%@ page import="static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<html>
<head>
    <title>
        <bean:write name="GameInfoForm" property="bankId"/> :: <bean:write name="GameInfoForm" property="gameId"/> ::
        <bean:write name="GameInfoForm" property="currencyCode"/> :: <bean:write name="GameInfoForm" property="gameName"/>
    </title>

    <script type='text/javascript'>
        function onOff() {
            list = document.getElementsByName('newPropKey');
            npk = list[0];
            list = document.getElementsByName('newPropValue');
            npv = list[0];
            list = document.getElementsByName('newProperty');
            np = list[0];
            npk.disabled = !np.checked;
            npv.disabled = !np.checked;
        }
    </script>

</head>
<body>
<b>FIELDS:</b> <br>
<b>Game name:</b> <bean:write name="GameInfoForm" property="gameName"/> <br>
<b>Bank id:</b> <bean:write name="GameInfoForm" property="bankId"/> <br>
<b>Currency:</b> <bean:write name="GameInfoForm" property="currencyCode"/> <br>
<b>Game type:</b> <bean:write name="GameInfoForm" property="gameType"/> <br>
<b>Game group:</b> <bean:write name="GameInfoForm" property="gameGroup"/> <br>
<b>Game variable type:</b> <bean:write name="GameInfoForm" property="gameVariableType"/> <br>
<b>External game id:</b> <bean:write name="GameInfoForm" property="externalGameId"/> <br>
<html:errors/>
<br>
<html:form action="/support/editgameprop">
    <html:hidden property="gameId"/>
    <html:hidden property="currencyCode"/>
    <html:hidden property="bankId"/>
    <input type="hidden" name="baseGameInfoXML" value="${baseGameInfoXML}">
    <b>RM class name</b> <html:text property="rmClassName" size="60"/><br>
    <b>GS class name</b> <html:text property="gsClassName" size="60"/><br>
    <b>Jackpot id</b> <html:text property="jackpotId"/><br>
    <b>Servlet</b> <html:text property="servletName"/> <html:checkbox
        property="acceptServletNameForSubcasino"/> accept servlet for this game for current subcasino <br>


    <logic:equal property="gameVariableType" name="GameInfoForm" value="LIMIT">
        <br>
        <b>Limit:</b>

        <logic:empty property="minLimitValue" name="GameInfoForm">
            <b>Set default bank Limit:</b>
            <bean:write property="bankLimit" name="GameInfoForm"/>
        </logic:empty>

        <br>
        <b>MinValue</b><html:text property="minLimitValue"/> <br>
        <b>MaxValue</b><html:text property="maxLimitValue"/> <br>
        <br>
    </logic:equal>

    <logic:equal property="gameVariableType" name="GameInfoForm" value="COIN">
        <b>Coins:</b>

        <logic:empty property="coinIds" name="GameInfoForm">
            <b>Set default bank coins:{</b>

            <bean:size id="bankCoinsListSize" property="bankCoins" name="GameInfoForm"/>
            <logic:iterate id="bankCoinId" property="bankCoins" name="GameInfoForm" indexId="bankCoinIndex">
                <bean:write name="bankCoinId"/>
                <logic:notEqual value="${bankCoinsListSize - 1}" name="bankCoinIndex">
                    ,
                </logic:notEqual>
            </logic:iterate>

            <b>}</b>
        </logic:empty>

        <br>
        <table>
            <tr>
                <logic:iterate id="coin" name="GameInfoForm" property="coins" indexId="elementIndex">
                    <bean:define id="mod" value="${elementIndex % 10}"/>

                    <logic:equal name="mod" value="0">
                        <td>
                    </logic:equal>

                    <html:multibox property="coinIds">
                        <bean:write name="coin" property="value"/>
                    </html:multibox>
                    <bean:write name="coin" property="label"/>

                    <br>

                    <logic:equal name="mod" value="9">
                        </td>
                    </logic:equal>

                </logic:iterate>
            </tr>
        </table>
    </logic:equal>


    <b>PROPERTIES:</b> <br>
    <table border="1" cellpadding="7" cellspacing="0">
        <tr>
            <td>KEY</td>
            <td>VALUE</td>
            <td>FOR ALL CURRENCIES</td>
            <td></td>
        </tr>
        <logic:iterate id="prop" name="GameInfoForm" property="properties" indexId="index" type="org.apache.struts.util.LabelValueBean">
            <%
                boolean disable = isInheritedFromTemplate(prop.getLabel());
                String tdBgColor = "";
                if (!containsProperty(prop.getLabel())) {
                    tdBgColor = "bgcolor=\"#ddeda1\"";
                } else if (isMandatoryProperty(prop.getLabel())) {
                    tdBgColor = "bgcolor=\"#b1f2c2\"";
                } else if (disable) {
                    tdBgColor = "bgcolor=\"#BBBBBB\"";
                }
            %>
            <tr>
                <td <%=tdBgColor%>><b><bean:write name="prop" property="label"/></b></td>
                <html:hidden name="prop" property="label" indexed="true"/>
                <td><%if (isBooleanProperty(prop.getLabel())) {%>
                    <html:checkbox name="prop" property="value" value="TRUE" indexed="true" disabled="<%=disable%>"/>
                    <%if (disable) {%>
                    <html:checkbox style="visibility: hidden" name="prop" property="value" value="TRUE" indexed="true"/>
                    <%}%>
                    <%} else {%>
                    <html:text name="prop" indexed="true" property="value" disabled="<%=disable%>"/>
                    <%if (disable) {%>
                    <html:hidden name="prop" property="value" indexed="true"/>
                    <%}%>
                    <%}%>
                </td>
                <td align="center"><html:multibox property="allCurrencyPropertyList" value="${prop.label}" disabled="<%=disable%>"/></td>
                <td align="left">
                    <logic:notEqual name="GameInfoForm" property="templateProperties.${prop.label}" value="${null}">
                        <logic:notEqual name="GameInfoForm" property="templateProperties.${prop.label}" value="${prop.value}">
                            <html:multibox property="resetList" value="${prop.label}" disabled="<%=disable%>"/>
                            reset to default value [<bean:write name="GameInfoForm" property="templateProperties.${prop.label}"/>]
                        </logic:notEqual>
                    </logic:notEqual>

                    <logic:equal name="GameInfoForm" property="templateProperties.${prop.label}" value="${null}">
                        <html:multibox property="removeList" value="${prop.label}" disabled="<%=disable%>"/> delete property
                    </logic:equal>
                </td>

            </tr>
        </logic:iterate>
    </table>
    <br>
    <b>New property</b> <html:checkbox property="newProperty" onclick="onOff()"/> <br>

    <html:text property="newPropKey" value="" disabled="true"/> key <br>
    <html:text property="newPropValue" value="" disabled="true"/> value
    <br><br>
    <b>Save parameters for all currencies(!)</b> <html:checkbox property="saveAllGamesByBank"/>
    <br><br>

    <html:submit value="back" property="button"/> <html:submit value="submit" property="button"/>
</html:form>

</body>
</html>
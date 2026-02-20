<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<html>
<head><title>Add game page</title></head>
<body>

<html:form action="/support/inputModeId">
    <b> Select the input mode of game ID </b>
    <html:select property="inputModeOfId">
        <html:option value="exist"> existing id </html:option>
        <html:option value="manual"> new id </html:option>
    </html:select>
    <html:submit value="enter"/>
</html:form>

<logic:equal name="AwayFromBankInfoForm" property="inputModeOfId" value="exist">
    <br>
    <b> Select the game id </b>
    <html:form action="/support/gameIdSelect">
        <html:select property="gameId">
            <html:optionsCollection name="AwayFromBankInfoForm" property="gameIds"/>
        </html:select>
        <html:submit value="show banks"/>
    </html:form>
    <br>
    <logic:equal name="AddGameForm" property="mustShowBanks" value="true">
        <b> Banks containing the selected game: </b> <br>
        <html:form action="/support/copyConfig">
            <html:select property="selectedBankId">
                <html:optionsCollection name="AddGameForm" property="banksWithSelectedGame"/>
            </html:select>
            <br><br>
            <html:hidden property="bankId" value="${AwayFromBankInfoForm.bankId}"/>
            <html:submit value="copyConfig"/>
        </html:form>
        <html:form action="/support/bankInfo.do?bankId=${AwayFromBankInfoForm.bankId}">
            <html:submit value="cancel"/>
        </html:form>
        <bean:write name="AddGameForm" property="configGameStatus"/>
    </logic:equal>

</logic:equal>


<logic:equal name="AwayFromBankInfoForm" property="inputModeOfId" value="manual">
    <html:form action="/support/createNewGame">
        <html:hidden property="bankId" value="${AwayFromBankInfoForm.bankId}"/>
        <b>Game id:</b> <html:text property="gameId"/>
        Max current id: <bean:write name="AwayFromBankInfoForm" property="currentMaxGameId"/> <br>
        <b>Game name:</b> <html:text property="gameName"/> <br>
        <b>Game type:</b> <html:select property="gameType">
        <html:option value="SP"> SP </html:option>
        <html:option value="MP"> MP </html:option>
    </html:select> <br>
        <b>Game group:</b> <html:select property="gameGroup">
        <html:option value="SLOTS"> Slots </html:option>
        <html:option value="TABLE"> Table </html:option>
        <html:option value="KENO"> Keno </html:option>
        <html:option value="VIDEOPOKER"> Video Poker </html:option>
        <html:option value="SOFT_GAMES"> Soft Games </html:option>
        <html:option value="PYRAMID_POKER"> Pyramid Poker </html:option>
        <html:option value="SOFT_GAME_ARCADE"> Arcade Soft Game </html:option>
        <html:option value="MULTIHAND_POKER"> Multihand Poker </html:option>
        <html:option value="MULTISTACK_POKER"> Multistack Poker </html:option>
        <html:option value="RUSH_THE_ROYAL"> Rush The Royal </html:option>
    </html:select> <br>
        <b>Game variable type:</b> <html:select property="gameVariableType">
        <html:option value="LIMIT"> LIMIT </html:option>
        <html:option value="COIN"> COIN </html:option>
    </html:select> <br>
        <b>RM Class name:</b> <html:text property="rmClassName"/> <br>
        <b>SP Class name:</b> <html:text property="spClassName"/> <br>
        <b>Limit:</b> <html:select property="limitId">
        <html:optionsCollection name="AwayFromBankInfoForm" property="allLimits"/>
    </html:select> <br>

        <b>Create Jackpot:</b> <html:select property="createJackpot">
        <html:option value="FALSE">FALSE</html:option>
        <html:option value="TRUE">TRUE</html:option>
    </html:select> <br>

        <b>PCRP: ( % )</b> <html:text property="pcrp"/> <br>
        <b>BCRP: ( % )</b> <html:text property="bcrp"/> <br>

        <b>Coins:</b> <br>
        <logic:iterate id="coin" name="AwayFromBankInfoForm" property="allCoins">
            <html:multibox property="coinIds">
                <bean:write name="coin" property="value"/>
            </html:multibox>
            <bean:write name="coin" property="label"/>
            <br>
        </logic:iterate> <br>

        <html:submit value="enterProperties" property="submitType"/> <br><br>

        <html:submit value="create" property="submitType"/>


    </html:form>
    <html:form action="/support/bankInfo.do?bankId=${AwayFromBankInfoForm.bankId}">
        <html:submit value="cancel"/>
    </html:form>
    <br>
    <bean:write name="AddGameForm" property="createGameStatus"/>
</logic:equal>


</body>
</html>
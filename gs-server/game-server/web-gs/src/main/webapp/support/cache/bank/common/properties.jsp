<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>


<html>
<head><title>Enter properties</title></head>
<body>

<b> Enter properties: </b> <br><br>
<html:form action="/support/enterProperties">
    <b>Payout percent: ( % )</b> <html:text property="payoutPercent"/> <br>
    <b>Jackpot multiplier:</b> <html:text property="jackpotMultiplier"/> <br>
    <b>Chip values:</b> <html:text property="chipValues"/> <br>
    <b>Def coin:</b> <html:text property="defCoin"/> <br>
    <b>Max bet1:</b> <html:text property="maxBet1"/> <br>
    <b>Max bet2:</b> <html:text property="maxBet2"/> <br>
    <b>Max bet3:</b> <html:text property="maxBet3"/> <br>
    <b>Max bet4:</b> <html:text property="maxBet4"/> <br>
    <b>Max bet5:</b> <html:text property="maxBet5"/> <br>
    <b>Max bet6:</b> <html:text property="maxBet6"/> <br>
    <b>Max bet12:</b> <html:text property="maxBet12"/> <br>
    <b>Max bet18:</b> <html:text property="maxBet18"/> <br>
    <b>Acs bank limit:</b> <html:text property="acsBankLimit"/> <br>
    <b>Acs bank sum:</b> <html:text property="acsBankSum"/> <br>
    <b>Image URL:</b> <html:text property="imageURL"/> <br>
    <b>Is enabled:</b> <html:select property="enabled">
    <html:option value="TRUE"/>
    <html:option value="FALSE"/>
</html:select> <br>

    <b>Key acs enabled:</b> <html:select property="keyAcsEnabled">
    <html:option value="TRUE"/>
    <html:option value="FALSE"/>
</html:select> <br>

    <b>Game testing</b> <html:select property="gameTesting">
    <html:option value="TRUE"/>
    <html:option value="FALSE"/>
</html:select>

    <br><br>

    <html:submit value="submit"/>
</html:form>


</body>
</html>
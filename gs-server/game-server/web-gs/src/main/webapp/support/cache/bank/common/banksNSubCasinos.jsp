<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html>
<head>
    <title>Remove Banks and SubCasinos</title>
</head>
<body>
<script language="JavaScript" type="text/javascript">

    var subCasinoNBanks = {
    <% for (Map.Entry<Long, SubCasino> entry : SubCasinoCache.getInstance().getAllObjects().entrySet()) { %>
    <%=entry.getKey()%> :
    [<% for (long bankId : entry.getValue().getBankIds()) { %><%=bankId%>, <% } %>],
            <% } %> 100000
    :
    [100000]
    }
    ;

    var banks = {
    <% for (Map.Entry<Long, BankInfo> entry : BankInfoCache.getInstance().getAllObjects().entrySet()) {%>
    <%=entry.getKey()%> :
    "<%=entry.getValue().getExternalBankIdDescription()%>",
            <% } %> 1000000
    :
    "null"
    }
    ;

    function selectSubCasino(oList) {
        var df = document.forms[0];
        var bankIds = df.bankId.options;
        selected = oList.selectedIndex;
        if (selected != -1) {
            value = oList.options[selected].value;
            bankIds.length = 0;
            if (value == "all") {
                i = 0;
                for (var bankId in banks) {
                    if (bankId != 1000000) {
                        df.bankId.options[i] = new Option(banks[bankId] + "=" + bankId, bankId);
                        i++;
                    }
                }
                //bankIds.length = ;
            } else {
                bankIds.length = subCasinoNBanks[value].length;
                for (i = 0; i < bankIds.length; i++) {
                    bankId = subCasinoNBanks[value][i];
                    df.bankId.options[i] = new Option(banks[bankId] + "=" + bankId, bankId);
                }
            }
        }
    }
</script>

<html:link href="/support/cache/bank/common/subcasinoSelect.jsp"> BACK </html:link>
<html:form action="/support/BankNSubCasinoControl">
    <html:select property="subCasinoId" size="40" onchange="selectSubCasino(this)">
        <html:option value="all"/>
        <html:option value=".."/>
        <html:optionsCollection property="subCasinoList"/>
    </html:select>

    <html:select property="bankId" size="40">
        <html:optionsCollection property="bankIdList"/>
    </html:select><br>
    <html:submit value="Remove SubCasino" property="button"/>
    <html:submit value="Remove Bank" property="button"/>
</html:form>
</body>
</html>
<%@ page import="com.abs.casino.account.AccountManager" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.abs.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.abs.casino.common.exception.CommonException" %>
<%@ page import="static org.apache.commons.lang.StringUtils.isNotBlank" %>

<%
    String userId = request.getParameter("userId");
    String currency = "EUR";
    String bankIdFromRequest = request.getParameter("bankId");
    if (isNotBlank(bankIdFromRequest)) {
        try {
            long bankId = Long.parseLong(bankIdFromRequest);
            long subCasinoId = SubCasinoCache.getInstance().getSubCasinoId(bankId);
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(subCasinoId, bankId, userId);
            if (accountInfo != null) {
                currency = accountInfo.getCurrency().getCode();
            } else if (bankId == 6274) {
                currency = "MMC";
            } else if (bankId == 6275) {
                currency = "MQC";
            }
        } catch (CommonException e) {
            e.printStackTrace();
        }
    }

%>
<EXTSYSTEM>
    <REQUEST>
        <USERID><%=userId %>
        </USERID>
        <HASH>13124234234234234234234</HASH>
    </REQUEST>
    <TIME>12 Jan 2004 15:15:15</TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <FIRSTNAME>BonusFirst</FIRSTNAME>
        <LASTNAME>BonusLast</LASTNAME>
        <EMAIL>BonusEmail</EMAIL>
        <USERNAME><%=userId %>
        </USERNAME>
        <CURRENCY><%=currency%></CURRENCY>
    </RESPONSE>
</EXTSYSTEM>

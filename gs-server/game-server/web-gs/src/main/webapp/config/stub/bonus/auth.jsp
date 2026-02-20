<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="static org.apache.commons.lang3.StringUtils.isNotBlank" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.account.AccountManager" %>

<%
    String token = request.getParameter("token");
    if (StringUtils.isTrimmedEmpty(token)) {
        throw new CommonException("Parameter 'token' not found");
    }
    String userId = token;
    String currency = "EUR";

    String bankIdFromRequest = request.getParameter("bankId");
    if (isNotBlank(bankIdFromRequest)) {
        try {
            long bankId = Long.parseLong(bankIdFromRequest);
            long subCasinoId = SubCasinoCache.getInstance().getSubCasinoId(bankId);
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(subCasinoId, bankId, userId);
            if (accountInfo != null) {
                currency = accountInfo.getCurrency().getCode();
            }
        } catch (CommonException e) {
            e.printStackTrace();
        }
    }
%>
<EXTSYSTEM>
    <REQUEST>
        <TOKEN><%=token%>
        </TOKEN>
        <HASH>13124234234234234234234</HASH>
    </REQUEST>
    <TIME>12 Jan 2004 15:15:15</TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <USERID><%=userId %>
        </USERID>
        <FIRSTNAME>BonusFirst</FIRSTNAME>
        <LASTNAME>BonusLast</LASTNAME>
        <EMAIL>BonusEmail</EMAIL>
        <USERNAME><%=userId%>
        </USERNAME>
        <CURRENCY><%=currency%>
        </CURRENCY>
    </RESPONSE>
</EXTSYSTEM>

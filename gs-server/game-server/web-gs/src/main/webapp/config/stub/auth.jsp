<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="static org.apache.commons.lang.StringUtils.isNotBlank" %>

<%
    String token = request.getParameter("token");
    if (StringUtils.isTrimmedEmpty(token)) {
        throw new CommonException("Parameter 'token' not found");
    }
    if ("genError".equals(token)) {
        throw new CommonException("getError token");
    }
    String userId = token;

    String currency = "EUR";

    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

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
        <TOKEN><%=token%>
        </TOKEN>
        <HASH>13124234234234234234234</HASH>
    </REQUEST>
    <TIME>12 Jan 2004 15:15:15</TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <USERID><%=userId %>
        </USERID>
        <FIRSTNAME><%=userId + " Fname"%>
        </FIRSTNAME>
        <LASTNAME><%=userId + " Lname"%>
        </LASTNAME>
        <EMAIL><%=userId %>
        </EMAIL>
        <USERNAME><%=userId %>
        </USERNAME>
        <CURRENCY><%=currency%>
        </CURRENCY>
        <BALANCE><%= extAccountInfo.getBalance()%>
        </BALANCE>
        <COUNTRYCODE>US</COUNTRYCODE>
        <BIRTH_DATE>23/07/1970</BIRTH_DATE>
        <GENDER>MALE</GENDER>
    </RESPONSE>
</EXTSYSTEM>

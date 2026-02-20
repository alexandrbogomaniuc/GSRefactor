<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.util.DigitFormatter" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="static org.apache.commons.lang3.StringUtils.isNotBlank" %>

<%
    String token = request.getParameter("token");
    if (StringUtils.isTrimmedEmpty(token)) {
        throw new CommonException("Parameter 'token' not found");
    }
    String userId = token;
    String currency = "EUR";

    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy hh:ss:mm");

    String bankIdFromRequest = request.getParameter("bankId");
    if (isNotBlank(bankIdFromRequest)) {
        try {
            long bankId = Long.parseLong(request.getParameter("bankId"));
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
        <HASH><%=token%>
        </HASH>
    </REQUEST>
    <TIME><%=dateFormatter.format(new Date())%>
    </TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <USERID><%=userId %>
        </USERID>
        <FIRSTNAME><%=userId %>
        </FIRSTNAME>
        <LASTNAME><%=userId %>
        </LASTNAME>
        <EMAIL><%=userId %>
        </EMAIL>
        <USERNAME><%=userId%>
        </USERNAME>
        <CURRENCY><%=currency%>
        </CURRENCY>
        <BALANCE><%=DigitFormatter.doubleToMoney(extAccountInfo.getBalance() / 100)%>
        </BALANCE>
        <GS><%=GameServer.getInstance().getServerId()%>
        </GS>
    </RESPONSE>
</EXTSYSTEM>

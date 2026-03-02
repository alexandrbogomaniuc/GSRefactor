<%@ page import="com.abs.casino.common.exception.CommonException" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.GameServer" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

<%
    String userId = request.getParameter("userId");
    if (StringUtils.isTrimmedEmpty(userId)) {
        throw new CommonException("Parameter 'userId' not found");
    }

    String casinoTransactionId = request.getParameter("casinoTransactionId");
    if (StringUtils.isTrimmedEmpty(casinoTransactionId)) {
        throw new CommonException("Parameter 'casinoTransactionId' not found");
    }

    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy hh:ss:mm");
%>
<EXTSYSTEM>
    <REQUEST>
        <USERID><%=userId%>
        </USERID>
        <CASINOTRANSACTIONID><%=casinoTransactionId%>
        </CASINOTRANSACTIONID>
        <HASH>8cb54b1924dbbd626a3b079a47527d17</HASH>
    </REQUEST>
    <TIME><%=dateFormatter.format(new Date())%>
    </TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <EXTSYSTEMTRANSACTIONID>154456456</EXTSYSTEMTRANSACTIONID>
        <GS><%=GameServer.getInstance().getServerId()%>
        </GS>
    </RESPONSE>
</EXTSYSTEM>

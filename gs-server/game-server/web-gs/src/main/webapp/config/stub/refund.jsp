<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CWError" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
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
    long betId = Long.parseLong(casinoTransactionId);
    String hash = request.getParameter("hash");
    CWError result = RemoteClientStubHelper.getInstance().refundBet(userId, betId);
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy hh:ss:mm");
%>
<EXTSYSTEM>
    <REQUEST>
        <USERID><%=userId%>
        </USERID>
        <CASINOTRANSACTIONID><%=casinoTransactionId%>
        </CASINOTRANSACTIONID>
        <HASH><%=hash%>
        </HASH>
    </REQUEST>
    <TIME><%=dateFormatter.format(new Date())%>
    </TIME>
    <RESPONSE>
        <RESULT><%=result == null ? "OK" : "FAILED"%>
        </RESULT>
        <%=
        result == null ? "<EXTSYSTEMTRANSACTIONID>" + userId + "_" + casinoTransactionId + "</EXTSYSTEMTRANSACTIONID>"
                : "<CODE>" + result.getCode() + "</CODE>"
        %>
    </RESPONSE>
</EXTSYSTEM>

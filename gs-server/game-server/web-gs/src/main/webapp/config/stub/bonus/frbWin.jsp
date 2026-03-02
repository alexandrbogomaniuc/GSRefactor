<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%
    String userId = request.getParameter("userId");
    String bonusId = request.getParameter("bonusId");
    String amount = request.getParameter("amount");
    String win = request.getParameter("win");
    String transactionId = request.getParameter("transactionId");

    if (!StringUtils.isTrimmedEmpty(win) && win.contains("|")) {
        amount = win.substring(0, win.indexOf("|"));
    }

    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

    long balance = extAccountInfo.getBalance() + Long.parseLong(amount);
    extAccountInfo.setBalance(balance);

%>


<EXTSYSTEM>
    <REQUEST>
        <USEDID><%=userId%>
        </USEDID>
        <BONUSID><%=bonusId%>
        </BONUSID>
        <AMOUNT><%=amount%>
        </AMOUNT>
        <TRANSACTIONID><%=transactionId%>
        </TRANSACTIONID>
        <HASH>1e3b0ae551b1dfdc48137bc50ad26d1c</HASH>
    </REQUEST>
    <TIME>18 Mar 2011 12:13:44</TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <BALANCE><%=balance%>
        </BALANCE>
    </RESPONSE>
</EXTSYSTEM>

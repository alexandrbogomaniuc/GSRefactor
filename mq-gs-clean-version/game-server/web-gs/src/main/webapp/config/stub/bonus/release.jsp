<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.exception.BonusException" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bonus.Bonus" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager" %>

<%
    String userId = request.getParameter("userId");
    String bonusId = request.getParameter("bonusId");
    String amount = request.getParameter("amount");
    String transactionId = request.getParameter("transactionId");

    Bonus bonus = BonusManager.getInstance().getById(Long.parseLong(bonusId));
    bankInfo = BankInfoCache.getInstance().getBankInfo(bonus.getBankId());

    List<String> paramList = new ArrayList<String>();
    paramList.add(userId);
    paramList.add(bonusId);
    paramList.add(amount);

    String hash = getHashValue(paramList);

    SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
    Date date = new Date();
    String timeStr = format.format(date);

    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

    long balance = extAccountInfo.getBalance() + Long.parseLong(amount);
    extAccountInfo.setBalance(balance);
%>


<EXTSYSTEM>
    <REQUEST>
        <USERID><%=userId%>
        </USERID>
        <BONUSID><%=bonusId%>
        </BONUSID>
        <AMOUNT><%=amount%>
        </AMOUNT>
        <TRANSACTIONID><%=transactionId%>
        </TRANSACTIONID>
        <HASH><%=hash%>
        </HASH>
    </REQUEST>
    <TIME><%=timeStr%>
    </TIME>
    <RESPONSE>
        <RESULT>OK</RESULT>
        <BALANCE><%=balance%>
        </BALANCE>
    </RESPONSE>
</EXTSYSTEM>


<%!
    BankInfo bankInfo;

    String getHashValue(List params) throws BonusException {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                sb.append(param);
            }
            sb.append(getBonusPass());

            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    protected String getBonusPass() {
        return bankInfo.getBonusPassKey();
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Resolve pending CT operations</title>
</head>
<body>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.abs.casino.common.cache.data.payment.transfer.PaymentTransaction" %>
<%@ page import="com.dgphoenix.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%

    long accId = -1;
    String bankIdStr = request.getParameter("bankId");
    int bankId = -1;
    String accStr = request.getParameter("accountId");
    if (!StringUtils.isTrimmedEmpty(bankIdStr)) {
        bankId = Integer.parseInt(bankIdStr);
    } else {
        if (!StringUtils.isTrimmedEmpty(accStr)) {
            accId = Long.parseLong(accStr);
        }
    }
    if (bankId == -1) {
        SessionHelper.getInstance().lock(accId);
    } else {
        SessionHelper.getInstance().lock(bankId, accStr);
    }
    try {
        SessionHelper.getInstance().openSession();
        PaymentTransaction operation = SessionHelper.getInstance().getTransactionData().getPaymentTransaction();
        if (operation != null) {
            if ("true".equals(request.getParameter("kill"))) {
                SessionHelper.getInstance().getTransactionData().setPaymentTransaction(null);
                response.getWriter().print("Operation was deleted: " + operation + "<br>");
                ThreadLog.info("Killed pending operation: " + operation);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } else { %>
Has pending CT transaction: <%=operation%>
<br>
<form action="pendingOperations.jsp" method="post">
    <input type="hidden" name="accountId" value="<%=accStr%>">
    <input type="hidden" name="bankId" value="<%=bankIdStr == null ? "" : bankIdStr%>">
    <input type="hidden" name="kill" value="true">
    <input type="submit" value="Kill"/>
</form>
<% }
} else {
    response.getWriter().print("Not have pending CT operations");
}
} finally {
    SessionHelper.getInstance().clearWithUnlock();
}
%>
</body>
</html>

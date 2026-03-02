<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="static com.google.common.base.Preconditions.*" %>
<%@ page import="com.abs.casino.common.SessionHelper" %>
<%@ page import="com.abs.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.abs.casino.account.AccountManager" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.config.HostConfiguration" %>

<%
    String extUserId = request.getParameter("extUserId");
    checkNotNull(extUserId, "External account id can't be null");
    String bankIdStr = request.getParameter("bankId");
    checkNotNull(bankIdStr, "bankId can't be null");
    String newBalanceAsString = request.getParameter("newBalance");
    Integer bankId = Integer.valueOf(bankIdStr);
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId.longValue());
    HostConfiguration hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);
    if (!hostConfiguration.isProductionCluster() && bankInfo.isStubMode()) {
        Long newBalance = newBalanceAsString == null ? null : Long.valueOf(newBalanceAsString);
        RemoteClientStubHelper.ExtAccountInfoStub stubAccountInfo = RemoteClientStubHelper.getInstance().getExtAccountInfo(extUserId);
        if (newBalance != null) {
            Long subcasinoId = SubCasinoCache.getInstance().getSubCasinoId(bankId.longValue());
            stubAccountInfo.setBalance(newBalance);
            try {
                SessionHelper.getInstance().lock(bankId, extUserId);
                SessionHelper.getInstance().openSession();
                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(subcasinoId.shortValue(), bankId, extUserId);
                accountInfo.setBalance(newBalance);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        response.getWriter().write("Account with extId = " + extUserId + " has balance = " + stubAccountInfo.getBalance());
    } else {
        response.getWriter().write("ERROR: Can not change balance on LIVE cluster or not stub bank!");
    }
%>



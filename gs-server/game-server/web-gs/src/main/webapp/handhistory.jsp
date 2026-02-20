<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringIdGenerator" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.web.history.GameHistoryURLBuilder" %>
<%@ page import="com.dgphoenix.casino.web.history.GameHistoryListAction" %>

<%!
    Integer getBankId(String sessionId) {
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            return null;
        }
        Pair<Integer, String> pair;
        try {
            pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
        } catch (Throwable ignore) {
            return null;
        }
        if (pair.getKey() != null) {
            return pair.getKey();
        }
        return null;
    }
%>

<%
    String sessionId = request.getParameter(GameHistoryListAction.SESSION_ID);
    Integer bankId = getBankId(sessionId);
    if (bankId == null) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        response.sendRedirect(baseUrl + "/error_pages/incorrect_parameters_error.jsp");
        return;
    }
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

    String historyPageUrl = GameHistoryURLBuilder
            .create(request, bankInfo, sessionId)
            .passRequestParameters(request)
            .build();

    response.sendRedirect(historyPageUrl);
%>
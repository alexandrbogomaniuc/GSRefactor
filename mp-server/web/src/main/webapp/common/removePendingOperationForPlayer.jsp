<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>
<%@ page import="com.betsoft.casino.mp.service.PendingOperationService" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger(PendingOperationService.class);
%>
<%
    LOG.debug("removePendingOperationForPlayer.jsp: query={}", request.getQueryString());
    String accountId = request.getParameter("accountId");
    LOG.debug("removePendingOperationForPlayer.jsp: accountId={}", accountId);
    PendingOperationService pendingOperationService = WebSocketRouter.getApplicationContext().getBean(PendingOperationService.class);

    if (!StringUtils.isTrimmedEmpty(accountId)) {
        pendingOperationService.remove(Long.parseLong(accountId));
        response.getWriter().println("Pending Operation removed for accountId=: " + accountId);
    } else {
        response.getWriter().println("accountId is null or empty");
    }
%>

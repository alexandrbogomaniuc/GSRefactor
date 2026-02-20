<%@ page import="org.apache.logging.log4j.LogManager" %>
<%@ page import="org.apache.logging.log4j.Logger" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final Logger LOG = LogManager.getLogger("com.betsoft.casino.mp.client.LogDebug");
%>
<%
    String logMessage = request.getParameter("logMessage");
    LOG.debug("logdebug.jsp: serverName={}, query={}, method={}, remoteAddr={}, logMessage='{}'",
            request.getServerName(), request.getQueryString(), request.getMethod(), request.getRemoteAddr(), logMessage);
%>

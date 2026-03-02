<%@ page import="com.abs.casino.common.cache.ServerConfigsTemplateCache" %>
<%@ page import="com.abs.casino.common.config.GameServerConfigTemplate" %>
<%@ page import="com.abs.casino.tracker.CurrencyUpdateProcessor" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%!
%><%
    ApplicationContext context = ApplicationContextHelper.getApplicationContext();
    RemoteCallHelper remoteCallHelper = context.getBean(RemoteCallHelper.class);
    GameServerConfigTemplate template = ServerConfigsTemplateCache.getInstance().getServerConfigTemplate();
    template.setProperty(CurrencyUpdateProcessor.USE_INTERNAL_PROPERTY, "true");
    remoteCallHelper.saveAndSendNotification(template);
%>
<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfo" %>
<%
    BaseGameInfoTemplate baseGameInfoTemplateById = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(829);
    BaseGameInfo defaultGameInfo = baseGameInfoTemplateById.getDefaultGameInfo();
    defaultGameInfo.setProperty("RTP", "97.5");
    RemoteCallHelper.getInstance().saveAndSendNotification(baseGameInfoTemplateById);
%>
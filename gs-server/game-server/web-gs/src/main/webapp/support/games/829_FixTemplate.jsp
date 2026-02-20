<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%
    BaseGameInfoTemplate baseGameInfoTemplateById = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(829);
    BaseGameInfo defaultGameInfo = baseGameInfoTemplateById.getDefaultGameInfo();
    defaultGameInfo.setProperty("RTP", "97.5");
    RemoteCallHelper.getInstance().saveAndSendNotification(baseGameInfoTemplateById);
%>
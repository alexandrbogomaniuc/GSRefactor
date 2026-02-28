<%@ page import="com.abs.casino.cassandra.persist.CassandraBaseGameInfoTemplatePersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.promo.battleground.BattlegroundConfig" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    CassandraPersistenceManager cpm = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraBaseGameInfoTemplatePersister persister = cpm.getPersister(CassandraBaseGameInfoTemplatePersister.class);
    long[] gameIds = { 864 };

    for(long gameId : gameIds) {
        BaseGameInfo baseGameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
        if( baseGameInfo != null) {
            baseGameInfo.setProperty(BaseGameConstants.KEY_MP_GAME_FOLDER_NAME, "game");
        }
    }

    persister.saveAll();

    response.getWriter().write("Done!");
%>

<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraBetPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraRoundGameSessionPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraTempBetPersister" %>
<%@ page import="com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager" %>
<%@ page import="org.apache.http.entity.ContentType" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.abs.casino.cassandra.PersisterDependencyInjector" %>
<%
    response.setContentType(ContentType.TEXT_PLAIN.getMimeType());
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getBean(CassandraPersistenceManager.class);
    CassandraBetPersister betPersister = persistenceManager.getPersister(CassandraBetPersister.class);
    CassandraRoundGameSessionPersister roundGameSessionPersister = betPersister.getRoundGameSessionPersister();
    PrintWriter writer = response.getWriter();
    if (roundGameSessionPersister == null) {
        writer.println("CassandraRoundGameSessionPersister: is null");
    } else {
        writer.println("CassandraRoundGameSessionPersister: is not null: " + roundGameSessionPersister);
    }
    CassandraTempBetPersister tempBetPersister = betPersister.getTempBetPersister();
    if (tempBetPersister == null) {
        writer.println("CassandraTempBetPersister: is null");
    } else {
        writer.println("CassandraTempBetPersister: is not null: " + tempBetPersister);
    }
    writer.println();

    PlayerBetPersistenceManager betPersistenceManager = ApplicationContextHelper.getBean(PlayerBetPersistenceManager.class);
    CassandraBetPersister persister = (CassandraBetPersister) betPersistenceManager.getPersister();
    CassandraRoundGameSessionPersister roundGameSessionPersister1 = persister.getRoundGameSessionPersister();
    if (roundGameSessionPersister1 == null) {
        writer.println("CassandraRoundGameSessionPersister(manager): is null");
    } else {
        writer.println("CassandraRoundGameSessionPersister(manager): is not null: " + roundGameSessionPersister1);
    }
    CassandraTempBetPersister tempBetPersister1 = persister.getTempBetPersister();
    if (tempBetPersister1 == null) {
        writer.println("CassandraTempBetPersister(manager): is null");
    } else {
        writer.println("CassandraTempBetPersister(manager): is not null: " + tempBetPersister1);
    }

    PersisterDependencyInjector pdi = ApplicationContextHelper.getBean(PersisterDependencyInjector.class);
    writer.println();
    writer.println(pdi.getDependencyMap());

%>

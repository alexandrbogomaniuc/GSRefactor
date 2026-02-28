<%@ page import="com.dgphoenix.casino.common.util.IdGenerator" %>
<%@ page import="com.abs.casino.promo.icon.TournamentIcon" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.promo.persisters.CassandraTournamentIconPersister" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%!
    CassandraPersistenceManager cpm =
            ApplicationContextHelper.getApplicationContext().getBean(CassandraPersistenceManager.class);
    CassandraTournamentIconPersister persister = cpm.getPersister(CassandraTournamentIconPersister.class);
%><%
    String name = request.getParameter("name");
    String httpAddress = request.getParameter("httpAddress");

    if (StringUtils.isTrimmedEmpty(name, httpAddress)) {
        response.getWriter().println("name or httpAddress was not specified!");
        return;
    }

    String idString = request.getParameter("id");
    long id = idString != null ? Long.parseLong(idString) : IdGenerator.getInstance().getNext(TournamentIcon.class);
    TournamentIcon icon = new TournamentIcon(id, name, httpAddress);
    persister.persist(icon);

    response.getWriter().println("Icon was saved: " + icon.toString());
%>


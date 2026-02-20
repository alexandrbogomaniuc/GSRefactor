<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.promo.persisters.CassandraTournamentIconPersister" %>
<%@ page import="com.dgphoenix.casino.promo.icon.TournamentIcon" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%!
    CassandraPersistenceManager cpm =
            ApplicationContextHelper.getApplicationContext().getBean(CassandraPersistenceManager.class);
    CassandraTournamentIconPersister persister = cpm.getPersister(CassandraTournamentIconPersister.class);
%><%
    long id = Long.parseLong(request.getParameter("id"));
    String name = request.getParameter("name");
    String httpAddress = request.getParameter("httpAddress");

    if (StringUtils.isTrimmedEmpty(name, httpAddress)) {
        response.getWriter().println("name or httpAddress was not specified!");
        return;
    }

    TournamentIcon icon = persister.getById(id);
    if (icon == null) {
        response.getWriter().println("Icon with such id is not found!");
        return;
    }

    icon.setName(name);
    icon.setHttpAddress(httpAddress);
    persister.persist(icon);

    response.getWriter().println("Icon was updated: " + icon.toString());
%>


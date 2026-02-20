<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%
    PrintWriter writer = response.getWriter();
    response.setContentType("text/plain");
    BaseGameInfoTemplateCache.getInstance().getDefGamesList().stream()
            .filter(BaseGameInfo::isEnabled)
            .sorted(Comparator.comparingLong(BaseGameInfo::getId))
            .forEach(bgi -> {
                int generation = bgi.getClientGeneration().getGeneration();
                writer.println(bgi.getId() + " = " + generation);
            });
%>

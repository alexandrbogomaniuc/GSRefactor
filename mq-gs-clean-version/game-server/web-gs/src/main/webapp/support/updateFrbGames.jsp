<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.StringTokenizer" %>
<%
    HashSet<Long> frbGames = new HashSet<Long>();
    String gids = request.getParameter("gids");

    if (gids != null && !gids.isEmpty()) {
        frbGames.addAll(BaseGameInfoTemplateCache.getInstance().getFrbGames());
        StringTokenizer st = new StringTokenizer(gids, ",");
        while (st.hasMoreTokens()) {
            long l = Long.parseLong(st.nextToken());
            if (!frbGames.contains(l)) {
                frbGames.add(l);
                response.getWriter().write("add game: " + l + "</br>");
            }
        }
        response.getWriter().write("new frbgames: " + frbGames.toString() + "</br>");
        BaseGameInfoTemplateCache.getInstance().setFrbGames(frbGames);

    } else
        response.getWriter().write("incorrect param gids=[gameId,...,gameId]" + "</br>");

%>
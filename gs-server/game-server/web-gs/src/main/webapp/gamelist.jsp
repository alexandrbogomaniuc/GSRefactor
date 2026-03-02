<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ page import="com.google.common.cache.CacheBuilder" %>
<%@ page import="com.google.common.cache.LoadingCache" %>
<%@ page import="com.google.common.cache.CacheLoader" %>
<%@ page import="com.abs.casino.common.web.statistics.StatisticsManager" %>
<%@ page import="java.util.*" %>
<%@ page import="com.abs.casino.common.web.statistics.IStatisticsGetter" %>

<%!
    static LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .recordStats()
            .refreshAfterWrite(20, TimeUnit.MINUTES)
            .build(new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String key) throws Exception {
                    List<String> result = new ArrayList<>();
                    Map<Long, BaseGameInfoTemplate> baseGameInfoTemplates = BaseGameInfoTemplateCache.getInstance().getAllObjects();
                    for (BaseGameInfoTemplate baseGameInfoTemplate : baseGameInfoTemplates.values()) {
                        Long gameId = baseGameInfoTemplate.getGameId();
                        String title = baseGameInfoTemplate.getTitle();
                        String repositoryFile = baseGameInfoTemplate.getDefaultGameInfo().getRepositoryFile();
                        result.add(title + ";" + gameId + ";" + repositoryFile);
                    }

                    Collections.sort(result);
                    return result;
                }
            });

    static {
        StatisticsManager.getInstance()
                .registerStatisticsGetter("/gamelist.jsp cache statistics", new IStatisticsGetter() {
                    @Override
                    public String getStatistics() {
                        return String.valueOf("size=" + cache.size() + ", stats=" + cache.stats());
                    }
                });
    }
%>

<%
    for (String str : cache.get("gameList")) {
        response.getWriter().write(str + "<br>");
    }
%>

<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.cache.data.game.GameGroup" %>
<%@ page import="java.util.*" %>
<%@ page import="com.abs.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.google.common.collect.Sets" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.common.cache.CacheBuilder" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ page import="com.google.common.cache.LoadingCache" %>
<%@ page import="com.google.common.cache.CacheLoader" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%!
    private static String getGameTitle(Long gameId) {
        BaseGameInfoTemplate tmpl = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);

        return tmpl != null ? tmpl.getTitle() : "Not found title for id=" + gameId;
    }

    private static final Set<GameGroup> GROUPS = Sets.immutableEnumSet(GameGroup.SLOTS, GameGroup.SOFT_GAMES, GameGroup.VIDEOPOKER,
            GameGroup.MULTIHAND_POKER, GameGroup.PYRAMID_POKER, GameGroup.TABLE, GameGroup.KENO, GameGroup.ACTION_GAMES);

    static class GameOutput {
        private Long id;
        private List<String> languages;
        private GameGroup group;
        private String title;

        GameOutput(Long id, List<String> languages, GameGroup group, String title) {
            this.id = id;
            this.languages = languages;
            this.group = group;
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public Collection<String> getLanguages() {
            return languages;
        }

        public GameGroup getGroup() {
            return group;
        }

        public String getTitle() {
            return title;
        }
    }

    static LoadingCache<Long, List<GameOutput>> cache = CacheBuilder.newBuilder().expireAfterAccess(7, TimeUnit.HOURS).maximumSize(70).
        recordStats().
        build(new CacheLoader<Long, List<GameOutput>>() {
            @Override
            public List<GameOutput> load(Long bankId) {
                return BaseGameCache.getInstance().getAllGameInfosAsMap(bankId, null).values().stream()
                    .filter(IBaseGameInfo::isEnabled)
                    .filter(e -> GROUPS.contains(e.getGroup()))
                    .map(e -> new GameOutput(e.getId(), e.getLanguages(), e.getGroup(), getGameTitle(e.getId())))
                    .collect(Collectors.toList());
            }
        });
%>
<%
    if ("true".equals(request.getParameter("stats"))) {
        response.getWriter().write(cache.stats()+"");
    } else if ("true".equals(request.getParameter("invalidate"))) {
        cache.invalidateAll();
        response.getWriter().write("invalidated");
    } else {
        long bankId = Long.parseLong(request.getParameter("bankId"));

        response.setContentType("text/json");
        response.getWriter().write(new Gson().toJson(cache.get(bankId)));
    }
%>
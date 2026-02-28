package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.cache.data.game.BaseGameConstants;
import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.ICreateGameListener;
import com.abs.casino.common.cache.IDistributedConfigCache;
import com.abs.casino.common.cache.data.game.MiniGameInfo;
import com.dgphoenix.casino.common.cache.data.game.*;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@com.abs.casino.common.cache.CacheKeyInfo(description = "game.id")
public class BaseGameInfoTemplateCache extends AbstractExportableCache<BaseGameInfoTemplate> implements IDistributedConfigCache {
    private static final Logger LOG = LogManager.getLogger(BaseGameInfoTemplateCache.class);
    private static final BaseGameInfoTemplateCache instance = new BaseGameInfoTemplateCache();
    private BaseGameInfoTemplate tournamentGameInfoTemplate;
    private BaseGameInfoTemplate networkTournamentGameInfoTemplate;
    private static final BaseGameInfo TOURNAMENT_GAME_INFO = new BaseGameInfo();
    private static final BaseGameInfo NETWORK_TOURNAMENT_GAME_INFO = new BaseGameInfo();

    //gameId->BaseGameInfoTemplate
    private NonBlockingHashMapLong<BaseGameInfoTemplate> games = new NonBlockingHashMapLong<>(1024);

    //gameName->gameId
    private final ConcurrentMap<String, Long> gamesByName = new ConcurrentHashMap<>(1024);

    private Set<Long> jackpotGames = null;
    private List<BaseGameInfo> defGamesList = null;
    private Set<Long> multiplayerGames = null;

    private final Set<Long> frbGames = Sets.newConcurrentHashSet();

    private final Set<ICreateGameListener> createGameListeners = new HashSet<>();

    private BaseGameInfoTemplateCache() {
    }

    public static BaseGameInfoTemplateCache getInstance() {
        return instance;
    }

    public void addListener(ICreateGameListener listener) {
        createGameListeners.add(listener);
    }

    public void init() {
        setUpTournamentGameInfo();
        setUpNetworkTournamentGameInfo();
    }

    private void setUpNetworkTournamentGameInfo() {
        int gameId = 0;
        String gameName = "TOURNAMENT_NETWORK_MP";
        NETWORK_TOURNAMENT_GAME_INFO.setId(gameId);
        NETWORK_TOURNAMENT_GAME_INFO.setGroup(GameGroup.ACTION_GAMES);
        NETWORK_TOURNAMENT_GAME_INFO.setName(gameName);
        networkTournamentGameInfoTemplate = new BaseGameInfoTemplate(gameId, gameName, null,
                NETWORK_TOURNAMENT_GAME_INFO, false, "");
        networkTournamentGameInfoTemplate.setTitle("Max Quest: Network Tournament");
    }

    private void setUpTournamentGameInfo() {
        int gameId = 1;
        String gameName = "TOURNAMENT_MP";
        TOURNAMENT_GAME_INFO.setId(gameId);
        TOURNAMENT_GAME_INFO.setGroup(GameGroup.ACTION_GAMES);
        TOURNAMENT_GAME_INFO.setName(gameName);
        tournamentGameInfoTemplate = new BaseGameInfoTemplate(gameId, gameName, null,
                TOURNAMENT_GAME_INFO, false, "");
        tournamentGameInfoTemplate.setTitle("Max Quest: Tournament");
    }

    public void clearCache() {
        games.clear();
        jackpotGames = null;
        multiplayerGames = null;
        defGamesList = null;
        gamesByName.clear();
    }

    @Override
    public void put(BaseGameInfoTemplate baseGameInfoTemplate) {
        if (baseGameInfoTemplate != null) {
            long gameId = baseGameInfoTemplate.getGameId();
            games.putIfAbsent(gameId, baseGameInfoTemplate);
            gamesByName.putIfAbsent(baseGameInfoTemplate.getGameName(), gameId);
            for (ICreateGameListener listener : createGameListeners) {
                try {
                    listener.notify(gameId);
                } catch (Exception e) {
                    LOG.error("error notify", e);
                }
            }
        }
    }

    public void remove(String id) {
        games.remove(Long.parseLong(id));
    }

    public Long getGameIdByName(final String name) {
        Long id = gamesByName.get(name);
        if (id == null) {
            BaseGameInfoTemplate template = Iterables.find(games.values(), new Predicate<BaseGameInfoTemplate>() {
                @Override
                public boolean apply(BaseGameInfoTemplate input) {
                    return name.equals(input.getGameName());
                }
            }, null);
            if (template != null) {
                return template.getId();
            }
        }
        return id;
    }

    public String getServletById(long id) {
        BaseGameInfoTemplate gameInfoTemplate = getObject(String.valueOf(id));
        if (gameInfoTemplate != null) {
            return gameInfoTemplate.getServlet();
        } else {
            return null;
        }
    }

    public String getGameNameById(long id) {
        BaseGameInfoTemplate baseGameInfoTemplate = getObject(String.valueOf(id));
        if (baseGameInfoTemplate != null) {
            return baseGameInfoTemplate.getGameName();
        } else {
            return null;
        }
    }

    public Set<Long> getMultiplayerGames() {
        Set<Long> localGames = multiplayerGames;
        if (localGames == null) {
            synchronized (this) {
                if ((localGames = multiplayerGames) == null) {
                    localGames = new HashSet<>();
                    for (Map.Entry<Long, BaseGameInfoTemplate> entry : games.entrySet()) {
                        BaseGameInfoTemplate template = entry.getValue();
                        if (template != null && template.isMultiplayerGame()) {
                            localGames.add(template.getGameId());
                        }
                    }
                    this.multiplayerGames = localGames;
                }
            }
        }
        return Collections.unmodifiableSet(localGames);
    }

    synchronized public void invalidateJackpotGames() {
        jackpotGames = null;
    }

    public List<BaseGameInfo> getDefGamesList() {
        List<BaseGameInfo> localDefGamesList = defGamesList;
        if (localDefGamesList == null) {
            synchronized (this) {
                if ((localDefGamesList = defGamesList) == null) {
                    localDefGamesList = new ArrayList<>();
                    for (Map.Entry<Long, BaseGameInfoTemplate> entry : games.entrySet()) {
                        BaseGameInfoTemplate template = entry.getValue();
                        if (template != null && template.getDefaultGameInfo() != null) {
                            localDefGamesList.add(template.getDefaultGameInfo());
                        }
                    }
                    this.defGamesList = localDefGamesList;
                }
            }
        }
        return Collections.unmodifiableList(localDefGamesList);
    }

    public BaseGameInfo getDefaultGameInfo(long gameId) {
        if (gameId == 0) {
            return NETWORK_TOURNAMENT_GAME_INFO;
        }
        if (gameId == 1) {
            return TOURNAMENT_GAME_INFO;
        }
        BaseGameInfoTemplate gameInfoTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        if (gameInfoTemplate != null) {
            return gameInfoTemplate.getDefaultGameInfo();
        } else {
            return null;
        }
    }

    public Integer getGameVariationId(long gameId, ClientType clientType) {
        return getBaseGameInfoTemplateById(gameId).getGameVariationId(clientType);
    }

    @Override
    public Map<Long, BaseGameInfoTemplate> getAllObjects() {
        return games;
    }

    public Set<Long> getAllGameIds() {
        Set<Long> gameIds = new HashSet<>();
        for (Long gameId : games.keySet()) {
            gameIds.add(gameId);
        }
        return gameIds;
    }

    @Override
    public BaseGameInfoTemplate getObject(String id) {
        return games.get(Long.valueOf(id));
    }

    public BaseGameInfoTemplate getBaseGameInfoTemplateById(long id) {
        if (id == 0) {
            return networkTournamentGameInfoTemplate;
        } else if (id == 1) {
            return tournamentGameInfoTemplate;
        }
        return getObject(String.valueOf(id));
    }

    @Override
    public int size() {
        return games.size();
    }

    @Override
    public String getAdditionalInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("[field:CSM] gamesDefaults has ").append(games.size()).append(" entries \n");
        builder.append("[field:CHM-local] jackpotGames has ").append(jackpotGames == null ? 0 : jackpotGames.size()).append(" entries \n");
        return builder.toString();
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        Collection<Map.Entry<Long, BaseGameInfoTemplate>> entries = games.entrySet();
        for (Map.Entry<Long, BaseGameInfoTemplate> entry : entries) {
            outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
        }
    }

    public void importEntry(ExportableCacheEntry entry) {
        synchronized (games) {
            if (entry.getValue() instanceof BaseGameInfoTemplate) {
                put((BaseGameInfoTemplate) entry.getValue());
            } else if (entry.getValue() instanceof SetOfLongsContainer) {
                setFrbGames(((SetOfLongsContainer) entry.getValue()).getFrbGames());
            } else {
                LOG.warn("Unsupported import object: {}",  entry.getValue());
            }
        }
    }

    public Set<Long> getFrbGames() {
        return Collections.unmodifiableSet(frbGames);
    }

    public void setFrbGames(Set<Long> frbgames) {
        frbGames.clear();
        frbGames.addAll(frbgames);
        for (Long frbgame : frbgames) {
            BaseGameInfoTemplate template = games.get(frbgame);
            if (template != null) {
                template.setFrbGame(true);
            }
        }
    }

    public void addFrbGame(Long id) {
        frbGames.add(id);
    }

    @Override
    public String printDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("gamesDefaults.size()=").append(games.size());
        sb.append(", jackpotGames.size()=").append(jackpotGames == null ? 0 : jackpotGames.size());
        sb.append(", gamesByName.size()=").append(gamesByName.size());
        sb.append(", frbGames.size()=").append(frbGames.size());
        return sb.toString();
    }

    public boolean isRequiredForImport() {
        return true;
    }

    public String getPlayerDeviceType(long gameId) {
        return games.get(gameId).getDefaultGameInfo().getProperty(BaseGameConstants.KEY_PLAYER_DEVICE_TYPE);
    }

    public List<Long> getGamesByGroup(final GameGroup groupName) {
        Collection<BaseGameInfoTemplate> allTemplates = Lists.newArrayList(games.values());
        return allTemplates.stream()
                .filter(template -> template.getDefaultGameInfo().getGroup() == groupName)
                .map(BaseGameInfoTemplate::getId)
                .collect(Collectors.toList());
    }

    public List<Long> getGamesByType(final GameType typeName) {
        Collection<BaseGameInfoTemplate> allTemplates = Lists.newArrayList(games.values());
        return allTemplates.stream()
                .filter(template -> template.getDefaultGameInfo().getGameType() == typeName)
                .map(BaseGameInfoTemplate::getId)
                .collect(Collectors.toList());
    }

    public void assertGameIsEnabled(long gameId) {
        BaseGameInfoTemplate template = getBaseGameInfoTemplateById(gameId);
        if (template == null
                || template.getDefaultGameInfo() == null
                || !template.getDefaultGameInfo().isEnabled()) {

            throw new IllegalStateException("Game: " + gameId + " is disabled. Template: " + template);
        }
    }

    private BaseGameInfoTemplate copyTemplate(MiniGameInfo miniGameInfo, BaseGameInfoTemplate baseGameInfoTemplate) {
        long miniGameId = miniGameInfo.getGameId();
        BaseGameInfo miniGameDefaultBGI = baseGameInfoTemplate.getDefaultGameInfo().copy();
        miniGameDefaultBGI.setId(miniGameId);
        miniGameDefaultBGI.setName(miniGameInfo.getName());
        BaseGameInfoTemplate miniGameTemplate = new BaseGameInfoTemplate(miniGameId, miniGameInfo.getName(),
                baseGameInfoTemplate.getGameVariationIdBase(), miniGameDefaultBGI,
                false, baseGameInfoTemplate.getServlet());
        miniGameTemplate.setTitle(miniGameInfo.getTitle());
        miniGameTemplate.setGameControllerClass(baseGameInfoTemplate.getGameControllerClass());
        miniGameTemplate.setEndRoundSignature(baseGameInfoTemplate.getEndRoundSignature());
        miniGameTemplate.setRoundFinishedHelper(baseGameInfoTemplate.getRoundFinishedHelper());
        miniGameTemplate.setSwfLocation(baseGameInfoTemplate.getSwfLocation());
        miniGameTemplate.setAdditionalParams(baseGameInfoTemplate.getAdditionalParams());
        miniGameTemplate.setOldTranslation(baseGameInfoTemplate.isOldTranslation());
        miniGameTemplate.setFrbGame(baseGameInfoTemplate.isFrbGame());
        return miniGameTemplate;
    }
}

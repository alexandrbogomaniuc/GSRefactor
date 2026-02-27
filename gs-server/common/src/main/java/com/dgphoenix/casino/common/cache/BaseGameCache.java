package com.dgphoenix.casino.common.cache;

import com.dgphoenix.casino.cassandra.persist.ICassandraBaseGameInfoPersister;
import com.dgphoenix.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.*;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.property.PropertyUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import one.util.streamex.EntryStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("rawtypes")
@CacheKeyInfo(description = "bank.id + game.id [ + currency.code + profile.id ]")
public class BaseGameCache extends AbstractExportableCache<BaseGameInfo>
        implements IDistributedConfigCache<BaseGameInfo>, ILoadingCache {
    private static final Logger LOG = LogManager.getLogger(BaseGameCache.class);
    private static final BaseGameCache instance = new BaseGameCache();

    private ICassandraBaseGameInfoPersister<BaseGameInfo> persister;
    private StreamPersister<String, BaseGameInfo> streamPersister;
    //bankId + gameId + currency.code->BaseGameInfo (specific)
    private LoadingCache<String, IBaseGameInfo> games;

    private BaseGameCache() {
    }

    public static BaseGameCache getInstance() {
        return instance;
    }

    public synchronized void initCache(ICassandraBaseGameInfoPersister<BaseGameInfo> persister,
                                       StreamPersister<String, BaseGameInfo> streamPersister, int maxCacheSize) {
        checkState(games == null, "ERROR: Runtime re-init is not currently supported.");
        this.persister = persister;
        this.streamPersister = streamPersister;
        games = createCache(maxCacheSize);

        StatisticsManager.getInstance()
                .registerStatisticsGetter("BaseGameCache statistics",
                        () -> "size=" + games.size() + ", stats=" + games.stats());
    }

    private LoadingCache<String, IBaseGameInfo> createCache(int maxCacheSize) {
        return CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .recordStats()
                .concurrencyLevel(8)
                .build(new CacheLoader<String, IBaseGameInfo>() {
                    @Override
                    public IBaseGameInfo load(String key) throws Exception {
                        long bankId = -1L;
                        StringTokenizer st = new StringTokenizer(key, ID_DELIMITER);
                        try {
                            bankId = Long.parseLong(st.nextToken());
                        } catch (Exception e) {
                            LOG.error("Cannot extract bankId, key={}", key);
                        }
                        if (bankId < 0) {
                            throw new CommonException("Negative bankId for key: " + key);
                        }
                        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                        if (bankInfo == null) {
                            LOG.warn("BankInfo not found, cannot load BaseGameInfo for key: " + key);
                            throw new CommonException("BankInfo not found for key: " + key);
                        }
                        IBaseGameInfo sharedInfo = getSharedCopy(key);
                        //master bank solution not compatible with game profile functionality
                        Long masterBankId = bankInfo.getMasterBankId();
                        if (masterBankId != null && masterBankId > 0 && bankId != masterBankId && sharedInfo == null) {
                            long gameId = -1L;
                            Currency currency = null;
                            try {
                                gameId = Long.parseLong(st.nextToken());
                            } catch (Exception e) {
                                LOG.error("Cannot extract gameId, key={}", key);
                            }
                            if (gameId < 0) {
                                throw new CommonException("gameId is negative for key: " + key);
                            }
                            if (st.hasMoreTokens()) {
                                String currencyCode = st.nextToken();
                                currency = CurrencyCache.getInstance().get(currencyCode);
                            }
                            BankInfo masterBankInfo = BankInfoCache.getInstance().getBankInfo(masterBankId);
                            if (masterBankInfo == null) {
                                LOG.warn("Master BankInfo not found, cannot load BaseGameInfo for key: {}, " +
                                        "masterBankId={}", key, masterBankId);
                                throw new CommonException("masterBankInfo not found for key: " + key);
                            }
                            if (currency == null) {
                                currency = bankInfo.getDefaultCurrency();
                            }
                            String masterBankKey = composeGameKey(masterBankId, gameId, currency);
                            BaseGameInfo masterBaseGameInfo = persister.get(masterBankKey);
                            if (masterBaseGameInfo == null) {
                                masterBankKey = composeGameKey(masterBankId, gameId,
                                        masterBankInfo.getDefaultCurrency());
                                masterBaseGameInfo = persister.get(masterBankKey);
                            }
                            if (masterBaseGameInfo == null) {
                                throw new CommonException("masterBaseGameInfo not found for key: " + key);
                            }
                            BaseGameInfo masterCopyGameInfo = copy(masterBaseGameInfo);

                            return new ImmutableBaseGameInfoWrapper(bankId, masterCopyGameInfo,
                                    bankInfo.isEnabled(), masterCopyGameInfo.getProperties(),
                                    masterCopyGameInfo.getLanguages(),
                                    masterCopyGameInfo.getLimit(),
                                    masterCopyGameInfo.getCoins() == null ?
                                            null : new ArrayList<>(masterCopyGameInfo.getCoins()));
                        }
                        if (sharedInfo != null) {
                            return sharedInfo;
                        } else {
                            throw new CommonException("sharedInfo not found for key: " + key);
                        }
                    }
                });
    }

    public IBaseGameInfo putToLocalCacheIfAbsent(final String key, final IBaseGameInfo gameInfo) {
        try {
            return games.get(key, () -> {
                if (gameInfo != null) {
                    IBaseGameInfo localGameInfo = gameInfo.lightCopy();
                    localGameInfo.setProperties(extractProperties(gameInfo));
                    localGameInfo.setLimit(extractLimit(gameInfo));
                    localGameInfo.setCoins(extractCoins(gameInfo));
                    return localGameInfo;
                }
                return null;
            });
        } catch (ExecutionException e) {
            LOG.error("put error for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void put(BaseGameInfo gameInfo) {
        String key = composeGameKey(gameInfo);
        putToLocalCacheIfAbsent(key, gameInfo);
    }

    public void putProfiled(BaseGameInfo gameInfo, String profile) {
        gameInfo.setProperty(BaseGameConstants.KEY_PROFILE_ID, profile);
        String key = composeGameKeyProfiled(gameInfo.getBankId(), gameInfo.getId(), gameInfo.getCurrency(), profile);
        putToLocalCacheIfAbsent(key, gameInfo);
    }

    public void remove(long bankId, long gameId, ICurrency currency) {
        remove(composeGameKey(bankId, gameId, currency));
    }

    @Override
    public void invalidate(String key) {
        games.invalidate(key);
    }

    public void invalidateAll() {
        games.invalidateAll();
    }

    @Override
    public void remove(String key) {
        games.invalidate(key);
        persister.delete(key);
    }

    public boolean isExist(long bankId, long gameId, ICurrency currency) {
        return isExistStrict(bankId, gameId, currency) || isExistStrict(bankId, gameId, null);
    }

    public boolean isExistStrict(long bankId, long gameId, ICurrency currency) {
        return getGameInfoShared(bankId, gameId, currency) != null;
    }

    public boolean isExistStrictNotMobile(final long bankId, final long gameId, final ICurrency currency) {
        IBaseGameInfo baseGameInfo = getGameInfoShared(bankId, gameId, currency);
        return baseGameInfo != null && !baseGameInfo.isMobile();
    }

    public IBaseGameInfo getGameInfoShared(long bankId, long gameId, ICurrency currency) {
        return getGameInfoShared(composeGameKey(bankId, gameId, currency));
    }

    public IBaseGameInfo getGameInfoShared(final String key) {
        try {
            return games.get(key);
        } catch (ExecutionException ignored) {
            return null;
        }
    }

    /**
     * Check of strict existence is for mobile games
     *
     * @param bankId   - players bankId
     * @param gameId   - starting gameId
     * @param currency - players currency, variable can't be null
     * @return true if game is exists else false
     */
    public boolean isExistStrictMobile(long bankId, long gameId, ICurrency currency) {
        IBaseGameInfo baseGameInfo = getGameInfoShared(bankId, gameId, currency);
        return baseGameInfo != null && baseGameInfo.isMobile();
    }

    /**
     * @param bankId   - players bankId
     * @param gameId   - starting gameId
     * @param currency - players currency, variable can't be null
     * @return gameInfo for specified parameters
     * <p/>
     * isExist must be called in order to initialize localGameCache game mapping
     */
    public IBaseGameInfo getGameInfoById(final long bankId, final long gameId, final ICurrency currency) {
        IBaseGameInfo gameInfo = getGameInfoShared(bankId, gameId, currency);
        if (gameInfo == null) {
            gameInfo = getGameInfoShared(bankId, gameId, null);
        }
        return gameInfo;
    }

    public IBaseGameInfo getGameInfoByIdProfiled(long bankId, long gameId, ICurrency currency, String profile) {
        LOG.info("profiled gameInfo requested: bankId={}, gameId={}, currency={}, profile={}", bankId, gameId,
                currency, profile);
        IBaseGameInfo gameInfo = getGameInfoShared(composeGameKeyProfiled(bankId, gameId, currency, profile));

        if (gameInfo == null) {
            gameInfo = getGameInfoShared(composeGameKey(bankId, gameId, currency));
        }

        if (gameInfo == null) {
            Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            gameInfo = getGameInfoShared(composeGameKeyProfiled(bankId, gameId, defaultCurrency, profile));
        }

        if (gameInfo == null) {
            gameInfo = getGameInfoById(bankId, gameId, currency);
        }
        LOG.info("gameInfo returned: {}", gameInfo);
        return gameInfo;
    }

    public IBaseGameInfo getGameInfoByName(long bankId, String gameName) {
        Long gameId = getGameIdByName(gameName);
        return gameId == null ? null : getGameInfoById(bankId, gameId, null);
    }

    public Long getGameIdByName(String name) {
        return BaseGameInfoTemplateCache.getInstance().getGameIdByName(name);
    }

    public Map<Long, IBaseGameInfo> getAllGameInfosAsMap(long bankId, ICurrency currency) {
        List<IBaseGameInfo> gameInfos = getGameInfosByBankAndCurrency(bankId, currency);

        return gameInfos.stream()
                .filter(Objects::nonNull)
                .filter(input -> !isProfiled(input))
                .collect(Collectors.toMap(Identifiable::getId, Function.identity()));
    }

    private boolean isProfiled(IBaseGameInfo input) {
        return !StringUtils.isTrimmedEmpty(input.getProperty(BaseGameConstants.KEY_PROFILE_ID));
    }

    public Set<Long> getAllGamesSet(long bankId, Currency currency) {
        return getAllGameInfosAsMap(bankId, currency).keySet();
    }

    public Set<Long> getAllMobileGamesSet(long bankId, ICurrency currency) {
        return EntryStream.of(getAllGameInfosAsMap(bankId, currency))
                .filterValues(IBaseGameInfo::isMobile)
                .keys()
                .toSet();
    }

    public Map<Long, IBaseGameInfo> getAllMobileGamesSet(long bankId, ICurrency currency, final PlayerDeviceType deviceType) {
        Predicate<IBaseGameInfo> deviceTypeFilter = checkDeviceType(deviceType);
        return getGameInfosByBankAndCurrency(bankId, currency).stream()
                .filter(deviceTypeFilter)
                .collect(Collectors.toMap(Identifiable::getId, Function.identity()));
    }

    private Predicate<IBaseGameInfo> checkDeviceType(PlayerDeviceType deviceType) {
        return bgi -> {
            if (bgi != null && !isProfiled(bgi)) {
                PlayerDeviceType bgiDeviceType = bgi.getPlayerDeviceType();
                if (bgiDeviceType != null && bgiDeviceType.equals(deviceType)) {
                    return true;
                }
                if (bgiDeviceType == null) {
                    return PropertyUtils.getBooleanProperty(bgi.getProperties(),
                            BaseGameConstants.KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS);
                }
            }
            return false;
        };
    }

    public Set<Long> getAllNotMobileGamesSet(long bankId, ICurrency currency) {
        return EntryStream.of(getAllGameInfosAsMap(bankId, currency))
                .filterValues(bgi -> !bgi.isMobile())
                .keys()
                .toSet();
    }

    public Collection<IBaseGameInfo> getAllGameInfosAsCollection(long bankId, ICurrency currency) {
        return getAllGameInfosAsMap(bankId, currency).values();
    }

    public String getGameNameById(long bankId, long gameId) {
        IBaseGameInfo gameInfo = getGameInfoById(bankId, gameId, null);
        return gameInfo == null ? null : gameInfo.getName();
    }

    public List<IBaseGameInfo> getGameInfosByGroup(final long bankId, final GameGroup groupName, final ICurrency currency) {
        final Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
        return getGameInfosByBank(bankId).stream()
                .filter(Objects::nonNull)
                .filter(input -> {
                    ICurrency inputCurrency = input.getCurrency();
                    return input.getGroup() == groupName && (inputCurrency.equals(currency) || inputCurrency.equals(defaultCurrency));
                })
                .collect(Collectors.toList());
    }

    public List<IBaseGameInfo> getGameInfosByGroup(long bankId, GameGroup groupName, ICurrency currency,
                                                   final Set<Long> skippedGameIds) {
        checkNotNull(skippedGameIds, "skippedGameIds is null");
        return getGameInfosByGroup(bankId, groupName, currency).stream()
                .filter(input -> !skippedGameIds.contains(input.getId()))
                .collect(Collectors.toList());
    }

    public List<IBaseGameInfo> getGameInfosByType(final long bankId, final GameType typeName) {
        Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
        return getGameInfosByBankAndCurrency(bankId, defaultCurrency).stream()
                .filter(Objects::nonNull)
                .filter(input -> input.getGameType() == typeName)
                .collect(Collectors.toList());
    }

    protected List<IBaseGameInfo> getGameInfosByBank(long bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            return Collections.emptyList();
        }
        Long masterBankId = bankInfo.getMasterBankId();
        if (masterBankId != null && masterBankId > 0 && bankId != masterBankId) {
            BankInfo masterBank = BankInfoCache.getInstance().getBankInfo(masterBankId);
            List<BaseGameInfo> mergedList = masterBank == null ? new ArrayList<>() :
                    persister.getByBank(masterBankId);
            List<IBaseGameInfo> resultList = new ArrayList<>(mergedList);
            List<BaseGameInfo> overrideList = persister.getByBank(bankId);
            if (!overrideList.isEmpty()) {
                resultList.addAll(overrideList);
            }
            List<IBaseGameInfo> gameInfoList = new ArrayList<>(resultList.size());
            for (IBaseGameInfo gameInfo : resultList) {
                try {
                    IBaseGameInfo cacheCopy = getGameInfo(bankId, gameInfo.getId(), bankInfo.getDefaultCurrency());
                    if (cacheCopy != null) {
                        gameInfoList.add(cacheCopy);
                    }
                } catch (Exception e) {
                    LOG.error("Cannot load BaseGameInfo, bankId={}, gameId={}", bankId, gameInfo.getId(), e);
                }
            }
            return gameInfoList;
        } else {
            List<BaseGameInfo> gameInfoList = persister.getByBank(bankId);
            return new ArrayList<>(putAllToLocalCache(gameInfoList));
        }
    }

    /**
     * Be aware that the logic of this method is different depending on whether a bank represented by {@param bankId}
     * is a slave or not.
     * For an ordinary bank it returns games configured only for {@param bankId} and {@param currency}.
     * For a slave it returns games configured directly for {@param bankId} and {@param currency} and, besides,
     * games configured for the master bank and its default currency.
     */
    protected List<IBaseGameInfo> getGameInfosByBankAndCurrency(long bankId, ICurrency currency) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            return Collections.emptyList();
        }
        Map<Long, IBaseGameInfo> gamesMap = new HashMap<>();
        Long masterBankId = bankInfo.getMasterBankId();
        if (masterBankId != null && masterBankId > 0 && bankId != masterBankId) {
            BankInfo masterBank = BankInfoCache.getInstance().getBankInfo(masterBankId);
            if (masterBank != null) {
                List<BaseGameInfo> mergedList = persister.getByBankAndCurrency(masterBankId, masterBank.getDefaultCurrency());
                for (BaseGameInfo gameInfo : mergedList) {
                    gamesMap.put(gameInfo.getId(), gameInfo);
                }
            }
            ICurrency gameCurrency = currency != null ? currency : BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            List<BaseGameInfo> slaveList = persister.getByBankAndCurrency(bankId, gameCurrency);
            for (BaseGameInfo gameInfo : slaveList) {
                gamesMap.put(gameInfo.getId(), gameInfo);
            }
            List<IBaseGameInfo> gameInfoList = new ArrayList<>(gamesMap.size());
            for (Entry<Long, IBaseGameInfo> entry : gamesMap.entrySet()) {
                try {
                    IBaseGameInfo cacheCopy = getGameInfo(bankId, entry.getKey(), currency);
                    if (cacheCopy != null) {
                        gameInfoList.add(cacheCopy);
                    }
                } catch (Exception e) {
                    LOG.error("Cannot load BaseGameInfo, bankId={}, gameId={}, currency={}",
                            bankId, entry.getKey(), currency, e);
                }

            }
            return gameInfoList;
        } else {
            ICurrency gameCurrency = currency != null ? currency : BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            List<BaseGameInfo> gameInfoList = persister.getByBankAndCurrency(bankId, gameCurrency);
            return new ArrayList<>(putAllToLocalCache(gameInfoList));
        }
    }

    private List<IBaseGameInfo> putAllToLocalCache(List<BaseGameInfo> gameInfoList) {
        List<IBaseGameInfo> result = new ArrayList<>(gameInfoList.size());
        for (IBaseGameInfo baseGameInfo : gameInfoList) {
            result.add(putToLocalCacheIfAbsent(composeGameKey(baseGameInfo), baseGameInfo));
        }
        return result;
    }

    public GameType getGameTypeById(long bankId, long gameId, ICurrency currency) throws CommonException {
        IBaseGameInfo gameInfo = getGameInfoById(bankId, gameId, currency);
        if (gameInfo == null) {
            throw new CommonException("BaseGameInfo not found for: bankId=" + bankId + ", gameId=" + gameId +
                    ", currency=" + currency);
        }

        return gameInfo.getGameType();
    }

    protected IBaseGameInfo getSharedCopy(String key) {
        BaseGameInfo sharedGameInfo = persister.get(key);
        //miniGames code removed
        if (sharedGameInfo != null) {
            return copy(sharedGameInfo);
        }
        return null;
    }


    private Map<String, String> extractProperties(IBaseGameInfo sharedGameInfo) {
        Map<String, String> localProperties = null;

        Map<String, String> sharedProperties = sharedGameInfo.getPropertiesMap();
        if (sharedProperties != null) {
            localProperties = new HashMap<>(sharedProperties);
        }

        BaseGameInfo defaultGameInfo = getDefaultGameInfo(sharedGameInfo.getId());
        if (defaultGameInfo != null) {
            Map<String, String> defaultProperties = defaultGameInfo.getPropertiesMap();
            if (defaultProperties != null) {
                if (localProperties == null) {
                    localProperties = new HashMap<>();
                }

                for (Entry<String, String> entry : defaultProperties.entrySet()) {
                    String propKey = entry.getKey();
                    if (!localProperties.containsKey(propKey)) {
                        localProperties.put(propKey, entry.getValue());
                    }
                }
            }
        }

        return localProperties;
    }

    private List<Coin> extractCoins(IBaseGameInfo sharedGameInfo) {
        if (sharedGameInfo.getVariableType() != GameVariableType.COIN) {
            return Collections.emptyList();
        }

        if (hasCoins(sharedGameInfo)) {
            return Coin.copyCoins(sharedGameInfo.getCoins());
        }

        long bankId = sharedGameInfo.getBankId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null || CollectionUtils.isEmpty(bankInfo.getCoins())) {
            bankInfo = BankInfoCache.getInstance().getDefaultBank();
        }

        return Coin.copyCoins(bankInfo.getCoins());
    }

    private Limit extractLimit(IBaseGameInfo sharedGameInfo) {
        if (sharedGameInfo.getVariableType() != GameVariableType.LIMIT) {
            return null;
        }

        if (hasLimit(sharedGameInfo)) {
            return (Limit) sharedGameInfo.getLimit();
        }

        long bankId = sharedGameInfo.getBankId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null || bankInfo.getLimit() == null) {
            bankInfo = BankInfoCache.getInstance().getDefaultBank();
        }

        return bankInfo.getLimit();
    }

    private BaseGameInfo getDefaultGameInfo(long gameId) {
        return BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
    }

    public IBaseGameInfo getGameInfo(long bankId, long gameId, ICurrency currency) {
        return getGameInfoShared(bankId, gameId, currency);
    }

    public IBaseGameInfo getGameInfo(long bankId, long gameId, String currencyCode) {
        return getGameInfoShared(composeGameKey(bankId, gameId, currencyCode));
    }

    private boolean hasLimitsOrCoins(IBaseGameInfo gameInfo) {
        return hasLimit(gameInfo) || hasCoins(gameInfo);
    }

    private boolean hasCoins(IBaseGameInfo gameInfo) {
        return gameInfo.hasCoins() && gameInfo.getVariableType() == GameVariableType.COIN;
    }

    private boolean hasLimit(IBaseGameInfo gameInfo) {
        return gameInfo.hasLimit() && gameInfo.getVariableType() == GameVariableType.LIMIT;
    }

    public String composeGameKey(IBaseGameInfo info) {
        String profile = info.getProperty(BaseGameConstants.KEY_PROFILE_ID);
        if (StringUtils.isTrimmedEmpty(profile)) {
            return composeGameKey(info.getBankId(), info.getId(), info.getCurrency());
        } else {
            return composeGameKeyProfiled(info.getBankId(), info.getId(), info.getCurrency(), profile);
        }
    }

    public String composeGameKey(long bankId, long gameId, ICurrency currency) {
        String defaultKey = bankId + ID_DELIMITER + gameId;
        return currency == null || currency.isDefault(bankId) ? defaultKey : defaultKey + ID_DELIMITER +
                currency.getCode();
    }

    public String composeGameKey(long bankId, long gameId, String currencyCode) {
        String defaultKey = bankId + ID_DELIMITER + gameId;
        return StringUtils.isTrimmedEmpty(currencyCode) ? defaultKey : defaultKey + ID_DELIMITER +
                currencyCode;
    }

    public String composeGameKeyProfiled(long bankId, long gameId, ICurrency currency, String profileId) {
        String key = bankId + ID_DELIMITER + gameId + ID_DELIMITER;
        key += (currency == null || currency.isDefault(bankId) ? "" : currency.getCode()) + ID_DELIMITER;
        if (!StringUtils.isTrimmedEmpty(profileId)) {
            key += profileId;
        }
        return key;
    }

    public String getGameProperty(long bankId, long gameId, String key, ICurrency currency) {
        if (!StringUtils.isTrimmedEmpty(key)) {
            IBaseGameInfo gameInfo = getGameInfoById(bankId, gameId, currency);
            if (gameInfo != null) {
                return gameInfo.getProperty(key);
            }
        }

        return null;
    }

    public IBaseGameInfo setGameProperty(long bankId, long gameId, ICurrency currency, String key, String value) {
        if (!StringUtils.isTrimmedEmpty(key)) {
            IBaseGameInfo gameInfo = getGameInfo(bankId, gameId, currency);
            if (gameInfo != null) {
                gameInfo.setProperty(key, value);
            }
            return gameInfo;
        }
        return null;
    }

    public String getExternalGameId(long originalGameId, long bankId) {
        Currency defBankCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
        IBaseGameInfo gameInfo = getGameInfoById(bankId, originalGameId, defBankCurrency);
        if (gameInfo == null) {
            return null;
        } else {
            return gameInfo.getExternalId();
        }
    }

    public Long getOriginalGameId(String externalGameId, long bankId) {
        return ExternalGameIdsCache.getInstance().getOriginalId(externalGameId, bankId);
    }

    public void putExternalGameIdCache(long originalGameId, String externalGameId, long bankId) {
        ExternalGameIdsCache.getInstance().putExternalGameId(originalGameId, externalGameId, bankId);
    }

    public void removeExternalGameId(String externalGameId, long bankId) {
        ExternalGameIdsCache.getInstance().remove(externalGameId, bankId);
    }

    @Override
    public void exportEntries(final ObjectOutputStream outStream) throws IOException {
        streamPersister.processAll(new CacheExportProcessor<BaseGameInfo>(outStream));
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream, Long bankId) throws IOException {
        streamPersister.processByCondition(new CacheExportProcessor<BaseGameInfo>(outStream), "byBank", bankId);
    }

    public void exportEntries(final ObjectOutputStream outStream, List<Long> bankIds) throws IOException {
        for (Long bankId : bankIds) {
            streamPersister.processByCondition(new CacheExportProcessor<BaseGameInfo>(outStream), "byBank", bankId);
        }
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        try {
            BaseGameInfo gameInfo = (BaseGameInfo) entry.getValue();
            long bankId = gameInfo.getBankId();
            if (!BankInfoCache.getInstance().isExist(bankId)) {
                LOG.warn("Attempt to import game for nonexistent bank, skip, game: {}", gameInfo);
                return;
            }
            if (StringUtils.isTrimmedEmpty(gameInfo.getProfileId())) {
                put(gameInfo);
            } else {
                String key = composeGameKeyProfiled(gameInfo.getBankId(), gameInfo.getId(), gameInfo.getCurrency(), gameInfo.getProfileId());
                putToLocalCacheIfAbsent(key, gameInfo);
            }
        } catch (Throwable e) {
            LOG.debug("Cannot put entry to cache: {}", entry, e);
            throw new IllegalStateException("Cannot put entry to cache", e);
        }

    }

    private Map<String, BaseGameInfo> getAllSharedObjects() {
        return persister.getAllAsMap();
    }

    @Override
    public int size() {
        return getAllObjects().size();
    }

    @Override
    public IBaseGameInfo getObject(String id) {
        return getGameInfoShared(id);
    }

    @Override
    public Map<String, IBaseGameInfo> getAllObjects() {
        return ImmutableMap.copyOf(games.asMap());
    }

    @Override
    public String getAdditionalInfo() {
        return games.stats().toString();
    }

    @Override
    public boolean isRequiredForImport() {
        return true;
    }

    @Override
    public String printDebug() {
        return "games.size()=" + size();
    }

    private BaseGameInfo copy(BaseGameInfo baseGameInfo) {
        BaseGameInfo copy = baseGameInfo.lightCopy();
        copy.setProperties(extractProperties(baseGameInfo));
        copy.setLimit(extractLimit(baseGameInfo));
        copy.setCoins(extractCoins(baseGameInfo));
        return copy;
    }
}

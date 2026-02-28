/**
 * User: flsh
 * Date: 08.07.2009
 */
package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.cache.data.bank.BankConstants;
import com.abs.casino.common.cache.IDistributedConfigCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.StreamUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@CacheKeyInfo(description = "bank.id")
public class BankInfoCache extends AbstractExportableCache<BankInfo> implements IDistributedConfigCache, ICurrencyRateMultiplierRetriever {
    private static final Logger LOG = LogManager.getLogger(BankInfoCache.class);
    private static final BankInfoCache instance = new BankInfoCache();

    // bankId -> BankInfo
    private final NonBlockingHashMapLong<BankInfo> banks = new NonBlockingHashMapLong<>();
    private final ConcurrentMap<String, Long> externalBankToBankId = new ConcurrentHashMap<>();
    private final BankPartnerIdCache partnerIdCache = new BankPartnerIdCache();
    private LoadingCache<Long, Set<Long>> frbGamesByBank;
    private LoadingCache<Long, CurrencyRateMultiplierContainer> bankCurrencyRateMultipliers;

    public static BankInfoCache getInstance() {
        return instance;
    }

    private BankInfoCache() {
    }

    public void init() throws CommonException {
        LOG.info("init started");
        BankInfo defaultBank = getDefaultBank();
        if (defaultBank == null) {
            LOG.info("init creating default bank");
            Limit defaultLimit = LimitsCache.getInstance().getLimit(1l);
            List<Coin> defaultCoins = new ArrayList<>();
            defaultCoins.add(Coin.getByValue(10));
            defaultCoins.add(Coin.getByValue(25));
            defaultCoins.add(Coin.getByValue(50));
            defaultCoins.add(Coin.getByValue(100));
            defaultCoins.add(Coin.getByValue(200));
            defaultCoins.add(Coin.getByValue(500));

            defaultBank = create(BankConstants.DEFAULT_BANK_ID,
                    String.valueOf(BankConstants.DEFAULT_BANK_ID), "DEFAULT BANK",
                    CurrencyCache.getInstance().get("EUR"), defaultLimit, defaultCoins);
            put(defaultBank);
            LOG.info("init creating default bank completed");
        }
        frbGamesByBank = createFrbGamesCache(1000);
        bankCurrencyRateMultipliers = createCurrencyRateMultipliersCache();
        LOG.info("init completed");
    }

    private LoadingCache<Long, Set<Long>> createFrbGamesCache(int maxCacheSize) {
        return CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .recordStats()
                .concurrencyLevel(8)
                .build(new CacheLoader<Long, Set<Long>>() {
                    @Override
                    public Set<Long> load(Long bankId) throws Exception {
                        BankInfo bankInfo = getBankInfo(bankId);
                        Function<String, Set<Long>> gameIdParser = parseGameIds(bankInfo);

                        Set<Long> frbGamesWhitelist = gameIdParser.apply(BankInfo.KEY_FRB_GAMES_ENABLE);
                        if (!frbGamesWhitelist.isEmpty()) {
                            return frbGamesWhitelist;
                        }

                        Set<Long> frbGamesBlacklist = gameIdParser.apply(BankInfo.KEY_FRB_GAMES_DISABLE);
                        return BaseGameInfoTemplateCache.getInstance().getFrbGames().stream()
                                .map(gameId -> BaseGameCache.getInstance().getGameInfoById(bankId, gameId, bankInfo.getDefaultCurrency()))
                                .filter(Objects::nonNull)
                                .map(IBaseGameInfo::getId)
                                .filter(gameId -> !frbGamesBlacklist.contains(gameId))
                                .collect(Collectors.toSet());
                    }
                });
    }

    private LoadingCache<Long, CurrencyRateMultiplierContainer> createCurrencyRateMultipliersCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .concurrencyLevel(8)
                .build(new CurrencyRateMultiplierLoader(this));
    }

    private Function<String, Set<Long>> parseGameIds(BankInfo bankInfo) {
        return propertyName -> {
            String value = bankInfo.getStringProperty(propertyName);
            if (value != null) {
                return StreamUtils.asStream(Splitter.on("|").split(value))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
            } else {
                return Collections.emptySet();
            }
        };
    }

    public Long getSubCasinoId(Long bankId) {
        BankInfo bankInfo = getBankInfo(bankId);
        return bankInfo == null ? null : bankInfo.getSubCasinoId();
    }

    public BankInfo create(long bankId, String externalBankId, String externalBankIdDescription,
                           Currency defaultCurrency, Limit limit,
                           List<Coin> coins) throws CommonException {
        limit = LimitsCache.getInstance().getLimit(limit.getId());
        if (limit == null) {
            LOG.debug("Limit is null");
            throw new CommonException("Limit is not initialized");
        }

        if (CollectionUtils.isEmpty(coins)) {
            LOG.debug("Coins are empty");
            throw new CommonException("Coins is not initialized");
        }

        return new BankInfo(bankId, externalBankId, externalBankIdDescription, defaultCurrency, limit, coins);
    }

    public void put(BankInfo bankInfo) {
        long bankId = bankInfo.getId();
        if (banks.putIfAbsent(bankId, bankInfo) != null) {
            frbGamesByBank.invalidate(bankId);
        }
        if (!StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())) {
            externalBankToBankId.putIfAbsent(bankInfo.getExternalBankId(), bankId);
        }
        String partnerId = bankInfo.getPartnerId();
        partnerIdCache.put(partnerId, (int) bankId);
    }

    public void remove(String id) {
        remove(Long.parseLong(id));
    }

    public void remove(long bankId) {
        if (isExist(bankId)) {
            String externalBankId = getExternalBankId(bankId);
            this.externalBankToBankId.remove(externalBankId);
            if (this.banks.remove(bankId) != null) {
                frbGamesByBank.invalidate(bankId);
                partnerIdCache.remove((int) bankId);
            }
        }
    }

    public void invalidateFrbGamesForBank(long bankId) {
        LOG.debug("invalidateFrbGamesForBank: Bank with id = {}", bankId);
        if (frbGamesByBank.getIfPresent(bankId) != null) {
            frbGamesByBank.invalidate(bankId);
        }
    }

    public boolean isExist(long bankId) {
        return banks.containsKey(bankId);
    }

    public BankInfo getBankInfo(long bankId) {
        return banks.get(bankId);
    }

    public BankInfo getByExternalBankId(String externalBankId) {
        Long bankId = externalBankToBankId.get(externalBankId);
        return bankId == null ? null : getBankInfo(bankId);
    }

    public String getExternalBankId(long bankId) {
        BankInfo bankInfo = getBankInfo(bankId);
        return bankInfo == null ? null : bankInfo.getExternalBankId();
    }

    public Long getBankId(String externalBankId) {
        return externalBankToBankId.get(externalBankId);
    }

    public void setExternalBankIdToBankId(String externalBankId, long bankId) {
        externalBankToBankId.put(externalBankId, bankId);
    }

    public BankInfo getDefaultBank() {
        return getBankInfo(BankConstants.DEFAULT_BANK_ID);
    }

    public Set<Long> getBankIds() {
        Set<Long> bankIds = new HashSet<>(banks.keySet());
        if (!bankIds.isEmpty()) {
            bankIds.remove(BankConstants.DEFAULT_BANK_ID);
            return bankIds;
        }
        long defaultBankId = BankConstants.DEFAULT_BANK_ID;
        for (Map.Entry<Long, BankInfo> entry : banks.entrySet()) {
            Long bankId = entry.getKey();
            if (defaultBankId == bankId) {
                continue;
            }
            bankIds.add(bankId);
        }
        return bankIds;
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        Collection<Map.Entry<Long, BankInfo>> entries = banks.entrySet();
        for (Map.Entry<Long, BankInfo> entry : entries) {
            outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
        }
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream, Long bankId) throws IOException {
        Collection<Map.Entry<Long, BankInfo>> entries = banks.entrySet();
        for (Map.Entry<Long, BankInfo> entry : entries) {
            if (bankId == null || bankId.equals(entry.getKey()) || entry.getKey() == 0) {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
            }
        }
    }

    public void exportEntry(ObjectOutputStream outStream, List<Long> bankIds) throws IOException {
        for (Long bankId : bankIds) {
            final BankInfo bankInfo = banks.get(bankId);
            if (bankInfo != null) {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(bankId), bankInfo));
            }
        }

    }

    public BankInfo getBank(String extBankId, long subCasinoId) {
        if (StringUtils.isTrimmedEmpty(extBankId)) {
            return null;
        }

        List<Long> banks = SubCasinoCache.getInstance().getBankIds(subCasinoId);
        if (CollectionUtils.isEmpty(banks)) {
            return null;
        }

        for (Long bankId : banks) {
            String externalBankId = getExternalBankId(bankId);
            if (externalBankId != null) {
                externalBankId = externalBankId.trim();
            }
            if (extBankId.equals(externalBankId)) {
                return getBankInfo(bankId);
            }
        }

        return null;
    }

    public BankInfo getBankInfoByDomainName(Short subCasinoId, String domainName) {
        List<Long> banks = SubCasinoCache.getInstance().getBankIds(subCasinoId);
        for (long bankId : banks) {
            BankInfo bankInfo = getBankInfo(bankId);
            if (bankInfo == null) {
                LOG.error("getBankInfoByDomainName: Bank with id={}, not found, but exist in banks for subCasino={}, domainName={}",
                        bankId, subCasinoId, domainName);
                continue;
            }
            String bankDomainName = bankInfo.getBankDomainName();
            if (!StringUtils.isTrimmedEmpty(bankDomainName) && bankDomainName.equals(domainName)) {
                return bankInfo;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return banks.size();
    }

    @Override
    public BankInfo getObject(String id) {
        return banks.get(Long.valueOf(id));
    }

    @Override
    public Map<Long, BankInfo> getAllObjects() {
        return banks;
    }

    @Override
    public String getAdditionalInfo() {
        return "[field:CSM] externalBankToBankId has " + externalBankToBankId.size() + " entries";
    }

    @Override
    public String printDebug() {
        return "banks.size()=" + banks.size() + ", externalBankToBankId.size()=" + externalBankToBankId.size();
    }

    public boolean isRequiredForImport() {
        return true;
    }

    public Set<Long> getFrbGames(BankInfo bankInfo) {
        Set<Long> frbGamesSet;
        try {
            long time = System.currentTimeMillis();
            frbGamesSet = frbGamesByBank.get(bankInfo.getId());
            StatisticsManager.getInstance().updateRequestStatistics("getFrbGames", System.currentTimeMillis() - time);
        } catch (ExecutionException ignored) {
            LOG.error("Can not get frb games set for bankId = {}", bankInfo.getId());
            frbGamesSet = new HashSet<>();
        }
        return frbGamesSet;
    }

    public Integer getBankIdByPartnerId(@NotNull String partnerId) {
        return partnerIdCache.getBankId(partnerId);
    }

    @Override
    public int getCurrencyRateMultiplier(long bankId, String currencyCode) {
        return bankCurrencyRateMultipliers.getUnchecked(bankId).getMultiplier(currencyCode);
    }

    public void invalidateCurrencyRateMultipliers(long bankId) {
        bankCurrencyRateMultipliers.invalidate(bankId);
    }

}

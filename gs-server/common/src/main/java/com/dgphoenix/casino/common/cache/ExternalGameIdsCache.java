package com.dgphoenix.casino.common.cache;


import com.dgphoenix.casino.common.cache.data.IdObject;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.regex.Pattern;

@CacheKeyInfo(description = "externalGameId + bank.id")
public class ExternalGameIdsCache extends AbstractLazyLoadingExportableCache<String, IdObject> {
    public static final IdObject NONE = new IdObject() {
        @Override
        public String toString() {
            return "NONE";
        }
    };
    private static final ExternalGameIdsCache instance = new ExternalGameIdsCache();

    private ExternalGameIdsCache() {
    }

    public static ExternalGameIdsCache getInstance() {
        return instance;
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        put(entry.getKey(), (IdObject) entry.getValue());
    }

    @Override
    public void put(IdObject entry) {
        throw new UnsupportedOperationException();
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void putExternalGameId(long originalGameId, String extGameId, long bankId) {
        Currency defBankCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfo(bankId, originalGameId, defBankCurrency);
        if (gameInfo == null) return;
        if (!StringUtils.isTrimmedEmpty(gameInfo.getExternalId())) {
            String oldKey = composeKey(gameInfo.getExternalId(), bankId);
            remove(oldKey);
        }
        gameInfo.setExternalId(extGameId);
        put(composeKey(extGameId, bankId), new IdObject(originalGameId));
    }

    public Long getOriginalId(String externalGameId, long bankId) {
        IdObject object = get(composeKey(externalGameId, bankId));
        return object == null || object == NONE ? null : object.getId();
    }

    @Override
    public void remove(String key) {
        delete(key, get(key));
    }

    public void remove(String externalGameId, long bankId) {
        remove(composeKey(externalGameId, bankId));
    }

    public static String composeKey(String externalGameId, long bankId) {
        if (externalGameId == null) return null;
        return externalGameId + ID_DELIMITER + bankId;
    }

    public static Pair<String, Integer> parseKey(String key) {
        if (StringUtils.isTrimmedEmpty(key)) {
            return null;
        }
        String[] split = key.split(Pattern.quote(ID_DELIMITER));
        if (split.length != 2) {
            return null;
        }
        try {
            Integer bankId = Integer.valueOf(split[1]);
            return new Pair<>(split[0], bankId);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public IdObject get(String id) {
        return super.get(id);
    }

    @Override
    public IdObject getObject(String id) {
        return super.get(id);
    }

    @Override
    public String getAdditionalInfo() {
        return "[field:CSM] extGameIds has " + size() + " entries";
    }

    @Override
    public String printDebug() {
        return "extGameIds.size()=" + size();
    }

    @Override
    public boolean isRequiredForImport() {
        return true;
    }
}

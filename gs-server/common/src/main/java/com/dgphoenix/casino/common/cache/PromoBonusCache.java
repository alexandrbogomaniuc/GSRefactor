package com.abs.casino.common.cache;

import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.data.bonus.PromoBonus;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.logkit.ThreadLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 3/29/12
 */
public class PromoBonusCache extends AbstractExportableCache<PromoBonus> {
    private static final Logger LOG = LogManager.getLogger(PromoBonusCache.class);
    private static final PromoBonusCache instance = new PromoBonusCache();

    // bonusId->PromoBonus
    private final ConcurrentMap<Long, PromoBonus> bonuses = new ConcurrentHashMap();

    private PromoBonusCache() {
    }

    public static PromoBonusCache getInstance() {
        return instance;
    }

    @Override
    public void put(PromoBonus bonus) throws CommonException {
        bonuses.putIfAbsent(bonus.getId(), bonus);
    }

    public void remove(Long id) {
        LOG.info("remove: " + id);
        bonuses.remove(id);
    }

    @Override
    public PromoBonus getObject(String id) {
        return bonuses.get(Long.valueOf(id));
    }

    public PromoBonus getById(long id) {
        return bonuses.get(id);
    }

    @Override
    public Map<Long, PromoBonus> getAllObjects() {
        return bonuses;
    }

    @Override
    public int size() {
        return bonuses.size();
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        synchronized (bonuses) {
            Set<Map.Entry<Long, PromoBonus>> entries = bonuses.entrySet();
            for (Map.Entry<Long, PromoBonus> entry : entries) {
                try {
                    final ExportableCacheEntry exportableCacheEntry = new ExportableCacheEntry(entry.getKey().toString(),
                            entry.getValue());
                    //LOG.info("exportableCacheEntry: " + exportableCacheEntry.toString());
                    outStream.writeObject(exportableCacheEntry);
                } catch (Exception e) {
                    ThreadLog.error("[export] cannot export promo bonus: " + entry.getKey(), e);
                }
            }
        }
    }

    @Override
    public String getAdditionalInfo() {
        return NO_INFO;
    }

    @Override
    public String printDebug() {
        return NO_INFO;
    }
}

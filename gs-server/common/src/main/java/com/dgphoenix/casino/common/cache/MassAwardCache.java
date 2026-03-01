package com.abs.casino.common.cache;

import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.IDistributedConfigCache;
import com.dgphoenix.casino.common.cache.data.bonus.BaseMassAward;
import com.abs.casino.common.exception.BonusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 11.07.13
 */

@com.abs.casino.common.cache.CacheKeyInfo(description = "massAward.id")
public class MassAwardCache extends AbstractExportableCache<BaseMassAward> implements IDistributedConfigCache {
    private static final Logger LOG = LogManager.getLogger(MassAwardCache.class);
    // massAwardId-> BaseMassAward
    private final ConcurrentMap<Long, BaseMassAward> massAwards = new ConcurrentHashMap<>();

    // bankId-> List of BaseMassAwardIds
    private final ConcurrentMap<Long, List<Long>> massAwardsByBankId = new ConcurrentHashMap<>();

    private static final MassAwardCache instance = new MassAwardCache();

    private MassAwardCache() {
    }

    public static MassAwardCache getInstance() {
        return instance;
    }

    public void put(BaseMassAward massAward) throws BonusException {
        if (!massAwards.containsKey(massAward.getId())) {

            List<Long> banks = massAward.getBankIds();
            LOG.debug("put creating massAward: id={}, banks={}, template={}, status={}", massAward.getId(), banks,
                    massAward.getTemplate(), massAward.getStatus());
            massAwards.putIfAbsent(massAward.getId(), massAward);

            for (Long bank : banks) {
                List<Long> massAwardIds = getByBankId(bank);
                if (massAwardIds != null) {
                    if (!massAwardIds.contains(massAward.getId())) {
                        massAwardIds.add(massAward.getId());
                    }
                } else {
                    massAwardIds = new ArrayList<>();
                    massAwardIds.add(massAward.getId());
                    massAwardsByBankId.put(bank, massAwardIds);
                }
                LOG.debug("put creating massAward for bank={}", bank);
            }
            LOG.debug("put creating bonusId={} completed", massAward.getId());
        } else {
            throw new BonusException("MassAward already exist!");
        }
    }

    public BaseMassAward getById(long massAwardId) {
        return massAwards.get(massAwardId);
    }

    public List<Long> getByBankId(long bankId) {
        return massAwardsByBankId.get(bankId);
    }

    public boolean isExist(long massAwardId) {
        return massAwards.containsKey(massAwardId);
    }

    public void remove(long massAwardId) {
        BaseMassAward massAward = getById(massAwardId);

        if (massAward != null) {
            LOG.debug("remove masAwardId={}", massAwardId);
            massAwards.remove(massAwardId);
            List<Long> banks = massAward.getBankIds();
            for (Long bank : banks) {
                List<Long> massAwardIds = getByBankId(bank);
                if (massAwardIds != null) {
                    massAwardIds.remove(massAward.getId());
                }
            }
            LOG.debug("remove massAwardId={} completed", massAwardId);
        }
    }

    @Override
    public int size() {
        return massAwards.size();
    }

    @Override
    public BaseMassAward getObject(String id) {
        return massAwards.get(Long.valueOf(id));
    }

    @Override
    public Map<Long, BaseMassAward> getAllObjects() {
        return massAwards;
    }

    @Override
    public String getAdditionalInfo() {
        return "";
    }

    public synchronized void clear() {
        massAwards.clear();
        massAwardsByBankId.clear();
    }

    @Override
    public String printDebug() {
        return "massAwards.size()=" + massAwards.size();
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        try {
            final BaseMassAward massAward = (BaseMassAward) entry.getValue();
            put(massAward);
        } catch (Throwable e) {
            LOG.error("Cannot put entry to cache: " + entry, e);
            throw new IllegalStateException("Cannot put entry to cache", e);
        }
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        synchronized (massAwards) {
            Set<Map.Entry<Long, BaseMassAward>> entries = massAwards.entrySet();
            for (Map.Entry<Long, BaseMassAward> entry : entries) {
                try {
                    final ExportableCacheEntry exportableCacheEntry = new ExportableCacheEntry(entry.getKey().toString(),
                            entry.getValue());
                    outStream.writeObject(exportableCacheEntry);
                } catch (Exception e) {
                    LOG.error("[export] cannot export bonus: " + entry.getKey(), e);
                }
            }
        }

    }

}


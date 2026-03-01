package com.abs.casino.common.cache;

import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.IDistributedConfigCache;
import com.abs.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: plastical
 * Date: 19.05.2010
 */

@com.abs.casino.common.cache.CacheKeyInfo(description = "subCasino.id")
public class SubCasinoCache extends AbstractExportableCache<SubCasino> implements IDistributedConfigCache {
    private static final Logger LOG = LogManager.getLogger(SubCasinoCache.class);

    private final ConcurrentMap<Long, SubCasino> subCasinosMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, SubCasino> byDomainNameMap = new ConcurrentHashMap<>();
    //key - bankId, value - subCasinoId;
    private final ConcurrentMap<Long, Long> bankToSubCasinoIdMap = new ConcurrentHashMap<>();

    public static SubCasinoCache getInstance() {
        return instance;
    }

    private static final SubCasinoCache instance = new SubCasinoCache();

    private SubCasinoCache() {
    }

    public synchronized void put(long subCasinoId, long bankId, boolean isDefault) {
        if (!isExist(subCasinoId, bankId)) {
            final SubCasino subCasino = subCasinosMap.get(subCasinoId);
            if (subCasino == null) {
                throw new RuntimeException("SubCasino not found, id=" + subCasinoId);
            }
            if (subCasino.getBankIds().isEmpty()) {
                subCasino.setBankIds(new ArrayList<Long>());
            }
            subCasino.addBankId(bankId);
            if (isDefault) {
                subCasino.setDefaultBank(bankId);
            }
            bankToSubCasinoIdMap.put(bankId, subCasinoId);
        } else {
            LOG.warn("Cannot put new Bank: " + bankId + ", to subCasino: " + subCasinoId + ", already exist");
        }
    }

    public synchronized void remove(long subCasinoId) {
        LOG.warn("remove: " + subCasinoId);
        subCasinosMap.remove(subCasinoId);
    }

    public boolean isExist(long subCasinoId, long bankId) {
        List<Long> banks = getBankIds(subCasinoId);
        return !CollectionUtils.isEmpty(banks) && banks.contains(bankId);
    }

    public synchronized void remove(long subCasinoId, long bankId) {
        LOG.warn("remove: " + subCasinoId + ", bankId=" + bankId);
        if (isExist(subCasinoId, bankId)) {
            List<Long> banks = getBankIds(subCasinoId);
            banks.remove(bankId);
            bankToSubCasinoIdMap.remove(bankId);
        }
    }

    public SubCasino getSubCasinoByDomainName(String domainName) {
        final SubCasino subCasino = byDomainNameMap.get(domainName);
        if (subCasino == null) {
            LOG.error("SubCasino not found: " + domainName);
            //throw new RuntimeException("SubCasino not found: " + domainName);
        }
        return subCasino;
    }

    public SubCasino get(long subCasinoId) {
        return subCasinosMap.get(subCasinoId);
    }

    public List<Long> getBankIds(long subCasinoId) {
        final SubCasino subCasino = subCasinosMap.get(subCasinoId);
        return subCasino == null ? null : subCasino.getBankIds();
    }

    public Long getSubCasinoId(Long bankId) {
        return bankToSubCasinoIdMap.get(bankId);
    }

    public Long getDefaultBankId(long subCasinoId) {
        return subCasinosMap.get(subCasinoId).getDefaultBank();
    }

    public void setDefaultBankId(long subCasinoId, long bankId) {
        final SubCasino subCasino = subCasinosMap.get(subCasinoId);
        subCasino.setDefaultBank(bankId);
    }

    public boolean isDefaultBankId(long subCasinoId, long bankId) {
        return bankId == getDefaultBankId(subCasinoId);
    }

    @Override
    public SubCasino getObject(String id) {
        return subCasinosMap.get(Long.valueOf(id));
    }

    @Override
    public Map<Long, SubCasino> getAllObjects() {
        return subCasinosMap;
    }

    @Override
    public int size() {
        return subCasinosMap.size();
    }

    @Override
    public String getAdditionalInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("filed [CSM] byDomainNameMap:").append(StringUtils.printProperties(byDomainNameMap));
        return builder.toString();
    }

    @Override
    public String printDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("subCasinosMap.size()=").append(subCasinosMap.size());
        sb.append(", byDomainNameMap.size()=").append(byDomainNameMap.size());
        return sb.toString();
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        for (SubCasino subCasino : subCasinosMap.values()) {
            outStream.writeObject(new ExportableCacheEntry(String.valueOf(subCasino.getId()), subCasino));
        }
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream, Long bankId) throws IOException {
        for (SubCasino subCasino : subCasinosMap.values()) {
            if (subCasino.getDefaultBank() == bankId) {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(subCasino.getId()), subCasino));
            }
        }
    }

    public void exportEntry(ObjectOutputStream outStream, SubCasino subCasino) throws IOException {
        outStream.writeObject(new ExportableCacheEntry(String.valueOf(subCasino.getId()), subCasino));
    }

    @Override
    public void put(SubCasino subCasino) throws CommonException {
        SubCasino result = subCasinosMap.put(subCasino.getId(), subCasino);
        if (result != null) {
            List<String> domainNames = result.getDomainNames();
            if (!CollectionUtils.isEmpty(domainNames)) {
                if (subCasino.getDomainNames() != null) {
                    domainNames = new ArrayList<>(domainNames);
                    domainNames.removeAll(subCasino.getDomainNames());
                }
                for (String domainName : domainNames) {
                    if (!StringUtils.isTrimmedEmpty(domainName)) {
                        byDomainNameMap.remove(domainName);
                    }
                }
            }
            List<Long> bankIds = result.getBankIds();
            if (!CollectionUtils.isEmpty(bankIds)) {
                if (subCasino.getBankIds() != null) {
                    bankIds = new ArrayList<>(bankIds);
                    bankIds.removeAll(subCasino.getBankIds());
                }
                for (Long bankId : bankIds) {
                    bankToSubCasinoIdMap.remove(bankId);
                }
            }
        }
        final List<String> domainNames = subCasino.getDomainNames();
        if (domainNames != null && !domainNames.isEmpty()) {
            for (String domainName : domainNames) {
                if (!StringUtils.isTrimmedEmpty(domainName)) {
                    byDomainNameMap.put(domainName, subCasino);
                }
            }
        }
        final List<Long> bankIds = subCasino.getBankIds();
        if (bankIds != null) {
            for (Long bankId : bankIds) {
                bankToSubCasinoIdMap.put(bankId, subCasino.getId());
            }
        } else {
            LOG.error("put: strange SubCasino, bankIds is null: " + subCasino);
        }
    }

    public boolean isRequiredForImport() {
        return true;
    }
}

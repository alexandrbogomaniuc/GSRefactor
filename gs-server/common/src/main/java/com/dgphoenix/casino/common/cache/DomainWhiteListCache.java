package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.IDistributedConfigCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@com.abs.casino.common.cache.CacheKeyInfo(description = "domain.id")
public class DomainWhiteListCache extends AbstractExportableCache<DomainWhiteList> implements IDistributedConfigCache {
    // gameId -> whiteList
    private final ConcurrentMap<Integer, DomainWhiteList> domains = new ConcurrentHashMap<>();
    private static final DomainWhiteListCache instance = new DomainWhiteListCache();

    public static DomainWhiteListCache getInstance() {
        return instance;
    }

    private DomainWhiteListCache() {

    }

    public boolean isContainsDomain(int gameId, String domain) {
        DomainWhiteList whiteList = getDomainWhiteList(gameId);
        if (whiteList != null) {
            List<String> domainList = whiteList.getDomainList();
            return domainList != null && domainList.contains(domain);
        } else {
            return false;
        }
    }

    public void addDomainIfAbsent(int gameId, String domain) {
        DomainWhiteList whiteList = getDomainWhiteList(gameId);
        if (whiteList == null) {
            whiteList = new DomainWhiteList();
            whiteList.setGameId(gameId);
        }
        whiteList.addDomainIfAbsent(domain);
        put(whiteList);
    }

    public void removeDomain(int gameId, String domain) {
        DomainWhiteList whiteList = getDomainWhiteList(gameId);
        if (whiteList != null) {
            whiteList.removeDomain(domain);
        }
    }

    @Override
    public void put(DomainWhiteList domainWhiteList) {
        if (domainWhiteList != null) {
            domains.putIfAbsent(domainWhiteList.getGameId(), domainWhiteList);
        }
    }

    public void remove(String id) {
        //nop
    }

    public DomainWhiteList getDomainWhiteList(int gameId) {
        return domains.get(gameId);
    }

    @Override
    public Object getObject(String id) {
        return domains.get(Integer.valueOf(id));
    }

    @Override
    public Map<Integer, DomainWhiteList> getAllObjects() {
        return domains;
    }

    @Override
    public int size() {
        return domains.size();
    }

    @Override
    public String getAdditionalInfo() {
        return "";
    }

    @Override
    public String printDebug() {
        return "domains.size()=" + domains.size();
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        synchronized (domains) {
            Collection<Map.Entry<Integer, DomainWhiteList>> entries = domains.entrySet();
            for (Map.Entry<Integer, DomainWhiteList> entry : entries) {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
            }
        }
    }

    public boolean isRequiredForImport() {
        return true;
    }

}

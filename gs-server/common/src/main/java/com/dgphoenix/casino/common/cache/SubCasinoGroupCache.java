package com.dgphoenix.casino.common.cache;


import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.data.bank.SubCasinoGroup;
import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by inter on 01.06.15.
 */

@com.abs.casino.common.cache.CacheKeyInfo(description = "subCasinoGroup.name")
public class SubCasinoGroupCache extends AbstractExportableCache<SubCasinoGroup> {
    private static final Logger LOG = Logger.getLogger(SubCasinoGroupCache.class);

    private final ConcurrentMap<String, SubCasinoGroup> subCasinosGroupMap = new ConcurrentHashMap<>();

    private static final SubCasinoGroupCache instance = new SubCasinoGroupCache();

    public static SubCasinoGroupCache getInstance() {
        return instance;
    }

    private SubCasinoGroupCache() {

    }

    @Override
    public void put(SubCasinoGroup subCasinoGroup) throws CommonException {
        if (subCasinosGroupMap.containsKey(subCasinoGroup.getName())) {
            throw new CommonException("SubCasinoGroup already exist, name=" + subCasinoGroup.getName());
        }
        if (subCasinoGroup.getName() == null) {
            throw new CommonException("Cannot add SubCasinoGroup with empty name");
        }
        subCasinosGroupMap.put(subCasinoGroup.getName(), subCasinoGroup);
    }

    @Override
    public SubCasinoGroup getObject(String id) {
        return subCasinosGroupMap.get(id);
    }

    @Override
    public Map getAllObjects() {
        return subCasinosGroupMap;
    }

    @Override
    public int size() {
        return subCasinosGroupMap.size();
    }

    @Override
    public String getAdditionalInfo() {
        StringBuilder sb = new StringBuilder("[field:CSM] subCasinosGroupMap.size=" +
                subCasinosGroupMap.size() + ": ");
        for (Map.Entry<String, SubCasinoGroup> entry : subCasinosGroupMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    @Override
    public String printDebug() {
        return "subCasinosGroupMap.size()=" + subCasinosGroupMap.size();
    }

    @Override
    public synchronized void exportEntries(ObjectOutputStream outStream) throws IOException {
        for (SubCasinoGroup subCasinoGroup : subCasinosGroupMap.values()) {
            outStream.writeObject(new ExportableCacheEntry(String.valueOf(subCasinoGroup.getName()), subCasinoGroup));
        }
    }

    @Override
    public boolean isRequiredForImport() {
        return false;
    }

    public synchronized void remove(String name) {
        subCasinosGroupMap.remove(name);
    }

    public SubCasinoGroup getBySubCasinoId(long subCasinoId) {
        for (SubCasinoGroup group : subCasinosGroupMap.values()) {
            if (group.getSubCasinoList() != null && group.getSubCasinoList().contains(subCasinoId)) {
                return group;
            }
        }
        return null;
    }
}

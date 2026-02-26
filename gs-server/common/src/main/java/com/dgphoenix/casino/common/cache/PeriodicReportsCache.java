package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.cache.data.report.PeriodicReportInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ReportPeriodEnum;
import com.dgphoenix.casino.common.util.ReportTypeEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CacheKeyInfo(description = "gameServer.id + bank.id + reportPeriod.name + ReportType.name")
public class PeriodicReportsCache extends AbstractDistributedCache<PeriodicReportInfo> {

    //key - gameServerId+bankId+ReportPeriodEnum+ReportTypeEnum
    private final ConcurrentMap<String, PeriodicReportInfo> reportInfo = new ConcurrentHashMap();

    private static PeriodicReportsCache instance = new PeriodicReportsCache();

    public static PeriodicReportsCache getInstance() {
        return instance;
    }

    private PeriodicReportsCache() {
    }

    public PeriodicReportInfo getReportInfo(long gameServerId, long bankId, ReportPeriodEnum period,
                                            ReportTypeEnum type)
            throws CommonException {
        String key = composeKey(gameServerId, bankId, period, type);
        PeriodicReportInfo info = reportInfo.get(key);
        if (info == null) {
            put(new PeriodicReportInfo(gameServerId, bankId, period, type));
            info = reportInfo.get(key);
        }
        return info;
    }

    public void clear() {
        reportInfo.clear();
    }

    @Override
    public void put(PeriodicReportInfo entry) throws CommonException {
        if (entry.getPeriod() == null) {
            throw new CommonException("Period cannot be null");
        }
        String key = composeKey(entry.getGameServerId(), entry.getBankId(), entry.getPeriod(), entry.getType());
        reportInfo.putIfAbsent(key, entry);
    }

    @Override
    public PeriodicReportInfo getObject(String id) {
        return reportInfo.get(id);
    }

    @Override
    public Map<String, PeriodicReportInfo> getAllObjects() {
        return reportInfo;
    }

    @Override
    public int size() {
        return reportInfo.size();
    }

    @Override
    public String getAdditionalInfo() {
        return "no info";
    }

    @Override
    public String printDebug() {
        return "empty";
    }

    public static String composeKey(long gameServerId, long bankId, ReportPeriodEnum period, ReportTypeEnum type) {
        return gameServerId + ID_DELIMITER + bankId + ID_DELIMITER + period.name() + ID_DELIMITER + type.name();
    }

}
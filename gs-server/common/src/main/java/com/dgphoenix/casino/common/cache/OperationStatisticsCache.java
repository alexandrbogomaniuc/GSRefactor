package com.dgphoenix.casino.common.cache;


import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.dgphoenix.casino.common.cache.data.payment.WOStatistics;
import com.abs.casino.common.cache.data.payment.WOStatisticsContainer;
import com.dgphoenix.casino.common.exception.CommonException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@com.abs.casino.common.cache.CacheKeyInfo(description = "bankId")
public class OperationStatisticsCache extends AbstractExportableCache<WOStatisticsContainer> {

    private static final OperationStatisticsCache instance = new OperationStatisticsCache();

    //key - bankId
    private final Map<Long, List<WOStatistics>> woStatistics = new ConcurrentHashMap<>();

    public static OperationStatisticsCache getInstance() {
        return instance;
    }

    private OperationStatisticsCache() {
    }

    public void put(long bankId, WOStatistics woStatistics) {
        //LOG.debug("WOStatisticsServlet:doPost dateFrom: " + dateFrom);
        //if(woStatistics.getDate() ==getStartOfDay(new Date(woStatistics.getDate())))
        //{
        //LOG.debug("OperationStatisticsCache: put:::bankId "+ bankId);
        //LOG.debug("OperationStatisticsCache: put:::woStatistics.toString() "+woStatistics.toString());
        //LOG.debug("OperationStatisticsCache: put:::isExist(bankId, woStatistics.getDate()) "+ isExist(bankId, woStatistics.getDate()));
        if (!isExist(bankId, woStatistics.getDate())) {
            List<WOStatistics> woStatis;
            synchronized (this.woStatistics) {
                woStatis = get(bankId);
                if (woStatis == null) {
                    woStatis = new ArrayList<>();
                    this.woStatistics.put(bankId, woStatis);
                }
            }
            synchronized (woStatis) {
                //LOG.debug("OperationStatisticsCache: synchronized (woStatis)");
                woStatis.add(woStatistics);
                //LOG.debug("OperationStatisticsCache: synchronized (woStatis) after add");

            }

        } else {
            List<WOStatistics> woStatis = get(bankId);
            synchronized (woStatis) {
                for (WOStatistics wo : woStatis) {
                    if (wo.getDate() == woStatistics.getDate()) {
                        wo.addTotalNumCreditTransactions(woStatistics.getTotalNumCreditTransactions());
                        wo.addTotalNumDebitTransactions(woStatistics.getTotalNumDebitTransactions());
                        wo.addTotalValueCreditTransactions(woStatistics.getTotalValueCreditTransactions());
                        wo.addTotalValueDebitTransactions(woStatistics.getTotalValueDebitTransactions());
                        break;
                    }
                }
            }

        }

    }

    public void remove(long bankId, long date) {
        if (isExist(bankId, date)) {
            List<WOStatistics> woStatistics = get(bankId);
            if (woStatistics != null) {
                synchronized (woStatistics) {
                    Iterator iwoStatistics = woStatistics.iterator();
                    while (iwoStatistics.hasNext()) {
                        if (((WOStatistics) iwoStatistics.next()).getDate() == date) {
                            iwoStatistics.remove();
                            break;
                        }
                    }
                }
            }
        }
    }


    public int sizeByBank(long bankId) {
        if (get(bankId) != null) {
            return get(bankId).size();
        } else {
            return 0;
        }

    }

    public List<WOStatistics> get(long bankId) {
        return woStatistics.get(bankId);
    }

    public WOStatistics getTotalWOStatistics(long bankId, long dateFrom, long dateTo) {
        WOStatistics resultWO = new WOStatistics();
        List<WOStatistics> woStats = get(bankId);
        if (woStats != null) {
            for (WOStatistics wo : woStats) {
                if (wo.getDate() >= dateFrom && wo.getDate() <= dateTo) {
                    resultWO.addTotalNumCreditTransactions(wo.getTotalNumCreditTransactions());
                    resultWO.addTotalNumDebitTransactions(wo.getTotalNumDebitTransactions());
                    resultWO.addTotalValueCreditTransactions(wo.getTotalValueCreditTransactions());
                    resultWO.addTotalValueDebitTransactions(wo.getTotalValueDebitTransactions());
                }
            }
        }
        return resultWO;
    }


    public boolean isExist(long bankId, long date) {
        List<WOStatistics> woStatistics = get(bankId);
        boolean exist = false;
        if (woStatistics != null) {
            for (WOStatistics wo : woStatistics) {
                if (wo.getDate() == date)
                    return true;
            }
        }
        return exist;
    }

    @Override
    public List<WOStatistics> getObject(String id) {
        return get(Long.valueOf(id));
    }

    @Override
    public Map<Long, List<WOStatistics>> getAllObjects() {
        return woStatistics;
    }

    @Override
    public int size() {
        return woStatistics.size();
    }


    @Override
    public String printDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append(" woStatistics.size()=").append(woStatistics.size());
        for (Map.Entry<Long, List<WOStatistics>> statisticsEntry : woStatistics.entrySet()) {
            Long bankId = statisticsEntry.getKey();
            sb.append(", woStatistics[").append(bankId).append("]=");
            List<WOStatistics> listStatistics = statisticsEntry.getValue();
            if (listStatistics == null) {
                sb.append("null");
            } else {
                sb.append(statisticsEntry.getValue().size());
            }
        }
        return sb.toString();
    }

    @Override
    public String getAdditionalInfo() {
        return "none additional info";
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        synchronized (woStatistics) {
            Collection<Map.Entry<Long, List<WOStatistics>>> entries = woStatistics.entrySet();
            for (Map.Entry<Long, List<WOStatistics>> entry : entries) {
                WOStatisticsContainer stat = new WOStatisticsContainer(entry.getValue());
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), stat));
            }
        }
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        synchronized (woStatistics) {
            WOStatisticsContainer woStatisticsContainer = (WOStatisticsContainer) entry.getValue();
            List<WOStatistics> statistics = woStatisticsContainer.getStatistics();
            Long bankId = Long.valueOf(entry.getKey());
            for (WOStatistics statistic : statistics) {
                put(bankId, statistic);
            }
        }
    }

    @Override
    public void put(WOStatisticsContainer woStatisticsContainer) throws CommonException {
        throw new CommonException("Unsupported method");
    }

    public boolean isRequiredForImport() {
        return true;
    }

}

/**
 * Author: Andrey Nazarov
 * Date: 22.03.2006
 */

package com.dgphoenix.casino.common.web.statistics;

import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.abs.casino.common.web.statistics.IntervalStatistics;
import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsManager {
    private static StatisticsManager instance = new StatisticsManager();
    private boolean enableStatistics = false;

    private final Map<String, IntervalStatistics> requestStatistics =
            new ConcurrentHashMap<String, IntervalStatistics>(128);
    private final Map<String, IStatisticsGetter> statisticGetters = new ConcurrentHashMap<String, IStatisticsGetter>(128);
    private String id = "unknown";
    private long lastResetDate = System.currentTimeMillis();

    public static StatisticsManager getInstance() {
        return instance;
    }

    public Map<String, Comparator> sortComparators = new HashMap<String, Comparator>(5);

    private StatisticsManager() {
        sortComparators.put("name", new Comparator<IntervalStatistics>() {
            @Override
            public int compare(IntervalStatistics o1, IntervalStatistics o2) {
                return o1.getName().toLowerCase().compareTo( o2.getName().toLowerCase() );
            }
        });
        sortComparators.put("avg", new Comparator<IntervalStatistics>() {
            @Override
            public int compare(IntervalStatistics o1, IntervalStatistics o2) {
                return (int) (o2.getAvgValue() - o1.getAvgValue());
            }
        });
        sortComparators.put("min", new Comparator<IntervalStatistics>() {
            @Override
            public int compare(IntervalStatistics o1, IntervalStatistics o2) {
                return (int) (o2.getMinValue() - o1.getMinValue());
            }
        });
        sortComparators.put("max", new Comparator<IntervalStatistics>() {
            @Override
            public int compare(IntervalStatistics o1, IntervalStatistics o2) {
                return (int) (o2.getMaxValue() - o1.getMaxValue());
            }
        });
        sortComparators.put("count", new Comparator<IntervalStatistics>() {
            @Override
            public int compare(IntervalStatistics o1, IntervalStatistics o2) {
                return o2.getExperimentCount() - o1.getExperimentCount();
            }
        });
    }

    public void setId(String id) {
        this.id = id;
    }

    public void updateRequestStatistics(String requestName, long time, long id) {
        updateRequestStatistics(requestName, time, String.valueOf(id));
    }
    
    public void updateRequestStatistics(String requestName, long time) {
        updateRequestStatistics(requestName, time, null);
    }

    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    public void updateRequestStatistics(String requestName, long time, String additionalInfo) {
        if (enableStatistics) {
            IntervalStatistics statistics = requestStatistics.get(requestName);
            if (statistics == null) {
                synchronized (this) {
                    statistics = requestStatistics.get(requestName);
                    if (statistics == null) {
                        statistics = new IntervalStatistics(requestName);
                        requestStatistics.put(requestName, statistics);
                    }
                }
            }
            statistics.update(time, StringUtils.isTrimmedEmpty(additionalInfo) ? Thread.currentThread().getName()
                    : additionalInfo);

        }
    }

    public void registerStatisticsGetter(String name, IStatisticsGetter getter) {
        statisticGetters.put(name, getter);
    }

    public void printRequestStatistics(StringBuilder buf, boolean printGetterStat) {
        printRequestStatistics(buf, printGetterStat, null);
    }

    public void printRequestStatistics(StringBuilder buf, boolean printGetterStat, String sort) {
        buf.append("=== RequestStatistics [").append(id).append("] ===");
        buf.append('\n');
        buf.append("Last reset date: ").append(new Date(lastResetDate));
        buf.append('\n').append('\n');
        if (enableStatistics) {
            if( sort == null || !sortComparators.containsKey(sort) ) {
                sort = "name";
            }
            List<IntervalStatistics> values = new ArrayList(requestStatistics.values());
            Collections.sort(values, sortComparators.get(sort) );
            for (IntervalStatistics s : values) {
                buf.append(s);
                buf.append('\n');
            }
            buf.append('\n');

            if(printGetterStat) {
                List<String>  names = new ArrayList<String>(statisticGetters.keySet());
                Collections.sort(names);
                for (String name : names) {
                    IStatisticsGetter s = statisticGetters.get(name);
                    buf.append(name).append(": ");
                    buf.append(s.getStatistics());
                    buf.append('\n');
                }
            }
        } else {
            buf.append("Statistics disabled");
        }
    }

    public void dropServiceStatistics() {
        synchronized (requestStatistics) {
            requestStatistics.clear();
            lastResetDate = System.currentTimeMillis();
        }
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }
}

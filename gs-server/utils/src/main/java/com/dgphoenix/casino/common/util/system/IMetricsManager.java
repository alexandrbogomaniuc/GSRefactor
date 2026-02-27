package com.abs.casino.common.util.system;


/**
 * Created by quant on 19.11.15.
 */
public interface IMetricsManager {
    void register(Metric metricId, IMetricEvaluator metric);

    void startup();

    void shutdown();
}

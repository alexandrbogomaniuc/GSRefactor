package com.betsoft.casino.mp.web.service;

import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

/**
 * User: flsh
 * Date: 14.09.18.
 */
@Service
public class StatisticsService {
    private static final Logger LOG = LogManager.getLogger(StatisticsService.class);
    private Disposable logUpdater;

    @PostConstruct
    private void init() {
        LOG.debug("Init started");
        StatisticsManager.getInstance().setEnableStatistics(true);
        logUpdater = Flux.interval(Duration.ofSeconds(60)).subscribe(i -> {
            StringBuilder sb = new StringBuilder();
            StatisticsManager.getInstance().printRequestStatistics(sb, true);
            LOG.info(sb.toString());
        });
        LOG.debug("Init completed");
    }

    @PreDestroy
    private void destroy() {
        if (logUpdater != null && !logUpdater.isDisposed()) {
            logUpdater.dispose();
        }
        LOG.debug("Destroy completed");
    }
}

package com.betsoft.casino.mp.server.status;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.kafka.KafkaMessageService;
import com.betsoft.casino.mp.server.ServerCoordinatorInfoProvider;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.ExecutorUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServersStatusWatcher {

    private static final long WATCH_INTERVAL_INITIAL_MASTER = TimeUnit.SECONDS.toMillis(5);
    private static final long WATCH_INTERVAL_SLAVE = TimeUnit.MINUTES.toMillis(5);

    private final ScheduledThreadPoolExecutor watchersExecutor = new ScheduledThreadPoolExecutor(1);
    private final WatchServersThreadMaster watchServersThreadMaster;
    private final WatchServersThreadSlave watchServersThreadSlave;
    private final ServerCoordinatorInfoProvider serverCoordinatorInfoProvider;

    public ServersStatusWatcher(ServerConfigService serverConfigService,
                                KafkaMessageService kafkaMessageService,
                                ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        watchServersThreadMaster = new WatchServersThreadMaster(this, kafkaMessageService,
                serverConfigService, serverCoordinatorInfoProvider);
        watchServersThreadSlave = new WatchServersThreadSlave(this, watchServersThreadMaster);
        this.serverCoordinatorInfoProvider = serverCoordinatorInfoProvider;
    }

    @PostConstruct
    private void startUp() {
        watchersExecutor.scheduleAtFixedRate(watchServersThreadMaster,
                WATCH_INTERVAL_INITIAL_MASTER, WATCH_INTERVAL_INITIAL_MASTER,
                TimeUnit.MILLISECONDS);
        watchersExecutor.scheduleAtFixedRate(watchServersThreadSlave,
                WATCH_INTERVAL_INITIAL_MASTER, WATCH_INTERVAL_SLAVE,
                TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void shutdown() {
        ExecutorUtils.shutdownService(getClass().getSimpleName(), watchersExecutor,
                TimeUnit.SECONDS.toMillis(3));
    }

    public static ServersStatusWatcher getInstance() {
        return ApplicationContextHelper.getApplicationContext()
                .getBean("serversStatusWatcher", ServersStatusWatcher.class);
    }

    public boolean isMaster() {
        return serverCoordinatorInfoProvider.isMaster();
    }

    public int getCurrentMasterMpId() {
        return serverCoordinatorInfoProvider.getMasterServerId();
    }
}

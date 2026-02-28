package com.abs.casino.gs.status;

import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.abs.casino.gs.IGameServerStatusListener;
import com.abs.casino.kafka.service.KafkaMessageService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServersStatusWatcher {

    private static final long WATCH_INTERVAL_INITIAL_MASTER = TimeUnit.SECONDS.toMillis(5);
    private static final long WATCH_INTERVAL_SLAVE = TimeUnit.MINUTES.toMillis(5);

    private final Set<IGameServerStatusListener> serverStatusListeners = new HashSet<>();
    private final ScheduledThreadPoolExecutor watchersExecutor = new ScheduledThreadPoolExecutor(1);
    private final WatchServersThreadMaster watchServersThreadMaster;
    private final WatchServersThreadSlave watchServersThreadSlave;
    private final ServerCoordinatorInfoProvider serverCoordinatorInfoProvider;

    public ServersStatusWatcher(LoadBalancerCache loadBalancerCache,
                                KafkaMessageService kafkaMessageService,
                                ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        watchServersThreadMaster = new WatchServersThreadMaster(this, kafkaMessageService, loadBalancerCache, serverCoordinatorInfoProvider);
        watchServersThreadSlave = new WatchServersThreadSlave(this, loadBalancerCache, watchServersThreadMaster);
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
        ExecutorUtils.shutdownService(getClass().getSimpleName(), watchersExecutor, TimeUnit.SECONDS.toMillis(3));
    }

    public static ServersStatusWatcher getInstance() {
        return ApplicationContextHelper.getApplicationContext()
                .getBean("serversStatusWatcher", ServersStatusWatcher.class);
    }

    public boolean isMaster() {
        return serverCoordinatorInfoProvider.isMaster();
    }

    public Collection<IGameServerStatusListener> getServerStatusListeners() {
        return serverStatusListeners;
    }

    public void addServerStatusListener(IGameServerStatusListener listener) {
        serverStatusListeners.add(listener);
    }

    public int getCurrentMasterGsId() {
        return serverCoordinatorInfoProvider.getMasterServerId();
    }
}

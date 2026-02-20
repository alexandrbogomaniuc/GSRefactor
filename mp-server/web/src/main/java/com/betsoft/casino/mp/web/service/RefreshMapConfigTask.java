package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.common.GameMapStore;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class RefreshMapConfigTask implements Callable<Boolean>, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(RefreshGameConfigTask.class);
    private transient ApplicationContext context;

    private final int mapId;

    public RefreshMapConfigTask(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public Boolean call() throws Exception {
        LOG.debug("Refresh config for map {}", mapId);
        context.getBean(GameMapStore.class).updateMeta(mapId);
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        return "RefreshMapConfigTask{" +
                "context=" + context +
                ", mapId=" + mapId +
                '}';
    }
}

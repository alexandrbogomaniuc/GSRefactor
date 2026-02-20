package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.service.SpawnConfigProvider;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class RefreshSpawnConfigTask implements Callable<Boolean>, Serializable, ApplicationContextAware {
    private final Long roomId;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(RefreshSpawnConfigTask.class);

    public RefreshSpawnConfigTask(Long roomId) {
        this.roomId = roomId;
    }

    public Boolean call() {
        LOG.debug("RefreshGameConfigTask call: {} ", this);
        context.getBean(SpawnConfigProvider.class).removeCachedConfig(roomId);
        return Boolean.TRUE;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefreshSpawnConfigTask{");
        sb.append("roomId=").append(roomId);
        sb.append(", context=").append(context);
        sb.append('}');
        return sb.toString();
    }
}

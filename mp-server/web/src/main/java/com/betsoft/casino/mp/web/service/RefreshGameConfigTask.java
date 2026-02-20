package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.service.GameConfigProvider;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class RefreshGameConfigTask implements Callable<Boolean>, Serializable, ApplicationContextAware {
    private final Long roomId;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(RefreshGameConfigTask.class);

    public RefreshGameConfigTask(Long roomId) {
        this.roomId = roomId;
    }

    public Boolean call() {
        LOG.debug("RefreshGameConfigTask call: {} ", toString());
        context.getBean(GameConfigProvider.class).removeCachedConfig(roomId);
        return Boolean.TRUE;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefreshGameConfigTask{");
        sb.append("roomId=").append(roomId);
        sb.append(", context=").append(context);
        sb.append('}');
        return sb.toString();
    }
}

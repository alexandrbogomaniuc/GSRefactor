package com.dgphoenix.casino.gs.singlegames.tools.cbservtools;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by grien on 17.03.15.
 */
public class GameSessionStateListenersFactory {
    private static final GameSessionStateListenersFactory instance = new GameSessionStateListenersFactory();

    private final Logger LOG = LogManager.getLogger(this.getClass());
    private final IGameSessionStateListener NOOP = new IGameSessionStateListener() {
        @Override
        public void listen(State state, GameSession gameSession, int bankId) {
            //noop
        }
    };
    private final Map<Long, IGameSessionStateListener> listeners = new HashMap<>();

    private GameSessionStateListenersFactory() {
    }

    public static GameSessionStateListenersFactory getInstance() {
        return instance;
    }

    public IGameSessionStateListener getGameSessionStateListener(long bankId) {
        IGameSessionStateListener listener = listeners.get(bankId);
        if (listener == null) {
            synchronized (this) {
                listener = listeners.get(bankId);
                if (listener == null) {
                    listener = instantiate(bankId);
                }
            }
        }
        return listener;
    }

    private IGameSessionStateListener instantiate(long bankId) {
        IGameSessionStateListener listener;
        String className = BankInfoCache.getInstance().getBankInfo(bankId).getGameSessionStateListener();
        if (StringUtils.isTrimmedEmpty(className)) {
            listeners.put(bankId, listener = NOOP);
        } else {
            try {
                Class<?> aClass = Class.forName(className);
                Constructor<?> constructor = aClass.getConstructor();
                listeners.put(bankId, listener = (IGameSessionStateListener) constructor.newInstance());
            } catch (Exception e) {
                LOG.error("IGameSessionStateListener::instantiate error:", e);
                return NOOP;
            }
        }
        return listener;
    }
}

package com.abs.casino.gs.managers.game.session;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameSessionManager {
    private static final GameSessionManager instance = new GameSessionManager();
    private static final Logger LOG = LogManager.getLogger(GameSessionManager.class);

    private final CassandraGameSessionPersister gameSessionPersister;

    private GameSessionManager() {
        super();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
    }

    public static GameSessionManager getInstance() {
        return instance;
    }

    public void remove(GameSession gameSession) throws CommonException {
        if (gameSession != null && SessionHelper.getInstance().getTransactionData().getGameSession() != null) {
            if (gameSession.getId() != SessionHelper.getInstance().getTransactionData().getGameSession().getId()) {
                LOG.error("remove: wrong game session is found gameSession={}", gameSession);
                return;
            }
        }

        LOG.debug("remove: gameSession={}", gameSession);
        SessionHelper.getInstance().getTransactionData().setGameSession(null);
    }

    public GameSession getGameSessionById(long gameSessionId) {
        return gameSessionPersister.get(gameSessionId);
    }
}

package com.dgphoenix.casino.gs.persistance;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraPlayerSessionHistoryPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 4/3/12
 */
public class PlayerSessionPersister {
    private static final Logger LOG = LogManager.getLogger(PlayerSessionPersister.class);
    private static PlayerSessionPersister instance = new PlayerSessionPersister();
    private final CassandraPlayerSessionHistoryPersister playerSessionHistoryPersister;

    private PlayerSessionPersister() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        playerSessionHistoryPersister = persistenceManager.getPersister(CassandraPlayerSessionHistoryPersister.class);
    }

    public static PlayerSessionPersister getInstance() {
        return instance;
    }

    public SessionInfo getSessionInfo() {
        return SessionHelper.getInstance().getTransactionData().getPlayerSession();
    }

    public void save(SessionInfo sessionInfo) {
        SessionHelper.getInstance().getTransactionData().setPlayerSession(sessionInfo);
    }

    public void remove(SessionInfo sessionInfo, String logoutReason, boolean clearLocal, BankInfo bankInfo,
                       boolean isPersistPlayerSession) {
        sessionInfo.setLastCloseGameReason(logoutReason);
        if (bankInfo.isPersistPlayerSessions() && isPersistPlayerSession) {
            playerSessionHistoryPersister.persist(sessionInfo);
        }
        SessionHelper.getInstance().getTransactionData().setPlayerSession(null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("removed " + sessionInfo);
        }
    }
}

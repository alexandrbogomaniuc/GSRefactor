package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraGameSessionExtendedPropertiesPersister;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.transactiondata.ITransactionData;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletRequest;

public class SessionTimerStopProcessor implements ILockedCommandProcessor {
    private static final Logger LOG = LogManager.getLogger(SessionTimerStopProcessor.class);

    private static final String SESSION_TIMER_STOP = "SESSION_TIMER_STOP";

    private final BankInfoCache bankInfoCache;
    private final CassandraGameSessionExtendedPropertiesPersister extendedPropertiesPersister;

    public SessionTimerStopProcessor(BankInfoCache bankInfoCache, CassandraPersistenceManager persistenceManager) {
        this.bankInfoCache = bankInfoCache;
        extendedPropertiesPersister = persistenceManager.getPersister(CassandraGameSessionExtendedPropertiesPersister.class);
    }


    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                        ITransactionData transactionData, IDBLink dbLink, boolean roundFinished)
            throws CommonException {
        return null;
    }

    @Override
    public String getCommand() {
        return SESSION_TIMER_STOP;
    }
}

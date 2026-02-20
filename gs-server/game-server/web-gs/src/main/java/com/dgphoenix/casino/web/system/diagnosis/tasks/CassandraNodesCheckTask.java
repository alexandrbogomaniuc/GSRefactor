package com.dgphoenix.casino.web.system.diagnosis.tasks;

import com.datastax.driver.core.Host;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.IKeyspaceManager;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

import java.util.Collection;
import java.util.Set;

public class CassandraNodesCheckTask extends AbstractCheckTask {
    private final CassandraPersistenceManager persistenceManager;

    public CassandraNodesCheckTask() {
        this.persistenceManager = ApplicationContextHelper
                .getApplicationContext().getBean("persistenceManager", CassandraPersistenceManager.class);
    }

    @Override
    public boolean isOut(boolean strongValidation) {
        boolean taskFailed = false;
        try {
            taskExecutionStartTime = getCurrentTime();
            Collection<IKeyspaceManager> keyspaceManagers = persistenceManager.getKeyspaceManagers();
            Set<Host> hosts;
            for (IKeyspaceManager keySpaceManager : keyspaceManagers) {
                hosts = keySpaceManager.getDownHosts();
                if (!hosts.isEmpty()) {
                    taskFailed = true;
                }
            }
        } catch (Throwable e) {
            getLog().error("An error has occurred during cassandra nodes checking: ", e);
            taskFailed = true;
        } finally {
            setTaskFailed(taskFailed);
            taskExecutionEndTime = getCurrentTime();
        }

        return super.isOut(strongValidation);
    }
}

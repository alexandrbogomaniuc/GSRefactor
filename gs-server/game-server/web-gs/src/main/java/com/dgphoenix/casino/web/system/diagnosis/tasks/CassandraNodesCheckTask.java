package com.abs.casino.web.system.diagnosis.tasks;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.IKeyspaceManager;
import com.abs.casino.common.util.ApplicationContextHelper;

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
            Set<String> hosts;
            for (IKeyspaceManager keySpaceManager : keyspaceManagers) {
                hosts = keySpaceManager.getDownHostAddresses();
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

package com.betsoft.casino.mp.server.status;

class WatchServersThreadSlave implements Runnable {

    private final ServersStatusWatcher serversStatusWatcher;
    private final WatchServersThreadMaster watchServersThreadMaster;

    WatchServersThreadSlave(ServersStatusWatcher serversStatusWatcher, WatchServersThreadMaster watchServersThreadMaster) {
        this.serversStatusWatcher = serversStatusWatcher;
        this.watchServersThreadMaster = watchServersThreadMaster;
    }

    @Override
    public void run() {
        if (serversStatusWatcher.isMaster()) {
            return;
        }

        watchServersThreadMaster.updateServers();
    }
}

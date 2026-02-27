package com.abs.casino.init;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.abs.casino.common.engine.tracker.DelayedExecutor;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.FatalException;
import com.abs.casino.common.util.hardware.HardwareConfigurationManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.TransactionDataTracker;
import com.abs.casino.gs.managers.game.session.CloseGameSessionNotifyTracker;
import com.abs.casino.sm.tracker.logout.LogoutTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GsInitThread {
    private static final Logger LOG = LogManager.getLogger(GsInitThread.class);

    private boolean initialized = false;

    public void startup() throws FatalException {
        GameServer.getInstance().initServerConfiguration();
        try {
            BankInfoCache.getInstance().init();
            HardwareConfigurationManager.getInstance().startup();
            DelayedExecutor.getInstance().startup();
        } catch (Throwable e) {
            throw new FatalException(e);
        }
        try {
            GameServer.getInstance().init();
        } catch (CommonException e) {
            throw new FatalException("Cannot initialize", e);
        }
        LogoutTracker.getInstance().startup();
        CloseGameSessionNotifyTracker.getInstance().startup();
        TransactionDataTracker.getInstance().init();
        GameServer.getInstance().setServletContextInitialized(true);
        initialized = true;

        LOG.info("Initialization was successfully completed");
    }


    public void terminate() {
        // implemented for stopping gs/bs with online players
        LOG.info("terminate started");
        TransactionDataTracker.getInstance().shutdown();
        LogoutTracker.getInstance().shutdown();
        DelayedExecutor.getInstance().shutdown();
        CloseGameSessionNotifyTracker.getInstance().shutdown();
        try {
            GameServer.getInstance().destroy();
            LOG.info("GS destroyed successfully");
        } catch (Exception e) {
            LOG.error("GS destroy exception", e);
        }
        HardwareConfigurationManager.getInstance().shutdown();
    }

    public boolean isInitialized() {
        return initialized;
    }

}

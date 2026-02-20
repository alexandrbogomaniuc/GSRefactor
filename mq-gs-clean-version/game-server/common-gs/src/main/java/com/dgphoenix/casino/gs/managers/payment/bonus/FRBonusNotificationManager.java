package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.frb.FRBonusNotificationStatus;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.payment.bonus.client.frb.FRBRESTNotificationClient;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FRBonusNotificationManager {

    private static final Logger LOG = LogManager.getLogger(FRBonusNotificationManager.class);
    private static final FRBonusNotificationManager instance = new FRBonusNotificationManager();
    private final FRBRESTNotificationClient client = FRBRESTNotificationClient.getInstance();

    private FRBonusNotificationManager() {
    }

    public static FRBonusNotificationManager getInstance() {
        return instance;
    }

    public void processNotify(FRBonusNotification notification) throws CommonException {
        try {
            client.notify(notification);
            notification.setExternalStatus(FRBonusNotificationStatus.COMPLETED);
            LOG.debug("processNotify success: " + notification);
        } catch (CommonException e) {
            LOG.error("processNotify error:", e);
            if (notification.getExternalStatus() != FRBonusNotificationStatus.PENDING) {
                notification.setExternalStatus(FRBonusNotificationStatus.FAIL);
            }
            throw e;
        }
    }

    public void processFinalize(FRBonusNotification notification) throws CommonException {
        LOG.debug("processFinalize: " + notification);
        if (notification.getExternalStatus().equals(FRBonusNotificationStatus.COMPLETED)) {
            SessionHelper.getInstance().getTransactionData().setFrbNotification(null);
            LOG.debug("processFinalize completed.");
        }
        SessionHelper.getInstance().getDomainSession().persistFrbNotification();
    }

    public boolean isLaunchPrevented(FRBonusNotification notification) {
        boolean result = true;
        LOG.debug("isLaunchPrevented for: " + notification);
        if (notification.getBonusStatus().equals(BonusStatus.EXPIRED)) {
            result = false;
            FRBonusNotificationTracker tracker = FRBonusNotificationTracker.getInstance();
            long accountId = notification.getAccountId();
            if (!tracker.containsKey(accountId)) {
                LOG.debug("isLaunchPrevented add for tacking: " + notification);
                tracker.addTask(accountId);
            }
        }
        LOG.debug("isLaunchPrevented result: " + result);
        return result;
    }
}

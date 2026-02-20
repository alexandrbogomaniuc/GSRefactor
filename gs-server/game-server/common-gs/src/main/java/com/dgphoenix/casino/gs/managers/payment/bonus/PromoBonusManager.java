package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.PromoBonusCache;
import com.dgphoenix.casino.common.cache.data.bonus.PromoBonus;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 3/29/12
 */
public class PromoBonusManager {
    private static final Logger LOG = LogManager.getLogger(PromoBonusManager.class);
    private static final PromoBonusManager instance = new PromoBonusManager();
    public static final long EXPIRED_CHECK_PERIOD = 24; //hours

    private final ReentrantLock lock = new ReentrantLock();
    private final ExpiredBonusCloseProcessor expiredProcessor = new ExpiredBonusCloseProcessor();
    private boolean started;
    private ScheduledFuture<?> expiredProcessorTask;

    public static PromoBonusManager getInstance() {
        return instance;
    }

    public PromoBonusManager() {
    }

    public void init() {
        if (started) {
            throw new RuntimeException("Already started");
        }
        started = true;
        ScheduledExecutorService scheduler = ApplicationContextHelper.getBean(CommonExecutorService.class);
        expiredProcessorTask = scheduler.scheduleAtFixedRate(expiredProcessor, 0, EXPIRED_CHECK_PERIOD, TimeUnit.HOURS);
    }

    public void shutdown() {
        if (!started) {
            LOG.warn("Shutdown skipped, not started");
            return;
        }
        started = false;
        if (expiredProcessor.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("interrupted", e);
            }
        }
        expiredProcessorTask.cancel(true);
    }

    public boolean changePromoBonus(long bonusId, boolean activated) throws CommonException {
        final PromoBonus promoBonus = PromoBonusCache.getInstance().getById(bonusId);
        if (promoBonus == null) {
            throw new CommonException("PromoBonus not found, id=" + bonusId);
        }
        promoBonus.setActivated(activated);
        LOG.info("PromoBonus " + bonusId + ", activated=" + activated);
        return promoBonus.isActivated();
    }

    protected void save(PromoBonus bonus) throws CommonException {
    }

    public void lock() {
        lock.tryLock();
    }

    public void unlock() {
        lock.unlock();
    }

    class ExpiredBonusCloseProcessor implements Runnable {
        private boolean running = false;

        @Override
        public void run() {
            if (!started) {
                LOG.warn("ExpiredBonusCloseProcessor skip run, not started");
                return;
            }
            try {
                LOG.debug("Start task");
                lock();
                running = true;
                int notExpired = 0;
                long now = System.currentTimeMillis();
                List<Long> expired = new ArrayList<Long>();
                final Collection<PromoBonus> promoBonuses = PromoBonusCache.getInstance().getAllObjects().values();
                for (PromoBonus promoBonus : promoBonuses) {
                    if (promoBonus.isNotExpired()) {
                        notExpired++;
                        continue;
                    }
                    save(promoBonus);
                    expired.add(promoBonus.getId());
                }
                LOG.info("ExpiredBonusCloseProcessor: notExpired: " + notExpired + ", expired: " + Arrays.asList(
                        expired));
                for (Long bonusId : expired) {
                    PromoBonusCache.getInstance().remove(bonusId);
                }

                StatisticsManager.getInstance().updateRequestStatistics("ExpiredBonusCloseProcessor run",
                        System.currentTimeMillis() - now);
            } catch (Exception e) {
                if (started) {
                    LOG.error("ExpiredBonusCloseProcessor error", e);
                } else {
                    LOG.error("ExpiredBonusCloseProcessor error (stop server?): " + e.getMessage());
                }
            } finally {
                running = false;
                unlock();
                LOG.info("ExpiredBonusCloseProcessor cycle completed");
            }
        }

        public boolean isRunning() {
            return running;
        }
    }

}

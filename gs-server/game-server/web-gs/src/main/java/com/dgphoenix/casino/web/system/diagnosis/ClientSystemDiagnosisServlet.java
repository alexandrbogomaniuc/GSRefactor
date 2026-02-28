package com.abs.casino.web.system.diagnosis;

import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.web.diagnostic.BaseDiagnosisServlet;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusWinTracker;
import com.abs.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.abs.casino.web.system.diagnosis.tasks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ClientSystemDiagnosisServlet extends BaseDiagnosisServlet {
    private static final Logger LOG = LogManager.getLogger(ClientSystemDiagnosisServlet.class);
    private static final long INITIAL_DELAY = 0L;
    private static final long DIAGNOSE_DELAY = 1L;
    private static final long EXPIRE_TIME_IN_MILLIS = TimeUnit.MINUTES.toMillis(2);
    private final AtomicBoolean systemOk = new AtomicBoolean(true);
    private final AtomicLong lastDiagnoseStartTime = new AtomicLong();
    private final AtomicLong lastDiagnoseEndTime = new AtomicLong();
    private final List<AbstractCheckTask> tasks = new ArrayList<>(14);

    @Override
    public void init() {
        LOG.debug("ClientSystemDiagnosisServlet::Start init");

        initTasks();
        ScheduledExecutorService executor = ApplicationContextHelper.getBean(CommonExecutorService.class);
        executor.scheduleWithFixedDelay(() -> {
            LOG.debug("Start task");
            boolean tempResult = true;
            try {
                lastDiagnoseStartTime.set(getCurrentTime());
                for (AbstractCheckTask checkTask : tasks) {
                    tempResult &= !checkTask.isOut(true);
                }
            } catch (Throwable e) {
                LOG.error("Error has occurred: ", e);
                systemOk.set(false);
            } finally {
                systemOk.set(tempResult);
                lastDiagnoseEndTime.set(getCurrentTime());
            }
            LOG.debug("End task");
        }, INITIAL_DELAY, DIAGNOSE_DELAY, TimeUnit.MINUTES);
        LOG.debug("ClientSystemDiagnosisServlet::initialized");
    }

    private void initTasks() {
        LOG.debug("ClientSystemDiagnosisServlet::Start init tasks");

        tasks.add(new TrackerCheckTask(WalletTracker.getInstance()));
        tasks.add(new TrackerCheckTask(FRBonusWinTracker.getInstance()));
        tasks.add(new CassandraNodesCheckTask());
        tasks.add(new CassandraStateCheckTask());
        tasks.add(new ThreadsCheckTask());
        tasks.add(new HttpClientConnectionCheckTask());

        LOG.debug("ClientSystemDiagnosisServlet::End init tasks");
    }

    @Override
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        LOG.info("ClientSystemDiagnosisServlet::run");
        boolean initialized = isInitializationCompleted();
        if (!initialized || !systemOk.get() || isStateExpired() || isDiagnosisTooLong()) {
            printError(writer);
        } else {
            printOk(writer);
        }

        LOG.info("ClientSystemDiagnosisServlet::end");
    }

    @Override
    public boolean discontinuesSameTypeErrorsDiagnosticEnabled() {
        //nop
        return false;
    }

    @Override
    protected boolean isInitializationCompleted() {
        return GameServer.getInstance().isServletContextInitialized();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private boolean isStateExpired() {
        return getCurrentTime() - lastDiagnoseEndTime.get() > EXPIRE_TIME_IN_MILLIS;
    }

    private boolean isDiagnosisTooLong() {
        return (lastDiagnoseEndTime.get() - lastDiagnoseStartTime.get()) > 3 * EXPIRE_TIME_IN_MILLIS;
    }
}
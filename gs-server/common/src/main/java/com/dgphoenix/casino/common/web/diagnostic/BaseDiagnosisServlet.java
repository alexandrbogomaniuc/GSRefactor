package com.abs.casino.common.web.diagnostic;

import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.string.StringBuilderWriter;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.utils.MBeanUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 4/18/11
 */
public abstract class BaseDiagnosisServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(BaseDiagnosisServlet.class);
    public static final String PARAM_STRONG = "STRONG";
    public static final String RESULT_STATUS = "STATUS";
    public static final String RESULT_DESCRIPTION = "DESCRIPTION";

    public static final String RESULT_STATUS_OK = "OK";
    public static final String RESULT_STATUS_ERROR = "ERROR";
    public static final String RESULT_STATUS_WARNING = "WARNING";

    private static final AtomicBoolean runningFlag = new AtomicBoolean(false);
    private static final AtomicLong lastRunningDate = new AtomicLong();

    protected static final long TERMINATION_TIMEOUT = 5000L;
    protected static final ScheduledThreadPoolExecutor checkerPool = new ScheduledThreadPoolExecutor(1);
    protected final List<CheckTask> taskList = new ArrayList<>(12);
    protected static final LoadingCache<ErrorObj, Integer> FAILED_TASKS_CACHE = createCache();

    @Override
    public void init() throws ServletException {
        super.init();
        LOG.debug("SystemDiagnosisServlet: init");
        taskList.add(new CheckTask("Too many threads (90% of max threads)", true) {
            @Override
            public boolean isOut(boolean strongValidation) {
                return MBeanUtils.getCurrentThreadCount() > MBeanUtils.getMaxThreads() * 0.9;
            }

            @Override
            public String getErrorMessage() {
                return errorMessage + " currentThreadCount:" + MBeanUtils.getCurrentThreadCount()
                        + " maxThreads:" + MBeanUtils.getMaxThreads();
            }
        });
    }

    public void destroy() {
        LOG.info(":shutdown started:");
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), checkerPool, TERMINATION_TIMEOUT);
        LOG.info("shutdown completed");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LOG.info("SystemDiagnosisServlet:: start");
        PrintWriter writer = resp.getWriter();
        if (runningFlag.compareAndSet(false, true)) {
            try {
                String strongParam = req.getParameter(PARAM_STRONG);
                boolean strongValidation = false;
                if (!StringUtils.isTrimmedEmpty(strongParam)) {
                    LOG.warn("SystemDiagnosisServlet: STRONG param = {}", strongParam);
                    strongValidation = Boolean.TRUE.toString().equalsIgnoreCase(strongParam);
                }
                lastRunningDate.set(System.currentTimeMillis());
                StringBuilderWriter sw = new StringBuilderWriter();
                this.processDiagnosisHeuristicLevelLow(sw, strongValidation);
                writer.write(sw.toString());
            } catch (Throwable e) {
                LOG.error("SystemDiagnosisServlet:: unknown error", e);
                FAILED_TASKS_CACHE.put(new ErrorObj(e.toString(), this.getClass().getSimpleName()), 0);
                printError(writer, "Unknown error");
            } finally {
                runningFlag.set(false);
            }
        } else {
            LOG.warn("Already running, try later. Start date: {}", new Date(lastRunningDate.get()));
            printError(writer, "Already running, try later. Start date: " + new Date(lastRunningDate.get()));
        }
        LOG.info("SystemDiagnosisServlet:: end: " + runningFlag.get());
    }

    protected void printError(Writer writer, String error) {
        try {
            writer.write(RESULT_STATUS + "=" + RESULT_STATUS_ERROR + "&" + RESULT_DESCRIPTION + "=" + error);
        } catch (IOException ex) {
            //nop
        }
    }

    protected void printError(Writer writer) {
        try {
            writer.write(RESULT_STATUS + "=" + RESULT_STATUS_ERROR);
        } catch (IOException e) {
            //nop
        }
    }

    public abstract boolean discontinuesSameTypeErrorsDiagnosticEnabled();

    protected void printWarning(Writer writer, String war) {
        try {
            writer.write(RESULT_STATUS + "=" + RESULT_STATUS_WARNING + "&" + RESULT_DESCRIPTION + "=" + war);
        } catch (IOException ex) {
            //nop
        }
    }

    protected void printOk(Writer writer) {
        try {
            writer.write(RESULT_STATUS + "=" + RESULT_STATUS_OK);
        } catch (IOException ex) {
            //nop
        }
    }

    protected boolean processDiagnosisHeuristicLevelLow(StringBuilderWriter writer, boolean strongValidation) {
        LOG.info("BaseDiagnosisServlet:: run");
        boolean initialized = isInitializationCompleted();
        if (initialized) {
            for (CheckTask task : taskList) {
                String className = task.getClass().getName();
                try {
                    if (task.isOut(strongValidation)) {
                        writer.clear();
                        String errorMessage = task.getErrorMessage();
                        if (task.isWarning()) {
                            LOG.warn("processDiagnosisHeuristicLevelLow: detect warn: {}", errorMessage);
                            printWarning(writer, errorMessage);
                        } else {
                            FAILED_TASKS_CACHE.put(new ErrorObj(errorMessage, className), 0);
                            LOG.error("processDiagnosisHeuristicLevelLow: detect error: {}", errorMessage);
                            printError(writer, errorMessage);
                        }
                        return false;
                    }
                } catch (Exception ex) {
                    LOG.error("processDiagnosisHeuristicLevelLow: Unexpected", ex);
                    FAILED_TASKS_CACHE.put(new ErrorObj(ex.toString(), task.getClass().getSimpleName()), 0);
                    printError(writer, ex.getMessage());
                    return false;
                }
            }
            FAILED_TASKS_CACHE.put(new ErrorObj("OK", ""), 0);
            return printMoreThen3ErrorsOrOK(writer);
        } else {
            printError(writer, "Game server is not initialized");
        }
        LOG.info("SystemDiagnosisServlet:: end");
        return false;
    }

    private Map<String, Long> getFailedErrors() {
        List<ErrorObj> listErrors = FAILED_TASKS_CACHE.asMap().keySet().stream()
                .sorted(Comparator.comparingLong(ErrorObj::getDatetime))
                .collect(Collectors.toList());

        String lastState = "";
        String errorMessage;
        Map<String, Long> result = new HashMap<>();
        for (ErrorObj errorObj : listErrors) {
            errorMessage = errorObj.getErrorMessage();
            if (lastState.equals(errorMessage)) {
                continue;
            } else {
                if (!errorMessage.equals("OK"))
                    result.merge(errorMessage, 1L, (oldVal, value) -> oldVal + 1L);
            }
            lastState = errorMessage;
        }
        return result;
    }

    private boolean printMoreThen3ErrorsOrOK(Writer writer) {
        if (!discontinuesSameTypeErrorsDiagnosticEnabled()) {
            printOk(writer);
            return true;
        }
        for (Map.Entry<String, Long> countedError : getFailedErrors().entrySet()) {
            if (countedError.getValue() > 3) {
                printWarning(writer, "Multiple (" + countedError.getValue() +
                        ") errors of the same type during the last hour: (" + countedError.getKey().split("\\|")[0] + ")" + "\n");
                return false;
            }
        }
        printOk(writer);
        return true;
    }

    private static LoadingCache<ErrorObj, Integer> createCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(50)
                .build(new CacheLoader<ErrorObj, Integer>() {
                    @Override
                    public Integer load(ErrorObj obj) {
                        return null;
                    }
                });
    }


    protected abstract boolean isInitializationCompleted();

    protected abstract class RunnableCheckTask extends CheckTask implements Runnable {
        public RunnableCheckTask(String errorMessage, boolean warning) {
            super(errorMessage, warning);
        }

    }

    protected class TrackerUpdaterCheckTask extends RunnableCheckTask {
        private long lastAsyncUpdate = System.currentTimeMillis();
        private AbstractCommonTracker tracker;
        private long lastTaskCount = 0;
        private long lastCompletedTaskCount = 0;
        private long outTime;

        public TrackerUpdaterCheckTask(String errorMessage, boolean warning, AbstractCommonTracker tracker, int outTime) {
            super(errorMessage, warning);
            this.tracker = tracker;
            this.outTime = outTime * 60 * 1000;
        }

        public boolean isOut(boolean strongValidation) {
            long now = System.currentTimeMillis();
            return (now - lastAsyncUpdate > outTime);
        }

        @Override
        public void run() {
            long taskCount = tracker.getTaskCount();
            long completedTaskCount = tracker.getCompletedTaskCount();
            if (lastTaskCount == taskCount || completedTaskCount > lastCompletedTaskCount) {
                lastAsyncUpdate = System.currentTimeMillis();
            }
            lastTaskCount = taskCount;
            lastCompletedTaskCount = completedTaskCount;
        }
    }

}

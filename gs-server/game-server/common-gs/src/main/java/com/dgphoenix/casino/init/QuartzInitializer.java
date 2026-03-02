package com.abs.casino.init;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraBankInfoPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.bank.BankInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by isador
 * on 10/10/17
 */
public class QuartzInitializer implements ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(QuartzInitializer.class);

    private final Scheduler scheduler;
    private final TableDefinition tableDefinition;
    private final CassandraBankInfoPersister bankInfoPersister;
    private ApplicationContext context;

    public QuartzInitializer(Scheduler scheduler, CassandraPersistenceManager persistenceManager) {
        this.scheduler = scheduler;
        bankInfoPersister = persistenceManager.getPersister(CassandraBankInfoPersister.class);
        tableDefinition = bankInfoPersister.getMainTableDefinition();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @PostConstruct
    public void initTasks() {
        LOG.info("Initializing cron tasks");
        bankInfoPersister.iterateAllColumnFamily(row -> {
            BankInfo bank = tableDefinition.deserializeFrom(row.getBytes("scn"), BankInfo.class);
            try {
                scheduleBankShutdownJobForBank(bank);
            } catch (SchedulerException e) {
                LOG.error("Could not schedule job for bankId: {}", bank.getId(), e);
            }
        });
    }

    @PreDestroy
    private void destroy() {
        LOG.debug("Destroying");
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            LOG.error("error on shutdown", e);
        }
        LOG.debug("Destroyed");
    }

    public void scheduleBankShutdownJobForBank(BankInfo bank) throws SchedulerException {
        if (bank != null) {
            String expr = bank.getCronShutdown();
            if (expr != null && !expr.isEmpty()) {
                Map<String, Object> jobDetailMap = getJobDetailMap();
                jobDetailMap.put("bankId", bank.getId());
                JobDetail jobDetail = JobBuilder.newJob(BankShutdownJob.class)
                        .usingJobData(new JobDataMap(jobDetailMap))
                        .withIdentity(String.valueOf(bank.getId()))
                        .build();
                CronTrigger trigger = getTrigger(expr, bank.getTimeZone(), bank.getId());
                Date date = scheduler.scheduleJob(jobDetail, trigger);
                LOG.debug("Shutdown for bankId: {} scheduled using {}: {}", bank.getId(),
                        trigger.getCronExpression(), date);
            }
        }
    }

    private Map<String, Object> getJobDetailMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("appContext", context);
        return map;
    }

    /**
     * Calculate offset according to bank timeZone
     */
    private CronTrigger getTrigger(String expr, String timeZone, long bankId) {
        CronScheduleBuilder exprBuilder = CronScheduleBuilder.cronSchedule(expr);
        if (timeZone != null && !timeZone.isEmpty()) {
            exprBuilder.inTimeZone(TimeZone.getTimeZone(timeZone));
        }
        return TriggerBuilder.newTrigger()
                .withSchedule(exprBuilder)
                .withIdentity(String.valueOf(bankId))
                .build();
    }
}

package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.IPendingOperationProcessor;
import com.betsoft.casino.mp.payment.PendingOperationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 12.08.2022.
 * This service periodically get all pending operations and try to process them.
 */
@Service
public class PendingOperationTracker {
    private static final Logger LOG = LogManager.getLogger(PendingOperationTracker.class);
    private final IPendingOperationService pendingOperationService;
    private final ISocketService socketService;
    private final IServerConfigService<?> serverConfigService;
    private ScheduledExecutorService scheduledExecutorService;

    public PendingOperationTracker(IPendingOperationService pendingOperationService, ISocketService socketService,
                                   IServerConfigService<?> serverConfigService) {
        this.pendingOperationService = pendingOperationService;
        this.socketService = socketService;
        this.serverConfigService = serverConfigService;
    }

    @PostConstruct
    private void init() {
        LOG.info("init: start");
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::process, 10, 30, TimeUnit.SECONDS);
        LOG.info("init: completed");
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutorService.shutdown();
        LOG.info("shutdown");
    }

    private void process() {
        try {
            Collection<Long> accounts = pendingOperationService.getAllKeys();
            for (Long accountId : accounts) {
                process(accountId);
            }
        } catch (Exception e) {
            LOG.error("process: unexpected error", e);
        }
    }

    private void process(long accountId) {
        boolean locked = pendingOperationService.tryLock(accountId);
        if (!locked) {
            LOG.warn("process, already locked, accountId={}", accountId);
            return;
        }
        try {
            IPendingOperation operation = pendingOperationService.get(accountId);
            if (operation == null) {
                LOG.warn("process: PendingOperation not found, accountId={}", accountId);
                return;
            }
            PendingOperationType operationType = operation.getOperationType();
            @SuppressWarnings("rawtypes")
            IPendingOperationProcessor processor = pendingOperationService.getProcessor(operationType);
            if (processor == null) {
                LOG.error("process: processor not found for operation={}", operation);
                return;
            }
            @SuppressWarnings("unchecked")
            boolean success = processor.process(socketService, serverConfigService.getServerId(), operation);
            LOG.debug("process: process operation result, success={}, type={}, accountId={}, operation={}",
                    success, operationType, operation.getAccountId(),operation.toString());
            if (success) {
                pendingOperationService.remove(operation.getAccountId());
            } else {
                Duration duration = Duration.between(Instant.ofEpochMilli(operation.getCreateDate()), Instant.now());
                if (duration.toDays() >= 1) {
                    LOG.error("process long running operation: failed operation has been removed, success={}, type={}, accountId={}, operation={}",
                            success, operationType, operation.getAccountId(), operation.toString());
//                    pendingOperationService.remove(operation.getAccountId()); //TODO consider removing operation in future, when we faoud the proper way to handle it in DataDog
                }
                pendingOperationService.save(operation);
            }
        } catch (Exception e) {
            LOG.error("process: failed process operation, accountId={}", accountId, e);
        } finally {
            pendingOperationService.unlock(accountId);
        }
    }
}

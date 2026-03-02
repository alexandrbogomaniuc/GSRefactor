package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.kafka.KafkaMessageService;
import com.abs.casino.cassandra.IRemoteUnlocker;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.abs.casino.kafka.dto.BooleanResponseDto;
import com.abs.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.dto.RemoteUnlockRequest;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.betsoft.casino.mp.utils.KafkaResponseConverterUtil.convertToType;

import java.util.Date;

public class RemoteUnlocker implements IRemoteUnlocker {

    private static final Logger LOG = LogManager.getLogger(RemoteUnlocker.class);

    private final KafkaMessageService kafkaMessageService;

    public RemoteUnlocker(KafkaMessageService kafkaMessageService) {
        this.kafkaMessageService = kafkaMessageService;
    }

    @Override
    public boolean unlock(int serverId, Class<? extends ILockManager> lockManagerClass, String lockId, long lockTime) {
        MutableBoolean unlocked = new MutableBoolean();
        String lockManagerName = lockManagerClass.getCanonicalName();
        try {
            RemoteUnlockRequest request = new RemoteUnlockRequest(lockManagerName, lockId, lockTime);
            Mono<KafkaResponse> response = kafkaMessageService.syncRequestToSpecificGs(request, serverId);
            BooleanResponseDto unlockedResponse = convertToType(response, (r) -> new BooleanResponseDto(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));

            unlocked.setValue(unlockedResponse.isBool());
        } catch (Exception e) {
            LOG.error("Error during remote unlock, serverId: {}, lockManager: {}, lockId: {}, lockTime: {}",
                    serverId, lockManagerName, lockId, new Date(lockTime));
        }
        return unlocked.getValue();
    }
}

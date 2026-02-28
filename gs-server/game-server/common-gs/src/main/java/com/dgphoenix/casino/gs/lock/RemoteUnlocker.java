package com.abs.casino.gs.lock;

import static com.dgphoenix.casino.gs.persistance.remotecall.KafkaResponseConverterUtil.convertToType;

import java.util.Date;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.abs.casino.cassandra.IRemoteUnlocker;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.dgphoenix.casino.kafka.dto.BooleanResponseDto;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.dto.RemoteUnlockRequest;
import com.abs.casino.kafka.service.KafkaMessageService;

import reactor.core.publisher.Mono;

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
            Mono<KafkaResponse> response = kafkaMessageService.syncRequestToSpecificGS(request, serverId);
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

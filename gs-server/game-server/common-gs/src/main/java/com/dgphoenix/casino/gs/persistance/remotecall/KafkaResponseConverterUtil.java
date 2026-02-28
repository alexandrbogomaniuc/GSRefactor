package com.abs.casino.gs.persistance.remotecall;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;

import reactor.core.publisher.Mono;

public class KafkaResponseConverterUtil {
    @SuppressWarnings("unchecked")
    public static <T extends BasicKafkaResponse> T convertToType(Mono<KafkaResponse> monoResponse, Function<KafkaResponse, T> newTypeObject) throws InterruptedException, ExecutionException {
        T response = monoResponse
        .map(m -> { 
            // some unknown error
            if (isCritical(m)) {
                throw new RuntimeException("Handler error happened: " + m.getReasonPhrases());
            }
            try {
                return (T) m; 
            } catch (ClassCastException e) {
                return newTypeObject.apply(m);
            }
        })
        .toFuture().get();
        
        if (isNull(response)) {
            return null;
        }
        return response;
    }

    private static boolean isCritical(KafkaResponse r) {
        return !r.isSuccess() && r.getStatusCode() == -1;
    }

    private static boolean isNull(KafkaResponse r) {
        return !r.isSuccess() && r.getStatusCode() == -100;
    }
}

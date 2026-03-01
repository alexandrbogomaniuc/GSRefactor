package com.abs.casino.common.cache;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BankPartnerIdCache {
    //partnerId-bankId
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();
    //bankId-partnerId
    private final Map<Integer, String> bankPartnerIdMap = new ConcurrentHashMap<>();

    public Integer getBankId(@NotNull String partnerId) {
        return cache.get(partnerId);
    }

    public synchronized void put(String partnerId, @NotNull Integer bankId) {
        String oldPartnerId = bankPartnerIdMap.get(bankId);
        if (!Objects.equals(partnerId, oldPartnerId)) {
            if (partnerId != null) {
                bankPartnerIdMap.put(bankId, partnerId);
                cache.put(partnerId, bankId);
                if (oldPartnerId != null) {
                    cache.remove(oldPartnerId);
                }
            } else {
                bankPartnerIdMap.remove(bankId);
                cache.remove(oldPartnerId);
            }
        }
    }

    public void remove(@NotNull Integer bankId) {
        String oldPartnerId = bankPartnerIdMap.get(bankId);
        bankPartnerIdMap.remove(bankId);
        if (oldPartnerId != null) {
            cache.remove(oldPartnerId);
        }
    }
}

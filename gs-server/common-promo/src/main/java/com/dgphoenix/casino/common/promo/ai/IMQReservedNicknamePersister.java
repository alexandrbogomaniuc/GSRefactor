package com.abs.casino.common.promo.ai;

import java.util.Set;

public interface IMQReservedNicknamePersister {

    void persist(String region, String nickname, long owner);
    void remove(String region, String nickname);
    Set<String> getNicknamesForEntireSystem(String region);
    Set<String> getAIBotNames();
    Set<String> getNicknames(String region, Long owner);
}

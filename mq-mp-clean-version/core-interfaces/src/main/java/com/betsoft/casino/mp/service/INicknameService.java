package com.betsoft.casino.mp.service;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 07.07.2022.
 */
public interface INicknameService {
    String getNickname(Long bankId, Long accountId);

    boolean isNicknameAvailableGlobally(String nickname);

    boolean isNicknameAvailable(String nickname, Long bankId, Long accountId);

    boolean changeNickname(Long bankId, Long accountId, String oldNickName, String newNickname)
            throws CommonException;

    String generateRandomNickname(boolean isGuest, long bankId, long accountId, String oldNickname);
}

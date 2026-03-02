package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.data.persister.PlayerNicknamePersister;
import com.betsoft.casino.mp.data.persister.ReservedNicknamePersister;
import com.betsoft.casino.mp.service.INicknameService;
import com.betsoft.casino.mp.service.ISocketService;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * User: flsh
 * Date: 11.02.2020.
 */
@Service
public class NicknameService implements INicknameService {
    private static final Logger LOG = LogManager.getLogger(NicknameService.class);
    private static final String AI_BOT_REGION = "AI_BOT_REGION";
    private final ReservedNicknamePersister reservedNicknamePersister;
    private final PlayerNicknamePersister playerNicknamePersister;
    private final ISocketService socketService;

    public NicknameService(CassandraPersistenceManager cpm, ISocketService socketService) {
        this.reservedNicknamePersister = cpm.getPersister(ReservedNicknamePersister.class);
        this.playerNicknamePersister = cpm.getPersister(PlayerNicknamePersister.class);
        this.socketService = socketService;
    }

    @Override
    public String getNickname(Long bankId, Long accountId) {
        return playerNicknamePersister.getNickname(bankId, accountId);
    }

    //used for initial setup scripts
    public boolean addReservedNicknameForEntireSystem(String nickname) throws CommonException {
        boolean available = playerNicknamePersister.isNicknameAvailable(nickname);
        if (!available) {
            LOG.warn("addReservedNicknameForEntireSystem: nickName already used: {}", nickname);
            return false;
        }
        reservedNicknamePersister.persistForEntireSystem(AI_BOT_REGION, nickname);
        socketService.addMQReservedNicknames(AI_BOT_REGION, -1, Collections.singleton(nickname));
        LOG.debug("addReservedNicknameForEntireSystem: success add nickname={}", nickname);
        return true;
    }

    public void removeReservedNicknameForEntireSystem(String nickname) throws CommonException {
        reservedNicknamePersister.remove(AI_BOT_REGION, nickname);
        socketService.removeMQReservedNicknames(AI_BOT_REGION, -1, Collections.singleton(nickname));
        LOG.debug("removeReservedNicknameForEntireSystem: success remove nickname={}", nickname);
    }

    @Override
    public boolean isNicknameAvailableGlobally(String nickname) {
        boolean available = playerNicknamePersister.isNicknameAvailable(nickname);
        if (available) {
            available = !reservedNicknamePersister.isExistForEntireSystem(AI_BOT_REGION, nickname);
        }
        return available;
    }

    @Override
    public boolean isNicknameAvailable(String nickname, Long bankId, Long accountId) {
        boolean available = playerNicknamePersister.isNicknameAvailable(nickname, bankId, accountId);
        if (available) {
            available = !reservedNicknamePersister.isExistForEntireSystem(AI_BOT_REGION, nickname);
            if (!available) {
                LOG.debug("isNicknameAvailable: not available for entire AI_BOT_REGION: {}", nickname);
            } else {
                //nothing at this moment, add call 'reservedNicknamePersister' to if need bank level blacklist checking
            }
        }
        return available;
    }

    @Override
    public boolean changeNickname(Long bankId, Long accountId, String oldNickName, String newNickname)
            throws CommonException {
        return playerNicknamePersister.changeNickname(bankId, accountId, oldNickName, newNickname);
    }

    @Override
    public String generateRandomNickname(boolean isGuest, long bankId, long accountId, String oldNickname) {
        if (isGuest) {
            return "Guest" + RNG.nextInt(100000);
        } else {
            while (true) {
                String nickname = "User" + RNG.nextInt(10000000);
                if (isNicknameAvailable(nickname, bankId, accountId)) {
                    try {
                        changeNickname(bankId, accountId, oldNickname, nickname);
                        return nickname;
                    } catch (CommonException e) {
                        LOG.error("Unexpected error", e);
                    }
                }
            }
        }
    }
}

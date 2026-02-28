package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.model.IPlayerInfo;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.common.util.ConcurrentHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LobbyManager {

    private static final Logger LOG = LogManager.getLogger(LobbyManager.class);

    private final ConcurrentMap<Long, ConcurrentMap<String, ILobbySocketClient>> players = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ConcurrentHashSet<String>> nicknames = new ConcurrentHashMap<>();

    public void registerPlayer(IPlayerInfo playerInfo, String nickname, ILobbySocketClient client) {
        client.setPlayerInfo(playerInfo);
        client.setNickname(nickname);
        client.setLoggedIn(true);
        getBankPlayers(playerInfo.getBankId()).put(client.getSessionId(), client);
    }

    public int getActivePlayersCount(long bankId) {
        return getBankPlayers(bankId).size();
    }

    private String nextNickname(String nickname, int attempt) {
        return attempt > 0 ? nickname + attempt : nickname;
    }

    private ConcurrentMap<String, ILobbySocketClient> getBankPlayers(long bankId) {
        if (!players.containsKey(bankId)) {
            players.putIfAbsent(bankId, new ConcurrentHashMap<>());
        }
        return players.get(bankId);
    }

    private ConcurrentHashSet<String> getBankNicknames(long bankId) {
        if (!nicknames.containsKey(bankId)) {
            nicknames.putIfAbsent(bankId, new ConcurrentHashSet<>());
        }
        return nicknames.get(bankId);
    }
}

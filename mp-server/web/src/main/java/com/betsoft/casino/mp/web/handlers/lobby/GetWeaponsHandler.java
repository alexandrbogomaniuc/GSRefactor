package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.WeaponsPersister;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.service.IWeaponService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.transport.GetWeapons;
import com.betsoft.casino.mp.transport.PlayerWeaponsResponse;
import com.betsoft.casino.mp.transport.Weapon;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;

@Component
public class GetWeaponsHandler extends MessageHandler<GetWeapons, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetWeaponsHandler.class);
    private final RoomPlayerInfoService playerInfoService;
    private final IWeaponService weaponService;


    public GetWeaponsHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                             RoomPlayerInfoService playerInfoService,
                             CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.playerInfoService = playerInfoService;
        this.weaponService = cpm.getPersister(WeaponsPersister.class);
    }

    @Override
    public void handle(WebSocketSession session, GetWeapons message, ILobbySocketClient client) {
        if (checkLogin(message, client)) {

            final Long bankId = client.getBankId();
            final Long accountId = client.getAccountId();
            MoneyType moneyType = client.getMoneyType();
            int mode = moneyType.ordinal();

            LOG.debug("GetWeaponsHandler bankId={}, accountId={}, mode={}", bankId, accountId, mode);

            LobbySession lobbySession = lobbySessionService.get(client.getSessionId());

            List<Long> playerStakes = lobbySession.getStakes();
            Set<Float> stakesSet = new HashSet<>();

            for (Long playerStake : playerStakes) {
                float baseStake = Money.fromCents(playerStake).toFloatCents();
                stakesSet.add(baseStake);
            }

            List<Float> stakes = new ArrayList<>(stakesSet);
            Collections.sort(stakes);

            LOG.debug("client.getSessionId()={} stakes={}", client.getSessionId(), stakes);

            Map<Float, List<Weapon>> weapons = new HashMap<>();
            Map<Money, Map<Integer, Integer>> allWeapons;
            Long bonusOrTournamentId = EnterLobbyHandler.getBonusOrTournamentId(lobbySession.getTournamentSession(),
                    lobbySession.getActiveFrbSession(), lobbySession.getActiveCashBonusSession());
            if(bonusOrTournamentId == null) {
                allWeapons = weaponService.getAllWeapons(client.getBankId(), client.getAccountId(),
                        client.getMoneyType().ordinal(), lobbySession.getGameId());
            } else {
                allWeapons = weaponService.getAllSpecialModeWeapons(bonusOrTournamentId, client.getAccountId(),
                        client.getMoneyType().ordinal(), lobbySession.getGameId());
            }
            allWeapons.forEach((coin, weapon) -> weapons.put(coin.toFloatCents(), convertWeapons(weapon)));
            allWeapons.put(getRoomPlayerStakeStake(client.getAccountId()), getActiveWeapons(client.getAccountId()));

            client.sendMessage(new PlayerWeaponsResponse(System.currentTimeMillis(), message.getRid(), weapons), message);

            LOG.debug("client.getSessionId()={}, weapons={}", client.getSessionId(), weapons);
        }
    }

    private Map<Integer, Integer> getActiveWeapons(long accountId) {
        IActionRoomPlayerInfo info = (IActionRoomPlayerInfo) playerInfoService.get(accountId);
        if (info == null) {
            return new HashMap<>();
        }
        return info.getWeapons();
    }

    private Money getRoomPlayerStakeStake(long accountId) {
        IRoomPlayerInfo info = playerInfoService.get(accountId);
        return info == null ? Money.INVALID : Money.fromCents(info.getStake());
    }

    private List<Weapon> convertWeapons(Map<Integer, Integer> weapons) {
        List<Weapon> weaponList = new ArrayList<>();
        weapons.forEach((key, value) -> weaponList.add(new Weapon(key, value)));
        return weaponList;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}

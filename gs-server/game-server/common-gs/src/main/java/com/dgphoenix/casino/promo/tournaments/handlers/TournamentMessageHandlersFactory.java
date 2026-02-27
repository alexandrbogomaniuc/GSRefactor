package com.abs.casino.promo.tournaments.handlers;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.transport.ITransportObject;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.gs.socket.mq.TournamentBuyInHelper;
import com.abs.casino.promo.PlayerAliasManager;
import com.abs.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.abs.casino.promo.tournaments.messages.*;
import com.abs.casino.promo.tournaments.messages.*;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.abs.casino.websocket.tournaments.IMessageHandler;
import com.abs.casino.websocket.tournaments.TournamentWebSocketMessageListener;
import com.abs.casino.websocket.tournaments.handlers.*;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentMessageHandlersFactory {
    private final Map<Class<? extends ITransportObject>, IMessageHandler<?>> handlers = new ConcurrentHashMap<>();
    private final List<TournamentWebSocketMessageListener> listeners = Lists.newCopyOnWriteArrayList();

    public TournamentMessageHandlersFactory(IPromoCampaignManager promoCampaignManager,
                                            CassandraPersistenceManager cpm,
                                            ICurrencyRateManager currencyRatesManager,
                                            TournamentBuyInHelper buyInHelper,
                                            ErrorPersisterHelper errorPersisterHelper,
                                            PlayerAliasManager playerAliasManager,
                                            MQServiceHandler mqServiceHandler) {
        CassandraMaxBalanceTournamentPersister tournamentPersister =
                cpm.getPersister(CassandraMaxBalanceTournamentPersister.class);
        addMessageHandler(EnterLobby.class, new EnterLobbyHandler(promoCampaignManager,
                currencyRatesManager,
                cpm,
                errorPersisterHelper, buyInHelper));
        addMessageHandler(GetTournamentDetails.class, new GetTournamentDetailsHandler(promoCampaignManager,
                currencyRatesManager,
                cpm,
                errorPersisterHelper));
        addMessageHandler(JoinTournament.class, new JoinTournamentHandler(promoCampaignManager,
                tournamentPersister,
                buyInHelper,
                listeners,
                errorPersisterHelper, playerAliasManager));
        addMessageHandler(JoinBattleground.class, new JoinBattlegroundHandler(errorPersisterHelper, buyInHelper, mqServiceHandler));
        addMessageHandler(GetBattlegroundHistory.class, new GetBattlegroundHistoryHandler(errorPersisterHelper, cpm));
        addMessageHandler(GetLeaderboard.class, new GetLeaderboardHandler(promoCampaignManager,
                currencyRatesManager,
                errorPersisterHelper));
    }

    private void addMessageHandler(Class<? extends ITransportObject> clazz, IMessageHandler<?> handler) {
        handlers.put(clazz, handler);
    }

    public IMessageHandler<?> getHandler(Class<? extends ITransportObject> clazz) {
        return handlers.get(clazz);
    }

    public void addTournamentWebSocketMessageListener(TournamentWebSocketMessageListener listener) {
        listeners.add(listener);
    }
}

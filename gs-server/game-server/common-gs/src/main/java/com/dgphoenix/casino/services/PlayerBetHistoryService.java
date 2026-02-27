package com.abs.casino.services;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraTempBetPersister;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

@Service
public class PlayerBetHistoryService {
    private static final Logger LOG = LogManager.getLogger(PlayerBetHistoryService.class);
    private static final String SERVLET_SOURCE = "ROUND_ID=";
    private static final String DATA_SOURCE = "playerRoundId=";

    private final PlayerBetPersistenceManager betPersistenceManager;
    private final CassandraTempBetPersister tempBetPersister;

    public PlayerBetHistoryService(PlayerBetPersistenceManager betPersistenceManager, CassandraPersistenceManager persistenceManager) {
        this.betPersistenceManager = betPersistenceManager;
        tempBetPersister = persistenceManager.getPersister(CassandraTempBetPersister.class);
    }

    public List<PlayerBet> getPlayerBets(long gameSessionId) {
        List<PlayerBet> bets = tempBetPersister.getOnlinePayerBets(gameSessionId);
        if (bets.isEmpty()) {
            LOG.debug("Online player bets not found for gameSessionId={}", gameSessionId);
            bets = betPersistenceManager.getBets(gameSessionId);
        }
        return bets;
    }

    private String parseRoundId(PlayerBet bet) {
        String servletData = bet.getServletData();
        String data = bet.getData();
        if (!isTrimmedEmpty(servletData) && servletData.contains(SERVLET_SOURCE)) {
            int index = servletData.indexOf(SERVLET_SOURCE);
            int lastIndex = servletData.indexOf("~", index);
            if (lastIndex == -1) {
                return servletData.substring(index + SERVLET_SOURCE.length());
            }
            return servletData.substring(index + SERVLET_SOURCE.length(), lastIndex);
        } else if (!isTrimmedEmpty(data) && data.contains(DATA_SOURCE)) {
            int index = data.indexOf(DATA_SOURCE);
            return data.substring(index + DATA_SOURCE.length(), data.indexOf(";", index));
        }
        return null;
    }
}

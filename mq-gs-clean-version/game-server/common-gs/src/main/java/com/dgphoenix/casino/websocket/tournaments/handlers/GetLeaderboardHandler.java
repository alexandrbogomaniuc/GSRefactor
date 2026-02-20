package com.dgphoenix.casino.websocket.tournaments.handlers;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.promo.tournaments.ErrorCodes;
import com.dgphoenix.casino.promo.tournaments.TournamentLeaderboard;
import com.dgphoenix.casino.promo.tournaments.TournamentManager;
import com.dgphoenix.casino.promo.tournaments.messages.Error;
import com.dgphoenix.casino.promo.tournaments.messages.GetLeaderboard;
import com.dgphoenix.casino.promo.tournaments.messages.Leaderboard;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.dgphoenix.casino.websocket.tournaments.ISocketClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

import static com.dgphoenix.casino.promo.tournaments.ErrorCodes.INTERNAL_ERROR;
import static com.dgphoenix.casino.promo.tournaments.ErrorCodes.NOT_LOGGED_IN;

public class GetLeaderboardHandler extends AbstractLobbyHandler<GetLeaderboard> {

    private static final Logger LOG = LogManager.getLogger(GetLeaderboardHandler.class);

    private final IPromoCampaignManager promoCampaignManager;
    private final ErrorPersisterHelper errorPersisterHelper;

    public GetLeaderboardHandler(IPromoCampaignManager promoCampaignManager,
                                 ICurrencyRateManager currencyRatesManager,
                                 ErrorPersisterHelper errorPersisterHelper) {
        super(currencyRatesManager);
        this.promoCampaignManager = promoCampaignManager;
        this.errorPersisterHelper = errorPersisterHelper;
    }

    @Override
    public void handle(GetLeaderboard message, ISocketClient client) {
        Consumer<Error> errorSaver = error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message);
        if (client.isConnected()) {
            IPromoCampaign campaign = promoCampaignManager.getPromoCampaign(message.getTournamentId());
            if (campaign == null || !campaign.getTemplate().getPromoType().isTournamentLogic()) {
                LOG.error("Wrong tournamentId: {}", message.getTournamentId());
                client.sendMessage(createErrorMessage(ErrorCodes.TOURNAMENT_NOT_FOUND, "Not found", message.getRid(), errorSaver));
                return;
            }

            try {
                TournamentLeaderboard leaderboard = ApplicationContextHelper.getBean(TournamentManager.class)
                        .getLeaderboard(campaign, client.getCurrency(), client.getSessionId());

                client.sendMessage(new Leaderboard(System.currentTimeMillis(),
                        message.getRid(),
                        message.getTournamentId(),
                        campaign.getActionPeriod().getEndDate().getTime(),
                        campaign.getStatus().name(),
                        leaderboard.getPrizePlaces(),
                        leaderboard.getPlaces(),
                        leaderboard.getCurrentPlayer()));
            } catch (CommonException e) {
                LOG.error("Unable to get leaderboard for {}", campaign, e);
                client.sendMessage(createErrorMessage(INTERNAL_ERROR, "Internal error", message.getRid(),
                        error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message, e)));
            }
        } else {
            client.sendMessage(createErrorMessage(NOT_LOGGED_IN, "Not logged in", message.getRid(), errorSaver));
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}

package com.abs.casino.websocket.tournaments.handlers;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.transport.TObject;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.promo.persisters.CassandraLocalizationsPersister;
import com.abs.casino.promo.tournaments.messages.Error;
import com.abs.casino.promo.tournaments.messages.GetTournamentDetails;
import com.abs.casino.promo.tournaments.messages.TournamentDetails;
import com.abs.casino.support.ErrorPersisterHelper;
import com.abs.casino.websocket.tournaments.ISocketClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

import static com.abs.casino.promo.tournaments.ErrorCodes.*;

public class GetTournamentDetailsHandler extends AbstractLobbyHandler<GetTournamentDetails> {

    private static final Logger LOG = LogManager.getLogger(GetTournamentDetails.class);

    private final IPromoCampaignManager promoCampaignManager;
    private final CassandraLocalizationsPersister localizationsPersister;
    private final ErrorPersisterHelper errorPersisterHelper;

    public GetTournamentDetailsHandler(IPromoCampaignManager promoCampaignManager,
                                       ICurrencyRateManager currencyRatesManager,
                                       CassandraPersistenceManager cpm,
                                       ErrorPersisterHelper errorPersisterHelper) {
        super(currencyRatesManager);
        this.promoCampaignManager = promoCampaignManager;
        this.localizationsPersister = cpm.getPersister(CassandraLocalizationsPersister.class);
        this.errorPersisterHelper = errorPersisterHelper;
    }

    @Override
    public void handle(GetTournamentDetails message, ISocketClient client) {
        Consumer<Error> errorSaver = error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message);
        if (client.isConnected()) {
            IPromoCampaign campaign = promoCampaignManager.getPromoCampaign(message.getTournamentId());
            if (campaign == null) {
                client.sendMessage(createErrorMessage(TOURNAMENT_NOT_FOUND, "Not found", message.getRid(), errorSaver));
                return;
            }
            if (campaign.isNetworkPromoCampaign() && !CollectionUtils.isEmpty(message.getNetworkEventIds())) {
                NetworkPromoCampaign networkPromoCampaign = (NetworkPromoCampaign) campaign;
                for (NetworkPromoEvent event : networkPromoCampaign.getEvents()) {
                    sendDetails(message, client, event, errorSaver);
                }
            } else if (campaign.getTemplate().getPromoType().isScoreCounting()) {
                sendDetails(message, client, campaign, errorSaver);
            }
        } else {
            client.sendMessage(createErrorMessage(NOT_LOGGED_IN, "Not logged in", message.getRid(), errorSaver));
        }
    }

    private void sendDetails(GetTournamentDetails message, ISocketClient client, IPromoCampaign campaign, Consumer<Error> errorSaver) {
        try {
            client.sendMessage(convert(message, campaign, client.getCurrency(), client.getLang(), errorSaver));
        } catch (Exception e) {
            LOG.error("Unable to GetTournamentDetails", e);
            sendErrorMessage(client, INTERNAL_ERROR, e.getMessage(), message.getRid(),
                    error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message, e));
        }
    }

    private TObject convert(GetTournamentDetails message, IPromoCampaign campaign, String playerCurrency, String lang,
                            Consumer<Error> errorSaver) throws CommonException {
        Set<RankPrize> prizes = getPrizes(campaign);
        if (prizes == null) {
            return createErrorMessage(INTERNAL_ERROR, "Bad tournament", message.getRid(), errorSaver);
        }

        MaxBalanceTournamentPromoTemplate template = (MaxBalanceTournamentPromoTemplate) campaign.getTemplate();
        String title = localizationsPersister.getLocalizedPromoTitle(campaign.getId(), lang);
        long buyInPrice = (long) currencyRatesManager
                .convert(template.getBuyInPrice(), campaign.getBaseCurrency(), playerCurrency);
        long reBuyPrice = (long) currencyRatesManager
                .convert(template.getReBuyPrice(), campaign.getBaseCurrency(), playerCurrency);
        long prize = (long) currencyRatesManager
                .convert(template.getPrize(), campaign.getBaseCurrency(), playerCurrency);
        return new TournamentDetails(System.currentTimeMillis(),
                message.getRid(),
                campaign.getId(),
                title == null ? campaign.getName() : title,
                "",
                campaign.getActionPeriod().getStartDate().getTime(),
                campaign.getActionPeriod().getEndDate().getTime(),
                buyInPrice,
                template.getBuyInAmount(),
                reBuyPrice,
                template.getReBuyAmount(),
                template.isReBuyEnabled(),
                template.getReBuyLimit(),
                prize,
                campaign.getStatus().name(),
                convertPrizes(prizes, campaign.getBaseCurrency(), playerCurrency),
                new ArrayList<>(campaign.getGameIds()));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}

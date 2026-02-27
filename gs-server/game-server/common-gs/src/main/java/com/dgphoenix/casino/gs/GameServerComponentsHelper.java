package com.dgphoenix.casino.gs;

import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.gs.managers.game.history.HistoryInformerManager;
import com.abs.casino.promo.IPromoMessagesDispatcher;
import com.abs.casino.promo.tournaments.TournamentManager;
import com.abs.casino.websocket.IWebSocketSessionsController;

/**
 * User: flsh
 * Date: 28.11.16.
 *
 * @deprecated Use constructor injection or ApplicationContextHelper.getBean
 */
@Deprecated
public class GameServerComponentsHelper {

    public static TournamentManager getTournamentManager() {
        return ApplicationContextHelper.getApplicationContext().
                getBean("tournamentManager", TournamentManager.class);
    }

    public static IPromoCampaignManager getPromoCampaignManager() {
        return ApplicationContextHelper.getApplicationContext().
                getBean("promoCampaignManager", IPromoCampaignManager.class);
    }

    public static IPromoMessagesDispatcher getPromoMessagesDispatcher() {
        return ApplicationContextHelper.getApplicationContext().
                getBean("promoMessagesDispatcher", IPromoMessagesDispatcher.class);
    }

    public static IWebSocketSessionsController getWebSocketSessionsController() {
        return ApplicationContextHelper.getApplicationContext().
                getBean("webSocketSessionsController", IWebSocketSessionsController.class);
    }

    public static HistoryInformerManager getHistoryInformerManager() {
        return ApplicationContextHelper.getApplicationContext().
                getBean("historyInformerManager", HistoryInformerManager.class);
    }

    private GameServerComponentsHelper() {
    }
}

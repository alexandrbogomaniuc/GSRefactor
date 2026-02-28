package com.abs.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.promo.AwardedPrize;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.abs.casino.common.promo.messages.server.responses.GetPromoMessagesResponse;
import com.abs.casino.common.promo.messages.server.responses.GetPromoNotificationsResponse;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command.ILockedCommandProcessor;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import com.abs.casino.promo.PromoNotificationsCreator;

import javax.servlet.ServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated It is the old version of {@link GetPromoNotificationsProcessor}
 * Left only for backward capability. Should be removed in near future.
 */
public class GetPromoMessagesProcessor implements ILockedCommandProcessor {
    private final GetPromoNotificationsProcessor getPromoNotificationsHandler;

    public GetPromoMessagesProcessor(PromoNotificationsCreator promoNotificationsCreator) {
        getPromoNotificationsHandler = new GetPromoNotificationsProcessor(promoNotificationsCreator);
    }

    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                        ITransactionData transactionData, IDBLink dbLink, boolean roundFinished) {
        ServerMessage response = getPromoNotificationsHandler
                .processLocked(request, sessionId, command, transactionData, dbLink, roundFinished);
        GetPromoNotificationsResponse notificationsResponse = (GetPromoNotificationsResponse) response;

        PromoCampaignMemberInfos promoMemberInfos = transactionData.getPromoMemberInfos();
        if (promoMemberInfos != null) {
            for (PromoCampaignMember member : promoMemberInfos.getPromoMembers().values()) {
                for (AwardedPrize awardedPrize : member.getAwardedPrizes()) {
                    Set<Long> sentNotifications = new HashSet<>(awardedPrize.getUnsentNotificationIds());
                    sentNotifications.forEach(awardedPrize::setNotificationSent);
                }
            }
        }

        return new GetPromoMessagesResponse(notificationsResponse.getNotifications());
    }

    @Override
    public String getCommand() {
        return IGameController.CMD_GET_PROMO_MESSAGES;
    }

}

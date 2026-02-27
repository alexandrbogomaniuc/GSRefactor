package com.abs.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.promo.PromoNotificationType;
import com.abs.casino.common.promo.messages.server.notifications.PromoNotification;
import com.abs.casino.common.promo.messages.server.responses.GetPromoNotificationsResponse;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.web.BaseAction;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command.ILockedCommandProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import com.dgphoenix.casino.promo.PromoNotificationsCreator;
import com.google.common.base.Splitter;

import javax.servlet.ServletRequest;
import java.util.*;

/**
 * Created by vladislav on 2/14/17.
 */
public class GetPromoNotificationsProcessor implements ILockedCommandProcessor {
    private final PromoNotificationsCreator promoNotificationsCreator;

    public GetPromoNotificationsProcessor(PromoNotificationsCreator promoNotificationsCreator) {
        this.promoNotificationsCreator = promoNotificationsCreator;
    }

    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                        ITransactionData transactionData, IDBLink dbLink, boolean roundFinished) {
        List<PromoNotification> notifications = new ArrayList<>();
        PromoCampaignMemberInfos promoMemberInfos = transactionData.getPromoMemberInfos();
        if (promoMemberInfos != null) {
            String activePromoIdsAsString = request.getParameter(BaseAction.ACTIVE_PROMO_IDS);
            Set<Long> activePromoIds = null;
            if (activePromoIdsAsString != null) {
                activePromoIds = extractPromoIds(activePromoIdsAsString);
            }
            Set<PromoNotificationType> notificationsTypes = EnumSet.allOf(PromoNotificationType.class);
            for (PromoCampaignMember promoMember : promoMemberInfos.getPromoMembers().values()) {
                if (activePromoIds == null || activePromoIds.contains(promoMember.getCampaignId())) {
                    notifications.addAll(promoNotificationsCreator.getNotifications(promoMember, notificationsTypes));
                }
            }
        }
        return new GetPromoNotificationsResponse(notifications);
    }

    private Set<Long> extractPromoIds(String promoIdsAsString) {
        Set<Long> promoIds = new HashSet<>();
        for (String promoIdAsString : Splitter.on("|").omitEmptyStrings().split(promoIdsAsString)) {
            promoIds.add(Long.valueOf(promoIdAsString));
        }
        return promoIds;
    }

    @Override
    public String getCommand() {
        return IGameController.CMD_GET_PROMO_NOTIFICATIONS;
    }

}

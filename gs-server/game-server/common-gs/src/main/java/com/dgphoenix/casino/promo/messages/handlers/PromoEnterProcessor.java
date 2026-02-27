package com.abs.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoType;
import com.dgphoenix.casino.common.promo.messages.client.requests.PromoEnter;
import com.dgphoenix.casino.common.promo.messages.server.responses.PromoEnterResponse;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.command.ILockedCommandProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import com.dgphoenix.casino.promo.IPromoMessagesDispatcher;
import com.abs.casino.promo.persisters.CassandraLocalizationsPersister;
import com.dgphoenix.casino.websocket.IWebSocketSessionsController;
import com.google.common.base.Splitter;
import com.google.common.net.UrlEscapers;

import javax.servlet.ServletRequest;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by vladislav on 12/20/16.
 */
public class PromoEnterProcessor extends AbstractMessageHandler<PromoEnter> implements ILockedCommandProcessor {
    private static final Splitter PROMO_IDS_SPLITTER = Splitter.on("|").omitEmptyStrings();

    private final IPromoCampaignManager promoCampaignManager;
    private final CassandraLocalizationsPersister localizationsPersister;
    private final int thisServerId;

    public PromoEnterProcessor(IPromoCampaignManager promoCampaignManager,
                               CassandraLocalizationsPersister localizationsPersister, int thisServerId) {
        super(PromoEnter.class);
        this.promoCampaignManager = promoCampaignManager;
        this.localizationsPersister = localizationsPersister;
        this.thisServerId = thisServerId;
    }

    @Override
    protected void processMessage(IWebSocketSessionsController webSocketSessionsController, String sessionId,
                                  PromoEnter promoEnter) throws CommonException {
        long[] promoIds = promoEnter.getPromoIds();
        checkNotNull(promoIds, "promoIds on PromoEnter can't be null");
        Set<Long> promoIdsAsSet = new HashSet<>();
        for (long promoId : promoIds) {
            promoIdsAsSet.add(promoId);
        }

        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();

            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            SessionInfo playerSession = transactionData.getPlayerSession();
            GameSession gameSession = transactionData.getGameSession();
            if (playerSession != null && playerSession.getSessionId().equals(sessionId) && gameSession != null) {
                PromoEnterResponse enterResponse = enterInPromos(promoIdsAsSet, transactionData.getAccount(),
                        gameSession, true);
                enterResponse.setRequestId(promoEnter.getId());
                webSocketSessionsController.sendMessage(sessionId, enterResponse);
                if (isNotEmpty(enterResponse.getActivePromos())) {
                    IPromoMessagesDispatcher messagesDispatcher = GameServerComponentsHelper.getPromoMessagesDispatcher();
                    messagesDispatcher.notifyPromoSessionIsOpen(sessionId, enterResponse.getActivePromosIds());
                    SessionHelper.getInstance().commitTransaction();
                }
            }

            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                        ITransactionData transactionData, IDBLink dbLink, boolean roundFinished) throws CommonException {
        GameSession gameSession = transactionData.getGameSession();
        String promoIdsAsString = request.getParameter(BaseAction.PROMO_IDS);
        checkNotNull(promoIdsAsString, "promoIds can't be null");
        Set<Long> promoIds = new HashSet<>();
        for (String promoIdAsString : PROMO_IDS_SPLITTER.split(promoIdsAsString)) {
            promoIds.add(Long.valueOf(promoIdAsString));
        }
        return enterInPromos(promoIds, transactionData.getAccount(), gameSession, null);
    }

    private PromoEnterResponse enterInPromos(Collection<Long> promoIds, IAccountInfo account, GameSession gameSession,
                                             Boolean hasWebSocketSupport) throws CommonException {
        long gameSessionId = gameSession.getId();
        long gameId = gameSession.getGameId();
        Map<IPromoCampaign, PromoCampaignMember> registeredCampaigns = promoCampaignManager
                .registerPlayerInPromos(promoIds, account, gameSessionId, gameId);

        PromoEnterResponse promoEnterResponse = new PromoEnterResponse();
        for (Map.Entry<IPromoCampaign, PromoCampaignMember> campaignMemberEntry : registeredCampaigns.entrySet()) {
            IPromoCampaign promoCampaign = campaignMemberEntry.getKey();
            PromoCampaignMember campaignMember = campaignMemberEntry.getValue();

            campaignMember.setLastEnteredServerId(thisServerId);
            if (hasWebSocketSupport != null) {
                campaignMember.setWebSocketSupport(hasWebSocketSupport);
            }

            String title = localizationsPersister.getLocalizedPromoTitle(promoCampaign.getId(), gameSession.getLang());
            if (StringUtils.isTrimmedEmpty(title)) {
                title = promoCampaign.getName();
            }
            String encodedTitle = UrlEscapers.urlFragmentEscaper().escape(title);
            PromoType promoType = promoCampaign.getTemplate().getPromoType();
            Date endDate = promoCampaign.getActionPeriod().getEndDate();
            TimeZone serverTimeZone = TimeZone.getDefault();
            int serverOffset = serverTimeZone.getOffset(endDate.getTime());
            long endTimeUTC = endDate.getTime() - serverOffset;

            promoEnterResponse.addActivePromo(promoCampaign.getId(), encodedTitle, promoType, endTimeUTC);
        }
        return promoEnterResponse;
    }

    @Override
    public String getCommand() {
        return IGameController.CMD_PROMO_ENTER;
    }

}

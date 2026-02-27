package com.abs.casino.promo.messages.handlers;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.promo.messages.client.requests.NotificationsShown;
import com.abs.casino.common.promo.messages.server.notifications.prizes.PrizeWonNotification;
import com.abs.casino.common.promo.messages.server.responses.NotificationsShownResponse;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command.ILockedCommandProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import com.dgphoenix.casino.promo.IPromoMessagesDispatcher;
import com.abs.casino.websocket.IWebSocketSessionsController;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vladislav on 2/14/17.
 */
public class NotificationsShownProcessor extends AbstractMessageHandler<NotificationsShown> implements ILockedCommandProcessor {
    private static final Logger LOG = LogManager.getLogger(NotificationsShownProcessor.class);

    private static final String NOTIFICATIONS_IDS = "NOTIFICATIONS_IDS";
    private static final Splitter NOTIFICATIONS_IDS_SPLITTER = Splitter.on(';');

    public NotificationsShownProcessor() {
        super(NotificationsShown.class);
    }

    @Override
    protected void processMessage(IWebSocketSessionsController webSocketSessionsController, String sessionId,
                                  NotificationsShown notificationsShown) throws CommonException {
        List<String> notificationsIds = Arrays.asList(notificationsShown.getNotificationsIds());
        ServerMessage notificationsShownResponse = processNotificationsShown(sessionId, notificationsIds,
                notificationsShown.getId());
        webSocketSessionsController.sendMessage(sessionId, notificationsShownResponse);
    }

    @Override
    public ServerResponse processLocked(ServletRequest request, String sessionId,
                                        String command, ITransactionData transactionData,
                                        IDBLink dbLink, boolean roundFinished) throws CommonException, IOException {
        String encodedNotificationsIds = request.getParameter(NOTIFICATIONS_IDS);
        String notificationsIdsAsString = URLDecoder.decode(encodedNotificationsIds, "UTF-8");
        Iterable<String> notificationsIds = NOTIFICATIONS_IDS_SPLITTER.split(notificationsIdsAsString);
        return processNotificationsShown(sessionId, notificationsIds, null);
    }

    private ServerResponse processNotificationsShown(String sessionId, Iterable<String> notificationsIds,
                                                     Long requestId) throws CommonException {
        Multimap<Long, Long> prizesIdsByCampaigns = HashMultimap.create();
        for (String notificationId : notificationsIds) {
            String[] keyArgs = notificationId.split("_");
            String type = keyArgs[0];
            if (PrizeWonNotification.PRIZE_WON_NOTIFICATION_PREFIX.equals(type)) {
                long campaignId = Long.valueOf(keyArgs[1]);
                long prizeId = Long.valueOf(keyArgs[2]);
                prizesIdsByCampaigns.put(campaignId, prizeId);
            } else {
                LOG.warn("Unknown notification type, type = {}", type);
            }
        }

        if (!prizesIdsByCampaigns.isEmpty()) {
            IPromoMessagesDispatcher messagesDispatcher = GameServerComponentsHelper.getPromoMessagesDispatcher();
            messagesDispatcher.markPrizesAsNotifiedAbout(sessionId, prizesIdsByCampaigns.asMap());

            NotificationsShownResponse notificationsShownResponse = new NotificationsShownResponse();
            if (requestId != null) {
                notificationsShownResponse.setRequestId(requestId);
            }
            return notificationsShownResponse;
        } else {
            LOG.error("Not found prizes ids by campaigns. SessionId = {}", sessionId);
            throw new CommonException("At least some notificationsIds must be specified");
        }
    }

    @Override
    public String getCommand() {
        return IGameController.CMD_NOTIFICATIONS_SHOWN;
    }

}

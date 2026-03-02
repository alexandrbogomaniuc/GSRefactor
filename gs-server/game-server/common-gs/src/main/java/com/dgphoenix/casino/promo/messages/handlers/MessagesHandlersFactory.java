package com.abs.casino.promo.messages.handlers;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.promo.IPromoCampaignManager;
import com.abs.casino.promo.persisters.CassandraLocalizationsPersister;
import com.abs.casino.system.configuration.GameServerConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by vladislav on 12/20/16.
 */
public class MessagesHandlersFactory {
    private final Map<String, IMessageHandler<?>> messagesHandlers = new ConcurrentHashMap<>();

    public MessagesHandlersFactory(IPromoCampaignManager promoCampaignManager,
                                   CassandraPersistenceManager persistenceManager,
                                   GameServerConfiguration gameServerConfiguration) {
        CassandraLocalizationsPersister localizationsPersister = persistenceManager
                .getPersister(CassandraLocalizationsPersister.class);
        checkNotNull(localizationsPersister, "Localizations persister not found");
        int thisServerId = gameServerConfiguration.getServerId();
        PromoEnterProcessor promoEnterHandler = new PromoEnterProcessor(promoCampaignManager, localizationsPersister,
                thisServerId);
        addMessageHandler(promoEnterHandler);

        NotificationShownHandler notificationShownHandler = new NotificationShownHandler();
        addMessageHandler(notificationShownHandler);

        NotificationsShownProcessor notificationsShownHandler = new NotificationsShownProcessor();
        addMessageHandler(notificationsShownHandler);
    }

    private void addMessageHandler(IMessageHandler<?> handler) {
        messagesHandlers.put(handler.getMessageType(), handler);
    }

    public IMessageHandler<?> getHandler(String messageType) {
        return messagesHandlers.get(messageType);
    }
}

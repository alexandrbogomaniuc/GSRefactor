package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.GameCommandsProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.ILockedProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.command.*;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.error.IErrorProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.error.SessionExpiredErrorProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.pre.FailedOperationHandleEnterProcessor;
import com.dgphoenix.casino.promo.PromoNotificationsCreator;
import com.abs.casino.promo.messages.handlers.GetPromoMessagesProcessor;
import com.abs.casino.promo.messages.handlers.GetPromoNotificationsProcessor;
import com.abs.casino.promo.messages.handlers.NotificationsShownProcessor;
import com.abs.casino.promo.messages.handlers.PromoEnterProcessor;
import com.abs.casino.promo.persisters.CassandraLocalizationsPersister;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GameCommandsProcessorsConfiguration {
    @Bean
    public GameCommandsProcessor gameCommandsProcessor(List<ILockedCommandProcessor> lockedCommandProcessors,
                                                       List<IUnlockedCommandProcessor> unlockedCommandProcessors,
                                                       List<ILockedProcessor> lockedProcessors,
                                                       List<IErrorProcessor> errorProcessors) {
        return new GameCommandsProcessor(lockedCommandProcessors, unlockedCommandProcessors, lockedProcessors,
                errorProcessors);
    }

    @Bean
    public PromoEnterProcessor promoEnterProcessor(IPromoCampaignManager promoCampaignManager,
                                                   CassandraPersistenceManager persistenceManager,
                                                   GameServerConfiguration gameServerConfiguration) {
        CassandraLocalizationsPersister localizationsPersister = persistenceManager
                .getPersister(CassandraLocalizationsPersister.class);
        int serverId = gameServerConfiguration.getServerId();
        return new PromoEnterProcessor(promoCampaignManager, localizationsPersister, serverId);
    }

    @Bean
    public NotificationsShownProcessor notificationsShownProcessor() {
        return new NotificationsShownProcessor();
    }

    @Bean
    public GetPromoMessagesProcessor getPromoMessagesProcessor(PromoNotificationsCreator promoNotificationsCreator) {
        return new GetPromoMessagesProcessor(promoNotificationsCreator);
    }

    @Bean
    public GetPromoNotificationsProcessor getPromoNotificationsProcessor(PromoNotificationsCreator promoNotificationsCreator) {
        return new GetPromoNotificationsProcessor(promoNotificationsCreator);
    }

    @Bean
    public FailedOperationHandleEnterProcessor failedOperationHandleEnterProcessor() {
        BankInfoCache bankInfoCache = BankInfoCache.getInstance();
        return new FailedOperationHandleEnterProcessor(bankInfoCache);
    }

    @Bean
    public SessionExpiredErrorProcessor sessionExpiredErrorProcessor() {
        return new SessionExpiredErrorProcessor();
    }

    @Bean
    public SessionTimerExpiredProcessor sessionTimerExpiredProcessor(NtpTimeProvider timeProvider, CassandraPersistenceManager persistenceManager) {
        BankInfoCache bankInfoCache = BankInfoCache.getInstance();
        return new SessionTimerExpiredProcessor(timeProvider, bankInfoCache, persistenceManager);
    }

    @Bean
    public GetTimeProcessor getTimeProcessor(NtpTimeProvider timeProvider) {
        BankInfoCache bankInfoCache = BankInfoCache.getInstance();
        return new GetTimeProcessor(timeProvider, bankInfoCache);
    }

    @Bean
    public SessionTimerStopProcessor sessionTimerStopProcessor(CassandraPersistenceManager persistenceManager) {
        BankInfoCache bankInfoCache = BankInfoCache.getInstance();
        return new SessionTimerStopProcessor(bankInfoCache, persistenceManager);
    }

    @Bean
    public RefreshBalanceProcessor refreshBalanceProcessor(NtpTimeProvider timeProvider) {
        return new RefreshBalanceProcessor(timeProvider);
    }
}

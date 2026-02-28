package com.abs.casino.services;

import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.promo.*;
import com.abs.casino.common.promo.network.NetworkTournamentEvent;
import com.abs.casino.common.promo.network.NetworkTournamentInfo;
import com.abs.casino.common.util.DatePeriod;
import com.abs.casino.common.util.xml.TransientFieldsAllowedProvider;
import com.abs.casino.common.util.xml.TransientFieldsReflectionProvider;
import com.abs.casino.common.util.xml.xstreampool.XStreamPool;
import com.dgphoenix.casino.gs.managers.payment.bonus.CreationBonusHelper;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.services.bonus.ForbiddenGamesForBonusProvider;
import com.abs.casino.services.gamelimits.GameManagerUtils;
import com.abs.casino.services.ServiceUtils;
import com.thoughtworks.xstream.XStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 26.11.2019
 */

@Configuration
public class GameServerServiceConfiguration {

    @Bean
    public GameManagerUtils gameManagerUtils(MQServiceHandler mqServiceHandler) {
        return new GameManagerUtils(BankInfoCache.getInstance(), BaseGameCache.getInstance(), mqServiceHandler);
    }

    @Bean
    public ServiceUtils serviceUtils() {
        return new ServiceUtils(BankInfoCache.getInstance(), CurrencyCache.getInstance());
    }

    @Bean
    public ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider() {
        return new ForbiddenGamesForBonusProvider(SubCasinoCache.getInstance());
    }

    @Bean
    public CreationBonusHelper creationBonusHelper(ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider) {
        return new CreationBonusHelper(BankInfoCache.getInstance(), BaseGameCache.getInstance(),
                BaseGameInfoTemplateCache.getInstance(), forbiddenGamesForBonusProvider);
    }

    @Bean
    public XStreamPool xStreamPool() {
        return new XStreamPool.Builder(() -> {
            XStream xStream = new XStream(new TransientFieldsAllowedProvider());
            xStream.registerConverter(new TransientFieldsReflectionProvider(xStream.getMapper(), xStream.getReflectionProvider()), XStream.PRIORITY_VERY_LOW);
            XStream.setupDefaultSecurity(xStream);
            xStream.ignoreUnknownElements();
            xStream.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**"});
            Stream.of(PromoCampaign.class, TournamentPromoTemplate.class, MaxBalanceTournamentPromoTemplate.class,
                            AbstractPrize.class, TicketPrize.class, FRBonusPrize.class,
                            InstantMoneyPrize.class, CacheBonusPrize.class, DesiredPrize.class, AwardedPrize.class, TournamentPrize.class,
                            GameBonusKey.class, PlayerBetEvent.class, PromoCampaignMember.class, BetAmountPrizeQualifier.class,
                            SpinCountPrizeQualifier.class, AlwaysQualifyBetQualifier.class, ByAmountBetEventQualifier.class, NoPrizeQualifier.class,
                            TournamentRankQualifier.class, TournamentSimpleBetEventQualifier.class, DelegatedEventQualifier.class, DatePeriod.class,
                            RankPrize.class, RankRange.class, TournamentMemberRank.class, TournamentObjective.class,
                            SupportedPlatform.class, NetworkTournamentEvent.class,
                            NetworkTournamentInfo.class, CurrencyRate.class)
                    .forEach(clazz -> xStream.alias(clazz.getSimpleName(), clazz));
            return xStream;
        }).softReferences().build();
    }

}

package com.abs.casino.gs;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.promo.SignificantEventType;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.promo.TournamentRankCalculatorService;
import com.abs.casino.promo.events.process.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Configuration
public class PromotionContextConfiguration {
    @Bean
    public BetEventQualifier betEventQualifier(ICurrencyRateManager currencyRateManager) {
        return new BetEventQualifier(currencyRateManager);
    }

    @Bean
    public WinEventQualifier winEventQualifier(ICurrencyRateManager currencyRateManager) {
        return new WinEventQualifier(currencyRateManager);
    }

    @Bean
    public BonusEventQualifier bonusEventQualifier() {
        return new BonusEventQualifier();
    }

    @Bean
    public EndRoundEventQualifier endRoundEventQualifier(ICurrencyRateManager currencyRateManager) {
        return new EndRoundEventQualifier(currencyRateManager);
    }

    @Bean
    public MqEndRoundEventQualifier mqEndRoundEventQualifier(ICurrencyRateManager currencyRateManager) {
        return new MqEndRoundEventQualifier(currencyRateManager);
    }

    @Bean
    public Map<SignificantEventType, ParticipantEventQualifier> eventQualifiersHolder(List<ParticipantEventQualifier> qualifiers) {
        return qualifiers.stream()
                .collect(Collectors.toMap(ParticipantEventQualifier::getEventType, identity()));
    }

    @Bean
    TournamentRankCalculatorService tournamentRankCalculatorService(CassandraPersistenceManager persistenceManager,
                                                                    ICurrencyRateManager currencyRateManager) {
        return new TournamentRankCalculatorService(persistenceManager, currencyRateManager);
    }

    @Bean
    public ParticipantEventProcessor participantEventProcessor(CassandraPersistenceManager persistenceManager, IPromoCampaignManager promoCampaignManager,
                                                               ICurrencyRateManager currencyRateManager, RemoteCallHelper remoteCallHelper,
                                                               Map<SignificantEventType, ParticipantEventQualifier> eventQualifiersHolder,
                                                               TournamentRankCalculatorService tournamentRankCalculatorService) {
        return new ParticipantEventProcessor(persistenceManager, promoCampaignManager, eventQualifiersHolder, currencyRateManager, remoteCallHelper,
                tournamentRankCalculatorService);
    }

}

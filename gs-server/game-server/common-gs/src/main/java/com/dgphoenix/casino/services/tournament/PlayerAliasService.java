package com.abs.casino.services.tournament;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.PlayerIdentificationType;
import com.abs.casino.common.util.IIntegerIdGenerator;
import com.dgphoenix.casino.common.util.Triple;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.promo.IPlayerAliasService;
import com.abs.casino.promo.persisters.CassandraPromoCampaignMembersPersister;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * User: flsh
 * Date: 30.05.2022.
 */
@Service
public class PlayerAliasService implements IPlayerAliasService {
    private static final Logger LOG = LogManager.getLogger(PlayerAliasService.class);
    //key is promoId/bankId/alias
    private LoadingCache<Triple<Long, Long, String>, Long> aliasesCache;
    private final CassandraPromoCampaignMembersPersister membersPersister;
    private final IIntegerIdGenerator integerIdGenerator;
    private final String clusterId;

    private static final String[] ZERO_ALIGNMENT_ARRAY = new String[]{"0", "00", "000", "0000", "00000", "000000", "0000000", "00000000"};

    public PlayerAliasService(CassandraPersistenceManager cpm, IIntegerIdGenerator integerIdGenerator, int clusterId) {
        this.membersPersister = cpm.getPersister(CassandraPromoCampaignMembersPersister.class);
        this.integerIdGenerator = integerIdGenerator;
        this.clusterId = String.valueOf(clusterId);
    }

    @PostConstruct
    private void init() {
        aliasesCache = CacheBuilder.newBuilder()
                .recordStats()
                .build(new CacheLoader<Triple<Long, Long, String>, Long>() {
                    @Override
                    public Long load(@Nonnull Triple<Long, Long, String> key) {
                        return membersPersister.getPromoAccountId(key.first(), key.second(), key.third());
                    }
                });

        StatisticsManager.getInstance().registerStatisticsGetter("PlayerAliasService : aliasesCache statistics",
                () -> "size=" + aliasesCache.size() + ", stats=" + aliasesCache.stats());
    }

    @Override
    public Long getPromoAccountId(long promoId, long bankId, String alias) {
        return aliasesCache.getUnchecked(new Triple<>(promoId, bankId, alias));
    }

    @Override
    public Map<Long, String> getAllPromoAliases(long promoId) {
        return membersPersister.getAllPromoAliases(promoId);
    }

    @Override
    public Map<Long, String> getAllBankAliases(long promoId, long bankId) {
        return membersPersister.getAllBankAliases(promoId, bankId);
    }

    @Override
    public String generateAlias(IAccountInfo account, IPromoCampaign campaign) throws CommonException  {
        String alias;
        PlayerIdentificationType identificationType = campaign.getPlayerIdentificationType();
        if (!identificationType.isUniqueForEachPromo()) {
            alias = identificationType.getName(account);
        } else if(PlayerIdentificationType.INTEGER_ID_GENERATOR.equals(identificationType)) {
            int promoAliasId = getNextAliasIdForPromo(campaign.getId());
            alias = generate10CharAlias(promoAliasId);
        } else {
            throw new CommonException("Unsupported PlayerIdentificationType=" + identificationType);
        }
        LOG.debug("generateAlias: accountId={}, promoId={}, alias={}", account.getId(), campaign.getId(), alias);
        return alias;
    }

    public String generate10CharAlias(int promoAliasId) throws CommonException {
        if (promoAliasId < 0) {
            throw new CommonException("promoAliasId is negative: " + promoAliasId);
        }
        int aliasLength = String.valueOf(promoAliasId).length();
        int notAlignmentSize = clusterId.length() + aliasLength;
        if(notAlignmentSize == 10) {
            return clusterId + promoAliasId;
        } else if (notAlignmentSize > 10) {
            throw new CommonException("promoAliasId too long: " + promoAliasId);
        }
        int zeroAlignmentSize = 10 - notAlignmentSize;
        return clusterId + ZERO_ALIGNMENT_ARRAY[zeroAlignmentSize - 1] + promoAliasId;
    }

    private int getNextAliasIdForPromo(long campaignId) {
        return integerIdGenerator.getNext(IPromoCampaign.class.getName() + "_" + campaignId);
    }

}

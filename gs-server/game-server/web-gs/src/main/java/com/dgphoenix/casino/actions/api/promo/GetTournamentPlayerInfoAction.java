package com.abs.casino.actions.api.promo;

import com.abs.casino.actions.api.promo.ErrorCodes;
import com.abs.casino.actions.api.promo.GetTournamentPlayerInfoForm;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.promo.TournamentMemberRank;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.XmlApiBaseAction;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.abs.casino.promo.persisters.CassandraTournamentRankPersister;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 03.09.2019.
 */
public class GetTournamentPlayerInfoAction extends XmlApiBaseAction<GetTournamentPlayerInfoForm> {
    private final IPromoCampaignManager promoCampaignManager;
    private final CassandraTournamentRankPersister rankPersister;
    private static final String POINTS_TAG = "POINTS";
    private static final String ROUNDS_TAG = "ROUNDS";
    private static final String BET_SUM_TAG = "BET_SUM";
    private static final String WIN_SUM_TAG = "WIN_SUM";
    //private static final String BONUS_COUNT_TAG = "BONUS_COUNT";

    private static final Logger LOG = Logger.getLogger(GetTournamentPlayerInfoAction.class);

    public GetTournamentPlayerInfoAction() {
        promoCampaignManager = GameServerComponentsHelper.getPromoCampaignManager();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        rankPersister = persistenceManager.getPersister(CassandraTournamentRankPersister.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, GetTournamentPlayerInfoForm form,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("process: form=" + form + ", request=" + request.getQueryString());
        if (form.getBankId() == null) {
            return printErrorResponse(request, response, ErrorCodes.BANK_ID_NOT_SPECIFIED.getCode());
        }
        if (form.getCampaignId() == null) {
            return printErrorResponse(request, response, ErrorCodes.CAMPAIGN_ID_NOT_SPECIFIED.getCode());
        }
        if (StringUtils.isTrimmedEmpty(form.getExtUserId())) {
            return printErrorResponse(request, response, ErrorCodes.USER_NOT_SPECIFIED.getCode());
        }
        TournamentMemberRank rank = null;
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            if (bankInfo == null) {
                return printErrorResponse(request, response, ErrorCodes.BANK_NOT_FOUND.getCode());
            }
            AccountInfo accountInfo = AccountManager.getInstance().getByCompositeKey(bankInfo.getSubCasinoId(),
                    bankInfo, form.getExtUserId());
            if (accountInfo == null) {
                return printErrorResponse(request, response, ErrorCodes.USER_NOT_FOUND.getCode());
            }
            IPromoCampaign promoCampaign = promoCampaignManager.getPromoCampaign(form.getCampaignId());
            if (promoCampaign == null) {
                return printErrorResponse(request, response, ErrorCodes.CAMPAIGN_NOT_FOUND.getCode());
            }
            rank = rankPersister.getForAccount(promoCampaign.getId(), accountInfo.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            return printErrorResponse(request, response, ErrorCodes.UNEXPECTED_ERROR.getCode());
        }
        if (rank == null) {
            return printErrorResponse(request, response, ErrorCodes.RANK_UNKNOWN.getCode());
        }
        Map<String, String> result = new HashMap<>();
        result.put(POINTS_TAG, String.valueOf(rank.getScore()));
        result.put(ROUNDS_TAG, String.valueOf(rank.getRoundsCount()));
        result.put(BET_SUM_TAG, String.valueOf(rank.getBetSum()));
        result.put(WIN_SUM_TAG, String.valueOf(rank.getWinSum()));

        return printSuccessResponse(request, response, result);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected String getRootTag() {
        return GameServerConfiguration.getInstance().getBrandApiRootTagName();
    }
}

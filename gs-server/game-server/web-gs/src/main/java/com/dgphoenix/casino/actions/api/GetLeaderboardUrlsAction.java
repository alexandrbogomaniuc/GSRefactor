package com.abs.casino.actions.api;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.JsonResultForLeaderboardUrls;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class GetLeaderboardUrlsAction extends BaseAction<GetLeaderboardUrlsForm> {
    private final IPromoCampaignManager promoCampaignManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public GetLeaderboardUrlsAction() {
        promoCampaignManager = GameServerComponentsHelper.getPromoCampaignManager();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected ActionForward process(ActionMapping mapping, GetLeaderboardUrlsForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        LOG.debug("Full request is: " + request.getRequestURL() + "?" + request.getQueryString());
        response.setContentType(APPLICATION_JSON.toString());
        SubCasino subCasino = SubCasinoCache.getInstance().getSubCasinoByDomainName(request.getServerName());
        if (subCasino == null) {
            return makeInvalidParameterResponse(response, "Invalid domain");
        }

        String bankIdParam = actionForm.getBankId();
        if (StringUtils.isTrimmedEmpty(bankIdParam)) {
            return makeInvalidParameterResponse(response, "Empty bankId");
        }
        BankInfo bankInfo = BankInfoCache.getInstance().getBank(bankIdParam, subCasino.getId());
        if (bankInfo == null) {
            return makeInvalidParameterResponse(response, "Unknown bankId: " + bankIdParam);
        }

        Set<IPromoCampaign> campaigns = promoCampaignManager.getActive(bankInfo.getId());
        List<String> urls = new ArrayList<>(campaigns.size());
        if (!campaigns.isEmpty()) {
            urls = campaigns.stream()
                    .map(getPromoCampaignUrls(request))
                    .collect(Collectors.toList());
        }
        return makeOkResponse(response, urls);
    }

    private Function<IPromoCampaign, String> getPromoCampaignUrls(HttpServletRequest request) {
        return campaign -> {
            String templateUrl = "/leaderboard.xml";
            return request.getScheme() + "://" + request.getServerName() + "/info/promo/" +
                    campaign.getId() + templateUrl;
        };
    }

    private ActionForward makeInvalidParameterResponse(HttpServletResponse response, String error) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        LOG.error(error);
        String responseBody = mapper.writeValueAsString(JsonResultForLeaderboardUrls.createErrorResult(error));
        response.getWriter().write(responseBody);
        return null;
    }

    private ActionForward makeOkResponse(HttpServletResponse response, List<String> urls) throws IOException {
        LOG.debug("GetLeaderboardUrlsAction: {}", urls);
        String responseBody = mapper.writeValueAsString(JsonResultForLeaderboardUrls.createSuccessResult(urls));
        response.getWriter().write(responseBody);
        return null;
    }
}

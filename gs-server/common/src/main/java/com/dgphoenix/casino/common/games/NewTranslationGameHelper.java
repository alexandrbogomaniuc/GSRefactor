package com.abs.casino.common.games;

import com.abs.casino.common.games.IDelegatedStartGameHelper;
import com.abs.casino.common.games.CdnCheckResult;
import com.abs.casino.common.games.ICassandraHostCdnPersister;
import com.abs.casino.common.games.SwfLocationInfo;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.BaseGameCache;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.abs.casino.common.util.string.StringUtils.isTrimmedEmpty;;

/**
 * User: flsh
 * Date: 26.10.11
 */
public class NewTranslationGameHelper extends AbstractStartGameHelper {
    private static final Logger LOG = Logger.getLogger(NewTranslationGameHelper.class);

    private static List<String> stringToListOfStrings(String ids) {
        List<String> result = new ArrayList<String>();
        if (!StringUtils.isTrimmedEmpty(ids)) {
            StringTokenizer st = new StringTokenizer(ids, ";");
            while (st.hasMoreTokens()) {
                String value = st.nextToken();
                if (!StringUtils.isTrimmedEmpty(value)) {
                    result.add(value.trim());
                }
            }
        }
        return result;
    }

    public NewTranslationGameHelper(long gameId, String servletName, String title, String swfLocation,
                                    String additionalParams, IDelegatedStartGameHelper delegatedHelper,
                                    ICassandraHostCdnPersister hostCdnPersister) {
        super(gameId, servletName, title, swfLocation, additionalParams, delegatedHelper, hostCdnPersister);
    }

    //negative gameServerId used for load from lobby
    @Override
    public SwfLocationInfo getSwfBase(long bankId, String lang, boolean realMode, HttpServletRequest req, long serverId) {

        String cdnCheck = null;
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(getGameId());
        String serverUrl = null;

        IBaseGameInfo gameInfo;
        if (bankInfo != null) {
            gameInfo = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), getGameId(),
                    bankInfo.getDefaultCurrency());
        } else {
            gameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(getGameId());
        }

        String cdn = req.getParameter(BaseAction.KEY_CDN);
        if (bankInfo != null && !bankInfo.getCdnUrlsMap().isEmpty() && bankInfo.isCdnForceAuto()) {
            cdn = "AUTO";
        }
        boolean cdnDisabled = cdn != null && cdn.equalsIgnoreCase("DISABLED");

        if (!cdnDisabled && bankInfo != null && cdn != null) {
            Map<String, String> cdnMap = bankInfo.getCdnUrlsMap();

            if (!cdn.equalsIgnoreCase("AUTO")) {
                serverUrl = cdnMap.get(cdn);
            } else if (cdnMap.size() == 1 && bankInfo.shouldNotUseOriginAsCDN()) {
                serverUrl = cdnMap.values().iterator().next();

            } else if (!cdnMap.isEmpty()) {
                String cndUrl = bankInfo.getCdnUrls();
                List<String> cdnList = stringToListOfStrings(cndUrl);
                ICassandraHostCdnPersister hostCdnPersister = getHostCdnPersister();

                if (!cdnList.isEmpty() && hostCdnPersister != null) {
                    String ip = req.getRemoteAddr();
                    List<CdnCheckResult> hostCdnCheckList = hostCdnPersister.getCdnByIp(ip);

                    if (!hostCdnCheckList.isEmpty()
                            && this.isAllCdnsChecked(hostCdnCheckList, cdnMap)
                            && !this.isCdnCheckExpired(bankInfo, hostCdnCheckList, cdnMap)) {

                        serverUrl = this.getBestCdn(hostCdnCheckList, cdnMap);
                    } else {
                        cdnCheck = cndUrl;
                    }
                }
            }
        }

        if (!cdnDisabled && serverUrl == null) {
            String cdnUrl = gameInfo.getCDNUrl();

            if (cdnUrl != null) {
                serverUrl = cdnUrl.replaceFirst("http://", "").replaceFirst("https://", "");
            }
        }

        boolean useCdn = !isTrimmedEmpty(serverUrl);
        serverUrl = isTrimmedEmpty(serverUrl) ? "" : req.getScheme() + "://" + serverUrl;

        String swfPath = getSwfPath(bankId);
        if (template.isOldTranslation()) {
            String baseUrl = serverUrl + (realMode ? "/real/" : "/free/") + lang + "/";
            return new SwfLocationInfo(serverUrl, baseUrl, serverUrl + swfPath,
                    useCdn, cdnCheck);
        } else {
            return new SwfLocationInfo(serverUrl, serverUrl + swfPath + "/" + lang + "/", serverUrl + swfPath,
                    useCdn, cdnCheck);
        }
    }

    private boolean isAllCdnsChecked(List<CdnCheckResult> hostCdnCheckList, Map<String, String> cdnMap) {
        return this.getCdnNames(hostCdnCheckList).containsAll(cdnMap.values());
    }

    private List<String> getCdnNames(List<CdnCheckResult> hostCdnCheckList) {
        List<String> result = new ArrayList<>();
        for (CdnCheckResult entry : hostCdnCheckList) {
            result.add(entry.getCdnUrl());
        }
        return result;
    }

    private boolean isCdnCheckExpired(BankInfo bankInfo,
                                      List<CdnCheckResult> hostCdnCheckList,
                                      Map<String, String> cdnMap) {

        long expireTime = bankInfo.getCdnExpireTime();

        long now = System.currentTimeMillis();

        for (CdnCheckResult entry : hostCdnCheckList) {
            Long lastUpdate = entry.getLastUpdateTime();

            if (cdnMap.containsValue(entry.getCdnUrl()) && now - lastUpdate > expireTime * 1000) {
                return true;
            }
        }

        return false;
    }

    private String getBestCdn(List<CdnCheckResult> hostCdnCheckList, Map<String, String> cdnMap) {

        CdnCheckResult bestResult = new CdnCheckResult("DISABLED", CdnCheckResult.MAX_LOAD_TIME, 0);

        for (CdnCheckResult result : hostCdnCheckList) {
            if ((cdnMap.containsValue(result.getCdnUrl()) || "DISABLED".equals(result.getCdnUrl())) &&
                    bestResult.getLoadTime() > result.getLoadTime()) {
                bestResult = result;
            }
        }

        String bestCdnUrl = bestResult.getCdnUrl();
        if (!"DISABLED".equals(bestCdnUrl) && bestResult.getLoadTime() != CdnCheckResult.MAX_LOAD_TIME) {
            return bestCdnUrl;
        }

        return "";
    }
}

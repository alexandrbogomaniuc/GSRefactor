package com.abs.casino.actions.enter.game.frb;


import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.abs.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class RestartGameAction extends Action {

    private static final Logger LOG = LogManager.getLogger(RestartGameAction.class);


    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long now = System.currentTimeMillis();
        RestartGameForm restartGameForm = (RestartGameForm) form;
        LOG.debug("execute: " + restartGameForm);
        long bankId = restartGameForm.getBankId();
        long gameId = restartGameForm.getGameId();
        String sessionId = restartGameForm.getSessionId();
        String lang = restartGameForm.getLang();
        String mode = restartGameForm.getMode();
        long bonusId = restartGameForm.getBonusId();
        boolean isStandalone = Boolean.TRUE.toString().equalsIgnoreCase(request.getParameter(BaseAction.STANDALONE));
        String homeUrl = request.getParameter(BaseAction.PARAM_HOME_URL);
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            LOG.warn("Bank not found in cache for restart request, bankId={}, gameId={}, sessionId={}", bankId, gameId, sessionId);
            ActionRedirect fallback = new ActionRedirect(request.getScheme() + "://" + request.getServerName() + "/cwstartgamev2.do");
            fallback.addParameter("bankId", bankId);
            fallback.addParameter("gameId", gameId);
            fallback.addParameter("mode", mode);
            fallback.addParameter("token", sessionId);
            if (!isTrimmedEmpty(lang)) {
                fallback.addParameter("lang", lang);
            }
            return fallback;
        }
        String walletPMClass = bankInfo.getWPMClass();
        String paymentProcessor = bankInfo.getPPClass();

        String url;
        String startGameDomain = bankInfo.getStartGameDomain();
        if (StringUtils.isTrimmedEmpty(startGameDomain)) {
            LOG.warn("startGameDomain not found for bank: " + bankId + ", use request.getServerName()");
            url = request.getScheme() + "://" + request.getServerName() + "/";
        } else {
            url = request.getScheme() + "://" + startGameDomain + "/";
        }

        if (bonusId > 0) {
            url += "red7cbtbsstartgame.do";
        } else if (bankInfo.getId() == 92) { //For 7Red casino
            url += "red7startgame.do";
        } else if (bankId == 160) { //hack for FB
            url += "startGame.do";
        } else if (bankId == 675) { //Demo Cluster
            url += "cwstartstgame.do";
        } else if (!StringUtils.isTrimmedEmpty(walletPMClass)) {
            url += (isStandalone ? "cwstartstgame.do" : "cwstartgame.do");
/*
            if (walletPMClass.contains("commonv2")) {
                url += "cwstartgame.do";
            } else if (walletPMClass.contains("commonv3")) {
                //url += "cwstartgamev2.do";
            	url += "cwstartgame.do";
            } else if (walletPMClass.contains("commonv4")) {
                //url += "cwstartgamev2.do";
            	url += "cwstartgame.do";
            } else {
                throw new CommonException("Wallet Session Manager is not defined");
            }
*/
        } else if (!StringUtils.isTrimmedEmpty(paymentProcessor)
                && paymentProcessor.contains("PTPT")) { //for ptpt-like integrations
            url += "startgame.do";
        } else {//CT
            url += "ctstartgame.do";
        }
        ActionRedirect redirect = new ActionRedirect(url);
        if (isStandalone) {
            redirect.addParameter("SID", sessionId);
            redirect.addParameter("updateBalance", Boolean.TRUE.toString());
        }
        redirect.addParameter("bankId", bankInfo.getExternalBankId());
        redirect.addParameter("gameId", gameId);
        redirect.addParameter("sessionId", sessionId);
        redirect.addParameter("lang", lang);
        redirect.addParameter("mode", mode);
        if (!StringUtils.isTrimmedEmpty(homeUrl)) {
            redirect.addParameter(BaseAction.PARAM_HOME_URL, homeUrl);
        }
        if (bonusId > 0) {
            redirect.addParameter("bonusId", bonusId);
        }

        if (restartGameForm.getBalance() > 0) {
            redirect.addParameter("balance", restartGameForm.getBalance());
        }
        String cdn = request.getParameter(BaseAction.KEY_CDN);
        if (cdn != null) {
            redirect.addParameter(BaseAction.KEY_CDN, cdn);
        }
        String platform = request.getParameter("platform");
        boolean isForceHtml5 = !StringUtils.isTrimmedEmpty(platform) && "html5".equalsIgnoreCase(platform);
        if (isForceHtml5) {
            redirect.addParameter(BaseAction.PLATFORM, "html5");
        }
        boolean isForceFlash = !StringUtils.isTrimmedEmpty(platform) && "flash".equalsIgnoreCase(platform);
        if (isForceFlash) {
            redirect.addParameter(BaseAction.PLATFORM, "flash");
        }
        String keepAlive = request.getParameter(BaseAction.PARAM_KEEPALIVE_URL);
        if (!isTrimmedEmpty(keepAlive)) {
            redirect.addParameter(BaseAction.PARAM_KEEPALIVE_URL, keepAlive);
        }
        String gameHistoryUrl = request.getParameter(BaseAction.GAME_HISTORY_URL);
        if (!isTrimmedEmpty(gameHistoryUrl)) {
            redirect.addParameter(BaseAction.GAME_HISTORY_URL, gameHistoryUrl);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                ":execute success", System.currentTimeMillis() - now);

        return redirect;

    }


}

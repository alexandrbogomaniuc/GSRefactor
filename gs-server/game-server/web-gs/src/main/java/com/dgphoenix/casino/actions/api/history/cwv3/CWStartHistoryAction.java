package com.abs.casino.actions.api.history.cwv3;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.abs.casino.exceptions.LoginErrorException;
import com.abs.casino.forms.api.history.cwv3.CWStartHistoryForm;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.helpers.login.CWv3Helper;
import com.abs.casino.sm.login.GameLoginRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CWStartHistoryAction extends BaseAction<CWStartHistoryForm> {

    private static final Logger LOG = LogManager.getLogger(CWStartHistoryAction.class);
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    private GameLoginRequest createLoginRequest(CWStartHistoryForm form) throws Exception {
        GameLoginRequest loginRequest = new GameLoginRequest();
        loginRequest.setToken(form.getToken());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setClientType(form.getClientType());
        if (!StringUtils.isTrimmedEmpty(form.getGameId())) {
            loginRequest.setGameId(Integer.parseInt(form.getGameId()));
        }
        return loginRequest;
    }

    @Override
    protected ActionForward process(ActionMapping mapping, CWStartHistoryForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        String sessionId = null;
        // login
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            boolean dontLogout = bankInfo.isDontLogoutOnStartHistory();
            SessionInfo sessionInfo = CWv3Helper.getInstance().login(createLoginRequest(form), !dontLogout).
                    getSessionInfo();
            if (sessionInfo == null) {
                throw new LoginErrorException(CommonWalletErrors.INTERNAL_ERROR);
            }
            sessionId = sessionInfo.getSessionId();
        } catch (LoginErrorException e) {
            LOG.error("could not login: " + e.getDescription(), e);
            return mapping.findForward(ERROR_FORWARD);
        }

        LOG.info("CWStartHistoryAction::sessionId {}", sessionId);

        return getForward(request, sessionId, form);

    }

    protected ActionForward getForward(HttpServletRequest request, String sessionId, CWStartHistoryForm form) throws ParseException {
        String url = "/handhistory.jsp";
        ActionRedirect redirect = BaseAction.getActionRedirect(request, url);
        redirect.addParameter("sessionId", sessionId);
        Map<String, String> aParameters = getAddParameters(form.getStartDate(), form.getEndDate(), form.getGameId());
        for (Map.Entry<String, String> aParameter : aParameters.entrySet()) {
            redirect.addParameter(aParameter.getKey(), aParameter.getValue());
        }
        if (!StringUtils.isTrimmedEmpty(form.getLang())) {
            redirect.addParameter("lang", form.getLang());
        }
        return redirect;
    }

    private Map<String, String> getAddParameters(String sdate, String edate, String gameId) throws ParseException {
        Map<String, String> date = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        if (!StringUtils.isTrimmedEmpty(sdate)) {
            calendar.setTime(formatter.parse(sdate));
            date.put("startDay", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            date.put("startMonth", String.valueOf(calendar.get(Calendar.MONTH) + 1));
            date.put("startYear", String.valueOf(calendar.get(Calendar.YEAR)));
            if (!StringUtils.isTrimmedEmpty(edate)) {
                calendar.setTime(formatter.parse(edate));
            } else {
                calendar.setTimeInMillis(System.currentTimeMillis());
            }
            date.put("endDay", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            date.put("endMonth", String.valueOf(calendar.get(Calendar.MONTH) + 1));
            date.put("endYear", String.valueOf(calendar.get(Calendar.YEAR)));
        }
        if (!StringUtils.isTrimmedEmpty(gameId)) {
            date.put("gameId", gameId);
        } else {
            if (sdate != null) {
                date.put("gameId", String.valueOf(-1L));
            }
        }
        return date;
    }
}
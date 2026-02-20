package com.dgphoenix.casino.common.web;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.StreamUtils;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.joining;

/**
 * User: flsh
 * Date: 16.04.2009
 */
public abstract class BaseAction<T extends ActionForm> extends Action {
    protected static final Logger LOG = LogManager.getLogger(BaseAction.class);
    protected static final String HTTP = "http";

    public static final String SUCCESS_FORWARD = "success";
    public static final String ERROR_FORWARD = "common_error";

    public static final String ACCOUNT_INFO = "ACCOUNT_INFO";
    public static final String ACCOUNT_ID_ATTRIBUTE = "AID";
    public static final String SESSION_ID_ATTRIBUTE = "SID";
    public static final String PLAYER_SESSION_ATTRIBUTE = "PLAYER_SESSION_ATTRIBUTE";

    public static final String BANK_ID_ATTRIBUTE = "BANKID";
    public static final String GAME_ID_ATTRIBUTE = "gameId";
    public static final String GAMEMODE_ATTRIBUTE = "MODE";

    public static final String GAMESERVERID_ATTRIBUTE = "GAMESERVERID";
    public static final String GAMESERVER_URL_ATTRIBUTE = "GAMESERVERURL";
    public static final String LANG_ID_ATTRIBUTE = "LANG";
    public static final String SHOW_REDIRECTED_UNFINISHED_GAME_MESSAGE = "SHOW_REDIRECTED_UNFINISHED_GAME_MESSAGE";
    public static final String TOKEN_ATTRIBUTE = "TOKEN";
    public static final String PLATFORM = "platform";
    public static final String PROFILE_ID = "profileId";
    public static final String REAL_GAME_URL = "realGameUrl";

    //UniversalNetworkJackpot.id
    public static final String UNJ_ID = "UNJ_ID";
    public static final String UNJ_NAME = "UNJ_NAME";
    public static final String UNJ_AMOUNT = "UNJ_AMOUNT";
    public static final String UNJ_WIN = "UNJ_WIN";
    public static final String UNJ_WIN_ANNOUNCE = "UNJ_WIN_ANNOUNCE";
    public static final String UNJ_SHARED_GAME_STATE = "UNJ_SHARED_GAME_STATE";
    public static final String UNJ_DELIM = ";";
    //For the new UNJ games (e.g. Legend of the Nile) UNJ_TRANSFERRED property should be set to FALSE
    //and game property BaseGameConstants.KEY_HANDLE_UNJ_WIN must be TRUE
    public static final String UNJ_TRANSFERRED = "UNJ_TRANSFERRED";

    public static final String BALANCE_ATTRIBUTE = "BALANCE";
    public static final String PROMOS_INFO_ATTRIBUTE = "PROMOSINF";

    public static final String MESSAGE_TYPE_INFO = "info";
    public static final String MESSAGE_TYPE_ERROR = "error";

    public static final String KEY_CDN = "CDN";
    public static final String RESPONSE_STATUS_SUCCESS = "SUCCESS";
    public static final String RESPONSE_STATUS_SERVER_ERROR = "GENERAL_SERVER_ERROR";
    public static final String GAME_LAUNCHER_JSP = "launch.jsp";
    public static final String STANDALONE = "stndl";
    public static final String PARAM_HOME_URL = "homeUrl";
    public static final String PARAM_CASHIER_URL = "cashierUrl";
    public static final String UNKNOWN_HOST = "UNKNOWNHOST";
    public static final String PARAM_SWF_PATH = "swfPath";   // Flash Name path
    public static final String PARAM_REDIRECT_TO_SWF = "redirectToSwf";
    public static final String PARAM_CONTEXT = "startGameContext";
    public static final String PARAM_SOUND = "sound";
    public static final String ERROR_INFO_ATTRIBUTE = "ERROR_INFO_ATTRIBUTE";
    public static final String PARAM_KEEPALIVE_URL = "keepAliveURL";

    public static final String FUNC_REDIRECT_TO_REGISTRATION = "redirectToRegistr";
    public static final String FLASH_PARAM_BANK_ID = "BANKID";
    public static final String FLASH_PARAM_FUNC_TO_REGISTRATION = "JS_TOREGISTRATION_FUNC_NAME";

    public static final String PARAM_SHELL_PATH = "ShellPath";
    public static final String PARAM_BONUS_ID = "BonusId";
    public static final String PARAM_FRBONUS_ID = "FRBonusId";
    public static final String PARAM_TOURNAMENT_ID = "TournamentId";

    public static final String PROMO_IDS = "PROMO_IDS";
    public static final String ACTIVE_PROMO_IDS = "ACTIVE_PROMO_IDS";
    public static final String PROMO_URL = "PROMO_URL";
    public static final String WEB_SOCKET_URL = "WEB_SOCKET_URL";
    public static final String SOCKET_POLICY_PORT = "SOCKET_POLICY_PORT";
    public static final String PROMO_DETAILS_URL = "PROMO_DETAILS_URL";
    public static final String SHOW_PROMO_BAR = "SHOW_PROMO_BAR";
    public static final String PROMO_IDS_DELIMITER = "|";

    public static final String SUPPORT_TICKET_ID_ATTRIBUTE = "supportTicketID";
    public static final String GAME_HISTORY_URL = "gameHistoryUrl";
    public static final String AUTOPLAY_ALLOWED = "autoplayAllowed";
    public static final String PLAYER_LOGIN_TIME = "playerLoginTime";
    public static final String UNIVERSAL_GAME_ENGINE_PARAMS_PREFIX = "ngnu_";
    public static final String REALITY_CHECK_INTERVAL = "rcInterval";
    public static final String HELP_URL = "helpUrl";
    public static final String REAL_MODE_URL = "realModeUrl";

    public static final String LANG_CODE = "langCode";
    public static final String DONT_LOAD_URL_FROM_PARENT = "dontLoadUrlFromParent";
    public static final String CUSTOM_HOME_URL = "customHomeUrl";
    public static final String CUSTOM_CASHIER_URL = "customCashierUrl";
    public static final String EXT_SESSION_DATA = "extSessionData";
    public static final String CLIENT_TOKEN = "clientToken";
    public static final String SHOW_BATTLEGROUND_TAB = "showBattlegroundTab";

    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long now = System.currentTimeMillis();
        String log = "Start process request, URI = " + request.getRequestURI();
        log += request.getMethod().equals("GET") ?
                ", query string = " + request.getQueryString() :
                ", parameters = " + getRequestParametersAsString(request);
        debug(log);
        ActionForward forward;
        try {
            forward = process(mapping, (T) actionForm, request, response);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":execute success", System.currentTimeMillis() - now);
            if (forward == null) {
                return null; //content may be directly written to httpResponse
            }
        } catch (Exception e) {
            error(null, e);
            forward = mapping.findForward(ERROR_FORWARD);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    ":execute failed", System.currentTimeMillis() - now);
        }
        return forward;
    }

    protected abstract ActionForward process(ActionMapping mapping, T actionForm,
                                             HttpServletRequest request, HttpServletResponse response)
            throws Exception;

    protected void error(String s, Throwable e) {
        getLog().error("{}:: action error: {}", this.getClass().getSimpleName(), s == null ? "" : s, e);
    }

    protected void error(String s) {
        getLog().error("{}:: action error: {}", this.getClass().getSimpleName(), s == null ? "" : s);
    }

    protected void warn(String s) {
        getLog().warn("{}:: action error: {}", this.getClass().getSimpleName(), s == null ? "" : s);
    }

    protected void debug(String s) {
        getLog().debug("{}:: {}", this.getClass().getSimpleName(), s);
    }

    protected void info(String s) {
        getLog().info("{}:: {}", this.getClass().getSimpleName(), s);
    }

    private void addError(HttpServletRequest request, String key, String param, Exception exception,
                          Long exceptionTime, boolean shouldBePersisted, boolean addSupportTicket) {
        ActionMessages messages = getErrors(request);
        ActionMessage actionMessage;
        if (param != null) {
            actionMessage = new ActionMessage(key, param);
        } else if (key != null) {
            actionMessage = new ActionMessage(key);
        } else {
            actionMessage = new ActionMessage(exception.getMessage(), false);
        }
        if (addSupportTicket) {
            String supportTicketId = (String) request.getAttribute(SUPPORT_TICKET_ID_ATTRIBUTE);
            if (supportTicketId == null) {
                supportTicketId = StringIdGenerator.generateSession(32).toUpperCase();
                request.setAttribute(SUPPORT_TICKET_ID_ATTRIBUTE, supportTicketId);
            }
            error("Add supportTicketID: " + supportTicketId);
        }
        messages.add(MESSAGE_TYPE_ERROR, actionMessage);
        request.setAttribute(Globals.ERROR_KEY, MESSAGE_TYPE_ERROR);
        saveErrors(request, messages);
        if (shouldBePersisted) {
            persistError(request, exception, exceptionTime);
        }
        debug(messages.toString());
    }

    public void addError(HttpServletRequest request, String key) {
        addError(request, key, null, null, null, false, true);
    }

    protected void addError(HttpServletRequest request, String key, String param) {
        addError(request, key, param, null, null, false, true);
    }

    protected void addErrorWithPersistence(HttpServletRequest request, Exception exception, long exceptionTime,
                                           boolean addSupportTicket) {
        addError(request, null, null, exception, exceptionTime, true, addSupportTicket);
    }

    protected void addErrorWithPersistence(HttpServletRequest request, String key, Exception exception,
                                           long exceptionTime) {
        addError(request, key, null, exception, exceptionTime, true, true);
    }

    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
    }

    protected void addInfo(HttpServletRequest request, String key) {
        ActionMessages messages = getMessages(request);
        ActionMessage actionMessage;
        actionMessage = new ActionMessage(key);
        messages.add(MESSAGE_TYPE_INFO, actionMessage);
        saveMessages(request, messages);
    }

    //use this methods for get localizated string from java code

    protected String getMessage(String key, HttpServletRequest request) {
        return MessageManager.getInstance().getApplicationMessage(getLocale(request), key);
    }

    protected String getMessage(String key) {
        return MessageManager.getInstance().getApplicationMessage(key);
    }

    protected DateFormat getDateFormat(String key, HttpServletRequest request) {
        return new SimpleDateFormat(getMessage(key, request));
    }

    public String getLanguage(HttpServletRequest request) {
        Locale locale = request.getLocale();
        if (locale == null) {
            locale = (Locale) getServlet().getServletConfig().getServletContext().
                    getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        }
        return locale == null ? Locale.getDefault().getLanguage() : locale.getLanguage();
    }

    public static String getMandatoryParameter(HttpServletRequest request, String paramName) throws CommonException {
        String param = BaseAction.extractRequestParameterIgnoreCase(request, paramName);
        if (StringUtils.isTrimmedEmpty(param)) {
            throw new CommonException("Mandatory parameter not found: '" + paramName + "'");
        }
        return param;
    }

    public static String extractRequestParameterIgnoreCase(HttpServletRequest request, String parameterName) {
        final String value = request.getParameter(parameterName);
        if (!StringUtils.isTrimmedEmpty(value)) {
            return value;
        }

        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
            if (parameterName.equalsIgnoreCase(parameterEntry.getKey())) {
                String[] values = parameterEntry.getValue();
                if (values != null) {
                    return values[0];
                }
            }
        }

        return null;
    }

    /**
     * Return parameters from request to string view, like as GET-request:
     * param1=1&param2=2&param2=56
     */
    public static String getRequestParametersAsString(HttpServletRequest request) {
        StringBuilder result = new StringBuilder();
        if (request == null) return null;

        Map<String, String[]> parameters = request.getParameterMap();
        Iterator<Map.Entry<String, String[]>> iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> entry = iterator.next();
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            result.append(paramName).append("=");

            for (int i = 0; i < paramValues.length; i++) {
                if (!StringUtils.isTrimmedEmpty(paramValues[i])) {
                    if (i > 0) { // In case like that: a=b&a=c
                        result.append("&").append(paramName).append("=");
                    }
                    result.append(paramValues[i]);
                }
            }

            if (iterator.hasNext()) {
                result.append("&");
            }
        }
        return result.toString();
    }

    protected void applyLangToGameSession(Long gameSessionId, String lang) {
        GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
        if (gameSessionId == null || StringUtils.isTrimmedEmpty(lang) || gameSession.getId() != gameSessionId) {
            return;
        }
        gameSession.setLang(lang);
    }

    protected Logger getLog() {
        return LOG;
    }

    public static ActionRedirect getActionRedirect(HttpServletRequest request, String path) {
        return new ActionRedirect(request.getScheme() + "://" + request.getServerName() +
                (path.startsWith("/") ? path : ("/" + path)));
    }

    public static ActionRedirect getActionRedirectByHost(HttpServletRequest request, String path) {
        return new ActionRedirect(request.getScheme() + "://" + request.getHeader("Host") +
                (path.startsWith("/") ? path : ("/" + path)));
    }

    public static UnaryOperator<String> getArrayValueConverter(String valueDelimiter, int fractionMultiplier) {
        return arrayStr -> StreamUtils.asStream(arrayStr, valueDelimiter)
                .map(Double::parseDouble)
                .map(value -> DigitFormatter.denominateMoney(value, fractionMultiplier))
                .map(DigitFormatter::doubleToMoney)
                .collect(joining(valueDelimiter));
    }
}

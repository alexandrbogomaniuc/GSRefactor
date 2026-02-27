package com.abs.casino.actions.lobby;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.property.PropertyUtils;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.MobileDetector;
import com.abs.casino.entities.lobby.LoginHelper;
import com.abs.casino.entities.lobby.ShortGameInfo;
import com.abs.casino.entities.lobby.StLobbyMode;
import com.abs.casino.exceptions.LoginErrorException;
import com.abs.casino.forms.lobby.STLobbyForm;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.sm.login.CWLoginRequest;
import com.abs.casino.sm.login.GameLoginRequest;
import com.abs.casino.sm.login.LoginRequest;
import com.abs.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.dgphoenix.casino.common.configuration.messages.MessageManager.GAME_NAME_PREFIX;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: Grien
 * Date: 13.10.2011 16:08
 */

/**
 * Contains next action:    CWGuestStartLobbyAction
 * CWStartLobbyAction
 * StandaloneLobbyAction
 */
public class STLobbyAction<F extends STLobbyForm> extends BaseAction<F> {
    public static final String AVAILABLE_MODE = "AVAILABLE_MODE";
    public static final String GAMES = "GAMES";
    public static final String OPEN_GAME_LINK = "OPEN_GAME_LINK";
    public static final String OPEN_BONUS_GAME_LINK = "OPEN_BONUS_GAME_LINK";
    public static final String STANDALONE_JSP_NAME = "STANDALONE_JSP_NAME";
    public static final String NEED_LOGOUT_GAME = "NEED_LOGOUT_GAME";
    public static final String ACCOUNT_EXTERNAL_ID = "AccountExternalId";
    public static final String CURRENCY_CODE = "CurrencyCode";
    protected static final Logger LOG = LogManager.getLogger(STLobbyAction.class);
    private static final String standaloneGameOpener = "cwstartstgame.do";
    private static final String standaloneBonusGameOpener = "bsstartgame.do";
    private static final String defaultStandaloneLobbyJspName = "/defaultstlobby.jsp";

    protected CWLoginRequest createCWLoginGuestRequest(F form, HttpServletRequest request) {
        CWLoginRequest loginRequest = new CWLoginRequest();
        loginRequest.setIsGuest(true);
        loginRequest.setFakeExternalSessionId(StringIdGenerator.generateTimeAndRandomBased());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setRemoteHost(request.getRemoteHost());
        loginRequest.setClientType(form.getClientType());

        return loginRequest;
    }

    protected CWLoginRequest createCWLoginRequest(F form, HttpServletRequest request) {
        CWLoginRequest loginRequest = new CWLoginRequest();
        loginRequest.setCheckBalance(false);
        loginRequest.setToken(form.getToken());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setRemoteHost(request.getRemoteHost());
        loginRequest.setClientType(form.getClientType());

        return loginRequest;
    }

    protected GameLoginRequest createCWv3LoginRequest(F form, HttpServletRequest request) {
        GameLoginRequest loginRequest = new GameLoginRequest();
        loginRequest.setToken(form.getToken());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setGameMode(GameMode.getByName(form.getMode()));
        loginRequest.setRemoteHost(request.getRemoteHost());
        loginRequest.setClientType(form.getClientType());

        return loginRequest;
    }

    protected LoginRequest createCTLoginRequest(F form, HttpServletRequest request) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setToken(form.getToken());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setRemoteHost(request.getRemoteHost());
        loginRequest.setClientType(form.getClientType());

        return loginRequest;
    }

    @Override
    protected ActionForward process(ActionMapping mapping, F form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        AccountInfo account;
        String sessionId = form.getSID();
        LoginHelper helper = form.getHelper();
        if (isTrimmedEmpty(sessionId)) {
            //if get not from stlobby.do
            LoginResponse loginResponse;
            try {
                loginResponse = getLoginResponse(helper, form, request);
            } catch (LoginErrorException e) {
                LOG.error("Could not login. form={}", form, e);
                return mapping.findForward(ERROR_FORWARD);
            }
            sessionId = loginResponse.getSessionInfo().getSessionId();
            account = loginResponse.getAccountInfo();
        } else {
            SessionHelper.getInstance().lock(sessionId);
            try {
                SessionHelper.getInstance().openSession();
                account = SessionHelper.getInstance().getTransactionData().getAccount();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }

        try {
            long time = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("form={}", form);
            }
            Integer bankId = form.getBankId();
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAccount: {}, time={}", sessionId, (System.currentTimeMillis() - time));
            }
            time = System.currentTimeMillis();

            if (account == null || account.isGuest()) {
                form.setCurrentMode(new StLobbyMode(GameMode.FREE, null).getKey());
            } else {
                collectModeValues(request, account);
            }
            StLobbyMode stLobbyMode = form.getCurrentModeValue();
            GameMode mode = stLobbyMode.getGameMode();
            Long bonusId = stLobbyMode.getBonusId();
            String lang = form.getLang();
            if (LOG.isDebugEnabled()) {
                LOG.debug("before collectGameInfos: {}, time={}", sessionId, (System.currentTimeMillis() - time));
            }
            time = System.currentTimeMillis();

            String userAgent = request.getHeader("User-Agent");
            PlayerDeviceType deviceType = MobileDetector.getPlayerDeviceType(userAgent);
            boolean isMobile = !deviceType.equals(PlayerDeviceType.PC);
            if (isMobile) {
                collectGameInfos(request, bankId, mode, bonusId, request.getLocale(), deviceType);
            } else {
                collectGameInfos(request, bankId, mode, bonusId, request.getLocale());
            }

            String externalBankId = BankInfoCache.getInstance().getBankInfo(bankId).getExternalBankId();
            if (LOG.isDebugEnabled()) {
                LOG.debug("after collectGameInfos: {}, time={}", sessionId, (System.currentTimeMillis() - time));
            }
            time = System.currentTimeMillis();

            String openGameLink = getStandaloneGameOpenerLink(request,
                    getStandaloneGameOpenerParameters(request, mode, bonusId, lang, sessionId, externalBankId, form));

            if (!isTrimmedEmpty(openGameLink)) {
                request.setAttribute(OPEN_GAME_LINK, openGameLink);
            } else {
                throw new CommonException("openGameLink is empty");
            }

            String openBonusGameLink = getStandaloneBonusGameOpenerLink(request,
                getStandaloneBonusGameOpenerParameters(request, mode, bonusId, lang, sessionId, externalBankId, form));
            request.setAttribute(OPEN_BONUS_GAME_LINK, openBonusGameLink);

            if (LOG.isDebugEnabled()) {
                LOG.debug("createOpenGameLink: {}, time={}", sessionId, (System.currentTimeMillis() - time));
            }
            time = System.currentTimeMillis();

            String jspName = collectStandaloneLobbyJspName(request, bankId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("collectStandaloneLobbyJspName: {}, time={}, jspName={}, mode={}, form={}",
                    sessionId, (System.currentTimeMillis() - time), jspName, mode, form);
            }
            if (BankInfoCache.getInstance().getBankInfo(bankId).isStandaloneLobbyNeedLogoutGame()) {
                request.setAttribute(NEED_LOGOUT_GAME, true);
            }
            if (account != null) {
                request.setAttribute(ACCOUNT_EXTERNAL_ID, account.getExternalId());
                request.setAttribute(CURRENCY_CODE, account.getCurrency().getCode());
            }
            request.setAttribute(BANK_ID_ATTRIBUTE, bankId);
            if (mode == null) {
                request.setAttribute(GAMEMODE_ATTRIBUTE, form.getCurrentMode());
            } else {
                request.setAttribute(GAMEMODE_ATTRIBUTE, mode.getModePath());
            }
            request.setAttribute(SESSION_ID_ATTRIBUTE, sessionId);
            request.setAttribute(LANG_ID_ATTRIBUTE, form.getLang());
            request.setAttribute(TOKEN_ATTRIBUTE, form.getToken());

            ActionForward forward = mapping.findForward("success");
            if (isMobile) {
                forward = mapping.findForward("success_mobile");
            }
            return forward;

        } catch (CommonException e) {
            LOG.error("::execute() exception", e);
            return new ActionForward("/error_pages/sessionerror.jsp");
        }
    }

    protected LoginResponse getLoginResponse(LoginHelper helper, F form, HttpServletRequest request) throws LoginErrorException {
        switch (helper) {
            case GUEST:
                return helper.getHelper().login(createCWLoginGuestRequest(form, request));
            case CW:
                return helper.getHelper().login(createCWLoginRequest(form, request));
            case CWv3:
                return helper.getHelper().login(createCWv3LoginRequest(form, request));
            default:
                LOG.error("Not found login helper");
                throw new LoginErrorException(CommonWalletErrors.INTERNAL_ERROR);
        }
    }

    private String collectStandaloneLobbyJspName(HttpServletRequest request, Integer bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String jspName = bankInfo.getStandaloneLobbyJspName();
        if (isTrimmedEmpty(jspName)) {
            jspName = defaultStandaloneLobbyJspName;
        }
        request.setAttribute(STANDALONE_JSP_NAME, jspName);
        return jspName;
    }

    protected String getStandaloneGameOpener() {
        return standaloneGameOpener;
    }

    protected Map<String, String> getStandaloneGameOpenerParameters(HttpServletRequest request,
                                                                    GameMode mode, Long bonusId, String lang,
                                                                    String sessionId, String externalBankId, F form) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put(BaseAction.SESSION_ID_ATTRIBUTE, sessionId);
        parameters.put(BaseAction.BANK_ID_ATTRIBUTE, externalBankId);
        parameters.put(BaseAction.GAMEMODE_ATTRIBUTE, mode.name());
        parameters.put(BaseAction.LANG_ID_ATTRIBUTE, lang);
        parameters.put("updateBalance", "true");
        if (mode == GameMode.BONUS) {
            parameters.put("bonusId", String.valueOf(bonusId));
        }
        parameters.put(GAME_ID_ATTRIBUTE, "");

        return parameters;
    }

    protected String getOpenerLink(HttpServletRequest request, Map<String, String> parameters, String opener) {

        StringBuilder builder = new StringBuilder();
        builder.append(opener).append("?");

        String cdn = request.getParameter(KEY_CDN);
        if (!isTrimmedEmpty(cdn)) {
            builder.append(KEY_CDN).append("=").append(cdn);
        }

        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (builder.length() != opener.length() + 1) {
                    builder.append("&");
                }
                builder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return builder.toString();
    }


    protected String getStandaloneGameOpenerLink(HttpServletRequest request, Map<String, String> parameters) {
        return getOpenerLink(request, parameters, getStandaloneGameOpener());
    }

    protected Map<String, String> getStandaloneBonusGameOpenerParameters(HttpServletRequest request,
                                                                    GameMode mode, Long bonusId, String lang,
                                                                    String sessionId, String externalBankId, F form) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("sessionId", sessionId);
        parameters.put(TOKEN_ATTRIBUTE, form.getToken());
        parameters.putAll(getStandaloneGameOpenerParameters(request, mode, bonusId, lang, sessionId, externalBankId, form));
        parameters.remove("updateBalance");
        parameters.remove(SESSION_ID_ATTRIBUTE);
        return parameters;
    }

    protected String getStandaloneBonusGameOpener() {
        return standaloneBonusGameOpener;
    }

    protected String getStandaloneBonusGameOpenerLink(HttpServletRequest request, Map<String, String> parameters) {
        return getOpenerLink(request, parameters, getStandaloneBonusGameOpener());
    }


    private void collectGameInfos(HttpServletRequest request, long bankId, GameMode gameMode, Long bonusId,
                                  Locale curLocale) throws CommonException {
        Collection<Long> gameIds = BaseGameCache.getInstance().getAllGamesSet(bankId, null);
        if (gameMode == GameMode.BONUS) {
            Bonus bonus;
            if (bonusId != null && (bonus = BonusManager.getInstance().getById(bonusId)) != null
                    && bonus.getStatus() == BonusStatus.ACTIVE) {
                gameIds = bonus.getValidGameIds(gameIds);
            } else {
                throw new BonusException("bonus is not found");
            }
        }

        List<IBaseGameInfo> gamesInfos = new ArrayList<>();
        for (Long gameId : gameIds) {
            IBaseGameInfo game = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, null);

            if (game == null || game.isMobile()) {
                continue;
            }

            if (game.getId() == 298 || game.getId() == 299
                    || game.getId() == 310 // ATTHECOPASMALLMOBILE (fake for small jackpot) 	N/A
                    || game.getId() == 311 // ATTHECOPAMIDDLEMOBILE (fake for middle jackpot) 	N/A
                    || game.getId() == 313 // ATTHECOPASMALLANDROID (fake for small jackpot) 	N/A
                    || game.getId() == 314 // ATTHECOPAMIDDLEANDROID (fake for middle jackpot) 	N/A
                    || game.getId() == 359 // ATTHECOPASMALLWINDOWSPHONE 	development
                    || game.getId() == 360 // ATTHECOPAMIDDLEWINDOWSPHONE 	development
                    || game.getName().contains("MOBILE")
                    || game.getName().contains("ANDROID")
                    || game.getName().contains("WINDOWSPHONE")
            ) {
                continue;
            }

            gamesInfos.add(game);
        }

        final Locale locale = curLocale;
        gamesInfos.sort((o1, o2) -> {
            if (o1.getGroup().getGroupName().compareTo(o2.getGroup().getGroupName()) == 0) {
                return getGameFullName(o1.getName(), o1.getId(), locale).compareTo(getGameFullName(o2.getName(), o2.getId(), locale));
            } else {
                return o1.getGroup().getGroupName().compareTo(o2.getGroup().getGroupName());
            }
        });

        Map<GameGroup, List<IBaseGameInfo>> gamesMap = new EnumMap<>(GameGroup.class);
        for (IBaseGameInfo game : gamesInfos) {
            //LOG.debug("STLOBBY:: gameName = " + game.getName());

            List<IBaseGameInfo> groupList = gamesMap.get(game.getGroup());
            if (groupList == null) {
                groupList = new ArrayList<>();
                gamesMap.put(game.getGroup(), groupList);
            }
            groupList.add(game);
        }

        request.setAttribute(GAMES, gamesMap);
    }

    private void collectGameInfos(HttpServletRequest request, long bankId, GameMode gameMode, Long bonusId,
                                  Locale curLocale, PlayerDeviceType deviceType) throws CommonException {
        Map<Long, IBaseGameInfo> gamesInfos = BaseGameCache.getInstance().getAllMobileGamesSet(bankId, null, deviceType);
        Map<GameGroup, List<ShortGameInfo>> gamesMap = new EnumMap<>(GameGroup.class);

        for (IBaseGameInfo game : gamesInfos.values()) {
            List<ShortGameInfo> groupList;
            String mobileGameName;
            String finalName;
            Long pcGameId;

            if (!game.isEnabled()) {
                continue;
            }

            groupList = gamesMap.get(game.getGroup());
            if (groupList == null) {
                groupList = new ArrayList<>();
                gamesMap.put(game.getGroup(), groupList);
            }

            mobileGameName = game.getName();
            if (PropertyUtils.getBooleanProperty(game.getProperties(), BaseGameConstants.KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS)) {
                finalName = mobileGameName;
                pcGameId = game.getId();
            } else {
                finalName = mobileGameName.substring(0, mobileGameName.indexOf(deviceType.getGameNameSignature()));
                pcGameId = Long.parseLong(game.getProperty("PC"));
            }
            groupList.add(new ShortGameInfo(getGameFullName(finalName, pcGameId, curLocale), game.getId(), pcGameId));
        }
        request.setAttribute(GAMES, gamesMap);
    }

    private String getGameFullName(String gameName, long gameId, Locale curLocale) {
        String fullName = MessageManager.getInstance().getGameTitle(GAME_NAME_PREFIX + gameId, curLocale);
        return isTrimmedEmpty(fullName) ? gameName : fullName;
    }

    protected void collectModeValues(HttpServletRequest request, AccountInfo account) throws CommonException {
        List<StLobbyMode> lobbyModes = new ArrayList<>();
        lobbyModes.add(new StLobbyMode(GameMode.REAL, null));
        lobbyModes.add(new StLobbyMode(GameMode.FREE, null));
        lobbyModes.addAll(prepareBonusesModeValues(account));
        request.setAttribute(AVAILABLE_MODE, lobbyModes);
    }

    private Collection<StLobbyMode> prepareBonusesModeValues(AccountInfo account) throws CommonException {
        List<StLobbyMode> stLobbyModes = new ArrayList<>();
        List<Long> bonusIds = BonusManager.getInstance().getBonusIdsForAccount(account.getId());
        if (bonusIds != null) {
            for (Long id : bonusIds) {
                Bonus bonus = BonusManager.getInstance().getById(id);
                if (bonus != null && bonus.getStatus() == BonusStatus.ACTIVE) {
                    stLobbyModes.add(new StLobbyMode(GameMode.BONUS, id));
                }
            }
        }
        return stLobbyModes;
    }
}

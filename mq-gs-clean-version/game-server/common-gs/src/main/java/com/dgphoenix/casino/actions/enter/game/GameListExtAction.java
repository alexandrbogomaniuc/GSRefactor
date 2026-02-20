package com.dgphoenix.casino.actions.enter.game;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.util.string.StringBuilderWriter;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.Attribute;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class GameListExtAction extends Action {

    public static final String TAG_GAMES_SUITES = "GamesSuites";
    public static final String TAG_SUITES = "Suites";
    public static final String TAG_SUITE = "Suite";
    public static final String TAG_GAMES = "Games";
    public static final String TAG_GAME = "Game";
    public static final String TAG_ERROR = "error";
    private static final String NEW_GAMES_ROUTE_GAME_ID_KEY = "NEW_GAMES_ROUTE_GAME_ID";
    private static final String NEW_GAMES_CLIENT_URL_KEY = "NEW_GAMES_CLIENT_URL";
    private static final String NEW_GAMES_API_URL_KEY = "NEW_GAMES_API_URL";
    private static final String NEW_GAMES_GAMELIST_ENABLED_KEY = "NEW_GAMES_GAMELIST_ENABLED";
    private static final String NEW_GAMES_GAMELIST_SUITE_KEY = "NEW_GAMES_GAMELIST_SUITE";
    private static final String NEW_GAMES_GAMELIST_TITLE_KEY = "NEW_GAMES_GAMELIST_TITLE";
    private static final String NEW_GAMES_GAMELIST_IMAGE_URL_KEY = "NEW_GAMES_GAMELIST_IMAGE_URL";
    private static final String NEW_GAMES_GAMELIST_LANGS_KEY = "NEW_GAMES_GAMELIST_LANGS";
    private static final String DEFAULT_NEW_GAMES_ROUTE_GAME_ID = "00010";
    private static final String DEFAULT_NEW_GAMES_SUITE_NAME = "New Games";
    private static final String DEFAULT_NEW_GAMES_TITLE = "Plinko";
    private static final String DEFAULT_NEW_GAMES_LANGS = "en";
    private static final Set<Long> DEFAULT_NEW_GAMES_BANK_IDS = new HashSet<>(Arrays.asList(6274L, 6275L));

    private Logger logger = Logger.getLogger(GameListExtAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String extBankId = request.getParameter("bankId");
        String test = request.getParameter("test");
        String showTestingGame;
        if (StringUtils.isTrimmedEmpty(test)) {
            showTestingGame = "FALSE";
        } else {
            if (test.equals("1")) {
                showTestingGame = "TRUE";
            } else {
                showTestingGame = "FALSE";
            }
        }

        Set<Long> gamesByBank = new HashSet<>(256);
        BaseGameCache gameCache = BaseGameCache.getInstance();

        BankInfo bankInfo = resolveBankInfo(extBankId, request.getServerName());
        if (bankInfo == null) {
            writeError(response, "Bank not found for bankId=" + extBankId);
            return null;
        }
        boolean needAddMasterBankGamesForDefaultCurrency = true;
        BankInfo masterBankInfo = null;
        for (Currency currency : bankInfo.getCurrencies()) {
            if (bankInfo.getMasterBankId() != null && bankInfo.getMasterBankId() > 0 &&
                    bankInfo.getMasterBankId() != bankInfo.getId()) {
                gamesByBank.addAll(gameCache.getAllGamesSet(bankInfo.getMasterBankId(), currency));
                if (masterBankInfo == null) {
                    masterBankInfo = BankInfoCache.getInstance().getBankInfo(bankInfo.getMasterBankId());
                }
                if (masterBankInfo.getDefaultCurrency().equals(currency)) {
                    needAddMasterBankGamesForDefaultCurrency = false;
                }

            }
            gamesByBank.addAll(gameCache.getAllGamesSet(bankInfo.getId(), currency));
        }
        if (masterBankInfo != null && needAddMasterBankGamesForDefaultCurrency) {
            gamesByBank.addAll(gameCache.getAllGamesSet(bankInfo.getMasterBankId(),
                    masterBankInfo.getDefaultCurrency()));
        }
        List<Long> mustRemoveGames = new ArrayList<>();
        for (Long gameId : gamesByBank) {
            for (Currency currency : bankInfo.getCurrencies()) {
                IBaseGameInfo gameInfo = gameCache.getGameInfoById(bankInfo.getId(), gameId, currency);
                if (gameInfo != null) {
                    String maintenanceMode = Boolean.toString(gameInfo.isMaintenanceMode());
                    if (!showTestingGame.equalsIgnoreCase(maintenanceMode)
                            || !Boolean.parseBoolean(gameInfo.getProperty(BaseGameConstants.KEY_ISENABLED))) {
                        mustRemoveGames.add(gameId);
                    }
                    break;
                }
            }
        }
        gamesByBank.removeAll(mustRemoveGames);

        Map<Long, String> imagesURL = new HashMap<>(gamesByBank.size());
        Map<Long, GameGroup> gameGroups = new HashMap<>(gamesByBank.size());

        for (Long gameId : gamesByBank) {
            for (Currency currency : bankInfo.getCurrencies()) {
                IBaseGameInfo gameInfo = gameCache.getGameInfoById(bankInfo.getId(), gameId, currency);

                if (gameInfo != null) {
                    imagesURL.put(gameInfo.getId(), gameInfo.getProperty(BaseGameConstants.KEY_GAME_IMAGE_URL));
                    gameGroups.put(gameInfo.getId(), gameInfo.getGroup());
                    break;
                }
            }
        }
        Map<String, List<Long>> groupedGames = new HashMap<>();
        for (Long gameId : gamesByBank) {
            GameGroup gameGroup = gameGroups.get(gameId);
            if (gameGroup == null) {
                continue;
            }
            String groupName = gameGroup.getGroupName();
            groupedGames.computeIfAbsent(groupName, k -> new ArrayList<>());
            groupedGames.get(groupName).add(gameId);
        }

        String userAgent = request.getHeader("User-Agent");
        userAgent = StringUtils.isTrimmedEmpty(userAgent) ? "" : userAgent.toLowerCase();
        if (userAgent.contains("safari")) {
            if (!userAgent.contains("chrome")) {
                response.setContentType("text/plain");
            } else {
                response.setContentType("text/xml");
            }
        } else {
            response.setContentType("text/xml");
        }

        StringBuilderWriter stringWriter = new StringBuilderWriter();
        // generate XML response
        XmlWriter xmlWriter = new XmlWriter(stringWriter);
        xmlWriter.header();
        xmlWriter.startNode(TAG_GAMES_SUITES);
        xmlWriter.startNode(TAG_SUITES);
        for (Map.Entry<String, List<Long>> groupedGamesEntry : groupedGames.entrySet()) {
            String groupName = groupedGamesEntry.getKey();
            Attribute[] attributes = new Attribute[2];
            attributes[0] = new Attribute("ID", groupName);
            attributes[1] = new Attribute("Name", groupName);
            xmlWriter.startNode(TAG_SUITE, attributes);
            xmlWriter.startNode(TAG_GAMES);
            for (Long gameId : groupedGamesEntry.getValue()) {
                attributes = new Attribute[4];
                String applicationMessage = null;
                try {
                    applicationMessage = MessageManager.getInstance()
                            .getApplicationMessage("game.name." + gameCache.getGameNameById(bankInfo.getId(), gameId));
                } catch (Exception e) {
                    logger.error("applicationMessage can't be null common-gs");
                }
//                logger.info("applicationMessage process enter");
                IBaseGameInfo gameInfo = gameCache.getGameInfoById(bankInfo.getId(), gameId, bankInfo.getDefaultCurrency());
                if (applicationMessage == null) {
//                    logger.info("applicationMessage is null");
                    if (gameInfo == null) {
                        logger.info("gameInfoTmp is null");
                    }
                    applicationMessage = gameInfo == null ? "" : gameInfo.getName();
                    if (applicationMessage == null) {
                        logger.info("applicationMessage final null");
                    }
                }
                attributes[0] = new Attribute("Name", applicationMessage);
                String extId = gameInfo == null ? null : gameInfo.getExternalId();
                if (extId == null) {
                    extId = gameId.toString();
                }
                attributes[1] = new Attribute("ID", extId);
                attributes[2] = new Attribute("ImageUrl", imagesURL.get(gameId));

                StringBuilder languages = new StringBuilder();
                List<String> langs = gameInfo == null ? Collections.emptyList() : gameInfo.getLanguages();
                int i = 1;
                for (String lang : langs) {
                    languages.append(lang).append((i == langs.size()) ? "" : ",");
                    i++;
                }
                attributes[3] = new Attribute("Languages", languages.toString());
                xmlWriter.startNode(TAG_GAME, attributes);
                xmlWriter.endNode(TAG_GAME);
            }
            xmlWriter.endNode(TAG_GAMES);
            xmlWriter.endNode(TAG_SUITE);
        }
        appendVirtualNewGamesSuite(xmlWriter, bankInfo, gamesByBank);
        xmlWriter.endNode(TAG_SUITES);
        xmlWriter.endNode(TAG_GAMES_SUITES);

        //logger.info("GameListAction::game list xml:" + result);
        response.getWriter().write(stringWriter.toString());
        response.getWriter().flush();
        return mapping.findForward("success");
    }

    private BankInfo resolveBankInfo(String extBankId, String serverName) {
        Long subCasinoId = resolveSubCasinoId(serverName);
        if (subCasinoId != null) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBank(extBankId, subCasinoId);
            if (bankInfo != null) {
                return bankInfo;
            }
        }

        try {
            return BankInfoCache.getInstance().getBankInfo(Long.parseLong(extBankId));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long resolveSubCasinoId(String serverName) {
        try {
            SubCasino subCasino = SubCasinoCache.getInstance().getSubCasinoByDomainName(serverName);
            return subCasino == null ? null : subCasino.getId();
        } catch (Exception e) {
            logger.error("Failed to resolve subcasino by domain: " + serverName, e);
            return null;
        }
    }

    private void appendVirtualNewGamesSuite(XmlWriter xmlWriter, BankInfo bankInfo, Set<Long> existingGames) throws Exception {
        if (!isVirtualNewGameEnabled(bankInfo)) {
            return;
        }

        String routeGameId = getBankProperty(bankInfo, NEW_GAMES_ROUTE_GAME_ID_KEY, DEFAULT_NEW_GAMES_ROUTE_GAME_ID);
        Long routeNumericGameId = parseLongOrNull(routeGameId);
        if (routeNumericGameId != null && existingGames.contains(routeNumericGameId)) {
            return;
        }

        String suiteName = getBankProperty(bankInfo, NEW_GAMES_GAMELIST_SUITE_KEY, DEFAULT_NEW_GAMES_SUITE_NAME);
        String gameName = getBankProperty(bankInfo, NEW_GAMES_GAMELIST_TITLE_KEY, DEFAULT_NEW_GAMES_TITLE);
        String imageUrl = getBankProperty(bankInfo, NEW_GAMES_GAMELIST_IMAGE_URL_KEY, "");
        String languages = getBankProperty(bankInfo, NEW_GAMES_GAMELIST_LANGS_KEY, DEFAULT_NEW_GAMES_LANGS);

        Attribute[] attributes = new Attribute[2];
        attributes[0] = new Attribute("ID", suiteName);
        attributes[1] = new Attribute("Name", suiteName);
        xmlWriter.startNode(TAG_SUITE, attributes);
        xmlWriter.startNode(TAG_GAMES);

        attributes = new Attribute[4];
        attributes[0] = new Attribute("Name", gameName);
        attributes[1] = new Attribute("ID", routeGameId);
        attributes[2] = new Attribute("ImageUrl", imageUrl);
        attributes[3] = new Attribute("Languages", languages);
        xmlWriter.startNode(TAG_GAME, attributes);
        xmlWriter.endNode(TAG_GAME);

        xmlWriter.endNode(TAG_GAMES);
        xmlWriter.endNode(TAG_SUITE);
    }

    private boolean isVirtualNewGameEnabled(BankInfo bankInfo) {
        String explicitFlag = getBankProperty(bankInfo, NEW_GAMES_GAMELIST_ENABLED_KEY, null);
        if (!StringUtils.isTrimmedEmpty(explicitFlag)) {
            return Boolean.parseBoolean(explicitFlag);
        }

        if (!StringUtils.isTrimmedEmpty(getBankProperty(bankInfo, NEW_GAMES_ROUTE_GAME_ID_KEY, null))) {
            return true;
        }
        if (!StringUtils.isTrimmedEmpty(getBankProperty(bankInfo, NEW_GAMES_CLIENT_URL_KEY, null))) {
            return true;
        }
        if (!StringUtils.isTrimmedEmpty(getBankProperty(bankInfo, NEW_GAMES_API_URL_KEY, null))) {
            return true;
        }
        return bankInfo != null && DEFAULT_NEW_GAMES_BANK_IDS.contains(bankInfo.getId());
    }

    private String getBankProperty(BankInfo bankInfo, String key, String defaultValue) {
        if (bankInfo == null || bankInfo.getProperties() == null || StringUtils.isTrimmedEmpty(key)) {
            return defaultValue;
        }
        String value = bankInfo.getProperties().get(key);
        return StringUtils.isTrimmedEmpty(value) ? defaultValue : value;
    }

    private Long parseLongOrNull(String value) {
        if (StringUtils.isTrimmedEmpty(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void writeError(ServletResponse response, String errorMessage) throws Exception {
        StringBuilderWriter stringWriter = new StringBuilderWriter();
        XmlWriter xmlWriter = new XmlWriter(stringWriter);
        xmlWriter.header();
        xmlWriter.node(TAG_ERROR, errorMessage);
        response.getWriter().write(stringWriter.toString());
        response.getWriter().flush();
    }

}

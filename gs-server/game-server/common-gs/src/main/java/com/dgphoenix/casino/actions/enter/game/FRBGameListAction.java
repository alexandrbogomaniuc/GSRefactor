package com.abs.casino.actions.enter.game;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.XmlWriterException;
import com.abs.casino.common.util.string.StringBuilderWriter;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.Attribute;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class FRBGameListAction extends Action {

    public static final String TAG_GAMES_SUITES = "GamesSuites";
    public static final String TAG_SUITES = "Suites";
    public static final String TAG_SUITE = "Suite";
    public static final String TAG_GAMES = "Games";
    public static final String TAG_GAME = "Game";
    private static final String TAG_ERROR = "error";

    private final Logger LOG = Logger.getLogger(FRBGameListAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String userAgent = request.getHeader("User-Agent");
        userAgent = StringUtils.isTrimmedEmpty(userAgent) ? "no" : userAgent.toLowerCase();
        if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            response.setContentType("text/plain");
        } else {
            response.setContentType("text/xml");
        }

        String strBankId = request.getParameter("bankId");
        BankInfo bankInfo;
        long bankId;
        try {
            long subCasinoId = SubCasinoCache.getInstance().getSubCasinoByDomainName(request.getServerName()).getId();
            bankInfo = BankInfoCache.getInstance().getBank(strBankId, subCasinoId);
            if (bankInfo == null) {
                bankInfo = BankInfoCache.getInstance().getBankInfo(Long.parseLong(strBankId));
            }
            bankId = bankInfo.getId();

            if (!SubCasinoCache.getInstance().isExist(subCasinoId, bankId)) {
                throw new CommonException("The bank isn't found in SubCasino");
            }
        } catch (Exception e) {
            LOG.error("Subcasino detecting error", e);
            writeError(response, e.getMessage());
            return null;
        }

        BaseGameCache gameCache = BaseGameCache.getInstance();
        Set<Long> frbGamesByBank = new HashSet<>(BankInfoCache.getInstance().getFrbGames(bankInfo));

        Map<Long, GameGroup> gameGroups = new HashMap<>();
        Set<Long> notOnBankGames = new HashSet<>();

        for (Long gameId : frbGamesByBank) {
            Currency currency = bankInfo.getDefaultCurrency();
            if (gameCache.isExist(bankId, gameId, currency)) {
                IBaseGameInfo gameInfo = gameCache.getGameInfoById(bankId, gameId, currency);
                if (gameInfo.isEnabled()) {
                    gameGroups.put(gameInfo.getId(), gameInfo.getGroup());
                }
            } else {
                notOnBankGames.add(gameId);
            }
        }
        frbGamesByBank.removeAll(notOnBankGames);

        Map<String, List<Long>> groupedGames = new HashMap<>();
        for (Long gameId : frbGamesByBank) {
            GameGroup gameGroup = gameGroups.get(gameId);
            if (gameGroup != null) {
                String groupName = gameGroup.getGroupName();
                groupedGames.computeIfAbsent(groupName, k -> new ArrayList<>()).add(gameId);
            }
        }

        StringBuilderWriter stringWriter = new StringBuilderWriter();
        // generate XML response
        XmlWriter xmlWriter = new XmlWriter(stringWriter);
        xmlWriter.header();
        xmlWriter.startNode(TAG_GAMES_SUITES);
        xmlWriter.startNode(TAG_SUITES);
        for (Entry<String, List<Long>> stringListEntry : groupedGames.entrySet()) {
            Attribute[] attributes = new Attribute[2];
            attributes[0] = new Attribute("ID", stringListEntry.getKey());
            attributes[1] = new Attribute("Name", stringListEntry.getKey());
            xmlWriter.startNode(TAG_SUITE, attributes);
            xmlWriter.startNode(TAG_GAMES);
            for (Long gameId : stringListEntry.getValue()) {
                attributes = new Attribute[3];
                String applicationMessage = null;
                try {
                    applicationMessage = MessageManager.getInstance()
                            .getApplicationMessage("game.name." + gameCache.getGameNameById(bankId, gameId));
                } catch (Exception ignored) {
                    LOG.error("applicationMessage can't be null common-gs");
                }
//                LOG.info("applicationMessage process enter");
                if (applicationMessage == null) {
//                    LOG.info("applicationMessage is null");
                    IBaseGameInfo gameInfoTmp = gameCache.getGameInfoById(bankId, gameId, BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency());
                    if (gameInfoTmp == null) {
                        LOG.info("gameInfoTmp is null, gameId: " + gameId);
                    }
                    applicationMessage = gameInfoTmp != null ? gameInfoTmp.getName() : null;
                    if (applicationMessage == null) {
                        LOG.info("applicationMessage final null");
                    }
                }
                attributes[0] = new Attribute("Name", applicationMessage);
                String extId = gameCache.getGameInfoById(bankId, gameId, BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency()).getExternalId();
                if (extId == null)
                    extId = gameId.toString();
                attributes[1] = new Attribute("ID", extId);
                xmlWriter.startNode(TAG_GAME, attributes);
                xmlWriter.endNode(TAG_GAME);
            }
            xmlWriter.endNode(TAG_GAMES);
            xmlWriter.endNode(TAG_SUITE);
        }
        xmlWriter.endNode(TAG_SUITES);
        xmlWriter.endNode(TAG_GAMES_SUITES);

        //LOG.info("GameListAction::game list xml:" + result);
        response.getWriter().write(stringWriter.toString());
        response.getWriter().flush();
        return mapping.findForward("success");
    }

    protected void writeError(ServletResponse response, String errorMessage) throws XmlWriterException, IOException {
        StringBuilderWriter stringWriter = new StringBuilderWriter();
        XmlWriter xmlWriter = new XmlWriter(stringWriter);
        xmlWriter.header();
        xmlWriter.node(TAG_ERROR, errorMessage);
        response.getWriter().write(stringWriter.toString());
        response.getWriter().flush();
    }

}

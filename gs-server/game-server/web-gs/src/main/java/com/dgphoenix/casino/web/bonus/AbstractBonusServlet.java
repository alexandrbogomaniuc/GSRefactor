package com.abs.casino.web.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.exception.XmlWriterException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.abs.casino.gs.singlegames.tools.util.StringUtils;
import com.dgphoenix.casino.services.bonus.ForbiddenGamesForBonusProvider;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: ktd
 * Date: 31.03.11
 */
public abstract class AbstractBonusServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(AbstractBonusServlet.class);

    public static final String BANK_ID_PARAM = "bankId";
    public static final String SUBCASINO_ID_PARAM = "subCasinoId";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    protected abstract void process(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException;

    protected void buildResponseXML(XmlWriter xw, HashMap inParams, HashMap outParams) {
        try {
            xw.startDocument(GameServerConfiguration.getInstance().getBrandApiRootTagName());

            if (!inParams.isEmpty()) {
                xw.startNode(CBonus.REQUEST_TAG);
                Iterator it = inParams.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    xw.node(key, (String) inParams.get(key));
                    //LOG.debug(key + " = " + inParams.get(key));
                }
                xw.endNode(CBonus.REQUEST_TAG);
            }

            Date time = new Date();
            xw.node(CBonus.TIME_TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(time));

            if (!outParams.isEmpty()) {
                xw.startNode(CBonus.RESPONSE_TAG);
                Iterator it = outParams.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();

                    if (key.equals(CBonus.BONUS_LIST)) {
                        List<Bonus> bonuses = (List<Bonus>) outParams.get(key);
                        for (Bonus bonus : bonuses) {
                            xw.startNode(CBonus.BONUS);
                            printBonusInfo(xw, bonus);
                            xw.endNode(CBonus.BONUS);
                        }

                    } else {
                        xw.node(key, outParams.get(key).toString());
                    }
                    //LOG.debug(key + " = " + outParams.get(key));
                }
                xw.endNode(CBonus.RESPONSE_TAG);
            }

            xw.endDocument(GameServerConfiguration.getInstance().getBrandApiRootTagName());
        } catch (XmlWriterException e) {
            LOG.error("can not buildResponseXML", e);
        }
    }

    protected void printBonusInfo(XmlWriter xw, Bonus bonus) throws XmlWriterException {

        xw.node(CBonus.BONUSID, Long.toString(bonus.getId()));
        xw.node(CBonus.TYPE, bonus.getType().toString());
        xw.node(CBonus.AWARDDATE, new SimpleDateFormat("dd.MM.yyyy").format(bonus.getTimeAwarded()));
        xw.node(CBonus.AMOUNT, Long.toString(bonus.getAmount()));
        xw.node(CBonus.BALANCE, Long.toString(bonus.getBalance()));
        xw.node(CBonus.ROLLOVER, Long.toString((long) (bonus.getRolloverMultiplier() * bonus.getAmount())));
        xw.node(CBonus.COLLECTED, Long.toString(bonus.getBetSum()));
        xw.node(CBonus.DESCRIPTION, bonus.getDescription());
        xw.node(CBonus.GAMEIDS, getGameIds(bonus));
        xw.node(CBonus.EXPDATE, new SimpleDateFormat("dd.MM.yyyy").format(bonus.getExpirationDate()));
    }

    public static String getHashValue(List params, long bankId) throws BonusException {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                sb.append(param);
            }
            sb.append(BankInfoCache.getInstance().getBankInfo(bankId).getBonusPassKey());

            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    protected String getGameIds(Bonus bonus) throws XmlWriterException {
        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(bonus.getAccountId());
        Set<Long> allGamesSet = BaseGameCache.getInstance().getAllGamesSet(
                accountInfo.getBankId(), BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId()).getDefaultCurrency());
        Collection<Long> games = bonus.getValidGameIds(allGamesSet == null ? new HashSet<Long>() : new HashSet<>(allGamesSet));

        ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider = ApplicationContextHelper.getApplicationContext()
                .getBean(ForbiddenGamesForBonusProvider.class);
        Set<Long> listGamesDeny = forbiddenGamesForBonusProvider.getGames(accountInfo.getBankId());
        games.removeAll(listGamesDeny);

        if (!games.isEmpty()) {
            BonusManager b = BonusManager.getInstance();
            StringBuilder buffer = new StringBuilder();
            for (Long gameId : games) {
                buffer.append(b.getExternalGameId(gameId, accountInfo.getBankId()));
                buffer.append(',');
            }
            return buffer.substring(0, buffer.length() - 1);
        }
        return "";
    }
}

package com.abs.casino.actions.enter.game;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameVariableType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: shegan
 * Date: 19.03.15
 */
public class GameSettingsActionV2 extends Action {

    public static final Logger LOG = LogManager.getLogger(GameSettingsActionV2.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        long bankId = Long.parseLong(request.getParameter("bankId"));
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        Currency defaultCurrency = bankInfo.getDefaultCurrency();
        response.setContentType("text");
        response.getWriter().println("Currency,GameName,GameType,GameId,Coins,DefCoin,Limits");

        for (Currency currency : bankInfo.getCurrencies()) {
            Map<Long, IBaseGameInfo> games = BaseGameCache.getInstance().getAllGameInfosAsMap(bankId, currency);
            for (Map.Entry<Long, IBaseGameInfo> entry : games.entrySet()) {
                long gameId = entry.getKey();
                IBaseGameInfo bgi = entry.getValue();
                if (bgi == null) {
                    bgi = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, defaultCurrency);
                }
                if (bgi != null) {
                    BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance()
                            .getBaseGameInfoTemplateById(gameId);
                    if (!bgi.isEnabled()) {
                        continue;
                    }
                    if (template.getTitle() == null) {
                        continue;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(currency.getCode()).append(",");
                    String title = template.getTitle();
                    if (title.contains(",")) {
                        title = title.replaceAll(",", "");
                    }
                    builder.append(title).append(",");
                    builder.append(template.getDefaultGameInfo().getVariableType().name()).append(",");
                    builder.append(gameId).append(",");
                    if (template.getDefaultGameInfo().getVariableType().equals(GameVariableType.COIN)) {
                        List<Coin> coins =
                                new ArrayList<Coin>((bgi.getCoins() != null && !bgi.getCoins().isEmpty())
                                        ? bgi.getCoins() : bankInfo.getCoins());

                        Collections.sort(coins);
                        Iterator<Coin> iterator = coins.iterator();
                        while (iterator.hasNext()) {
                            builder.append(iterator.next().getValue());
                            if (iterator.hasNext()) {
                                builder.append("|");
                            }
                        }
                        builder.append(",");
                        if (bgi.getProperty(BaseGameConstants.KEY_DEFAULT_COIN) != null) {
                            try {
                                builder.append(coins.get(bgi.getDefaultCoin()).getValue());
                            } catch (Exception e) {
                                builder.append("incorrectDefCoin");
                            }
                        } else {
                            builder.append("incorrectDefCoin");
                        }
                        builder.append(",");
                    } else {
                        builder.append(",").append(",");
                        Limit limit = bgi.getLimit() != null ? (Limit) bgi.getLimit() : bankInfo.getLimit();
                        builder.append(limit.getMinValue()).append("-").append(limit.getMaxValue());
                    }
                    response.getWriter().println(builder.toString());
                }
            }
        }

        return null;
    }


}

package com.abs.casino.support.cache.bank.edit.actions.common;


import com.dgphoenix.casino.bgm.BaseGameHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CoinsCache;
import com.dgphoenix.casino.common.cache.LimitsCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.game.GameVariableType;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.support.cache.bank.edit.forms.common.AddGameForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateNewGameAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String submitType = request.getParameter("submitType");
        if (submitType.equals("enterProperties")) {
            return mapping.findForward("properties");
        }

        AddGameForm gameInfoForm = (AddGameForm) form;
        Map<String, String> properties = constructProperties(gameInfoForm);

        long bankId = Long.parseLong(gameInfoForm.getBankId());
        long gameId = Long.parseLong(gameInfoForm.getGameId());
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        Currency defCurrency = bankInfo.getDefaultCurrency();
        String gameName = gameInfoForm.getGameName().trim();
        if (gameName.isEmpty()) {
            gameInfoForm.setCreateGameStatus("name of the game is missing");
            return mapping.findForward("success");
        }

        GameType gameType = GameType.valueOf(gameInfoForm.getGameType());
        GameGroup gameGroup = GameGroup.valueOf(gameInfoForm.getGameGroup());
        GameVariableType gameVariableType;
        if (gameInfoForm.getGameVariableType().equals("LIMIT")) {
            gameVariableType = GameVariableType.LIMIT;
        } else {
            gameVariableType = GameVariableType.COIN;
        }

        String rmClassName = gameInfoForm.getRmClassName().trim();
        if (rmClassName.isEmpty()) rmClassName = null;

        String spClassName = gameInfoForm.getSpClassName().trim();
        if (spClassName.isEmpty()) spClassName = null;

        long limitId = Long.parseLong(gameInfoForm.getLimitId());
        Limit limit = LimitsCache.getInstance().getLimit(limitId);

        List<Coin> coins = new ArrayList<Coin>();
        for (String coinId : gameInfoForm.getCoinIds()) {
            long id = Long.parseLong(coinId);
            Coin coin = CoinsCache.getInstance().getCoin(id);
            coins.add(coin);
        }
        if (coins.size() == 0) {
            coins = null;
        }

        boolean createJackpot = gameInfoForm.getCreateJackpot().equals("TRUE");

        Double pcrp;
        try {
            pcrp = Double.parseDouble(gameInfoForm.getPcrp().trim());
        } catch (NumberFormatException e) {
            pcrp = null;
        }

        Double bcrp;
        try {
            bcrp = Double.parseDouble(gameInfoForm.getBcrp().trim());
        } catch (NumberFormatException e) {
            bcrp = null;
        }

        try {
            BaseGameHelper.createGame(bankId, gameId, defCurrency, gameName,
                    gameType, gameGroup, gameVariableType, rmClassName, spClassName,
                    properties, limit, coins, createJackpot, pcrp, bcrp
            );
        } catch (Exception e) {
            gameInfoForm.setCreateGameStatus("Create game error!");
        }

        gameInfoForm.setCreateGameStatus("The game was created successfully");
        return mapping.findForward("success");
    }

    private Map<String, String> constructProperties(AddGameForm gameInfoForm) {
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put(BaseGameConstants.KEY_ISENABLED, gameInfoForm.getEnabled());
        properties.put(BaseGameConstants.KEY_GAME_TESTING, gameInfoForm.getGameTesting());

        String value = gameInfoForm.getPayoutPercent();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, value);
        }
        value = gameInfoForm.getChipValues();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_CHIPVALUES, value);
        }
        value = gameInfoForm.getDefCoin();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_DEFAULT_COIN, value);
        }
        value = gameInfoForm.getMaxBet1();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_1, value);
        }
        value = gameInfoForm.getMaxBet2();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_2, value);
        }
        value = gameInfoForm.getMaxBet3();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_3, value);
        }
        value = gameInfoForm.getMaxBet4();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_4, value);
        }
        value = gameInfoForm.getMaxBet5();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_5, value);
        }
        value = gameInfoForm.getMaxBet6();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_6, value);
        }
        value = gameInfoForm.getMaxBet12();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_12, value);
        }
        value = gameInfoForm.getMaxBet18();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_MAX_BET_18, value);
        }
        value = gameInfoForm.getImageURL();
        if (!StringUtils.isTrimmedEmpty(value)) {
            properties.put(BaseGameConstants.KEY_GAME_IMAGE_URL, value);
        }

        return properties;
    }

}

package com.abs.casino.support.cache.bank.edit.actions.common;

import com.abs.casino.bgm.BaseGameHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.support.cache.bank.edit.forms.common.AddGameForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CopyConfigAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {


        AddGameForm gameInfoForm = (AddGameForm) form;
        long bankId = Long.parseLong(gameInfoForm.getBankId());
        long gameId = Long.parseLong(gameInfoForm.getGameId());
        long selectedBankId;
        try {
            selectedBankId = Long.parseLong(gameInfoForm.getSelectedBankId());
        } catch (NumberFormatException e) {
            gameInfoForm.setConfigGameStatus("There is no bank with a selected game");
            return mapping.findForward("success");
        }

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        BankInfo selectedBankInfo = BankInfoCache.getInstance().getBankInfo(selectedBankId);

        IBaseGameInfo gameInfo =
                BaseGameCache.getInstance().getGameInfoById(selectedBankId, gameId, selectedBankInfo.getDefaultCurrency());
        try {
            BaseGameHelper.createGame(bankId, gameId, bankInfo.getDefaultCurrency(),
                    gameInfo.getName(), gameInfo.getGameType(), gameInfo.getGroup(),
                    gameInfo.getVariableType(), gameInfo.getRmClassName(), gameInfo.getGsClassName(),
                    gameInfo.getPropertiesMap(), gameInfo.getLimit(), gameInfo.getCoins(),
                    false, 0.01d, 0.00d);
        } catch (Exception e) {
            gameInfoForm.setConfigGameStatus("Failed to configure the game");
            return mapping.findForward("success");
        }
        gameInfoForm.setConfigGameStatus("Game configured successfully");

        return mapping.findForward("success");
    }

}

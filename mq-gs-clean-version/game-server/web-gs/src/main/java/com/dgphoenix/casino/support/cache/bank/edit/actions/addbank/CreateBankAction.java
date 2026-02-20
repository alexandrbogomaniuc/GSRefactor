package com.dgphoenix.casino.support.cache.bank.edit.actions.addbank;

import com.dgphoenix.casino.bgm.BaseGameHelper;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraBankInfoPersister;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.persistance.remotecall.RefreshConfigCall;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.dgphoenix.casino.support.cache.bank.edit.forms.addBank.NewBankForm;
import com.dgphoenix.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
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

public class CreateBankAction extends Action {

    private long scId;
    private long id;

    private final CassandraBankInfoPersister bankInfoPersister;

    public CreateBankAction() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        bankInfoPersister = persistenceManager.getPersister(CassandraBankInfoPersister.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        NewBankForm bankForm = (NewBankForm) form;

        setSubCasinoId(bankForm);

        if (request.getParameter("button").equals("back")) {
            return BaseAction.getActionRedirectByHost(request, getRedirectUrl());
        }

        id = Long.parseLong(bankForm.getId().trim());
        String extId = bankForm.getExtId().trim();
        String name = bankForm.getName();

        Currency defCurrency = CurrencyCache.getInstance().get(bankForm.getDefCurCode());
        long limitId = Long.parseLong(bankForm.getLimitId());
        String defLang = bankForm.getDefLang();

        List<Coin> coins = new ArrayList<Coin>();
        String[] coinIds = bankForm.getCoinIds();
        for (String coinId : coinIds) {
            coins.add(CoinsCache.getInstance().getCoin(Long.parseLong(coinId)));
        }

        BankInfo newBank = new BankInfo(id, extId, name, defCurrency,
                LimitsCache.getInstance().getLimit(limitId), coins);
        newBank.setDefaultLanguage(defLang);
        for (String curCode : bankForm.getCurrencyCodes()) {
            newBank.addCurrency(CurrencyCache.getInstance().get(curCode));
        }

        newBank.setSubCasinoId(scId);
        BankInfoCache.getInstance().put(newBank);
        bankInfoPersister.persist(newBank.getId(), newBank);
        RemoteCallHelper.getInstance().sendCallToAllServers(new RefreshConfigCall(
                BankInfoCache.class.getCanonicalName(), String.valueOf(newBank.getId())));
        putBankToSubCasinoCache();
        SubCasino subCasino = SubCasinoCache.getInstance().get(scId);
        RemoteCallHelper.getInstance().saveAndSendNotification(subCasino);
        String[] gameIds = bankForm.getGameIds();
        if (gameIds != null) {
            for (String strGameId : gameIds) {
                long gameId = Long.parseLong(strGameId);
                long copyBankId = Long.parseLong(bankForm.getCopyBankId());
                IBaseGameInfo copyGame = BaseGameCache.getInstance().getGameInfo(copyBankId, gameId, defCurrency);
                Double pcrp = null;
                Double bcrp = null;
                BaseGameHelper.createGame(id, gameId, defCurrency, copyGame.getName(),
                        copyGame.getGameType(), copyGame.getGroup(),
                        copyGame.getVariableType(), copyGame.getRmClassName(), copyGame.getGsClassName(),
                        cloneProperties(copyGame.getPropertiesMap()), copyGame.getLimit(), copyGame.getCoins(), false, pcrp, bcrp);

            }
        }

        BankPropertiesForm bpForm = bankForm.getBpsForm();
        BankInfo bankInfo = newBank;

        for (Map.Entry<String, String> entry : bpForm.getBankProperties().entrySet()) {
            bankInfo.setProperty(entry.getKey(), entry.getValue());
        }

        // Fields
        if (!StringUtils.isTrimmedEmpty(bpForm.getCashierUrl()))
            bankInfo.setCashierUrl(bpForm.getCashierUrl());
        else
            bankInfo.setCashierUrl(null);


        return mapping.findForward("success");
    }

    private Map<String, String> cloneProperties(Map<String, String> original) {
        Map<String, String> clone = new HashMap<String, String>();
        for (Map.Entry<String, String> propertyEntry : original.entrySet()) {
            clone.put(propertyEntry.getKey(), propertyEntry.getValue());
        }
        return clone;
    }

    public String getRedirectUrl() {
        return "/support/cache/bank/common/systemInfo.jsp?subcasinoId=" + scId;
    }

    public void putBankToSubCasinoCache() {
        SubCasinoCache.getInstance().put(scId, id, false);
    }

    public void setSubCasinoId(NewBankForm bankForm) {
        this.scId = Long.parseLong(bankForm.getSubcasinoId());
    }
}

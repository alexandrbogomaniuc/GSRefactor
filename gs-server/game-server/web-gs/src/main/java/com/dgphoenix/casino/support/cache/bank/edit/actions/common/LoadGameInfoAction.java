package com.abs.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.GameVariableType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.support.cache.bank.edit.forms.common.GameInfoForm;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.axis.encoding.Base64;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class LoadGameInfoAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        GameInfoForm gameForm = (GameInfoForm) form;
        long bankId = Long.parseLong(request.getParameter("bankId"));
        long gameId = Long.parseLong(request.getParameter("gameId"));
        String curCode = request.getParameter("curCode");
        IBaseGameInfo gameInfo =
                BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, CurrencyCache.getInstance().get(curCode));
        XStream xStream = new XStream(new StaxDriver());
        String xml = xStream.toXML(gameInfo);
        request.setAttribute("baseGameInfoXML", Base64.encode(xml.getBytes()));

        gameForm.setGameName(gameInfo.getName());
        gameForm.setBankId(String.valueOf(gameInfo.getBankId()));
        gameForm.setCurrencyCode(gameInfo.getCurrency().getCode());
        gameForm.setGameType(gameInfo.getGameType().getName());
        gameForm.setGameGroup(gameInfo.getGroup().getGroupName());
        gameForm.setServletName(gameInfo.getServlet());
        gameForm.setRmClassName(gameInfo.getRmClassName());
        gameForm.setGsClassName(gameInfo.getGsClassName());

        //gameVariableType
        if (gameInfo.getVariableType() == GameVariableType.COIN) {
            gameForm.setGameVariableType("COIN");
        } else {
            gameForm.setGameVariableType("LIMIT");
        }

        //limits
        if (gameInfo.getLimit() != null) {
            gameForm.setMinLimitValue(String.valueOf(gameInfo.getLimit().getMinValue()));
            gameForm.setMaxLimitValue(String.valueOf(gameInfo.getLimit().getMaxValue()));
        } else {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            gameForm.setBankLimit("{minValue=" + bankInfo.getLimit().getMinValue() +
                    ", maxValue=" + bankInfo.getLimit().getMaxValue() + "}");
        }

        //coins
        List<String> coinIds = new ArrayList<>();
        List<Coin> coins = gameInfo.getCoins();

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        List<Coin> bankCoins = bankInfo.getCoins();

        if (coins != null && !coins.isEmpty() &&
                bankCoins != null && !bankCoins.isEmpty() && coins.equals(bankCoins)) {
            coins = null;
        }

        if (coins != null && !coins.isEmpty()) {
            for (Coin coin : coins) {
                coinIds.add(String.valueOf(coin.getId()));
            }
        } else if (bankCoins != null && !bankCoins.isEmpty()) {
            List<Coin> sortedBankCoins = generateSortedCoins(bankCoins);

            List<String> bankCoinsAsString = new ArrayList<>();
            for (Coin coin : sortedBankCoins) {
                bankCoinsAsString.add(String.valueOf(centsToDouble(coin.getValue())));
            }
            gameForm.setBankCoins(bankCoinsAsString);
        }
        gameForm.setCoinIds(coinIds.toArray(new String[coinIds.size()]));

        List<Coin> sortedCachedCoins = generateSortedCoins(CoinsCache.getInstance().getAll());
        List<LabelValueBean> cachedCoinsAsLabel = new ArrayList<>();
        for (Coin coin : sortedCachedCoins) {
            cachedCoinsAsLabel.add(
                    new LabelValueBean(DigitFormatter.doubleToMoney(centsToDouble(coin.getValue())),
                            String.valueOf(coin.getId()))
            );
        }
        gameForm.setCoins(cachedCoinsAsLabel);

        //properties
        List<LabelValueBean> propList = new ArrayList<>();
        Map<String, String> propertiesMap = gameInfo.getPropertiesMap();
        for (String propertyKey : propertiesMap.keySet()) {
            propList.add(new LabelValueBean(propertyKey, gameInfo.getProperty(propertyKey)));
        }
        gameForm.setProperties(propList);

        //properties in BaseGameInfoTemplate
        BaseGameInfo defaultGameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
        Map<String, String> templateProperties = new HashMap<>();
        for (String propertyKey : defaultGameInfo.getPropertiesMap().keySet()) {
            templateProperties.put(propertyKey, defaultGameInfo.getProperty(propertyKey));
        }
        gameForm.setTemplateProperties(templateProperties);

        gameForm.setRemoveList(new String[0]);
        gameForm.setResetList(new String[0]);
        gameForm.setAllCurrencyPropertyList(new String[0]);
        gameForm.setNewProperty(false);
        gameForm.setAcceptServletNameForSubcasino(false);

        return mapping.findForward("success");
    }

    private List<Coin> generateSortedCoins(List<Coin> coins) {
        List<Coin> sortedCoins = new ArrayList<>(coins);
        Collections.sort(sortedCoins, new Comparator<Coin>() {
            @Override
            public int compare(Coin o1, Coin o2) {
                return Long.compare(o1.getValue(), o2.getValue());
            }
        });

        return sortedCoins;
    }

    private static double centsToDouble(long cents) {
        return cents / 100d;
    }

}

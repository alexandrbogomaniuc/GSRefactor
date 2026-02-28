package com.abs.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.InheritFromTemplate;
import com.abs.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.support.CacheObjectComparator;
import com.abs.casino.support.cache.bank.edit.forms.common.GameInfoForm;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.axis.encoding.Base64;
import org.apache.struts.action.*;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dgphoenix.casino.common.cache.data.game.BaseGameConstants.*;
import static org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE;

public class EditGameAction extends Action {

    private boolean isPropValueGood(String key, String value, ActionMessages errors) {
        if (isMandatoryProperty(key)) {
            if (value == null || value.isEmpty()) {
                errors.add("gameInfo", new ActionMessage("error.gameInfoForm.invalidProperty", key));
                return false;
            }
        } else if (isNumericProperty(key)) {
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e1) {
                    errors.add("gameInfo", new ActionMessage("error.bankPropertiesForm.incorrectNumericFormat", key, value));
                    return false;
                }
            }
        } else if (isUrlProperty(key)) {
            try {
                new URL(value);
            } catch (MalformedURLException e) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage("error.bankPropertiesForm.incorrectURLFormat", key, value));
                return false;
            }
        } else if (isClassProperty(key)) {
            try {
                ReflectionUtils.forNameWithCompatibilityAliases(value);
            } catch (Throwable e) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage("error.bankPropertiesForm.incorrectClassName", key, value));
                return false;
            }
        }

        return true;
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionMessages errors = getErrors(request);
        if (errors == null) {
            errors = new ActionErrors();
        }

        GameInfoForm gameForm = (GameInfoForm) form;
        if (request.getParameter("button").equals("back")) {
            return BaseAction.getActionRedirectByHost(request, "/support/bankInfo.do?bankId=" + gameForm.getBankId());
        }

        fillForm(gameForm, request);
        long gameId = Long.parseLong(gameForm.getGameId());
        long bankId = Long.parseLong(gameForm.getBankId());
        String rmClassName = propertyValue(gameForm.getRmClassName());
        String gsClassName = propertyValue(gameForm.getGsClassName());
        String servlet = propertyValue(gameForm.getServletName());

        if (gameForm.isAcceptServletNameForSubcasino()) {
            acceptServletForSubcasino(servlet, bankId, gameId);
        }

        Long jackpotId;
        if (!StringUtils.isTrimmedEmpty(gameForm.getJackpotId())) {
            jackpotId = Long.parseLong(gameForm.getJackpotId().trim());
        } else {
            jackpotId = null;
        }

        //limits
        Limit limit = null;
        if (gameForm.getMinLimitValue() != null && gameForm.getMaxLimitValue() != null) {
            try {
                int minLimitValue = Integer.valueOf(gameForm.getMinLimitValue());
                int maxLimitValue = Integer.valueOf(gameForm.getMaxLimitValue());

                if (minLimitValue <= maxLimitValue && minLimitValue > 0 && maxLimitValue > 0) {
                    limit = Limit.valueOf(minLimitValue, maxLimitValue);
                }

            } catch (NumberFormatException ignored) {
                //ignore
            }
        }

        //coins
        List<Coin> coins = new ArrayList<>();
        if (gameForm.getCoinIds() != null) {
            for (String coinId : gameForm.getCoinIds()) {
                coins.add(CoinsCache.getInstance().getCoin(Long.parseLong(coinId)));
            }
        }
        if (CollectionUtils.isEmpty(coins)) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null || CollectionUtils.isEmpty(bankInfo.getCoins())) {
                bankInfo = BankInfoCache.getInstance().getDefaultBank();
            }
            coins = Coin.copyCoins(bankInfo.getCoins());
        }

        XStream xStream = new XStream(new StaxDriver());
        BaseGameInfo oldBaseGameInfo = (BaseGameInfo) xStream.fromXML(new String(Base64.decode(request.getParameter("baseGameInfoXML"))));

        checkNewPropertyExisting(gameForm.getNewPropKey(), errors);

        // properties annotated @InheritedFromTemplate and can't be changed, remove, reset
        Set<String> inheritedProperties = getPropertiesInheritedFromTemplate();
        checkAddInheritedProperty(inheritedProperties, gameForm.getNewPropKey(), errors);

        Set<String> propertiesToRemove = getPropertiesToRemove(inheritedProperties, gameForm.getRemoveList(), errors);
        Set<String> propertiesToReset = getPropertiesToReset(inheritedProperties, gameForm.getResetList(), errors);

        if (gameForm.isSaveAllGamesByBank()) {
            for (Currency currency : BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies()) {

                IBaseGameInfo currentGameInfo = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);
                if (currentGameInfo == null) {
                    continue;
                }

                // remove all gameInfo properties
                Set<String> removeSet = new HashSet<>(currentGameInfo.getPropertiesMap().keySet());
                removeSet.removeIf(inheritedProperties::contains);
                for (String aKey : removeSet) {
                    currentGameInfo.removeProperty(aKey);
                }

                // add all set properties
                IBaseGameInfo defaultGameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
                for (LabelValueBean property : gameForm.getProperties()) {
                    if (!propertiesToRemove.contains(property.getLabel())) {
                        setProperty(inheritedProperties, property, propertiesToReset, currentGameInfo, defaultGameInfo, errors);
                    }
                }

                setNewProperty(inheritedProperties, gameForm, currentGameInfo, errors);

                RemoteCallHelper.getInstance().saveAndSendNotification(currentGameInfo);
            }
        } else {
            Currency currency = CurrencyCache.getInstance().get(gameForm.getCurrencyCode());
            IBaseGameInfo currentGameInfo = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);
            Map<String, HashSet<String>> dp = CacheObjectComparator.compare(oldBaseGameInfo, currentGameInfo, true);

            if (dp.containsKey("rmClassName") && !dp.get("rmClassName").isEmpty()) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage("error.versioning.cannotChangeValue", "rmClassName",
                                oldBaseGameInfo.getRmClassName(), currentGameInfo.getRmClassName(), rmClassName));
            } else {
                currentGameInfo.setRmClassName(rmClassName);
            }

            if (dp.containsKey("gsClassName") && !dp.get("gsClassName").isEmpty()) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage("error.versioning.cannotChangeValue", "gsClassName",
                                oldBaseGameInfo.getGsClassName(), currentGameInfo.getGsClassName(), gsClassName));
            } else {
                currentGameInfo.setGsClassName(gsClassName);
            }

            if (dp.containsKey("servlet") && !dp.get("servlet").isEmpty()) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage("error.versioning.cannotChangeValue", "servlet",
                                oldBaseGameInfo.getServlet(), currentGameInfo.getServlet(), servlet));
            } else {
                currentGameInfo.setServlet(servlet);
            }

            if (dp.containsKey("limit") && !dp.get("limit").isEmpty()) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage("error.versioning.cannotChangeValue", "limit",
                                oldBaseGameInfo.getLimit(), currentGameInfo.getLimit(), limit));
            } else {
                currentGameInfo.setLimit(limit);
            }

            if (dp.containsKey("coins") && !dp.get("coins").isEmpty()) {
                String onLoadCoinsListString;
                String onServerCoinsListString;
                String settingCoinsListString;

                StringBuilder builder = new StringBuilder();
                for (Coin coin : oldBaseGameInfo.getCoins()) {
                    builder.append("<br>");
                    builder.append(DigitFormatter.doubleToMoney(centsToDouble(coin.getValue())));
                }
                onLoadCoinsListString = builder.toString();
                builder = new StringBuilder();

                List<Coin> coins1 = currentGameInfo.getCoins();
                for (Coin coin : coins1) {
                    builder.append("<br>");
                    builder.append(DigitFormatter.doubleToMoney(centsToDouble(coin.getValue())));
                }
                onServerCoinsListString = builder.toString();
                builder = new StringBuilder();

                for (Coin coin : coins) {
                    builder.append("<br>");
                    builder.append(DigitFormatter.doubleToMoney(centsToDouble(coin.getValue())));
                }
                settingCoinsListString = builder.toString();

                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage("error.versioning.cannotChangeValue", "coins",
                                onLoadCoinsListString, onServerCoinsListString, settingCoinsListString));
            } else {
                currentGameInfo.setCoins(coins);
            }

            // list of properties for all currencies
            Set<String> propForAllCurrenciesKeySet = new HashSet<>();
            if (gameForm.getAllCurrencyPropertyList() != null) {
                propForAllCurrenciesKeySet.addAll(Arrays.asList(gameForm.getAllCurrencyPropertyList()));
            }

            BaseGameInfo defaultGameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
            for (LabelValueBean prop : gameForm.getProperties()) {
                if (propForAllCurrenciesKeySet.contains(prop.getLabel())) {
                    for (Currency cur : BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies()) {
                        if (!cur.equals(currency)) {
                            IBaseGameInfo game = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, cur);
                            if (game != null) {
                                removeOrSetProperty(inheritedProperties, prop, propertiesToRemove, propertiesToReset,
                                        defaultGameInfo, game, errors);
                                RemoteCallHelper.getInstance().saveAndSendNotification(currentGameInfo);
                            }
                        }
                    }
                } else if (dp.containsKey("propertiesMap") && dp.get("propertiesMap").contains(prop.getLabel())) {
                    errors.add(GLOBAL_MESSAGE,
                            new ActionMessage("error.versioning.cannotChangeValue", "propertiesMap." + prop.getLabel(),
                                    oldBaseGameInfo.getProperty(prop.getLabel()), currentGameInfo.getProperty(prop.getLabel()), prop.getValue()));
                    continue;
                }
                removeOrSetProperty(inheritedProperties, prop, propertiesToRemove, propertiesToReset, defaultGameInfo,
                        currentGameInfo, errors);
            }
            setNewProperty(inheritedProperties, gameForm, currentGameInfo, errors);

            RemoteCallHelper.getInstance().saveAndSendNotification(currentGameInfo);
        }

        saveErrors(request.getSession(), errors);
        return BaseAction.getActionRedirectByHost(request, "/support/loadgameinfo.do?bankId=" + gameForm.getBankId() +
                "&curCode=" + gameForm.getCurrencyCode() +
                "&gameId=" + gameForm.getGameId());
    }

    private void checkNewPropertyExisting(String newPropertyKey, ActionMessages errors) {
        if (newPropertyKey != null && !BaseGameConstants.containsProperty(newPropertyKey)) {
            errors.add(GLOBAL_MESSAGE,
                    new ActionMessage("error.nonexistent.property.add", newPropertyKey));
        }
    }

    private void setNewProperty(Set<String> inheritProperties, GameInfoForm gameForm, IBaseGameInfo currentGameInfo, ActionMessages errors) {
        if (gameForm.isNewProperty()) {
            String newProperty = gameForm.getNewPropKey().trim();
            String newPropertyValue = gameForm.getNewPropValue().trim();
            if (!inheritProperties.contains(newProperty)
                    && BaseGameConstants.containsProperty(newProperty)
                    && isPropValueGood(newProperty, newPropertyValue, errors)) {
                currentGameInfo.setProperty(newProperty, newPropertyValue);
            }
        }
    }

    private void removeOrSetProperty(Set<String> inheritProperties, LabelValueBean property, Set<String> propertiesToRemove,
                                     Set<String> propertiesToReset, BaseGameInfo defaultGameInfo,
                                     IBaseGameInfo currentGameInfo, ActionMessages errors) {
        if (!propertiesToRemove.contains(property.getLabel())) {
            setProperty(inheritProperties, property, propertiesToReset, currentGameInfo, defaultGameInfo, errors);
        } else {
            if (!inheritProperties.contains(property.getLabel())) {
                currentGameInfo.removeProperty(property.getLabel());
            }
        }
    }

    private void setProperty(Set<String> inheritProperties, LabelValueBean property, Set<String> propertiesToReset,
                             IBaseGameInfo currentGameInfo, IBaseGameInfo defaultGameInfo, ActionMessages errors) {
        if (!propertiesToReset.contains(property.getLabel())) {
            if (!inheritProperties.contains(property.getLabel()) && isPropValueGood(property.getLabel(), property.getValue(), errors)) {
                currentGameInfo.setProperty(property.getLabel(), property.getValue());
            }
        } else {
            currentGameInfo.setProperty(property.getLabel(), defaultGameInfo.getProperty(property.getLabel()));
        }
    }

    private Set<String> filterInheritedProperties(Set<String> inheritProperties, Collection<String> properties) {
        return properties.stream()
                .filter(inheritProperties::contains)
                .collect(Collectors.toSet());
    }

    private void checkAddInheritedProperty(Set<String> inheritProperties, String newPropertyKey, ActionMessages errors) {
        if (inheritProperties.contains(newPropertyKey)) {
            errors.add(GLOBAL_MESSAGE,
                    new ActionMessage("error.inherit.property.add", newPropertyKey));
        }
    }

    private Set<String> getPropertiesToReset(Set<String> inheritedProperties, String[] resetList, ActionMessages errors) {
        Set<String> propertiesToReset = new HashSet<>();
        if (resetList != null) {
            propertiesToReset.addAll(Arrays.asList(resetList));
            Set<String> propertiesToResetInherited = filterInheritedProperties(inheritedProperties, propertiesToReset);
            addInheritedPropertiesErrors(propertiesToResetInherited, "error.inherit.property.reset", errors);
            propertiesToReset.removeAll(propertiesToResetInherited);
        }
        return propertiesToReset;
    }

    private Set<String> getPropertiesToRemove(Set<String> inheritedProperties, String[] removeList, ActionMessages errors) {
        Set<String> propertiesToRemove = new HashSet<>();
        if (removeList != null) {
            propertiesToRemove.addAll(Arrays.asList(removeList));
            Set<String> propertiesToRemoveInherited = filterInheritedProperties(inheritedProperties, propertiesToRemove);
            addInheritedPropertiesErrors(propertiesToRemoveInherited, "error.inherit.property.remove", errors);
            propertiesToRemove.removeAll(propertiesToRemoveInherited);
        }
        return propertiesToRemove;
    }

    private void addInheritedPropertiesErrors(Set<String> propertiesWithError, String errorType, ActionMessages errors) {
        if (!propertiesWithError.isEmpty()) {
            List<ActionMessage> messages = propertiesWithError.stream()
                    .map(property -> new ActionMessage(errorType, property))
                    .collect(Collectors.toList());
            for (ActionMessage message : messages) {
                errors.add(GLOBAL_MESSAGE, message);
            }
        }
    }

    private void fillForm(GameInfoForm form, HttpServletRequest request) {
        List<LabelValueBean> properties = new ArrayList<>();
        int propertiesCount = 0;
        for (String key : request.getParameterMap().keySet()) {
            if (key.contains("prop") && key.contains("label")) {
                propertiesCount++;
            }
        }

        for (int i = 0; i < propertiesCount; i++) {
            properties.add(getlvbPropertyByIndexId(i, request));
        }
        form.setProperties(properties);
    }

    private void acceptServletForSubcasino(String servlet, long bankId, long gameId) {
        long subcasinoId = getSubcasinoId(bankId);
        List<Long> banks = SubCasinoCache.getInstance().getBankIds(subcasinoId);
        for (long bank : banks) {
            Currency defCurrency = BankInfoCache.getInstance().getBankInfo(bank).getDefaultCurrency();
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfo(bank, gameId, defCurrency);
            if (gameInfo != null) {
                gameInfo.setServlet(servlet);
                try {
                    RemoteCallHelper.getInstance().saveAndSendNotification(gameInfo);
                } catch (CommonException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private LabelValueBean getlvbPropertyByIndexId(int id, HttpServletRequest request) {
        String label = request.getParameter("prop[" + id + "].label");
        String value;
        if (isBooleanProperty(label)) {
            value = (request.getParameterMap().containsKey("prop[" + id + "].value")) ? TRUE : "FALSE";
        } else {
            value = request.getParameter("prop[" + id + "].value");
        }
        return new LabelValueBean(label, value);
    }

    private long getSubcasinoId(long bankId) {
        for (Long subcasinoIdStr : SubCasinoCache.getInstance().getAllObjects().keySet()) {
            List<Long> banks = SubCasinoCache.getInstance().getBankIds(subcasinoIdStr);
            if (banks.contains(bankId)) {
                return subcasinoIdStr;
            }
        }
        return -1;
    }

    private String propertyValue(String value) {
        if (StringUtils.isTrimmedEmpty(value)) {
            return null;
        } else {
            return value.trim();
        }
    }

    private Set<String> getPropertiesInheritedFromTemplate() {
        Class<BaseGameConstants> clazz = BaseGameConstants.class;
        Set<Field> fields = Stream.of(clazz.getDeclaredFields())
                .filter(field -> isConstant(field.getModifiers()))
                .filter(field -> field.getAnnotation(InheritFromTemplate.class) != null)
                .collect(Collectors.toSet());
        Set<String> properties = new HashSet<>();
        for (Field field : fields) {
            try {
                String value = (String) field.get(null);
                properties.add(value);
            } catch (IllegalAccessException ignore) {
            }
        }
        return properties;
    }

    private boolean isConstant(int modifiers) {
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    private static double centsToDouble(long cents) {
        return cents / 100d;
    }
}

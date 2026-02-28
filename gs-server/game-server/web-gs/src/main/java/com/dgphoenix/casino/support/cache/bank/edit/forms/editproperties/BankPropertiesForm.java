package com.abs.casino.support.cache.bank.edit.forms.editproperties;


import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.util.CurrencyLabelValueListBean;
import com.abs.casino.common.util.LanguageLabelValueBean;
import com.abs.casino.common.util.LimitLabelValueListBean;
import com.abs.casino.common.util.property.BooleanProperty;
import com.abs.casino.common.util.property.MandatoryProperty;
import com.abs.casino.common.util.property.NumericProperty;
import com.abs.casino.common.util.property.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.dgphoenix.casino.common.cache.data.bank.BankInfo.KEY_FREE_BALANCE;
import static org.apache.struts.action.ActionErrors.GLOBAL_MESSAGE;

public class BankPropertiesForm extends ActionForm {

    private static final Logger LOG = LogManager.getLogger(BankPropertiesForm.class);

    private String bankId;
    private String mode;
    private Collection<LabelValueBean> allBanks;

    private String newKey;
    private String newValue;

    private String externalBankId;
    private String externalBankIdDescription;
    private String defaultCurrencyCode;
    private String limitId;
    private String defaultLanguage;
    private String freeGameOverRedirectUrl;

    private final CurrencyLabelValueListBean currenciesList = new CurrencyLabelValueListBean();
    private final LimitLabelValueListBean limitsList = new LimitLabelValueListBean();
    private final LanguageLabelValueBean languagesList = new LanguageLabelValueBean();

    private String cashierUrl;

    private String allowedRefererDomains;
    private String forbiddenRefererDomains;

    private Map<String, String> bankProperties = new HashMap<>();

    public LanguageLabelValueBean getLanguagesList() {
        return languagesList;
    }

    public List getCurrenciesList() {
        return currenciesList;
    }

    public LimitLabelValueListBean getLimitsList() {
        return limitsList;
    }

    public String getExternalBankId() {
        return externalBankId;
    }

    public void setExternalBankId(String externalBankId) {
        this.externalBankId = externalBankId;
    }

    public String getExternalBankIdDescription() {
        return externalBankIdDescription;
    }

    public void setExternalBankIdDescription(String externalBankIdDescription) {
        this.externalBankIdDescription = externalBankIdDescription;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    public void setDefaultCurrencyCode(String defaultCurrency) {
        this.defaultCurrencyCode = defaultCurrency;
    }

    public String getLimitId() {
        return limitId;
    }

    public void setLimitId(String limit) {
        this.limitId = limit;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getFreeGameOverRedirectUrl() {
        return freeGameOverRedirectUrl;
    }

    public void setFreeGameOverRedirectUrl(String freeGameOverRedirectUrl) {
        this.freeGameOverRedirectUrl = freeGameOverRedirectUrl;
    }

    public Map<String, String> getBankProperties() {
        return bankProperties;
    }

    public void setBankProperties(Map<String, String> properties) {
        this.bankProperties = new TreeMap<>(properties);
    }

    public void setBankProperty(String key, String value) {
        bankProperties.put(key, value);
    }

    public String getBankProperty(String key) {
        return bankProperties.get(key);
    }

    public void setValue(String key, Object value) {
        bankProperties.put(key, (String) value);
    }

    public Object getValue(String key) {
        return bankProperties.get(key);
    }

    public void setExternalBankId() {

    }

    public String getAllowedRefererDomains() {
        return allowedRefererDomains;
    }

    public void setAllowedRefererDomains(String allowedRefererDomains) {
        this.allowedRefererDomains = allowedRefererDomains;
    }

    public String getForbiddenRefererDomains() {
        return forbiddenRefererDomains;
    }

    public void setForbiddenRefererDomains(String forbiddenRefererDomains) {
        this.forbiddenRefererDomains = forbiddenRefererDomains;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        Class bankInfoClass = BankInfo.class;
        Field[] fields = bankInfoClass.getDeclaredFields();
        for (Map.Entry<String, String> d : bankProperties.entrySet()) {
            try {
                String key = d.getKey();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(BooleanProperty.class) &&
                            Modifier.isPublic(field.getModifiers()) &&
                            Modifier.isStatic(field.getModifiers()) &&
                            Modifier.isFinal(field.getModifiers())) {
                        Object fieldValue = field.get(null);
                        if (key.equals(fieldValue)) {
                            bankProperties.put(key, "false");
                            break;
                        }
                    }
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        LOG.debug("enter validate");
        ActionErrors errors = new ActionErrors();
        if (request.getParameter("button") != null && request.getParameter("button").equals("save")) {
            Field[] fields = BankInfo.class.getDeclaredFields();
            for (Map.Entry<String, String> entry : bankProperties.entrySet()) {
                String mapValue = StringUtils.trim(entry.getValue());
                String key = entry.getKey();
                for (Field field : fields) {
                    if ((field.isAnnotationPresent(BooleanProperty.class) || field.isAnnotationPresent(StringProperty.class) ||
                            field.isAnnotationPresent(NumericProperty.class)) &&
                            Modifier.isPublic(field.getModifiers()) &&
                            Modifier.isStatic(field.getModifiers()) &&
                            Modifier.isFinal(field.getModifiers())) {
                        try {
                            field.setAccessible(true);
                            Object fieldValue = field.get(null);
                            if (key.equals(fieldValue)) {
                                if (field.isAnnotationPresent(MandatoryProperty.class) && mapValue.trim().isEmpty()) {
                                    errors.add(GLOBAL_MESSAGE,
                                            new ActionMessage("error.bankPropertiesForm.propertyIsMandatory", key));
                                } else {
                                    if (field.isAnnotationPresent(BooleanProperty.class)) {
                                        if (!(mapValue.equalsIgnoreCase("true") ||
                                                mapValue.equalsIgnoreCase("false") || mapValue.isEmpty())) {
                                            errors.add(GLOBAL_MESSAGE,
                                                    new ActionMessage("error.bankPropertiesForm.incorrectBooleanFormat",
                                                            key, mapValue));
                                        }
                                    } else {
                                        if (field.isAnnotationPresent(NumericProperty.class) && !mapValue.isEmpty()) {
                                            validateNumericProperties(errors, mapValue, key);
                                        }
                                    }
                                }
                                break;
                            }
                        } catch (IllegalAccessException e) {
                            LOG.error("validate error", e);
                        }
                    }
                }
            }
        }
        return errors;
    }

    private void validateNumericProperties(ActionErrors errors, String mapValue, String key) {
        try {
            long value = Long.parseLong(mapValue);
            if (key.equals(KEY_FREE_BALANCE) && value < 1) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage("error.bankPropertiesForm.lessThenOne",
                                key, mapValue));
            }
        } catch (NumberFormatException e) {
            errors.add(GLOBAL_MESSAGE,
                    new ActionMessage("error.bankPropertiesForm.incorrectNumericFormat",
                            key, mapValue));
        }
    }

    public String getCashierUrl() {
        return cashierUrl;
    }

    public void setCashierUrl(String cashierUrl) {
        this.cashierUrl = cashierUrl;
    }

    public String getNewKey() {
        return newKey;
    }

    public void setNewKey(String newKey) {
        this.newKey = newKey;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public Collection<LabelValueBean> getAllBanks() {
        return allBanks;
    }

    public void setAllBanks(Collection<LabelValueBean> allBanks) {
        this.allBanks = allBanks;
    }
}

package com.abs.casino.support.cache.bank.edit.actions.editproperties;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.abs.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.support.CacheObjectComparator;
import com.abs.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.axis.encoding.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE;

public class EditPropertiesAction extends Action {
    private static final Logger LOG = LogManager.getLogger(EditPropertiesAction.class);
    public static final String LOG_MESSAGE_FORMAT = "Change BankInfo Bank_ID={0} property {1}: {2} -> {3} " +
            "|Editor info: IP={4} Host={5}";
    protected static final String PROPERTY_CHANGED = "message.EditPropertiesAction.propertyChanged";
    protected static final String CANNOT_CHANGE_VALUE = "error.versioning.cannotChangeValue";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        ActionMessages messages = new ActionMessages();
        ActionMessages errors = getErrors(request);
        if (errors == null) {
            errors = new ActionErrors();
        }
        BankPropertiesForm bpForm = (BankPropertiesForm) form;
        long bankId = Long.parseLong(bpForm.getBankId());

        if (request.getParameter("button").equals("back")) {
            ActionRedirect redirect = BaseAction.getActionRedirectByHost(request, "/support/bankInfo.do");
            redirect.addParameter("bankId", bankId);
            return redirect;
        }

        XStream xStream = new XStream(new StaxDriver());
        BankInfo defaultBankInfo = (BankInfo) xStream.fromXML(new String(Base64.decode(request.getParameter("bankInfoXML"))));
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        HashMap<String, HashSet<String>> differentProperties = CacheObjectComparator.compare(defaultBankInfo, bankInfo);

        for (Map.Entry<String, String> entry : bankInfo.getProperties().entrySet()) {
            if (differentProperties.containsKey("properties") && differentProperties.get("properties").contains(entry.getKey())) {
                continue;
            }
            if (bankInfo.getStringProperty(entry.getKey()) != null
                    && bankInfo.getStringProperty(entry.getKey()).equalsIgnoreCase("true")
                    && (request.getParameterValues("value(" + entry.getKey() + ")") == null)) {
                logChangeProperty(request, bankId, entry.getKey(), "true", "false");
                messages.add(GLOBAL_MESSAGE, new ActionMessage(PROPERTY_CHANGED, entry.getKey(), "true", "false"));
                bankInfo.setProperty(entry.getKey(), "false");
            }
        }

        for (Map.Entry<String, String> entry : bpForm.getBankProperties().entrySet()) {
            if (differentProperties.containsKey("properties") && differentProperties.get("properties").contains(entry.getKey())) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage(CANNOT_CHANGE_VALUE,
                        entry.getKey(), defaultBankInfo.getStringProperty(entry.getKey()), bankInfo.getStringProperty(entry.getKey()), entry.getValue().trim())
                );
                continue;
            }
            String key = entry.getKey();
            String newValue = entry.getValue().trim();
            if (isTrimmedEmpty(newValue)) {
                newValue = null;
            }
            String oldValue = bankInfo.getStringProperty(key);
            if ((oldValue != null && !oldValue.equals(newValue)) || (oldValue == null && newValue != null)) {
                String validationError = validateChangedProperty(key, newValue, bankInfo);
                if (validationError != null) {
                    errors.add(GLOBAL_MESSAGE, new ActionMessage("message.EditPropertiesAction.validationFailed", key, validationError));
                    continue;
                }
                logChangeProperty(request, bankId, key, oldValue, newValue);
                messages.add(GLOBAL_MESSAGE, new ActionMessage(PROPERTY_CHANGED, key, oldValue, newValue));
                bankInfo.setProperty(key, newValue);
            }
        }

        if ((bankInfo.getCashierUrl() == null && bpForm.getCashierUrl() != null) ||
                (bankInfo.getCashierUrl() != null && !bankInfo.getCashierUrl().equals(bpForm.getCashierUrl()))) {
            if (differentProperties.containsKey("cashierUrl")) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage(CANNOT_CHANGE_VALUE,
                        "cashierUrl", defaultBankInfo.getCashierUrl(), bankInfo.getCashierUrl(), bpForm.getCashierUrl())
                );
            } else {
                logChangeProperty(request, bankId, "CashierUrl", bankInfo.getCashierUrl(), bpForm.getCashierUrl());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "CashierUrl", bankInfo.getCashierUrl(), bpForm.getCashierUrl()));
                bankInfo.setCashierUrl(bpForm.getCashierUrl());
            }
        }

        if ((bankInfo.getExternalBankId() == null && bpForm.getExternalBankId() != null) ||
                (bankInfo.getExternalBankId() != null && !bankInfo.getExternalBankId().equals(
                        bpForm.getExternalBankId()))) {
            if (differentProperties.containsKey("externalBankId")) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage(CANNOT_CHANGE_VALUE,
                                "ExternalBankId", defaultBankInfo.getExternalBankId(), bankInfo.getExternalBankId(), bpForm.getExternalBankId())
                );
            } else {
                logChangeProperty(request, bankId, "ExternalBankId", bankInfo.getExternalBankId(), bpForm.getExternalBankId());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "ExternalBankId", bankInfo.getExternalBankId(), bpForm.getExternalBankId()));
                bankInfo.setExternalBankId(bpForm.getExternalBankId());
            }
        }

        if ((bankInfo.getExternalBankIdDescription() == null && bpForm.getExternalBankIdDescription() != null) ||
                (bankInfo.getExternalBankIdDescription() != null && !bankInfo.getExternalBankIdDescription().equals(
                        bpForm.getExternalBankIdDescription()))) {
            if (differentProperties.containsKey("externalBankIdDescription")) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage(CANNOT_CHANGE_VALUE,
                        "ExternalBankIdDescription", defaultBankInfo.getExternalBankIdDescription(),
                        bankInfo.getExternalBankIdDescription(), bpForm.getExternalBankIdDescription())
                );
            } else {
                logChangeProperty(request, bankId, "ExternalBankIdDescription", bankInfo.getExternalBankIdDescription(),
                        bpForm.getExternalBankIdDescription());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "ExternalBankIdDescription", bankInfo.getExternalBankIdDescription(),
                                bpForm.getExternalBankIdDescription()));
                bankInfo.setExternalBankIdDescription(bpForm.getExternalBankIdDescription());
            }
        }

        if ((bankInfo.getDefaultCurrency() == null && bpForm.getDefaultCurrencyCode() != null && !bpForm.
                getDefaultCurrencyCode().trim().isEmpty())
                || (bankInfo.getDefaultCurrency() != null && !bankInfo.getDefaultCurrency().getCode().equals(
                bpForm.getDefaultCurrencyCode()))) {
            if (differentProperties.containsKey("defaultCurrency")) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage(CANNOT_CHANGE_VALUE, "DefaultCurrency",
                        defaultBankInfo.getDefaultCurrency(), bankInfo.getDefaultCurrency(), bpForm.getDefaultCurrencyCode())
                );
            } else {
                String oldCurrencyCode = "__";
                if (bankInfo.getDefaultCurrency() != null) {
                    oldCurrencyCode = bankInfo.getDefaultCurrency().getCode();
                }
                logChangeProperty(request, bankId, "DefaultCurrency", oldCurrencyCode, bpForm.getDefaultCurrencyCode());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "DefaultCurrency", oldCurrencyCode, bpForm.getDefaultCurrencyCode()));
                //change default currency is more complex and dangerous operation,
                //commented to prevent accidental changes
                //bankInfo.setDefaultCurrency(CurrencyCache.getInstance().get(bpForm.getDefaultCurrencyCode()));
            }
        }

        if ((bankInfo.getLimit() == null && bpForm.getLimitId() != null && !bpForm.getLimitId().trim().isEmpty())
                || (bankInfo.getLimit() != null && bankInfo.getLimit().getId() != Long.parseLong(
                bpForm.getLimitId()))) {
            if (differentProperties.containsKey("limit")) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage(CANNOT_CHANGE_VALUE,
                        "Limit", defaultBankInfo.getLimit(), bankInfo.getLimit(), bpForm.getLimitId())
                );
            } else {
                String oldLimitId = "__";
                if (bankInfo.getLimit() != null) {
                    oldLimitId = Long.toString(bankInfo.getLimit().getId());
                }
                logChangeProperty(request, bankId, "Limit", oldLimitId, bpForm.getLimitId());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "Limit", oldLimitId, bpForm.getLimitId()));
                bankInfo.setLimit(Limit.getById((Long.parseLong(bpForm.getLimitId()))));
            }
        }

        if ((bankInfo.getDefaultLanguage() == null && bpForm.getDefaultLanguage() != null)
                || (bankInfo.getDefaultLanguage() != null && !bankInfo.getDefaultLanguage().equals(
                bpForm.getDefaultLanguage()))) {
            if (differentProperties.containsKey("defaultLanguage")) {
                errors.add(GLOBAL_MESSAGE, new ActionMessage(CANNOT_CHANGE_VALUE, "defaultLanguage",
                        defaultBankInfo.getDefaultLanguage(), bankInfo.getDefaultLanguage(), bpForm.getDefaultLanguage())
                );
            } else {
                logChangeProperty(request, bankId, "defaultLanguage", bankInfo.getDefaultLanguage(),
                        bpForm.getDefaultLanguage());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "DefaultLanguage", bankInfo.getDefaultLanguage(),
                                bpForm.getDefaultLanguage()));
                bankInfo.setDefaultLanguage(bpForm.getDefaultLanguage());
            }
        }

        if ((bankInfo.getFreeGameOverRedirectUrl() == null && bpForm.getFreeGameOverRedirectUrl() != null)
                || (bankInfo.getFreeGameOverRedirectUrl() != null && !bankInfo.getFreeGameOverRedirectUrl().equals(
                bpForm.getFreeGameOverRedirectUrl()))) {
            if (differentProperties.containsKey("freeGameOverRedirectUrl")) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage(CANNOT_CHANGE_VALUE, "FreeGameOverRedirectUrl", defaultBankInfo.getFreeGameOverRedirectUrl(),
                                bankInfo.getFreeGameOverRedirectUrl(), bpForm.getFreeGameOverRedirectUrl())
                );
            } else {
                logChangeProperty(request, bankId, "FreeGameOverRedirectUrl", bankInfo.getFreeGameOverRedirectUrl(),
                        bpForm.getFreeGameOverRedirectUrl());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "FreeGameOverRedirectUrl", bankInfo.getFreeGameOverRedirectUrl(),
                                bpForm.getFreeGameOverRedirectUrl()));
                bankInfo.setFreeGameOverRedirectUrl(bpForm.getFreeGameOverRedirectUrl());
            }
        }

        if ((bankInfo.getAllowedRefererDomains() == null && bpForm.getAllowedRefererDomains() != null)
                || (bankInfo.getAllowedRefererDomains() != null && !bankInfo.getAllowedRefererDomains().equals(
                bpForm.getAllowedRefererDomains()))) {
            if (differentProperties.containsKey("allowedRefererDomains")) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage(CANNOT_CHANGE_VALUE,
                                "allowedDomains", defaultBankInfo.getAllowedRefererDomains(), bankInfo.getAllowedRefererDomains(),
                                bpForm.getAllowedRefererDomains())
                );
            } else {
                logChangeProperty(request, bankId, "allowedRefererDomains", bankInfo.getAllowedRefererDomains(),
                        bpForm.getAllowedRefererDomains());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "allowedRefererDomains", bankInfo.getAllowedRefererDomains(),
                                bpForm.getAllowedRefererDomains()));
                bankInfo.setAllowedRefererDomains(bpForm.getAllowedRefererDomains());
            }
        }

        if ((bankInfo.getForbiddenRefererDomains() == null && bpForm.getForbiddenRefererDomains() != null)
                || (bankInfo.getForbiddenRefererDomains() != null && !bankInfo.getForbiddenRefererDomains().equals(
                bpForm.getForbiddenRefererDomains()))) {
            if (differentProperties.containsKey("forbiddenRefererDomains")) {
                errors.add(GLOBAL_MESSAGE,
                        new ActionMessage(CANNOT_CHANGE_VALUE,
                                "restrictedDomains", defaultBankInfo.getForbiddenRefererDomains(), bankInfo.getForbiddenRefererDomains(),
                                bpForm.getForbiddenRefererDomains())
                );
            } else {
                logChangeProperty(request, bankId, "forbiddenRefererDomains", bankInfo.getForbiddenRefererDomains(),
                        bpForm.getForbiddenRefererDomains());
                messages.add(GLOBAL_MESSAGE,
                        new ActionMessage(PROPERTY_CHANGED, "forbiddenRefererDomains", bankInfo.getForbiddenRefererDomains(),
                                bpForm.getForbiddenRefererDomains()));
                bankInfo.setForbiddenRefererDomains(bpForm.getForbiddenRefererDomains());
            }
        }

        saveMessages(request.getSession(), messages);
        saveErrors(request.getSession(), errors);
        RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
        String redirectTo = request.getScheme() + "://" + request.getServerName() +
                "/support/bankSelectAction.do?bankId=" + bankId;
        LOG.info("request.getServerName()={}, request={}, redirectTo={}", request.getServerName(), request.getClass(), redirectTo);
        return BaseAction.getActionRedirectByHost(request, "/support/bankSelectAction.do?bankId=" + bankId);
    }

    private String validateChangedProperty(String key, String newValue, BankInfo bankInfo) {
        switch (key) {
            case BankInfo.KEY_FRB_GAMES_ENABLE:
                return !isTrimmedEmpty(newValue) && !isTrimmedEmpty(bankInfo.getStringProperty(BankInfo.KEY_FRB_GAMES_DISABLE)) ?
                        BankInfo.KEY_FRB_GAMES_DISABLE + " already specified" : null;
            case BankInfo.KEY_FRB_GAMES_DISABLE:
                return !isTrimmedEmpty(newValue) && !isTrimmedEmpty(bankInfo.getStringProperty(BankInfo.KEY_FRB_GAMES_ENABLE)) ?
                        BankInfo.KEY_FRB_GAMES_ENABLE + " already specified" : null;
            default:
                return null;
        }
    }

    private void logChangeProperty(HttpServletRequest request, long bankId, String propertyName, Object oldValue,
                                   Object newValue) {
        String errorMessage = MessageFormat.format(LOG_MESSAGE_FORMAT, bankId, propertyName, oldValue, newValue,
                request.getRemoteAddr(), request.getRemoteHost());
        LOG.error(errorMessage);
    }
}

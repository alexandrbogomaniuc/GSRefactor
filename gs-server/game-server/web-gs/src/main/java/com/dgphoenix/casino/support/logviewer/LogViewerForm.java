package com.abs.casino.support.logviewer;

import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.support.AdditionalInfoAttribute;
import com.dgphoenix.casino.common.util.support.HttpCallInfo;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Predicate;

import static com.dgphoenix.casino.common.util.support.AdditionalInfoAttribute.*;
import static java.util.stream.Collectors.toList;
import static org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 10.02.2020
 */
public class LogViewerForm extends ActionForm {

    private static final List<String> SEARCH_ATTRIBUTES = ImmutableList.of(
            SESSION_ID.name(),
            SUPPORT_TICKET_ID.name(),
            TOKEN.name(),
            EXTERNAL_ID.name(),
            GAME_SESSION_ID.name(),
            ROUND_ID.name(),
            TRANSACTION_ID.name()
    );

    private List<LabelValueBean> searchAttributeLabelValues;
    private String searchAttribute;
    private String searchValue;
    private long bankId;
    private List<HttpCallInfo> httpCallInfoList;
    private String message;

    public LogViewerForm() {
        searchAttributeLabelValues = SEARCH_ATTRIBUTES.stream()
                .map(a -> new LabelValueBean(a, a))
                .collect(toList());
    }

    public List<LabelValueBean> getSearchAttributeLabelValues() {
        return searchAttributeLabelValues;
    }

    public void setSearchAttributeLabelValues(List<LabelValueBean> searchAttributeLabelValues) {
        this.searchAttributeLabelValues = searchAttributeLabelValues;
    }

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public void setSearchAttribute(String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public List<HttpCallInfo> getHttpCallInfoList() {
        return httpCallInfoList;
    }

    public void setHttpCallInfoList(List<HttpCallInfo> httpCallInfoList) {
        this.httpCallInfoList = httpCallInfoList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        Predicate<String> empty = StringUtils::isTrimmedEmpty;
        Predicate<String> invalidNumber = value -> !NumberUtils.isNumber(value) || Long.parseLong(value) <= 0;
        if (searchAttribute != null) {
            AdditionalInfoAttribute attribute = AdditionalInfoAttribute.valueOf(searchAttribute);
            switch (attribute) {
                case SESSION_ID:
                case SUPPORT_TICKET_ID:
                case TOKEN:
                    checkSearchValue(empty, errors);
                    break;
                case GAME_SESSION_ID:
                case ROUND_ID:
                case TRANSACTION_ID:
                    checkSearchValue(invalidNumber, errors);
                    break;
                case EXTERNAL_ID:
                    checkSearchValue(empty, errors);
                    checkBankId(errors);
                    break;
                default:
                    break;
            }
        }
        return errors;
    }

    private void checkSearchValue(Predicate<String> isInputInvalid, ActionErrors errors) {
        if (isInputInvalid.test(searchValue)) {
            errors.add(GLOBAL_MESSAGE, new ActionMessage("error.LogViewerForm.invalidSearchValue"));
        }
    }

    private void checkBankId(ActionErrors errors) {
        if (bankId <= 0) {
            errors.add(GLOBAL_MESSAGE, new ActionMessage("error.LogViewerForm.invalidBankId"));
        }
    }

    @Override
    public String toString() {
        return "LogViewerForm{" +
                "searchAttributeLabelValues=" + searchAttributeLabelValues +
                ", searchAttribute='" + searchAttribute + '\'' +
                ", searchValue='" + searchValue + '\'' +
                ", bankId=" + bankId +
                ", httpCallInfoList=" + httpCallInfoList +
                ", message='" + message + '\'' +
                "} " + super.toString();
    }
}

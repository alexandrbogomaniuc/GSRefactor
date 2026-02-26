package com.abs.casino.actions.support.apiIssues;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by quant on 03.12.15.
 */
public class APIIssuesForm extends ActionForm {
    private static final int MAX_DAYS = 30;
    private int startDay;
    private int startMonth;
    private int startYear;
    private int endDay;
    private int endMonth;
    private int endYear;

    private String sortBy;
    private String urlFilter;
    private boolean sortByCount = false;
    private boolean descendingOrder = false;

    private Map.Entry<Date, Map<Object, List<String>>>[] apiIssues;

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(int endMonth) {
        this.endMonth = endMonth;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public Date getStartDate() {
        return new GregorianCalendar(startYear, startMonth - 1, startDay, 0, 0, 0).getTime();
    }

    public Date getEndDate() {
        return new GregorianCalendar(endYear, endMonth - 1, endDay, 23, 59, 59).getTime();
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getUrlFilter() {
        return urlFilter;
    }

    public void setUrlFilter(String urlFilter) {
        this.urlFilter = urlFilter;
    }

    public boolean isSortByCount() {
        return sortByCount;
    }

    public void setSortByCount(boolean sortByCount) {
        this.sortByCount = sortByCount;
    }

    public boolean isDescendingOrder() {
        return descendingOrder;
    }

    public void setDescendingOrder(boolean descendingOrder) {
        this.descendingOrder = descendingOrder;
    }

    public Map.Entry<Date, Map<Object, List<String>>>[] getApiIssues() {
        return apiIssues;
    }

    public void setApiIssues(Map.Entry<Date, Map<Object, List<String>>>[] apiIssues) {
        this.apiIssues = apiIssues;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        long timeDiff = getEndDate().getTime() - getStartDate().getTime();
        if (timeDiff < 0) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.apiIssuesForm.startAfterEnd"));
        } else if (TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) > MAX_DAYS) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.apiIssuesForm.tooLargeDateRange", MAX_DAYS));
        }
        return errors;
    }

    @Override
    public String toString() {
        return "APIIssuesForm{" +
                "startDay=" + startDay +
                ", startMonth=" + startMonth +
                ", startYear=" + startYear +
                ", endDay=" + endDay +
                ", endMonth=" + endMonth +
                ", endYear=" + endYear +
                ", sortBy='" + sortBy + '\'' +
                ", urlFilter='" + urlFilter + '\'' +
                ", sortByCount=" + sortByCount +
                ", descendingOrder=" + descendingOrder +
                '}';
    }
}

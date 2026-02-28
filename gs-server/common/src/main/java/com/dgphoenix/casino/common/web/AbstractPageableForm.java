package com.abs.casino.common.web;

import com.dgphoenix.casino.common.web.IPageableForm;

import org.apache.struts.action.ActionForm;

/**
 * User: flsh
 * Date: 16.04.2009
 */
public abstract class AbstractPageableForm extends ActionForm implements IPageableForm {
    private int count;
    private int page = 1;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getItemsPerPage() {
        return DEFAULT_ITEMS_PER_PAGE;
    }

    public int getPagesPerPage() {
        return DEFAULT_PAGES_PER_PAGE;
    }

    public int getOffset() {
        return (getPage() - 1) * getItemsPerPage();
    }

    public int getLastPage() {
        if (getCount() == 0) return 1;
        return (int) Math.ceil((double) getCount() / getItemsPerPage());
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("AbstractPageableForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("count=").append(count);
        sb.append(", page=").append(page);
        sb.append(", offset=").append(getOffset());
        sb.append(", itemsPerPage=").append(getItemsPerPage());
        sb.append(']');
        return sb.toString();
    }
}

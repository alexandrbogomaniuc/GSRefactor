package com.dgphoenix.casino.common.web;

/**
 * User: flsh
 * Date: 16.04.2009
 */
public interface IPageableForm {
    public final static int DEFAULT_ITEMS_PER_PAGE = 10;
    public final static int DEFAULT_PAGES_PER_PAGE = 5;

    int getPage();
    int getCount();
    int getItemsPerPage();
    int getPagesPerPage();
    int getOffset();
    int getLastPage();
}

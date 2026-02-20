package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import org.apache.struts.util.LabelValueBean;

import java.util.ArrayList;
import java.util.Map;

public class CurrencyLabelValueListBean extends ArrayList<LabelValueBean> {

    public CurrencyLabelValueListBean() {
        Map<String, Currency> mapCurrency = CurrencyCache.getInstance().getAllObjects();
        for (Currency currency : mapCurrency.values()) {
            this.add(new LabelValueBean(currency.getSymbol(), currency.getCode()));
        }

    }
}

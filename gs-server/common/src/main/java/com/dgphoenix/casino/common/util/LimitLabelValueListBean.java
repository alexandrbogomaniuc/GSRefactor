package com.abs.casino.common.util;

import com.abs.casino.common.cache.LimitsCache;
import com.abs.casino.common.cache.data.bank.Limit;
import org.apache.struts.util.LabelValueBean;

import java.util.ArrayList;
import java.util.List;

public class LimitLabelValueListBean extends ArrayList<LabelValueBean> {
    public LimitLabelValueListBean() {
        List<Limit> limits = LimitsCache.getInstance().getAll();
        for (Limit limit : limits) {
            String label = "minValue=" + limit.getMinValue() + ", maxValue=" + limit.getMaxValue();
            this.add(new LabelValueBean(label, Long.toString(limit.getId())));
        }
    }
}

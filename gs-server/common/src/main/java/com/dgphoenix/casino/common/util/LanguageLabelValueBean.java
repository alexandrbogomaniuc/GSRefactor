package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.cache.data.language.LanguageType;
import org.apache.struts.util.LabelValueBean;

import java.util.ArrayList;

public class LanguageLabelValueBean extends ArrayList<LabelValueBean> {

    public LanguageLabelValueBean() {
        for (LanguageType languageType : LanguageType.values()) {
            this.add(new LabelValueBean(languageType.name(), languageType.getCode()));
        }
    }
}
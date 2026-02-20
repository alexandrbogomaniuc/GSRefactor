package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.exception.PromoCreationException;

public interface IPromoLocalization {
    void setItem(String itemName, String itemValue) throws PromoCreationException;

    String getWelcomeMessage();

    void setWelcomeMessage(String welcomeMessage);

    String getRules();

    void setRules(String rules);

    String getTitle();

    void setTitle(String title);
}

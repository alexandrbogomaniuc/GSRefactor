package com.abs.casino.actions.api.bonus.response;

import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;

public class JSONResponse {
    private static final Logger LOG = LogManager.getLogger(JSONResponse.class);
    @SerializedName(CBonus.REQUEST_TAG)
    private Map<String, String> inputParams = new HashMap<>();
    @SerializedName(CBonus.TIME_TAG)
    private String time;
    @SerializedName(CBonus.RESPONSE_TAG)
    private Map<String, Object> outputParams = new HashMap<>();

    public JSONResponse(Map<String, String> inputParams, Map<String, Object> outParams, BonusForm form) {
        for (Map.Entry<String, String> entry : inputParams.entrySet()) {
            this.inputParams.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        try {
            for (Map.Entry<String, Object> entry : outParams.entrySet()) {
                if (entry.getKey().equals(CBonus.BONUS_LIST)) {
                    List<com.dgphoenix.casino.common.cache.data.bonus.BaseBonus> bonuses = (List<com.dgphoenix.casino.common.cache.data.bonus.BaseBonus>) entry.getValue();
                    bonuses.sort(Comparator.comparingLong(com.dgphoenix.casino.common.cache.data.bonus.BaseBonus::getTimeAwarded));
                    List<com.abs.casino.actions.api.bonus.response.BaseBonus> jsonBonuses = new ArrayList<>();
                    for (com.dgphoenix.casino.common.cache.data.bonus.BaseBonus bonus : bonuses) {
                        if (bonus instanceof com.dgphoenix.casino.common.cache.data.bonus.Bonus) {
                            jsonBonuses.add(new Bonus((com.dgphoenix.casino.common.cache.data.bonus.Bonus) bonus, form));
                        } else {
                            jsonBonuses.add(new FRBonus((com.dgphoenix.casino.common.cache.data.bonus.FRBonus) bonus, form));
                        }
                    }
                    outputParams.put(CBonus.BONUS_LIST, jsonBonuses);
                } else {
                    outputParams.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (CommonException e) {
            LOG.error(e.getMessage(), e);
            outputParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outputParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
            outputParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
        }
        LocalDateTime now = LocalDateTime.now();
        this.time = now.format(AbstractBonusAction.DATE_TIME_MS_FORMATTER);
    }
}

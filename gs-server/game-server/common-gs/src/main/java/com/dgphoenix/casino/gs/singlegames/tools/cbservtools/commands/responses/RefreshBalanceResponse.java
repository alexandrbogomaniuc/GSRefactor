package com.abs.casino.gs.singlegames.tools.cbservtools.commands.responses;

import com.abs.casino.common.util.DigitFormatter;
import com.abs.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class RefreshBalanceResponse extends ServerResponse {
    private double balance;
    private long balanceId;

    public RefreshBalanceResponse(double balance, long balanceId) {
        this.balance = balance;
        this.balanceId = balanceId;
    }

    @Override
    public String httpFormat() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put(IGameController.PARAMBALANCE, DigitFormatter.doubleToMoney(balance));
        params.put(IGameController.PARAM_BALANCE_ID, String.valueOf(balanceId));
        return PARAMS_JOINER.join(params);
    }
}

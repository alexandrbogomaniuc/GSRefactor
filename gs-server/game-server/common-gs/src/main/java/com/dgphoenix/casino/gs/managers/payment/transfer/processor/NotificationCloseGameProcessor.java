package com.dgphoenix.casino.gs.managers.payment.transfer.processor;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.dgphoenix.casino.gs.managers.ICloseGameProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by inter on 07.05.15.
 */
public class NotificationCloseGameProcessor implements ICloseGameProcessor {
    private static final Logger LOG = LogManager.getLogger(NotificationCloseGameProcessor.class);

    protected BankInfo bankInfo = null;

    @Override
    public void process(GameSession gameSession, AccountInfo accountInfo, ClientType clientType) throws CommonException {
        try {
            if (bankInfo == null) {
                bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            }
            String url = bankInfo.getNotificationCloseGameProcessorUrl();
            if (url != null) {
                Map<String, String> params = getParams(gameSession, accountInfo, clientType);
                LOG.info("Request to url:{} bankId:{} is:{}", url, accountInfo.getBankId(), logRequest(params));
                String sb = HttpClientConnection.newInstance(TIME_OUT).doRequest(url, params, bankInfo.isPOST(), bankInfo.isUseHttpProxy());
                LOG.info("Response from url:{} bankId:{} is:{}", url, accountInfo.getBankId(), sb);
            }
        } catch (Exception ex) {
            LOG.error("Can't get request ", ex);
        }
    }

    protected Map<String, String> getParams(GameSession gameSession, AccountInfo accountInfo, ClientType clientType) throws CommonException {
        Map<String, String> params = new HashMap<>();
        params.put("userId", accountInfo.getExternalId());
        long gameId = gameSession.getGameId();
        String externalGameId = BaseGameCache.getInstance().getExternalGameId(gameId, bankInfo.getId());
        if (externalGameId != null) {
            params.put("gameId", externalGameId);
        } else {
            params.put("gameId", String.valueOf(gameId));
        }
        params.put("gameSessionId", String.valueOf(gameSession.getId()));
        return params;
    }

    protected String buildHash(List<String> values) throws CommonException {
        try {
            StringBuilder sb = new StringBuilder();
            for (String value : values) {
                if (value != null) {
                    sb.append(value);
                }
            }
            sb.append(bankInfo.getAuthPassword());
            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new CommonException(this.getClass().getSimpleName() + ":: buildHash exception", e);
        }
    }

    public String logRequest(Map<String, String> params) {
        StringBuilder sb = new StringBuilder(" request parameters:");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }
}

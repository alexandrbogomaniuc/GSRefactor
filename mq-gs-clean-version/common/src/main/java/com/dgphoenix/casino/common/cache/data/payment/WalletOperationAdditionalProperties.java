package com.dgphoenix.casino.common.cache.data.payment;

import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * User: zhevlakoval
 * Date: 23.01.14
 * Time: 15:11
 */
public class WalletOperationAdditionalProperties {

    private static final String KEY_EXTERNAL_USER_ID = "EXTERNAL_USER_ID";
    private static final String KEY_GAME_TYPE = "GAME_TYPE";
    private static final String KEY_GAME_NAME = "GAME_NAME";
    private static final String KEY_CURRENCY = "CURRENCY";

    private static final String PROPERTIES_SEPARATOR = ";";
    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final Logger LOG = LogManager.getLogger(WalletOperationAdditionalProperties.class);
    private final static WalletOperationAdditionalProperties instance = new WalletOperationAdditionalProperties();

    private WalletOperationAdditionalProperties() {
    }

    public static WalletOperationAdditionalProperties getInstance() {
        return instance;
    }

    public void setExternalUserId(CommonWalletOperation commonWalletOperation, String propertyValue) {
        put(commonWalletOperation, KEY_EXTERNAL_USER_ID, propertyValue);
    }

    public String getExternalUserId(CommonWalletOperation commonWalletOperation) {
        return get(commonWalletOperation, KEY_EXTERNAL_USER_ID);
    }

    public void setGameType(CommonWalletOperation commonWalletOperation, String propertyValue) {
        put(commonWalletOperation, KEY_GAME_TYPE, propertyValue);
    }

    public String getGameType(CommonWalletOperation commonWalletOperation) {
        return get(commonWalletOperation, KEY_GAME_TYPE);
    }

    public void setGameName(CommonWalletOperation commonWalletOperation, String propertyValue) {
        put(commonWalletOperation, KEY_GAME_NAME, propertyValue);
    }

    public String getGameName(CommonWalletOperation commonWalletOperation) {
        return get(commonWalletOperation, KEY_GAME_NAME);
    }

    public void setCurrency(CommonWalletOperation commonWalletOperation, String propertyValue) {
        put(commonWalletOperation, KEY_CURRENCY, propertyValue);
    }

    public String getCurrency(CommonWalletOperation commonWalletOperation) {
        return get(commonWalletOperation, KEY_CURRENCY);
    }

    public void setAdditionalProperties(CommonWalletOperation commonWalletOperation, String currency, String gameName,
                                        String gameType, String extUserId) {
        String additionalProperties = commonWalletOperation.getAdditionalProperties();
        boolean empty = StringUtils.isTrimmedEmpty(additionalProperties);
        StringBuilder sb = empty ? new StringBuilder() : new StringBuilder(additionalProperties.trim());
        if (empty || !additionalProperties.contains(KEY_EXTERNAL_USER_ID)) {
            sb.append(empty ? "" : PROPERTIES_SEPARATOR).append(KEY_EXTERNAL_USER_ID).append(KEY_VALUE_SEPARATOR).
                    append(extUserId);
        }
        if (empty || !additionalProperties.contains(KEY_GAME_TYPE)) {
            sb.append(PROPERTIES_SEPARATOR).append(KEY_GAME_TYPE).append(KEY_VALUE_SEPARATOR).append(gameType);
        }
        if (empty || !additionalProperties.contains(KEY_GAME_NAME)) {
            sb.append(PROPERTIES_SEPARATOR).append(KEY_GAME_NAME).append(KEY_VALUE_SEPARATOR).append(gameName);
        }
        if (empty || !additionalProperties.contains(KEY_CURRENCY)) {
            sb.append(PROPERTIES_SEPARATOR).append(KEY_CURRENCY).append(KEY_VALUE_SEPARATOR).append(currency);
        }
        commonWalletOperation.setAdditionalProperties(sb.toString());
    }

    public void put(CommonWalletOperation commonWalletOperation, String propertyName, String propertyValue) {
        String additionalProperties = commonWalletOperation.getAdditionalProperties();
        if (additionalProperties == null) {
            additionalProperties = "";
        }
        String property = get(commonWalletOperation, propertyName);
        if (StringUtils.isTrimmedEmpty(property)) {
            additionalProperties += PROPERTIES_SEPARATOR;
            additionalProperties += propertyName;
            additionalProperties += KEY_VALUE_SEPARATOR;
            additionalProperties += propertyValue;
            commonWalletOperation.setAdditionalProperties(additionalProperties);
        }
    }

    public String get(CommonWalletOperation commonWalletOperation, String propertyName) {
        String res = "";
        try {
            String additionalProperties = commonWalletOperation.getAdditionalProperties();
            if (additionalProperties != null) {
                String[] properties = splitUsingTokenizer(additionalProperties, PROPERTIES_SEPARATOR);
                if (properties.length > 0) {
                    for (String property : properties) {
                        String[] keyValue = splitUsingTokenizer(property, KEY_VALUE_SEPARATOR);
                        if (keyValue.length == 2) {
                            String key = keyValue[0];
                            String value = keyValue[1];
                            if (key.equals(propertyName)) {
                                res = value;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return res;
    }

    public void setAdditionalProperties(AbstractWalletOperation operation, Map<String, String> properties) {
        String updatedProperty = CollectionUtils
                .modifyStringProperty(null, PROPERTIES_SEPARATOR, KEY_VALUE_SEPARATOR)
                .addAll(properties)
                .getString();

        operation.setAdditionalProperties(updatedProperty);
    }

    public void addAdditionalProperties(AbstractWalletOperation operation, Map<String, String> properties) {
        String additionalProperties = operation.getAdditionalProperties();

        String updatedProperty = CollectionUtils
                .modifyStringProperty(additionalProperties, PROPERTIES_SEPARATOR, KEY_VALUE_SEPARATOR)
                .addAll(properties)
                .getString();

        operation.setAdditionalProperties(updatedProperty);
    }

    public void addAdditionalProperty(AbstractWalletOperation operation, String key, String value) {
        String additionalProperties = operation.getAdditionalProperties();

        String updatedProperty = CollectionUtils
                .modifyStringProperty(additionalProperties, PROPERTIES_SEPARATOR, KEY_VALUE_SEPARATOR)
                .add(key, value)
                .getString();

        operation.setAdditionalProperties(updatedProperty);
    }

    private String[] splitUsingTokenizer(String Subject, String Delimiters) {
        StringTokenizer StrTkn = new StringTokenizer(Subject, Delimiters);
        ArrayList<String> ArrLis = new ArrayList<>(Subject.length());
        while (StrTkn.hasMoreTokens()) {
            ArrLis.add(StrTkn.nextToken());
        }
        return ArrLis.toArray(new String[ArrLis.size()]);
    }
}

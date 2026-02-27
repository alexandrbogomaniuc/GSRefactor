package com.abs.casino.payment.wallet.client.v4;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.rest.CustomRestTemplate;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.SimpleLoggableContainer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** CW standard integration using POST requests with JSON body */
public class StandardJsonCWClient extends RESTCWClient {
    private static final Logger LOG = LogManager.getLogger(StandardJsonCWClient.class);
    public static final String RESULT_TAG = "result".toUpperCase();

    /**
     * Key is field name, value is function that consumes string value and converts it to required type.
     *  Contains parsers only for not string fields
     */
    private static final Map<String, Function<String, Object>> FIELD_TYPE_PARSERS = new HashMap<>();

    private final CustomRestTemplate restTemplate;

    private final Gson gson;

    static {
        //keys should be in UPPER case for case-insensitive search
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_ROUNDID.toUpperCase(), Long::parseLong);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_GAMEID.toUpperCase(), Long::parseLong);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_NEGATIVE_BET.toUpperCase(), Long::parseLong);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_BANKID.toUpperCase(), Long::parseLong);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_REAL_BET.toUpperCase(), Double::parseDouble);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_REAL_WIN.toUpperCase(), Double::parseDouble);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_SW_BET.toUpperCase(), Double::parseDouble);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_SW_COMPENSATED_WIN.toUpperCase(), Long::parseLong);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_ROUND_FINISHED.toUpperCase(), Boolean::valueOf);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_ROUND_STARTED.toUpperCase(), Boolean::valueOf);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PARAM_BALANCE.toUpperCase(), Double::parseDouble);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PROMO_WIN_AMOUNT.toUpperCase(), Double::parseDouble);
        FIELD_TYPE_PARSERS.put(CCommonWallet.PROMO_ID.toUpperCase(), Long::parseLong);
        FIELD_TYPE_PARSERS.put("JPCONTRIBUTION", Double::parseDouble);
        FIELD_TYPE_PARSERS.put("JPWIN", Long::parseLong);
        FIELD_TYPE_PARSERS.put("UNJCONTRIBUTION", Long::parseLong);
        FIELD_TYPE_PARSERS.put("UNJWIN", Long::parseLong);
    }

    public StandardJsonCWClient(long bankId) {
        super(bankId);
        setLoggableContainer(new SimpleLoggableContainer());
        gson = new Gson();
        restTemplate = new CustomRestTemplate();
        restTemplate.setGsonSerializer(gson);
        restTemplate.setLoggableClient(this);
        restTemplate.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    protected XmlRequestResult doRequest(Map<String, String> htbl, String url, long bankId, long timeout) throws CommonException {
        Map<String,Object> capitalizedHtbl = new HashMap<>();
        htbl.forEach((k, v) -> {
            String capitalizedKey = k.toUpperCase();
            capitalizedHtbl.put(capitalizedKey, parseValue(capitalizedKey, v));
        });
        try {
            JsonObject response = restTemplate.sendRequest(url, capitalizedHtbl, JsonObject.class);
            return mapToXmlRequestResult(response);
        } catch (Exception e) {
            LOG.error("StandardJsonCWClient::request error, bankId = {}, url = {}", bankId, url, e);
            throw new CommonException(e);
        }
    }

    /**
     * Convert given field string value to required by documentation type.
     * Type parser selected from the {@link #FIELD_TYPE_PARSERS}
     * @param field field name in UPPER case.
     * @param value value string representation
     * @return parsed value
     */
    private Object parseValue(String field, String value) {
        return FIELD_TYPE_PARSERS.getOrDefault(field, s -> s).apply(value);
    }

    private XmlRequestResult mapToXmlRequestResult(JsonObject json) {
        XmlRequestResult xml = new XmlRequestResult();
        JsonObject extsystem = json.get("EXTSYSTEM").getAsJsonObject();
        JsonElement request = extsystem.get("REQUEST");
        JsonElement response = extsystem.get("RESPONSE");
        gson.fromJson(request, Map.class).forEach((key, value) -> xml.putRequestParameter(String.valueOf(key), String.valueOf(value)));
        gson.fromJson(response, Map.class).forEach((key, value) -> xml.putResponseParameter(String.valueOf(key), String.valueOf(value)));
        String status = String.valueOf(xml.getResponseParameters().get(RESULT_TAG));
        xml.setStatus(status);
        return xml;
    }
}

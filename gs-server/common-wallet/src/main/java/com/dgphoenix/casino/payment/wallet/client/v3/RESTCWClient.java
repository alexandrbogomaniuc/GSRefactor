package com.abs.casino.payment.wallet.client.v3;

import com.dgphoenix.casino.cassandra.persist.ExtendedAccountInfoPersister;
import com.dgphoenix.casino.cassandra.persist.ExtendedAccountInfoPersisterInstanceHolder;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.account.Gender;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.ICommonWalletClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

public class RESTCWClient extends com.abs.casino.payment.wallet.client.v2.RESTCWClient
        implements ICommonWalletClient {
    private static final Logger LOG = LogManager.getLogger(RESTCWClient.class);

    public static final String PARAM_HASH = "hash";
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_GAME_ID = "gameId";
    public static final String PARAM_SERVER_ID = "serverId";
    public static final String DATE_OF_BIRTH_FORMAT = "dd/MM/yyyy";
    private static final String COUNTRYCODE_TAG = "COUNTRYCODE";
    private static final ExtendedAccountInfoPersister persister = ExtendedAccountInfoPersisterInstanceHolder.getPersister();

    public RESTCWClient(long bankId) {
        super(bankId);
    }

    protected Map<String, String> prepareAuthParams(String token, String gameId, String serverId,
                                                    ClientType clType)
            throws CommonException {
        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_TOKEN, token);

        if (gameId != null) {
            params.put(PARAM_GAME_ID, gameId);
        }

        if (serverId != null) {
            params.put(PARAM_SERVER_ID, serverId);
        }
        List<String> paramList = new ArrayList<>();
        paramList.add(token);
        if (isSendClientType()) {
            String clientType = clType == null ? "unknown" : clType.name();
            params.put("clientType", clientType);
            if (addClientTypeToHashOnAuth) { //mostly for Vera&John
                paramList.add(clientType);
            }
        }
        prepareAuthParamsAndHash(gameId, params, paramList);
        params.put(PARAM_HASH, getHashValue(paramList));
        return params;
    }

    protected void prepareAuthParamsAndHash(String gameId, Map<String, String> params, List<String> hashParams) {
        if (gameId != null && sendGameIdOnAuth) {
            params.put(PARAM_GAME_ID, gameId);
            if (addGameIdToHashOnAuth) {
                hashParams.add(gameId);
            }
        }
    }

    @Override
    public CommonWalletAuthResult auth(String token, String gameId, String serverId, ClientType clType, Map<String, String> additionalParams) throws CommonException {
        return auth(token, gameId, serverId, clType);
    }

    protected String extractUserIdForStubMode(String token) {
        return token;
    }

    @Override
    public CommonWalletAuthResult auth(String token, String gameId, String serverId, ClientType clType)
            throws CommonException {

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
        Map<String, String> htbl = prepareAuthParams(token, gameId, serverId, clType);

        IXmlRequestResult output = request(htbl, bankInfo.getCWAuthUrl(), getBankId());

        if (!output.isSuccessful()) {
            LOG.error("auth: IXmlRequestResult output is not successfully: {} {}", output.getResponseCode(), output.getResponseParameters());
            throw new CommonException("RESTCWClient:auth response was not successful");
        }
        String userId;
        double balance;
        String userName;
        String firstName;
        String lastName;
        String email;
        String currency;
        String countryCode;

        if (isStubMode()) {
            userId = extractUserIdForStubMode(token);
            balance = Double.parseDouble((String) output.getResponseParameters().get(CCommonWallet.BALANCE_TAG));
            userName = token;
            firstName = token;
            lastName = token;
            email = (String) output.getResponseParameters().get(CCommonWallet.EMAIL_TAG);
            currency = (String) output.getResponseParameters().get(CCommonWallet.CURRENCY_TAG);
            countryCode = "US";
        } else {
            userId = (String) output.getResponseParameters().get(CCommonWallet.USERID_TAG);
            balance = Double.parseDouble((String) output.getResponseParameters().get(CCommonWallet.BALANCE_TAG));
            userName = (String) output.getResponseParameters().get(CCommonWallet.USERNAME_TAG);
            firstName = (String) output.getResponseParameters().get(CCommonWallet.FIRSTNAME_TAG);
            lastName = (String) output.getResponseParameters().get(CCommonWallet.LASTNAME_TAG);
            email = (String) output.getResponseParameters().get(CCommonWallet.EMAIL_TAG);
            currency = (String) output.getResponseParameters().get(CCommonWallet.CURRENCY_TAG);
            countryCode = (String) output.getResponseParameters().get(CCommonWallet.COUNTRYCODE_TAG);
        }
        String dateOfBirth = (String) output.getResponseParameters().get(AccountInfo.BIRTH_DATE);
        String gender = (String) output.getResponseParameters().get(AccountInfo.GENDER);
        if (bankInfo.isDemographicInfoMandatory() && !isDemographicParametersValid(countryCode, dateOfBirth, gender)) {
            LOG.error("auth: Demographic parameters are not valid: Country: " + countryCode + ", dateOfBirth: " + dateOfBirth + ", gender: " + gender);
            throw new CommonException("RESTCWClient: One of mandatory request parameters is null for bank with mandatory demographic info option");
        }
        if (countryCode != null || dateOfBirth != null || gender != null) {
            try {
                persistDemographicInfo(bankInfo, userId, countryCode, dateOfBirth, gender);
            } catch (Exception e) {
                LOG.error("auth: Cannot save extended properties: userId=" + userId +
                        ", countryCode=" + countryCode + ", dateOfBirth=" + dateOfBirth + ", gender=" + gender, e);
                if (bankInfo.isDemographicInfoMandatory()) {
                    throw new CommonException(e);
                }
            }
        }

        CommonWalletAuthResult authResult = new CommonWalletAuthResult(userId, balance, userName, firstName, lastName,
                email, currency, output.isSuccessful(), countryCode);
        authResult = additionalAuthProcess(output, authResult);

        return authResult;
    }

    protected CommonWalletAuthResult additionalAuthProcess(IXmlRequestResult output, CommonWalletAuthResult authResult) {
        return authResult;
    }

    private boolean isDemographicParametersValid(String countryCode, String dateOfBirth, String gender) {
        return countryCode != null && dateOfBirth != null && gender != null;
    }

    private void persistDemographicInfo(BankInfo bankInfo, String userId, String countryCode, String dateOfBirth, String gender) throws CommonException {
        Map<String, String> properties = new HashMap<>(3);
        boolean isMandatory = bankInfo.isDemographicInfoMandatory();

        addCountryProperty(countryCode, properties, isMandatory);
        addBirthDateProperty(dateOfBirth, properties, isMandatory);
        addGenderProperty(gender, properties, isMandatory);

        persister.persist(bankInfo.getId(), userId, properties);
    }

    private void addCountryProperty(String countryCode, Map<String, String> properties, boolean isMandatory) throws CommonException {
        if (!StringUtils.isTrimmedEmpty(countryCode) & countryCode.length() == 2) {
            properties.put(COUNTRYCODE_TAG, countryCode);
        } else {
            if (isMandatory) {
                throw new CommonException("RESTCWClient: Invalid countryCode format");
            }
        }
    }

    private void addBirthDateProperty(String dateOfBirth, Map<String, String> properties, boolean isMandatory) throws CommonException {
        SimpleDateFormat df = new SimpleDateFormat(DATE_OF_BIRTH_FORMAT);
        try {
            final Date date = df.parse(dateOfBirth);
            properties.put(AccountInfo.BIRTH_DATE, String.valueOf(date.getTime()));
        } catch (Exception e) {
            if (isMandatory) {
                throw new CommonException("RESTCWClient: Invalid dateOfBirth format", e);
            }
        }
    }

    private void addGenderProperty(String gender, Map<String, String> properties, boolean isMandatory) throws CommonException {
        try {
            final Gender parsedGender = Gender.valueOf(gender.toUpperCase());
            properties.put(AccountInfo.GENDER, parsedGender.name());
        } catch (Exception e) {
            if (isMandatory) {
                throw new CommonException("RESTCWClient: Invalid gender format", e);
            }
        }
    }

    @Override
    public CommonWalletAuthResult auth(String token, ClientType clType) throws CommonException {
        return auth(token, null, null, clType);
    }

    @Override
    public CommonWalletAuthResult auth(String token, String gameId, ClientType clType) throws CommonException {
        return auth(token, gameId, null, clType);
    }

    @Override
    protected Map<String, String> prepareWagerParams(CommonWallet wallet, Map<String, String> params, long accountId,
                                                     String extUserId, String bet,
                                                     String win, Boolean isRoundFinished,
                                                     long gsRoundId, long mpRoundId, String gameId, long bankId, long gameSessionId,
                                                     long negativeBet, ClientType clType, String currencyCode,
                                                     String cmd)
            throws CommonException {

        Map<String, String> result = super.prepareWagerParams(wallet, params, accountId, extUserId, bet, win,
                isRoundFinished, gsRoundId, mpRoundId, String.valueOf(gameId), bankId, gameSessionId, negativeBet, clType,
                currencyCode, cmd);

        List<String> paramList = new ArrayList<>(7);
        paramList.add(extUserId);
        if (!bet.isEmpty()) {
            paramList.add(bet);
        }
        if (!win.isEmpty()) {
            paramList.add(win);
        }

        if (isRoundFinished != null) {
            paramList.add(isRoundFinished ? CCommonWallet.VALUE_TRUE : CCommonWallet.VALUE_FALSE);
        }
        paramList.add(String.valueOf(gsRoundId));
        paramList.add(String.valueOf(gameId));
        if (isSendClientType()) {
            String clientType = params.get(CLIENT_TYPE_PARAM);
            if (clientType == null || clientType.isEmpty()) {
                clientType = clType == null ? ClientType.FLASH.name() : clType.name();
            }
            result.put(CLIENT_TYPE_PARAM, clientType);
            if (addClientTypeToHashOnWager) { //mostly for Vera&John
                paramList.add(clientType);
            }
        } else {
            params.remove(CLIENT_TYPE_PARAM);
        }

        result.put(PARAM_HASH, getHashValue(paramList));
        return result;
    }

    @Override
    protected Map<String, String> prepareStatusParams(String extUserId, long transactionId) throws CommonException {
        Map<String, String> params = super.prepareStatusParams(extUserId, transactionId);

        List<String> paramList = new ArrayList<>(2);
        paramList.add(extUserId);
        paramList.add(String.valueOf(transactionId));

        params.put(PARAM_HASH, getHashValue(paramList));
        return params;
    }

    @Override
    protected Map<String, String> prepareGetBalanceParams(String extUserId) throws CommonException {
        Map<String, String> params = super.prepareGetBalanceParams(extUserId);

        List<String> paramList = new ArrayList<>(1);
        paramList.add(extUserId);

        params.put(PARAM_HASH, getHashValue(paramList));
        return params;
    }

    @Override
    protected Map<String, String> prepareCancelParams(String extUserId, long transactionId) throws CommonException {
        Map<String, String> params = super.prepareCancelParams(extUserId, transactionId);

        List<String> paramList = new ArrayList<>(2);
        paramList.add(extUserId);
        paramList.add(String.valueOf(transactionId));

        params.put(PARAM_HASH, getHashValue(paramList));

        return params;
    }

    protected String getHashValue(List params) throws CommonException {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                sb.append(param);
            }
            sb.append(getAuthPass());
            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    @Override
    protected boolean isPost() {
        return false;
    }

    protected String getAuthPass() {
        return bankInfo.getAuthPassword();
    }

}

package com.abs.casino.payment.wallet.client.v4;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.payment.WalletOperationStatus;
import com.abs.casino.common.cache.data.payment.WalletOperationType;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.client.canex.request.privateroom.Status;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletStatusResult;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletWagerResult;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.gs.managers.payment.wallet.*;
import com.abs.casino.gs.managers.payment.wallet.v4.ICommonWalletClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abs.casino.gs.managers.payment.wallet.CCommonWallet.*;

/**
 * User: flsh
 * Date: 9/27/12
 */
public class RESTCWClient extends com.abs.casino.payment.wallet.client.v3.RESTCWClient
        implements ICommonWalletClient {
    private static final long MAX_UNKNOWN_TRANSACTION_TIME = 5 * 60 * 1000;
    private static final Logger LOG = LogManager.getLogger(RESTCWClient.class);

    private Long debitTimeout;
    private Long creditInGameTimeout;

    public RESTCWClient(long bankId) {
        super(bankId);
        debitTimeout = bankInfo.getDebitTimeout();
        creditInGameTimeout = bankInfo.getCreditInGameTimeout();
    }

    protected String getRefundBetUrl() {
        return BankInfoCache.getInstance().getBankInfo(getBankId()).getRefundBetUrl();
    }

    @Override
    public boolean refundBet(long operationStartTime, long accountId, String extUserId,
                             CommonWalletOperation debitOperation, long gameId)
            throws CommonException {

        Map<String, String> htbl = prepareRefundBetParameters(debitOperation, accountId, extUserId, gameId);

        IXmlRequestResult output = request(htbl, getRefundBetUrl(), getBankId());
        if (!output.isSuccessful()) {
            String code = (String) output.getResponseParameters().get(CODE_ATTRIBUTE);
            if (StringUtils.isTrimmedEmpty(code)) {
                throw new CommonException("RESTCWClient:wager response was not successful");
            }
            if (code.equals(String.valueOf(CommonWalletErrors.UNKNOWN_TRANSACTION_ID.getCode()))) {
                if (System.currentTimeMillis() - operationStartTime > MAX_UNKNOWN_TRANSACTION_TIME) {
                    LOG.warn("refundBet: accountId=" + accountId + ", extUserId=" + extUserId +
                            ", casinoTransactionId=" + debitOperation.getId() + ", received error code: " + code +
                            ", and MAX_UNKNOWN_TRANSACTION_TIME is reached. refund success");
                } else {
                    throw new WalletException("RESTCWClient:wager response was not successful", code);
                }
            } else {
                throw new WalletException("RESTCWClient:wager response was not successful", code);
            }
        }

        return true;
    }

    protected Map<String, String> prepareRefundBetParameters(CommonWalletOperation debitOperation, long accountId,
                                                             String extUserId, long gameId)
            throws CommonException {
        long operationId = debitOperation.getId();
        String casinoTransactionId = String.valueOf(operationId);
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(PARAM_USERID, extUserId);
        htbl.put(PARAM_CASINOTRANSACTIONID, casinoTransactionId);
        if (bankInfo.isSendDetailsOnRefund()) {
            htbl.put(PARAM_ROUNDID, String.valueOf(debitOperation.getRoundId()));
            htbl.put(AMOUNT_TAG.toLowerCase(), String.valueOf(debitOperation.getAmount()));
            htbl.put(PARAM_GAMEID, String.valueOf(gameId));
        }
        List<String> paramList = new ArrayList<>();
        paramList.add(extUserId);
        paramList.add(casinoTransactionId);
        checkAndSetToken(htbl, accountId);
        htbl.put(PARAM_HASH, getHashValue(paramList));
        if (getBankInfo().isAddTokenMode() && getBankInfo().isSaveAndSendTokenInGameWallet()) {
            String additionalProperties = debitOperation.getAdditionalProperties();
            Map<String, String> params = StringUtils.isTrimmedEmpty(additionalProperties) ?
                    new HashMap<>() : CollectionUtils.stringToMap(additionalProperties);
            String token = params.get(PARAM_TOKEN);
            if (token != null) {
                htbl.put(PARAM_TOKEN, token);
            } else {
                LOG.warn("refundBet: isAddTokenMode & isSaveAndSendTokenInGameWallet - enabled both," +
                        " but operation has not token in additionalProperties");
            }
        }
        return htbl;
    }

    @Override
    public boolean updatePlayerStatusInPrivateRoom(String privateRoomId, String nickname, String externalId,
                                                   Status status, int bankId) throws CommonException {

        Map<String, String> result =
                prepareUpdatePlayerStatusInPrivateRoomParameters(privateRoomId, nickname, externalId, status);

        String url = getUpdatePlayerStatusInPrivateRoomUrl();
        IXmlRequestResult output = request(result, url, bankId);

        if (!output.isSuccessful()) {
            throw new CommonException("RESTCWClient:updatePlayerStatusInPrivateRoom response was not successful");
        }

        return true;
    }

    protected Map<String, String> prepareUpdatePlayerStatusInPrivateRoomParameters(String privateRoomId, String nickname,
                                                                                   String externalId, Status status) throws CommonException {

        if(StringUtils.isTrimmedEmpty(privateRoomId)) {
            throw new CommonException("RESTCWClient:prepareUpdatePlayerStatusInPrivateRoomParameters privateRoomId is empty");
        }

        if(StringUtils.isTrimmedEmpty(nickname)) {
            throw new CommonException("RESTCWClient:prepareUpdatePlayerStatusInPrivateRoomParameters nickname is empty");
        }

        if(status == null) {
            throw new CommonException("RESTCWClient:prepareUpdatePlayerStatusInPrivateRoomParameters status is null");
        }

        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(PARAM_PRIVATE_ROOM_ID, privateRoomId);
        htbl.put(PARAM_NICKNAME, nickname);
        htbl.put(PARAM_EXTERNAL_ID, externalId);
        htbl.put(PARAM_STATUS, status.toString());

        return htbl;
    }

    protected String getUpdatePlayerStatusInPrivateRoomUrl() {
        return BankInfoCache.getInstance().getBankInfo(getBankId()).getUpdatePlayerStatusInPrivateRoomUrl();
    }

    protected String getUpdatePlayersRoomsNumberUrl() {
        return BankInfoCache.getInstance().getBankInfo(getBankId()).getUpdatePlayersRoomsNumberUrl();
    }

    protected String getInvitePlayersToPrivateRoomUrl() {
        return BankInfoCache.getInstance().getBankInfo(getBankId()).getInvitePlayersToPrivateRoomUrl();
    }

    protected String getFriendsUrl() {
        return BankInfoCache.getInstance().getBankInfo(getBankId()).getFriendsUrl();
    }

    protected String getOnlineStatusUrl() {
        return BankInfoCache.getInstance().getBankInfo(getBankId()).getPlayersOnlineStatusUrl();
    }

    @Override
    public CommonWalletWagerResult wager(long accountId, String extUserId, String bet, //bet_amount|transactionID
                                         String win, //win_amount|transactionID
                                         Boolean isRoundFinished, long gsRoundId, long mpRoundId, long gameId, long bankId,
                                         CommonWalletOperation operation, CommonWallet wallet, ClientType clientType,
                                         Currency curr)
            throws CommonException {
        long gameSessionId = operation.getGameSessionId();
        String additionalProperties = operation.getAdditionalProperties();
        Map<String, String> params = StringUtils.isTrimmedEmpty(additionalProperties) ?
                new HashMap<>() : CollectionUtils.stringToMap(additionalProperties);
        checkAndSetToken(params, accountId);
        Map<String, String> result = prepareWagerParams(wallet, params, accountId, extUserId, bet, win,
                isRoundFinished, gsRoundId, mpRoundId, getExternalGameId(gameId, bankId), bankId, gameSessionId,
                operation.getNegativeBet(), clientType, curr.getCode(), operation.getCmd());

        long timeout = 0;
        if (operation.getType() == WalletOperationType.DEBIT && debitTimeout != null) {
            timeout = debitTimeout;
        } else if (operation.getType() == WalletOperationType.CREDIT
                && creditInGameTimeout != null
                && operation.getExternalStatus() == WalletOperationStatus.STARTED) {
            timeout = creditInGameTimeout;
        }

        IXmlRequestResult output = request(result, getWagerUrl(bankId), bankId, timeout);

        if (!output.isSuccessful()) {
            String code = output.getResponseCode();
            if (isPreciseWagerError(code)) {
                return new CommonWalletWagerResult(code, (String) output.getResponseParameters().get(CCommonWallet.MESSAGE_TAG));
            }
            throw new WalletException("RESTCWClient:wager response was not successful", code);
        }

        String extSystemTransactionId = (String) output.getResponseParameters().get(CCommonWallet.CWTRANSACTIONID_TAG);
        double balance = Double.parseDouble((String) output.getResponseParameters().get(CCommonWallet.BALANCE_TAG));

        //always try loading bonusBet, because them may be returned on bet and win (if send negativeBet)
        String sBonusBet = (String) output.getResponseParameters().get(CCommonWallet.BONUS_BET_AMOUNT_TAG);
        String sBonusWin = StringUtils.isTrimmedEmpty(win) ? null :
                (String) output.getResponseParameters().get(CCommonWallet.BONUS_WIN_AMOUNT_TAG);
        Double bonusBet = StringUtils.isTrimmedEmpty(sBonusBet) ? null : Double.parseDouble(sBonusBet);
        Double bonusWin = StringUtils.isTrimmedEmpty(sBonusWin) ? null : Double.parseDouble(sBonusWin);
        return new CommonWalletWagerResult(extSystemTransactionId, balance, output.isSuccessful(), bonusBet, bonusWin);
    }


    @Override
    protected Map<String, String> prepareWagerParams(CommonWallet wallet, Map<String, String> params, long accountId,
                                                     String extUserId,
                                                     String bet, String win, Boolean isRoundFinished, long gsRoundId, long mpRoundId,
                                                     String gameId, long bankId, long gameSessionId, long negativeBet,
                                                     ClientType clientType, String currencyCode, String cmd)
            throws CommonException {
        Map<String, String> htbl = super.prepareWagerParams(wallet, params, accountId, extUserId, bet, win,
                isRoundFinished, gsRoundId, mpRoundId, String.valueOf(gameId), bankId, gameSessionId, 0, clientType, currencyCode,
                cmd);
        checkAndSetToken(htbl, accountId);
        if (negativeBet > 0) {
            htbl.put(CCommonWallet.PARAM_NEGATIVE_BET, String.valueOf(negativeBet));
        }
        return htbl;
    }

    protected AccountInfo getAccountInfo(String extUserId)
            throws CommonException {
        long subCasinoId = BankInfoCache.getInstance().getSubCasinoId(getBankId());
        return walletHelper.getAccountInfo((short) subCasinoId, (int) getBankId(), extUserId);
    }

    protected BankInfo getBankInfo() {
        return BankInfoCache.getInstance().getBankInfo(getBankId());
    }

    protected void checkAndSetToken(Map<String, String> htbl, String extUserId)
            throws CommonException {
        if (getBankInfo().isAddTokenMode()) {
            AccountInfo accountInfo = getAccountInfo(extUserId);
            if (accountInfo != null && htbl.get(PARAM_TOKEN) == null) {
                htbl.put(PARAM_TOKEN, accountInfo.getFinsoftSessionId());
            }
        }
    }

    protected void checkAndSetToken(Map<String, String> htbl, long accountId)
            throws CommonException {
        if (getBankInfo().isAddTokenMode()) {
            AccountInfo accountInfo = walletHelper.getAccountInfo(accountId);
            if (accountInfo != null && htbl.get(PARAM_TOKEN) == null) {
                htbl.put(PARAM_TOKEN, accountInfo.getFinsoftSessionId());
            }
        }
    }


    @Override
    protected Map<String, String> prepareStatusParams(String extUserId, long transactionId)
            throws CommonException {
        Map<String, String> htbl = super.prepareStatusParams(extUserId, transactionId);
        checkAndSetToken(htbl, extUserId);
        return htbl;
    }

    @Override
    protected Map<String, String> prepareGetBalanceParams(String extUserId) throws CommonException {
        Map<String, String> htbl = super.prepareGetBalanceParams(extUserId);
        checkAndSetToken(htbl, extUserId);
        return htbl;
    }

    @Override
    protected Map<String, String> prepareCancelParams(String extUserId, long transactionId)
            throws CommonException {
        Map<String, String> htbl = super.prepareCancelParams(extUserId, transactionId);
        checkAndSetToken(htbl, extUserId);
        return htbl;
    }

}

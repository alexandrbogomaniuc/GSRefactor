package com.abs.casino.controller.stub.cw;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.client.canex.request.CanexRequest;
import com.abs.casino.common.client.canex.request.RequestType;
import com.abs.casino.common.client.canex.response.CanexJsonResponse;
import com.abs.casino.common.client.canex.response.CanexResponse;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.NotEnoughMoneyException;
import com.abs.casino.controller.RequestContext;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@RestController
@RequestMapping("config/stub/canex")
public class CanexStubController {

    private static final String TOKEN = "token";
    private static final String HASH = "hash";
    private static final String CLIENT_TYPE = "clientType";
    private static final String DEBIT_TYPE = "debitType";

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:ss:mm");
    private final SubCasinoCache subCasinoCache;
    private final AccountManager accountManager;
    private final RemoteClientStubHelper remoteClientStubHelper;
    private final RemoteCallHelper remoteCallHelper;
    private final RequestContext requestContext;
    private final BankInfoCache bankInfoCache;

    public CanexStubController(SubCasinoCache subCasinoCache, AccountManager accountManager,
                               RemoteClientStubHelper remoteClientStubHelper, RemoteCallHelper remoteCallHelper,
                               RequestContext requestContext, BankInfoCache bankInfoCache) {
        this.subCasinoCache = subCasinoCache;
        this.accountManager = accountManager;
        this.remoteClientStubHelper = remoteClientStubHelper;
        this.remoteCallHelper = remoteCallHelper;
        this.requestContext = requestContext;
        this.bankInfoCache = bankInfoCache;
    }

    @PostMapping("/auth")
    public ResponseEntity<CanexJsonResponse> auth(@RequestBody Map<String, String> body) throws CommonException {
        CanexRequest request = createAuthRequest(body);
        String userId = request.getToken();
        String currency = getCurrency(request.getBankId(), userId);
        RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo = remoteClientStubHelper.getExtAccountInfo(userId);
        CanexResponse response = createAuthResponse(userId, currency, extAccountInfo.getBalance());
        CanexJsonResponse jsonResponse = new CanexJsonResponse(request, response, formatter.format(new Date()));
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    @PostMapping("/bad/auth")
    public ResponseEntity<CanexJsonResponse> badAuth(@RequestBody Map<String, String> body) {
        CanexRequest request = createAuthRequest(body);
        CanexResponse response = createErrorResponse(399);
        CanexJsonResponse jsonResponse = new CanexJsonResponse(request, response, formatter.format(new Date()));
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    @PostMapping("/balance")
    public ResponseEntity<CanexJsonResponse> balance(@RequestBody Map<String, String> body) {
        CanexRequest request = createBalanceRequest(body);
        RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo = remoteClientStubHelper.getExtAccountInfo(request.getUserId());
        CanexResponse response = createBalanceResponse(extAccountInfo.getBalance());
        CanexJsonResponse jsonResponse = new CanexJsonResponse(request, response, formatter.format(new Date()));
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/wager")
    public ResponseEntity<CanexJsonResponse> wager(@RequestBody Map<String, String> body) throws NotEnoughMoneyException {
        CanexRequest request = createWagerRequest(body);
        String userId = request.getUserId();
        if (isNotEmpty(request.getBet())) {
            String betStr = request.getBet().split("\\|")[0];
            long bet = Long.parseLong(betStr);
            remoteClientStubHelper.makeBet(userId, bet);
        } else {
            String winStr = request.getWin().split("\\|")[0];
            long win = Long.parseLong(winStr);
            remoteClientStubHelper.makeWin(userId, win);
        }
        RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo = remoteClientStubHelper.getExtAccountInfo(userId);
        long resultBalance = extAccountInfo.getBalance();
        remoteCallHelper.updateStubBalance(userId, resultBalance);

        CanexResponse response = createWagerResponse(userId, request.getRoundId(), extAccountInfo.getBalance());
        CanexJsonResponse jsonResponse = new CanexJsonResponse(request, response, formatter.format(new Date()));
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    @PostMapping("/refund")
    public ResponseEntity<CanexJsonResponse> refund(@RequestBody Map<String, String> body) {
        CanexRequest request = createRefundRequest(body);
        CanexResponse response = createRefundResponse(request.getUserId(), request.getCasinoTransactionId());
        CanexJsonResponse jsonResponse = new CanexJsonResponse(request, response, formatter.format(new Date()));
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    private CanexRequest createAuthRequest(Map<String, String> body) {
        Map<String, String> params = new HashMap<>();
        params.put(HASH, body.get(HASH.toUpperCase()));
        params.put(TOKEN, body.get(TOKEN.toUpperCase()));
        params.put(PARAM_BANKID, body.get(PARAM_BANKID.toUpperCase()));
        params.put(PARAM_GAMEID, body.get(PARAM_GAMEID.toUpperCase()));
        return new CanexRequest(RequestType.AUTH, params);
    }

    private CanexResponse createAuthResponse(String userId, String currencyCode, long balance) {
        CanexResponse response = new CanexResponse();
        response.setResult("OK");
        response.setUserId(userId);
        response.setUserName(userId);
        response.setFirstname(userId + " Fname");
        response.setLastname(userId + "Lname");
        response.setEmail(userId + "@test.st");
        response.setCurrency(currencyCode);
        response.setBalance(balance);
        return response;
    }

    private CanexResponse createErrorResponse(int code) {
        CanexResponse response = new CanexResponse();
        response.setResult("ERROR");
        response.setCode(code);
        return response;
    }

    private CanexRequest createBalanceRequest(Map<String, String> body) {
        Map<String, String> params = new HashMap<>();
        params.put(HASH, body.get(HASH.toUpperCase()));
        params.put(PARAM_USERID, body.get(PARAM_USERID.toUpperCase()));
        params.put(PARAM_BANKID, body.get(PARAM_BANKID.toUpperCase()));
        return new CanexRequest(RequestType.BALANCE, params);
    }

    private CanexResponse createBalanceResponse(long balance) {
        CanexResponse response = new CanexResponse();
        response.setResult("OK");
        response.setBalance(balance);
        return response;
    }

    private CanexRequest createRefundRequest(Map<String, String> body) {
        Map<String, String> params = new HashMap<>();
        params.put(HASH, body.get(HASH.toUpperCase()));
        params.put(PARAM_USERID, body.get(PARAM_USERID.toUpperCase()));
        params.put(PARAM_BANKID, body.get(PARAM_BANKID.toUpperCase()));
        params.put(PARAM_CASINOTRANSACTIONID, body.get(PARAM_CASINOTRANSACTIONID.toUpperCase()));
        return new CanexRequest(RequestType.REFUND, params);
    }

    private CanexResponse createRefundResponse(String userId, String transactionId) {
        CanexResponse response = new CanexResponse();
        response.setResult("OK");
        response.setExtSystemTransactionId(userId + "_" + transactionId);
        return response;
    }

    private CanexRequest createWagerRequest(Map<String, String> body) {
        Map<String, String> params = new HashMap<>();
        params.put(HASH, body.get(HASH.toUpperCase()));
        params.put(PARAM_USERID, body.get(PARAM_USERID.toUpperCase()));
        params.put(PARAM_BANKID, body.get(PARAM_BANKID.toUpperCase()));
        params.put(PARAM_GAMEID, body.get(PARAM_GAMEID.toUpperCase()));
        params.put(PARAM_GAMESESSIONID, body.get(PARAM_GAMESESSIONID.toUpperCase()));
        params.put(PARAM_ROUNDID, body.get(PARAM_ROUNDID.toUpperCase()));
        params.put(PARAM_ROUND_FINISHED, body.get(PARAM_ROUND_FINISHED.toUpperCase()));
        params.put(CLIENT_TYPE, body.get(CLIENT_TYPE.toUpperCase()));
        params.put(PARAM_BET, body.get(PARAM_BET.toUpperCase()));
        params.put(PARAM_REAL_BET, body.get(PARAM_REAL_BET.toUpperCase()));
        params.put(PARAM_WIN, body.get(PARAM_WIN.toUpperCase()));
        params.put(PARAM_REAL_WIN, body.get(PARAM_REAL_WIN.toUpperCase()));
        params.put(DEBIT_TYPE, body.get(DEBIT_TYPE.toUpperCase()));
        return new CanexRequest(RequestType.WAGER, params);
    }

    private CanexResponse createWagerResponse(String userId, Long roundId, long balance) {
        CanexResponse response = new CanexResponse();
        response.setResult("OK");
        response.setExtSystemTransactionId(userId + "_" + roundId + "_" + balance);
        response.setBalance(balance);
        return response;
    }

    private String getCurrency(long bankId, String userId) throws CommonException {
        long subCasinoId = subCasinoCache.getSubCasinoId(bankId);
        AccountInfo accountInfo = accountManager.getAccountInfo(subCasinoId, bankId, userId);
        return accountInfo != null ? accountInfo.getCurrency().getCode() : bankInfoCache.getBankInfo(bankId).getDefaultCurrency().getCode();
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(NotEnoughMoneyException.class)
    public Object notEnoughMoneyException() {
        CanexRequest request = (CanexRequest) requestContext.getBody();
        CanexResponse response = createErrorResponse(CommonWalletErrors.INSUFFICIENT_FUNDS.getCode());
        CanexJsonResponse jsonResponse = new CanexJsonResponse(request, response, formatter.format(new Date()));
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

}

package com.dgphoenix.casino.payment.wallet.client.v4;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.client.IJsonCWClient;
import com.dgphoenix.casino.common.client.canex.request.CanexJsonRequest;
import com.dgphoenix.casino.common.client.canex.request.CanexRequest;
import com.dgphoenix.casino.common.client.canex.request.RequestType;
import com.dgphoenix.casino.common.client.canex.request.friends.GetFriendsOutput;
import com.dgphoenix.casino.common.client.canex.request.friends.GetFriendsRequest;
import com.dgphoenix.casino.common.client.canex.request.friends.GetFriendsResponse;
import com.dgphoenix.casino.common.client.canex.request.onlineplayer.GetOnlinePlayersOutput;
import com.dgphoenix.casino.common.client.canex.request.onlineplayer.GetOnlinePlayersRequest;
import com.dgphoenix.casino.common.client.canex.request.onlineplayer.GetOnlinePlayersResponse;
import com.dgphoenix.casino.common.client.canex.request.onlinerooms.PushRoomsPlayersOutput;
import com.dgphoenix.casino.common.client.canex.request.onlinerooms.PushRoomsPlayersRequest;
import com.dgphoenix.casino.common.client.canex.request.onlinerooms.Room;
import com.dgphoenix.casino.common.client.canex.request.privateroom.InvitePlayersOutput;
import com.dgphoenix.casino.common.client.canex.request.privateroom.InvitePlayersRequest;
import com.dgphoenix.casino.common.client.canex.request.privateroom.PrivateRoom;
import com.dgphoenix.casino.common.client.canex.request.privateroom.Status;
import com.dgphoenix.casino.common.client.canex.response.CanexJsonResponse;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.rest.CustomRestTemplate;
import com.dgphoenix.casino.common.util.xml.IXmlRequestResult;
import com.dgphoenix.casino.common.util.xml.XmlRequestResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.SimpleLoggableContainer;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

public class CanexCWClient extends RESTCWClient implements IJsonCWClient {

    private static final Logger LOG = LogManager.getLogger(CanexCWClient.class);

    protected static final String REQUEST_TYPE = "_requestType";
    private static final String DEBIT_TYPE_NAME = "debitType";
    private static final String BET = "BET";
    private static final String TRANSFER = "TRANSFER";
    private final CustomRestTemplate restTemplate;

    public CanexCWClient(long bankId) {
        super(bankId);
        setLoggableContainer(new SimpleLoggableContainer());
        Gson gson = new Gson();
        restTemplate = new CustomRestTemplate();
        restTemplate.setGsonSerializer(gson);
        restTemplate.setLoggableClient(this);
        restTemplate.setContentType(MediaType.APPLICATION_JSON);
    }

    protected CanexCWClient(long bankId, CustomRestTemplate restTemplate) {
        super(bankId);
        this.restTemplate = restTemplate;
    }

    @Override
    protected Map<String, String> prepareWagerParams(CommonWallet wallet, Map<String, String> params, long accountId, String extUserId, String bet,
                                                     String win, Boolean isRoundFinished, long gsRoundId, long mpRoundId, String gameId, long bankId, long gameSessionId,
                                                     long negativeBet, ClientType clientType, String currencyCode, String cmd) throws CommonException {
        Map<String, String> wagerParams = super.prepareWagerParams(wallet, params, accountId, extUserId, bet, win, isRoundFinished, gsRoundId, mpRoundId, gameId,
                bankId, gameSessionId, negativeBet, clientType, currencyCode, cmd);

        if (StringUtils.isNotBlank(bet)) {

            String debitType = BET;

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if(bankInfo != null) {

                Currency currency = bankInfo.getDefaultCurrency();
                if(currency != null) {
                    try {
                        long gameIdIsLong = Long.parseLong(gameId);

                        Map<Long, IBaseGameInfo> baseGameInfoMap =
                                BaseGameCache.getInstance().getAllGameInfosAsMap(bankId, currency);

                        if(baseGameInfoMap != null) {

                            IBaseGameInfo baseGameInfo = baseGameInfoMap.get(gameIdIsLong);

                            if (baseGameInfo != null && baseGameInfo instanceof BaseGameInfo) {

                                if (((BaseGameInfo) baseGameInfo).isReserveBalance()) {
                                    debitType = TRANSFER;
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        LOG.error("prepareWagerParams: Exception={}", e.getMessage(), e);
                    }
                }
            }

            wagerParams.put(DEBIT_TYPE_NAME, debitType);
        }

        wagerParams.put(REQUEST_TYPE, RequestType.WAGER.name());

        return wagerParams;
    }

    @Override
    protected Map<String, String> prepareAuthParams(String token, String gameId, String serverId, ClientType clType) throws CommonException {
        Map<String, String> authParams = super.prepareAuthParams(token, gameId, serverId, clType);
        authParams.put(REQUEST_TYPE, RequestType.AUTH.name());
        return authParams;
    }

    @Override
    protected Map<String, String> prepareGetBalanceParams(String extUserId) throws CommonException {
        Map<String, String> balanceParams = super.prepareGetBalanceParams(extUserId);
        balanceParams.put(REQUEST_TYPE, RequestType.BALANCE.name());
        return balanceParams;
    }

    @Override
    protected Map<String, String> prepareRefundBetParameters(CommonWalletOperation debitOperation, long accountId, String extUserId, long gameId) throws CommonException {
        Map<String, String> refundParams = super.prepareRefundBetParameters(debitOperation, accountId, extUserId, gameId);
        refundParams.put(REQUEST_TYPE, RequestType.REFUND.name());
        return refundParams;
    }

    @Override
    protected Map<String, String> prepareUpdatePlayerStatusInPrivateRoomParameters(String privateRoomId, String nickname,
                                                                                   String externalId, Status status) throws CommonException {
        Map<String, String> updatePlayerStatusParams =
                super.prepareUpdatePlayerStatusInPrivateRoomParameters(privateRoomId, nickname, externalId, status);
        updatePlayerStatusParams.put(REQUEST_TYPE, RequestType.PLAYER_STATUS.name());
        return updatePlayerStatusParams;
    }

    @Override
    public boolean updatePlayerStatusInPrivateRoom(PrivateRoom privateRoom) throws CommonException {

        String url = getUpdatePlayerStatusInPrivateRoomUrl();

        IXmlRequestResult output = request(privateRoom, url);

        if (!output.isSuccessful()) {
            throw new CommonException("CanexCWClient:updatePlayerStatusInPrivateRoom response was not successful");
        }

        return true;
    }

    @Override
    public boolean invitePlayersToPrivateRoom(List<String> externalIds, String privateRoomId) throws CommonException {

        String url = getInvitePlayersToPrivateRoomUrl();

        InvitePlayersRequest invitePlayersRequest = new InvitePlayersRequest(externalIds, privateRoomId) ;

        InvitePlayersOutput output = restTemplate.sendRequest(url, invitePlayersRequest, InvitePlayersOutput.class);

        if (output == null || output.getExtSystem() == null) {
            throw new CommonException("CanexCWClient:invitePlayersToPrivateRoom response was not successful");
        }

        return output.getExtSystem().getResponse() != null;
    }

    @Override
    public GetFriendsResponse getFriends(String externalId, String nickname) throws CommonException {

        String url = getFriendsUrl();

        GetFriendsRequest getFriendsRequest = new GetFriendsRequest(externalId, nickname) ;

        GetFriendsOutput output = restTemplate.sendRequest(url, getFriendsRequest, GetFriendsOutput.class);

        if (output == null || output.getExtSystem() == null) {
            throw new CommonException("CanexCWClient:getFriends response was not successful");
        }

        return output.getExtSystem().getResponse();
    }

    @Override
    public GetOnlinePlayersResponse getOnlineStatus(List<String> externalIds) throws CommonException {

        String url = getOnlineStatusUrl();

        GetOnlinePlayersRequest getOnlinePlayersRequest = new GetOnlinePlayersRequest(externalIds) ;

        GetOnlinePlayersOutput output = restTemplate.sendRequest(url, getOnlinePlayersRequest, GetOnlinePlayersOutput.class);

        if (output == null || output.getExtSystem() == null) {
            throw new CommonException("CanexCWClient:getOnlineStatus response was not successful");
        }

        return output.getExtSystem().getResponse();
    }

    @Override
    public boolean pushRoomsPlayers(List<Room> rooms) throws CommonException {

        String url = getUpdatePlayersRoomsNumberUrl();

        PushRoomsPlayersRequest pushRoomsPlayersRequest = new PushRoomsPlayersRequest(rooms) ;

        PushRoomsPlayersOutput output = restTemplate.sendRequest(url, pushRoomsPlayersRequest, PushRoomsPlayersOutput.class);

        if (output == null || output.getExtSystem() == null || output.getExtSystem().getResponse() == null
                || output.getExtSystem().getResponse().getResult() == null) {
            throw new CommonException("CanexCWClient:pushRoomsPlayers response was not successful");
        }

        return output.getExtSystem().getResponse().getResult().equals("OK");
    }

    protected XmlRequestResult request(CanexJsonRequest canexJsonRequest, String url) throws CommonException {
        return request(canexJsonRequest, url, 0);
    }

    protected XmlRequestResult request(CanexJsonRequest canexJsonRequest, String url, long timeout)
            throws CommonException {
        try {
            LOG.info("CanexCWClient::request, request to url:{} is:{}", url, canexJsonRequest);
            return doRequest(canexJsonRequest, url, timeout);
        } catch (Exception e) {
            LOG.error("RESTCWClient::request error, url = {}", url, e);
            throw new CommonException(e);
        }
    }

    protected XmlRequestResult doRequest(CanexJsonRequest canexJsonRequest, String url, long timeout) throws CommonException {

        try {
            CanexJsonResponse response = restTemplate.sendRequest(url, canexJsonRequest, CanexJsonResponse.class);
            return response.toXmlResult();
        } catch (CommonException e) {
            LOG.error("request error, url = {}, request body: {}", url, canexJsonRequest, e);
            throw new CommonException(e);
        }
    }

    @Override
    protected XmlRequestResult doRequest(Map<String, String> params, String url, long bankId, long timeout) throws CommonException {
        RequestType requestType = RequestType.valueOf(params.get(REQUEST_TYPE));
        CanexRequest request = new CanexRequest(requestType, params);
        try {
            CanexJsonResponse response = restTemplate.sendRequest(url, request, CanexJsonResponse.class);
            return response.toXmlResult();
        } catch (CommonException e) {
            LOG.error("request error, bankId = {}, url = {}", bankId, url, e);
            throw new CommonException(e);
        }
    }

}

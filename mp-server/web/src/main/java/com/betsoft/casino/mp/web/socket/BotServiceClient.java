package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.kafka.KafkaMessageService;
import com.betsoft.casino.mp.model.bots.dto.BotLogInResult;
import com.betsoft.casino.mp.model.bots.dto.BotLogOutResult;
import com.betsoft.casino.mp.model.bots.dto.BotStatusResult;
import com.betsoft.casino.mp.model.bots.dto.BotsMap;
import com.betsoft.casino.mp.service.BotManagerService;
import com.betsoft.casino.mp.service.IBotServiceClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;
import com.dgphoenix.casino.kafka.dto.StringResponseDto;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.dto.bots.request.BotGetStatusRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.BotLogInRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.BotSitOutRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.BotStatusRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.ConfirmNextRoundBuyInRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.GetBotsMapRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.GetDetailBotInfoRequest;
import com.dgphoenix.casino.kafka.dto.bots.request.RemoveBotRequest;
import com.dgphoenix.casino.kafka.dto.bots.response.BotLogInResultDto;
import com.dgphoenix.casino.kafka.dto.bots.response.BotLogOutResultDto;
import com.dgphoenix.casino.kafka.dto.bots.response.BotStatusResponse;
import com.dgphoenix.casino.kafka.dto.bots.response.BotsMapResponseDto;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import static com.betsoft.casino.mp.utils.KafkaResponseConverterUtil.convertToType;

/**
 * User: flsh
 * Date: 07.07.2022.
 */
@Service
public class BotServiceClient implements IBotServiceClient {
    private static final Logger LOG = LogManager.getLogger(BotServiceClient.class);

    private KafkaMessageService kafkaMessageService;
    private BotManagerService botManagerService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public BotServiceClient(KafkaMessageService kafkaMessageService) {
        this.kafkaMessageService = kafkaMessageService;
    }

    public BotManagerService getBotManagerService() {
        return botManagerService;
    }

    public void setBotManagerService(BotManagerService botManagerService) {
        this.botManagerService = botManagerService;
    }

    public boolean isBotServiceEnabled() {
        return botManagerService != null && botManagerService.isBotServiceEnabled();
    }

    private void assertSupportEnabled() throws CommonException {
        if (!isBotServiceEnabled()) {
            throw new CommonException("Bot server support not enabled");
        }
    }

    @Override
    public BotLogInResult logIn(int botServerId, long botId, String userName, String password, long bankId, long gameId, long buyIn, String botNickname,
                                 long roomId, String lang, String enterLobbyWsUrl, String openRoomWSUrl, long expiresAt, double shootsRate, double bulletsRate)
            throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("sitIn: botServerId={}, botId={}, userName={}, password=******, bankId={}, gameId={}, buyIn={}, botNickname={}, roomId={}, lang={}, " +
                "enterLobbyWsUrl={}, openRoomWSUrl={}, expiresAt={}", botServerId, botId, userName, /*password,*/ bankId, gameId, buyIn, botNickname, roomId, lang,
                enterLobbyWsUrl, openRoomWSUrl, expiresAt);
        MutableObject<BotLogInResultDto> mutable = new MutableObject<>();

        BotLogInRequest request =
                new BotLogInRequest(botId, userName, password, bankId, gameId, buyIn, botNickname, roomId, lang, botServerId,
                        enterLobbyWsUrl, openRoomWSUrl, expiresAt, shootsRate, bulletsRate);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);
        try {
            BotLogInResultDto result =
                    convertToType(response, (r) -> new BotLogInResultDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: sitIn", System.currentTimeMillis() - now,
                    botNickname);
            LOG.debug("sitIn: botId={}, result={}", botId, result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        BotLogInResultDto value = mutable.getValue();
        return new BotLogInResult(value.getSessionId(), value.getMmcBalance(), value.getMqcBalance(), value.isSuccess(), value.getStatusCode(), value.getReasonPhrases());
    }

    @Override
    public BotStatusResult getStatusForNewBot(String userName, String password, String botNickName, long bankId, long gameId) throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("getStatusForNewBot: userName={}, botNickName={}, bankId={}, gameId={}", userName, botNickName, bankId, gameId);
        MutableObject<BotStatusResponse> mutable = new MutableObject<>();

        BotStatusRequest request = new BotStatusRequest(userName, password, botNickName, bankId, gameId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);
        try {
            BotStatusResponse result =
                    convertToType(response, (r) -> new BotStatusResponse(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: getStatusForNewBot", System.currentTimeMillis() - now,
                    botNickName);
            LOG.debug("getStatusForNewBot: botNickName={}, result={}", botNickName, result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("getStatusForNewBot failed, botNickName={}", botNickName, e);
            throw new CommonException(e);
        }
        BotStatusResponse value = mutable.getValue();
        return new BotStatusResult(value.getStatus(), value.getMmcBalance(), value.getMqcBalance(),
                value.isSuccess(), value.getStatusCode(), value.getReasonPhrases());
    }

    @Override
    public BotStatusResult getStatus(long botId, String sessionId, String botNickname, long roomId) throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("getStatus: botId={}, sessionId={}, botNickname={}, roomId={}", botId, sessionId, botNickname, roomId);
        MutableObject<BotStatusResponse> mutable = new MutableObject<>();

        BotGetStatusRequest request = new BotGetStatusRequest(botId, sessionId, botNickname, roomId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);

        try {
            BotStatusResponse result =
                    convertToType(response, (r) -> new BotStatusResponse(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: getStatus", System.currentTimeMillis() - now,
                    botNickname);
            LOG.debug("getStatus: botId={}, result={}", botId, result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("getStatus failed, botId={}", botId, e);
            throw new CommonException(e);
        }
        BotStatusResponse value = mutable.getValue();
        return new BotStatusResult(value.getStatus(), value.getMmcBalance(), value.getMqcBalance(),
                value.isSuccess(), value.getStatusCode(), value.getReasonPhrases());
 
    }

    @Override
    public BotStatusResult confirmNextRoundBuyIn(long botId, String sessionId, String botNickname, long roomId, long roundId) throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("confirmNextRoundBuyIn: botId={}, sessionId={}, botNickname={}, roomId={}, roundId={}", botId, sessionId, botNickname,
                roomId, roundId);
        MutableObject<BotStatusResponse> mutable = new MutableObject<>();

        ConfirmNextRoundBuyInRequest request = new ConfirmNextRoundBuyInRequest(botId, sessionId, botNickname, roomId, roundId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);
        try {
            BotStatusResponse result =
                    convertToType(response, (r) -> new BotStatusResponse(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: confirmNextRoundBuyIn", System.currentTimeMillis() - now,
                    botNickname);
            LOG.debug("confirmNextRoundBuyIn: botId={}, result={}", botId, result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("confirmNextRoundBuyIn failed, botId={}", botId, e);
            throw new CommonException(e);
        }
        BotStatusResponse value = mutable.getValue();
        return new BotStatusResult(value.getStatus(), value.getMmcBalance(), value.getMqcBalance(),
                value.isSuccess(), value.getStatusCode(), value.getReasonPhrases());
 

    }

    @Override
    public BotLogOutResult logOut(long botId, String sessionId, String botNickname, long roomId) throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("sitOut: botId={}, sessionId={}, botNickname={}, roomId={}", botId, sessionId, botNickname, roomId);
        MutableObject<BotLogOutResultDto> mutable = new MutableObject<>();

        BotSitOutRequest request = new BotSitOutRequest(botId, sessionId, botNickname, roomId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);

        try {
            BotLogOutResultDto result =
                    convertToType(response, (r) -> new BotLogOutResultDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: sitOut", System.currentTimeMillis() - now,
                    botNickname);
            LOG.debug("sitOut: botId={}, result={}", botId, result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("logOut failed, botId={}", botId, e);
            throw new CommonException(e);
        }
        BotLogOutResultDto value = mutable.getValue();
        return new BotLogOutResult(value.isSuccess(), value.getStatusCode(), value.getReasonPhrases());
    }

    @Override
    public void removeBot(long botId, String botNickname, long roomId) throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("removeBot: botId={}, botNickname={}, roomId={}", botId, botNickname, roomId);
        RemoveBotRequest request = new RemoveBotRequest(botId, botNickname, roomId);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);
        try {
            convertToType(response, (r) -> new VoidKafkaResponse(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));
            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: removeBot", System.currentTimeMillis() - now,
                    botNickname);
        } catch (Exception e) {
            LOG.error("removeBot failed, botId={}", botId, e);
            throw new CommonException(e);
        }
    }

    @Override
    public String getDetailBotInfo(long botId, String botNickname) throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("getDetailBotInfo: botId={}, botNickname={}", botId, botNickname);
        MutableObject<String> mutable = new MutableObject<>();

        GetDetailBotInfoRequest request = new GetDetailBotInfoRequest(botId, botNickname);
        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);

        try {
            StringResponseDto resultDto =
                    convertToType(response, (r) -> new StringResponseDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            String result = resultDto.getValue();
            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: getDetailBotInfo", System.currentTimeMillis() - now,
                    botNickname);
            LOG.debug("getDetailBotInfo: botId={}, result={}", botId, result);
            mutable.setValue(result);
        } catch (Exception e) {
            LOG.error("removeBot failed, botId={}", botId, e);
            throw new CommonException(e);
        }
        return mutable.getValue();

    }

    @Override
    public BotsMap getBotsMap() throws CommonException {
        assertSupportEnabled();
        final long now = System.currentTimeMillis();
        LOG.debug("getBotsMap: start");
        MutableObject<BotsMap> mutable = new MutableObject<>();
        GetBotsMapRequest request = new GetBotsMapRequest();

        Mono<KafkaResponse> response =
                kafkaMessageService.syncRequestToBotService(request);

        try {
            BotsMapResponseDto resultDto =
                    convertToType(response, (r) -> new BotsMapResponseDto(r.isSuccess(),
                            r.getStatusCode(), r.getReasonPhrases()));

            BotsMap botsMap = resultDto.getBotsMap();
            StatisticsManager.getInstance().updateRequestStatistics("BotServiceClient: getBotsMap", System.currentTimeMillis() - now);
            LOG.debug("getBotsMap: result={}", botsMap);
            mutable.setValue(botsMap);
        } catch (Exception e) {
            LOG.error("getBotsMap failed", e);
            throw new CommonException(e);
        }

        return mutable.getValue();
    }
}

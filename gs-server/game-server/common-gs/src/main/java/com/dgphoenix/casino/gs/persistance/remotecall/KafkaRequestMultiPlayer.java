package com.dgphoenix.casino.gs.persistance.remotecall;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.kafka.dto.*;
import com.abs.casino.kafka.dto.privateroom.request.*;
import com.abs.casino.kafka.dto.privateroom.response.*;
import com.dgphoenix.casino.kafka.service.KafkaMessageService;
import com.dgphoenix.casino.promo.IKafkaRequestMultiPlayer;
import com.abs.casino.kafka.dto.StringResponseDto;
import com.abs.casino.kafka.dto.privateroom.response.DeactivateRoomResultDto;
import com.abs.casino.kafka.dto.privateroom.response.PrivateRoomIdResultDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.dgphoenix.casino.gs.persistance.remotecall.KafkaResponseConverterUtil.convertToType;


public class KafkaRequestMultiPlayer implements IKafkaRequestMultiPlayer {
    private static final Logger LOGGER = LogManager.getLogger(KafkaRequestMultiPlayer.class);

    private KafkaMessageService kafkaMessageService;

    public KafkaRequestMultiPlayer(KafkaMessageService kafkaMessageService) {
        this.kafkaMessageService = kafkaMessageService;
    }

    public StringResponseDto getPrivateRoomURL(PrivateRoomURLDto request) {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            StringResponseDto data = convertToType(response, (r) -> new StringResponseDto(r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            String result = data.getValue();
            if (!StringUtils.isTrimmedEmpty(result)) {
                data.setValue(result);
            }
            return data;

        }catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            return new StringResponseDto(false, 500, e.getMessage());
        }
    }

    public PrivateRoomIdResultDto getPrivateRoomId(PrivateRoomIdDto request) {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            PrivateRoomIdResultDto data = convertToType(response, (r) -> new PrivateRoomIdResultDto(r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            String privateRoomId = data.getPrivateRoomId();
            if (!StringUtils.isTrimmedEmpty(privateRoomId)) {
                data.setPrivateRoomId(privateRoomId);
            }
            List<String> activePrivateRooms = data.getActivePrivateRooms();
            if(activePrivateRooms != null){
                data.setActivePrivateRooms(activePrivateRooms);
            }

            return data;

        }  catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            return new PrivateRoomIdResultDto(false,500, e.getMessage());
        }
    }

    public DeactivateRoomResultDto deactivate(DeactivateRoomDto request) throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new DeactivateRoomResultDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response: ", e);
        }
    }

    public UpdateRoomResultDto updatePlayersStatusInPrivateRoom(UpdateRoomDto request) throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new UpdateRoomResultDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));
        }catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response: ", e);
        }
    }

    public UpdateOnlinePlayersResultDto updateOnlinePlayers(UpdateOnlinePlayersDto request) throws CommonException  {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new UpdateOnlinePlayersResultDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));
        }catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response: ", e);
        }
    }

    public UpdateFriendsResultDto updateFriends(UpdateFriendsDto request) throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new UpdateFriendsResultDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));
        }catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response: ", e);
        }
    }

    public CollectionResponseDto getParticipantAccountIdsInRound(GetParticipantAccountIdsInRoundDto request) throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new CollectionResponseDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response: ", e);
        }
    }

    public RoomInfoResultDto loadCurrentBattlegroundRoomInfoForPlayer(ParticipantGameSessionDto request) throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new RoomInfoResultDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));

        }catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            return new RoomInfoResultDto(false, 500, e.getMessage());
        }
    }

    public PrivateRoomInfoResultDto getPrivateRoomInfo(GetPrivateRoomInfoRequest request) throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(request);
        try {
            return convertToType(response, (r) -> new PrivateRoomInfoResultDto(
                r.isSuccess(),
                r.getStatusCode(),
                r.getReasonPhrases()));

        }catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            return new PrivateRoomInfoResultDto(false, 500, e.getMessage());
        }
    }

    public void sendChangeBonusStatusToMQ(BonusStatusDto request) throws CommonException {
        kafkaMessageService.asyncRequestToAllMP(request);
    }

    public void sitOut(SitOutRequest2 request) throws CommonException {
        kafkaMessageService.asyncRequestToAllMP(request);
    }

    public void sendTournamentEnded(long campaignId, String oldStatus, String newStatus) throws CommonException {
        kafkaMessageService.asyncRequestToAllMP(new TournamentEndedDto(campaignId, oldStatus, newStatus));
    }

    public void enableBotService(boolean enable) throws CommonException {
        LOGGER.debug("enableBotService: enable={}", enable);

        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new EnableBotServiceRequest(enable));
        try {
            convertToType(response, (r) -> new VoidKafkaResponse(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
    }

    public boolean isBotServiceEnabled() throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new IsBotServiceEnabledRequest());
        AtomicReference<Boolean> result = new AtomicReference<>();
        try {
            BooleanResponseDto bool = convertToType(response, (r) -> new BooleanResponseDto(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(bool.isBool());
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }

        return result.get();
    }

    public List<BotConfigInfoDto> getAllBotConfigInfos() throws CommonException {
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new GetAllBotConfigInfosRequest());
        AtomicReference<List<BotConfigInfoDto>> result = new AtomicReference<>();
        try {
            BotConfigInfosResponse responseDto = convertToType(response, (r) -> new BotConfigInfosResponse(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(responseDto.getList());
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }

        return result.get();
    }

    public BotConfigInfoDto getBotConfigInfo(long botId) throws CommonException {
        AtomicReference<BotConfigInfoDto> result = new AtomicReference<>();

        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new GetBotConfigInfoRequest(botId));
        try {
            BotConfigInfoDto responseDto = convertToType(response, (r) -> new BotConfigInfoDto(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(responseDto);
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return result.get();
    }

    public BotConfigInfoDto getBotConfigInfoByUserName(String username) throws CommonException {
        AtomicReference<BotConfigInfoDto> result = new AtomicReference<>();

        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new GetBotConfigInfoByUserNameRequest(username));
        try {
            BotConfigInfoDto responseDto = convertToType(response, (r) -> new BotConfigInfoDto(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(responseDto);
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return result.get();
    }

    public BotConfigInfoDto getBotConfigInfoByMqNickName(String mqNickname) throws CommonException {
        AtomicReference<BotConfigInfoDto> result = new AtomicReference<>();
        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new GetBotConfigInfoByNickNameRequest(mqNickname));
        try {
            BotConfigInfoDto responseDto = convertToType(response, (r) -> new BotConfigInfoDto(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(responseDto);
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return result.get();
    }

    public List<BotConfigInfoDto> upsertBotConfigInfo(List<BotConfigInfoDto> botConfigInfos) throws CommonException {
        AtomicReference<List<BotConfigInfoDto>> result = new AtomicReference<>();

        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new UpsertBotConfigInfoRequest(botConfigInfos));
        try {
            BotConfigInfosResponse responseDto = convertToType(response, (r) -> new BotConfigInfosResponse(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(responseDto.getList());
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return result.get();
    }

    public List<BotConfigInfoDto> removeBotConfigInfo(List<Long> botIds) throws CommonException {
        AtomicReference<List<BotConfigInfoDto>> result = new AtomicReference<>();

        Mono<KafkaResponse> response = kafkaMessageService.syncRequestToRandomMP(new RemoveBotConfigInfoRequest(botIds));
        try {
            BotConfigInfosResponse responseDto = convertToType(response, (r) -> new BotConfigInfosResponse(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
            result.set(responseDto.getList());
        } catch (Exception e) {
            LOGGER.error("Error getting response: ", e);
            throw new CommonException("Error getting response", e);
        }
        return result.get();
    }

}

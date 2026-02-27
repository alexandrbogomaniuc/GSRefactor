package com.abs.casino.controller.mqb;

import com.dgphoenix.casino.common.client.canex.request.friends.UpdateFriendsRequest;
import com.dgphoenix.casino.common.client.canex.request.friends.UpdateFriendsResponse;
import com.dgphoenix.casino.common.client.canex.request.onlineplayer.UpdateOnlinePlayersRequest;
import com.dgphoenix.casino.common.client.canex.request.onlineplayer.UpdateOnlinePlayersResponse;
import com.abs.casino.common.client.canex.request.privateroom.PrivateRoom;
import com.abs.casino.common.client.canex.request.privateroom.UpdateRoomResponse;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.web.ClientTypeFactory;
import com.abs.casino.controller.mqb.response.*;
import com.dgphoenix.casino.gs.socket.mq.BattlegroundService;
import com.abs.casino.kafka.dto.privateroom.response.DeactivateRoomResultDto;
import com.abs.casino.kafka.dto.privateroom.response.PrivateRoomIdResultDto;
import com.abs.casino.kafka.dto.StringResponseDto;
import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RestController
@DependsOn("applicationContextHelper")
public class BattlegroundPrivateRoomController {
    private static final Logger LOG = LogManager.getLogger(BattlegroundPrivateRoomController.class);

    private final BattlegroundService battlegroundService;

    public BattlegroundPrivateRoomController(BattlegroundService battlegroundService) {
        this.battlegroundService = battlegroundService;
    }

    @GetMapping("/getPrivateRoomId")
    public ResponseEntity<Object> getPrivateRoomId(@RequestParam(name = "token") String token,
                                                   @RequestParam(name = "gameId") int gameId,
                                                   @RequestParam(name = "bankId") long bankId,
                                                   @RequestParam(name = "buyIn") long buyIn,
                                                   HttpServletRequest request){
        try {
            LOG.debug("Get private room id request. token: {}, gameId: {}, bankId: {}, buyIn: {}", token, gameId, bankId, buyIn);
            PrivateRoomIdResultDto response = battlegroundService.getPrivateRoomId(token, gameId, bankId, buyIn, ClientTypeFactory.getByHttpRequest(request));

            if (HttpStatus.valueOf(response.getStatusCode()).is5xxServerError() || HttpStatus.valueOf(response.getStatusCode()).is4xxClientError()){
                LOG.error("Unable get private room id token: {}, gameId: {}, bankId: {}, buyIn: {}, status code: {}, reasonPhrases: {}",
                        token,
                        gameId,
                        bankId,
                        buyIn,
                        response.getStatusCode(),
                        response.getReasonPhrases());
                List<String> activePrivateRooms = new ArrayList<>();
                if (!CollectionUtils.isEmpty(response.getActivePrivateRooms())){
                    activePrivateRooms = response.getActivePrivateRooms();
                }

                return ResponseEntity.status(response.getStatusCode())
                        .body(new GetPrivateRoomIdError("ERROR", response.getReasonPhrases(), activePrivateRooms));
            }

            LOG.debug("Get private room id result. privateRoomId: {}", response.getPrivateRoomId());
            return ResponseEntity.ok().body(new GetPrivateRoomIdOkResult("OK", response.getPrivateRoomId()));
        } catch (Exception e) {
            LOG.error("Failed get private room id", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/deactivatePrivateRoom")
    public ResponseEntity<Object> deactivatePrivateRoom(@RequestParam(name = "token") String token,
                                                        @RequestParam(name = "bankId") long bankId,
                                                        @RequestParam(name = "privateRoomId") String privateRoomId,
                                                        HttpServletRequest request){
        try {
            LOG.debug("Deactivate private room url request. token: {}, bankId: {}, privateRoomId: {}", token, bankId, privateRoomId);
            DeactivateRoomResultDto result = battlegroundService.deactivate(token, privateRoomId, bankId, ClientTypeFactory.getByHttpRequest(request));
            if (result.getStatusCode() == 200) {
                return ResponseEntity.status(result.getStatusCode()).body(new BaseResult("OK", result.getReasonPhrases()));
            } else {
                return ResponseEntity.status(result.getStatusCode()).body(new BaseResult("ERROR", result.getReasonPhrases()));
            }
        } catch (Exception e) {
            LOG.error("Failed deactivate private room", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/deactivatePrivateRooms")
    public ResponseEntity<Object> deactivatePrivateRooms(@RequestBody List<String> privateRoomIds) {
        try {
            LOG.debug("Deactivate private rooms url request. privateRoomIds: {}", privateRoomIds);
            DeactivateRoomResultDto result = battlegroundService.deactivate(privateRoomIds);
            if (result.getStatusCode() == 200) {
                return ResponseEntity.status(result.getStatusCode()).body(new BaseResult("OK", result.getReasonPhrases()));
            } else {
                return ResponseEntity.status(result.getStatusCode()).body(new BaseResult("ERROR", result.getReasonPhrases()));
            }
        } catch (Exception e) {
            LOG.error("Failed deactivate private room", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> errorHandler(Exception e) {
        String message = Throwables.getRootCause(e).getMessage();
        if (message.equals("unsuccessful authentication")) {
            return ResponseEntity.status(401).body(new BaseResult("ERROR", message));
        } else if (message.equals("non-existent buy-in")) {
            return ResponseEntity.status(402).body(new BaseResult("ERROR", message));
        } else if (message.equals("wrong gameId")) {
            return ResponseEntity.status(430).body(new BaseResult("ERROR", message));
        } else {
            return ResponseEntity.status(400).body(new BaseResult("ERROR", message));
        }
    }

    @GetMapping("/getPrivateRoomUrl")
    public ResponseEntity<String> getPrivateRoomUrl(@RequestParam(name = "token") String token,
                                                    @RequestParam(name = "gameId") int gameId,
                                                    @RequestParam(name = "bankId") long bankId,
                                                    @RequestParam(name = "buyIn") long buyIn,
                                                    @RequestParam(name = "domainUrl") String domainUrl,
                                                    HttpServletRequest request) throws CommonException {
        LOG.debug("Get private room url request. token: {}, gameId: {}, bankId: {}, buyIn: {}, domainUrl: {}", token, gameId, bankId, buyIn, domainUrl);
        StringResponseDto response = battlegroundService.getPrivateRoomURL(token, gameId, bankId, buyIn, domainUrl, ClientTypeFactory.getByHttpRequest(request));

        if (HttpStatus.valueOf(response.getStatusCode()).is5xxServerError() || HttpStatus.valueOf(response.getStatusCode()).is4xxClientError()){
            LOG.debug("Error getting response: {}", response);
            return ResponseEntity.status(response.getStatusCode()).body(response.getReasonPhrases());
        }

        LOG.debug("Get private room url result: {}", response);
        return ResponseEntity.ok().body(response.getValue());
    }

    @PostMapping("/updatePlayersStatusInPrivateRoom")
    public ResponseEntity<Result> updatePlayersStatusInPrivateRoom(@RequestBody PrivateRoom privateRoom) {
        LOG.debug("updatePlayersStatusInPrivateRoom: Update Players Status In Private Room request. privateRoom: {}", privateRoom);

        if(privateRoom == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: privateRoom is null");
            return ResponseEntity.status(500).body(new BaseResult("ERROR", "privateRoom is null"));
        }

        if(privateRoom.getPlayers() == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: privateRoom.getPlayers() is null");
            return ResponseEntity.status(500).body(new BaseResult("ERROR", "players list is null"));
        }

        try {
            UpdateRoomResponse result = battlegroundService.updatePlayersStatusInPrivateRoom(privateRoom, false);
            if(result == null) {
                return ResponseEntity.status(500).body(new BaseResult("ERROR", "result is null"));
            }

            return ResponseEntity.status(result.getCode())
                    .body(new PostPrivateRoomPlayers(
                            result.getCode() == 200 ? "OK" : "ERROR",
                            result.getMessage(),
                            result.getPrivateRoomId(),
                            result.getPlayers()));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResult("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/updatePlayersStatusByCanex")
    public ResponseEntity<Result> updatePlayersStatusByCanex(@RequestBody PrivateRoom privateRoom) {
        LOG.debug("updatePlayersStatusByCanex: " +
                "Update Players Status In Private Room request. privateRoom: {}", privateRoom);

        if(privateRoom == null) {
            LOG.error("updatePlayersStatusByCanex: privateRoom is null");
            return ResponseEntity.status(500).body(new BaseResult("ERROR", "privateRoom is null"));
        }

        if(privateRoom.getPlayers() == null) {
            LOG.error("updatePlayersStatusByCanex: privateRoom.getPlayers() is null");
            return ResponseEntity.status(500).body(new BaseResult("ERROR", "players list is null"));
        }

        try {
            UpdateRoomResponse result =
                    battlegroundService.updatePlayersStatusInPrivateRoom(privateRoom, true);
            if(result == null) {
                return ResponseEntity.status(500).body(
                        new BaseResult("ERROR", "result is null"));
            }

            return ResponseEntity.status(result.getCode())
                    .body(new PostPrivateRoomPlayers(
                            result.getCode() == 200 ? "OK" : "ERROR",
                            result.getMessage(),
                            result.getPrivateRoomId(),
                            result.getPlayers()));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResult("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/updateFriends")
    public ResponseEntity<Result> updateFriends(@RequestBody UpdateFriendsRequest updateFriendsRequest) {
        LOG.debug("updateFriends: Update Friends request. updateFriendsRequest: {}", updateFriendsRequest);
        try {
            UpdateFriendsResponse result = battlegroundService.updateFriends(updateFriendsRequest);
            if(result == null) {
                return ResponseEntity.status(500).body(
                        new BaseResult("ERROR", "result is null"));
            }

            return ResponseEntity.status(result.getCode())
                    .body(new PostFriendsResponse(
                            result.getCode() == 200 ? "OK" : "ERROR",
                            result.getMessage(),
                            result.getNickname(),
                            result.getExternalId(),
                            result.getFriends()));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResult("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/updateOnlinePlayers")
    public ResponseEntity<Result> updateOnlinePlayers(@RequestBody UpdateOnlinePlayersRequest updateOnlinePlayersRequest) {
        LOG.debug("updateOnlinePlayers: Update Online Players request. updateOnlinePlayersRequest: {}", updateOnlinePlayersRequest);
        try {
            UpdateOnlinePlayersResponse result = battlegroundService.updateOnlinePlayers(updateOnlinePlayersRequest);
            if(result == null) {
                return ResponseEntity.status(500).body(
                        new BaseResult("ERROR", "result is null"));
            }

            return ResponseEntity.status(result.getCode())
                    .body(new PostOnlinePlayersResponse(
                            result.getCode() == 200 ? "OK" : "ERROR",
                            result.getMessage(),
                            result.getOnlinePlayers()));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new BaseResult("ERROR", e.getMessage()));
        }
    }
}

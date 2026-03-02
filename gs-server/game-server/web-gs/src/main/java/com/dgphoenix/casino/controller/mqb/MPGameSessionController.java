package com.abs.casino.controller.mqb;

import com.abs.casino.battleground.messages.MPGameSessionFinishResponse;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.gs.socket.mq.BattlegroundService;
import com.abs.casino.services.mp.MPGameSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/mqb")
@DependsOn("applicationContextHelper")
public class MPGameSessionController {

    private static final Logger LOG = LogManager.getLogger(MPGameSessionController.class);

    private final MPGameSessionService mpGameSessionService;
    private final BattlegroundService battlegroundService;

    public MPGameSessionController(MPGameSessionService mpGameSessionService, BattlegroundService battlegroundService) {
        this.mpGameSessionService = mpGameSessionService;
        this.battlegroundService = battlegroundService;
    }

    @GetMapping(value = "/finishGameSession", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MPGameSessionFinishResponse> finishGameSession(@RequestParam(name = "sid") String sid,
                    @RequestParam(name = "privateRoomId", required = false) String privateRoomId) throws CommonException {
        LOG.debug("DEBUG log test");
        LOG.info("INFO log test");
        LOG.warn("WARN log test");
        LOG.error("ERROR log test");
        System.out.println("MPGameSessionController :: finishGameSession");

        LOG.debug("finishGameSession: sid={}, privateRoomId={}", sid, privateRoomId);
        Pair<GameSession, Boolean> finishSessionResult = mpGameSessionService.finishGameSessionAndMakeSitOut(sid, privateRoomId);
        LOG.debug("finishGameSession: sid={}, finishSessionResult={}", sid, finishSessionResult);
        Set<String> users = battlegroundService.getParticipationNicknamesBySessionId(sid, finishSessionResult.getKey());
        LOG.debug("finishGameSession: sid={}, users={}", sid, users);
        return new ResponseEntity<>(new MPGameSessionFinishResponse(finishSessionResult.getValue(), users), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        LOG.error("Failed finish MP game session:  ", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

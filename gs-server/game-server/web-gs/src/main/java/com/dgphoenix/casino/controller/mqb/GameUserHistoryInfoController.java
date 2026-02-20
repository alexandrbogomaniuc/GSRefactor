package com.dgphoenix.casino.controller.mqb;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.ClientTypeFactory;
import com.dgphoenix.casino.gs.socket.mq.GameUserHistoryService;
import com.dgphoenix.casino.mqb.GameUserHistoryInfo;
import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mqb/")
@DependsOn("applicationContextHelper")
public class GameUserHistoryInfoController {
    private static final Logger LOG = LogManager.getLogger(GameUserHistoryInfoController.class);

    private final GameUserHistoryService gameUserHistoryService;

    public GameUserHistoryInfoController(GameUserHistoryService gameUserHistoryService) {
        this.gameUserHistoryService = gameUserHistoryService;
    }

    @GetMapping("/gameHistory")
    public ResponseEntity<Object> getPlayerGameHistory(@RequestParam("mmcBankId") long mmcBankId,
                                                       @RequestParam("mmcToken") String mmcToken,
                                                       @RequestParam("mqcBankId") long mqcBankId,
                                                       @RequestParam("mqcToken") String mqcToken,
                                                       @RequestParam(value = "startTime", required = false) Long startTime,
                                                       @RequestParam(value = "endTime", required = false) Long endTime,
                                                       HttpServletRequest request) throws CommonException {
        GameUserHistoryInfo userGameHistoryInfo = gameUserHistoryService.getUserGameHistoryInfo(mmcBankId, mmcToken,
                mqcBankId, mqcToken, startTime, endTime, ClientTypeFactory.getByHttpRequest(request));
        return new ResponseEntity<>(userGameHistoryInfo, HttpStatus.OK);
    }

    @ExceptionHandler({CommonException.class})
    public ResponseEntity<Object> handleExceptions(Exception e) {
        LOG.error("Failed to load game history ", e);
        return new ResponseEntity<>(Throwables.getRootCause(e).getMessage(), HttpStatus.BAD_REQUEST);
    }
}

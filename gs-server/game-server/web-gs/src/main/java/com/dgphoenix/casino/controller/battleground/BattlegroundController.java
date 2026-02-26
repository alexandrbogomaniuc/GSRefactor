package com.abs.casino.controller.battleground;

import com.dgphoenix.casino.battleground.messages.BattlegroundInfo;
import com.dgphoenix.casino.battleground.messages.BattlegroundRoundHistoryInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.ClientTypeFactory;
import com.dgphoenix.casino.gs.socket.mq.BattlegroundService;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/battleground")
@DependsOn("applicationContextHelper")
public class BattlegroundController {
    private static final Logger LOG = LogManager.getLogger(BattlegroundController.class);

    private final BattlegroundService battlegroundService;

    public BattlegroundController(BattlegroundService battlegroundService) {
        this.battlegroundService = battlegroundService;
    }

    @GetMapping("/config")
    public ResponseEntity<Object> getGameConfigs(@RequestParam("bankId") Long bankId) {
        List<BattlegroundInfo> result = battlegroundService.getGamesByBankId(bankId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/playerRoundHistory")
    public ResponseEntity<Object> getPlayerRoundHistory(@RequestParam("mmcBankId") Long mmcBankId,
                                                        @RequestParam("mmcToken") String mmcToken,
                                                        @RequestParam("mqcBankId") Long mqcBankId,
                                                        @RequestParam("mqcToken") String mqcToken,
                                                        @RequestParam(value = "startTime", required = false) Long startTime,
                                                        @RequestParam(value = "endTime", required = false) Long endTime,
                                                        HttpServletRequest request) throws CommonException {
        BattlegroundRoundHistoryInfo playerBattlegroundHistory = battlegroundService.getPlayerBattlegroundHistory(mmcBankId, mmcToken,
                mqcBankId, mqcToken, startTime, endTime, ClientTypeFactory.getByHttpRequest(request));
        return new ResponseEntity<>(playerBattlegroundHistory, HttpStatus.OK);
    }

    @ExceptionHandler({UncheckedExecutionException.class, CommonException.class})
    public ResponseEntity<Object> handleExceptions(Exception e) {
        LOG.error("Failed to get battleground games ", e);
        return new ResponseEntity<>(Throwables.getRootCause(e).getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

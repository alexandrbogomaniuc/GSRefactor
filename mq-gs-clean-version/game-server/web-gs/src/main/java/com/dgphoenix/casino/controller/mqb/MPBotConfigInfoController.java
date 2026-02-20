package com.dgphoenix.casino.controller.mqb;

import com.dgphoenix.casino.ats.AllAtsResponse;
import com.dgphoenix.casino.ats.AtsServiceEnableRequest;
import com.dgphoenix.casino.ats.BotConfigInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.services.mp.MPBotConfigInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.dgphoenix.casino.common.util.string.DateTimeUtils.toHumanReadableFormat;

@RestController
@RequestMapping("/ats")
@DependsOn("applicationContextHelper")
public class MPBotConfigInfoController {

    private static final Logger LOG = LogManager.getLogger(MPBotConfigInfoController.class);

    private final MPBotConfigInfoService mpBotConfigInfoService;

    public MPBotConfigInfoController(MPBotConfigInfoService mpBotConfigInfoService) {
        this.mpBotConfigInfoService = mpBotConfigInfoService;
    }

    @PostMapping("/enableAtsService")
    public ResponseEntity<Object> enableAtsService(@RequestBody AtsServiceEnableRequest atsServiceEnableRequest) throws Exception {
        try {
            LOG.debug("enableAtsService: atsServiceEnableRequest={}", atsServiceEnableRequest);

            mpBotConfigInfoService.enableBotService(atsServiceEnableRequest.isEnable());

            return ResponseEntity.status(HttpStatus.OK).body("OK");

        } catch (Exception e) {
            LOG.error("enableAtsService: Failed", e);
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping(value = "/isAtsServiceEnabled", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isAtsServiceEnabled() throws Exception {
        try {
            LOG.debug("isAtsServiceEnabled: Started at: {}", toHumanReadableFormat(System.currentTimeMillis()));
            boolean isBotServiceEnabled = mpBotConfigInfoService.isBotServiceEnabled();
            LOG.debug("isAtsServiceEnabled: isBotServiceEnabled={}", isBotServiceEnabled);
            return new ResponseEntity<>(isBotServiceEnabled, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("isAtsServiceEnabled: Failed", e);
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AllAtsResponse> getAllAts() throws Exception {
        try {
            LOG.debug("getAllAts: Started at: {}", toHumanReadableFormat(System.currentTimeMillis()));
            List<BotConfigInfo> allAts = mpBotConfigInfoService.getAllBotConfigInfos();
            LOG.debug("getAllAts: allAts.size()={}", (allAts != null ? allAts.size() : null));
            boolean isBotServiceEnabled = mpBotConfigInfoService.isBotServiceEnabled();
            LOG.debug("getAllAts: isBotServiceEnabled={}", isBotServiceEnabled);
            return new ResponseEntity<>(new AllAtsResponse(isBotServiceEnabled, allAts), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("getAllAts: Failed", e);
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<BotConfigInfo> getAts(@RequestParam(name = "id") long id) throws Exception {
        try {
            LOG.debug("getAts: id={}", id);
            BotConfigInfo returned = mpBotConfigInfoService.getBotConfigInfo(id);
            LOG.debug("getAts: successfully got botConfigInfo={}", returned);
            return ResponseEntity.status(HttpStatus.OK).body(returned);

        } catch (Exception e) {
            LOG.error("getAts: Failed", e);
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping("/upsert")
    public ResponseEntity<BotConfigInfo> upsertAts(@RequestBody BotConfigInfo botConfigInfo) throws Exception {
        try {
            LOG.debug("upsertAts: botConfigInfo={}", botConfigInfo);
            BotConfigInfo returned = mpBotConfigInfoService.upsertBotConfigInfo(botConfigInfo);
            LOG.debug("upsertAts: successfully upsert botConfigInfo={}", returned);
            return ResponseEntity.status(HttpStatus.OK).body(returned);

        } catch (Exception e) {
            LOG.error("upsertAts: Failed", e);
            throw new Exception(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<BotConfigInfo> removeAts(@RequestParam(name = "id") long id) throws Exception {
        try {
            LOG.debug("removeAts: id={}", id);
            BotConfigInfo returned = mpBotConfigInfoService.removeBotConfigInfo(id);
            LOG.debug("removeAts: successfully removed botConfigInfo={}", returned);
            return ResponseEntity.status(HttpStatus.OK).body(returned);

        } catch (Exception e) {
            LOG.error("removeAts: Failed", e);
            throw new Exception(e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        LOG.error("Failed ats action:  ", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

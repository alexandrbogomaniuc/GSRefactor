package com.betsoft.casino.mp.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * User: flsh
 * Date: 11.08.17.
 */
@Controller
public class WebSocketController {
    private static final Logger LOG = LogManager.getLogger(WebSocketController.class);

    public WebSocketController() {
        LOG.debug("Initialized ");
    }

/*    @GetMapping("/websocket")
    public String websocket() {
        LOG.debug("websocket: ");
        return "websocket";
    }*/
}

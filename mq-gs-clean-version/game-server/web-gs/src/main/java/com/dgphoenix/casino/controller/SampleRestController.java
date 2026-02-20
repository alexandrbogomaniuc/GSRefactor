package com.dgphoenix.casino.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.04.2021
 */
@RestController
@RequestMapping("/support/rest")
public class SampleRestController {

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }
}

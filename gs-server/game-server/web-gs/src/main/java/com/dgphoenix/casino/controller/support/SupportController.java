package com.dgphoenix.casino.controller.support;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 27.04.2021
 */
@Controller
@RequestMapping("/support")
public class SupportController {

    @GetMapping("/")
    public String index() {
        return "/support/index";
    }

    @RequestMapping(value = "/metrics", method = {RequestMethod.GET, RequestMethod.POST})
    public String metricsIndex() {
        return "/support/metrics/index";
    }
}

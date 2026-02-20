package com.dgphoenix.casino.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.04.2021
 */
@Controller
@RequestMapping("/support/mvc")
public class SampleMvcController {

    @GetMapping("ping")
    public String ping() {
        return "pages/ping";
    }

    @GetMapping("hello")
    public String hello(@RequestParam(required = false) String name, Model model) {
        model.addAttribute("name", name);
        return "/views/hello";
    }
}

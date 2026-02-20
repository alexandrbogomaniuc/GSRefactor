package com.betsoft.casino.mp.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * User: flsh
 * Date: 25.10.17.
 */
@Configuration
@EnableWebMvc
public class WebContextConfiguration implements WebMvcConfigurer {
    private static final Logger LOG = LogManager.getLogger(WebContextConfiguration.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        LOG.debug("addResourceHandlers: call");
        registry.addResourceHandler("/classic/listener.html").addResourceLocations("/listener.html");
        registry.addResourceHandler("/listener.html").addResourceLocations("/listener.html");
    }

}

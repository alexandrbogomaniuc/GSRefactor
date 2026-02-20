package com.betsoft.casino.mp.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.support.AbstractAnnotationConfigDispatcherHandlerInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * User: flsh
 * Date: 11.08.17.
 * need use org.springframework.web.reactive.support.AbstractAnnotationConfigDispatcherHandlerInitializer for flux
 */
@Order(2)
public class WebApplicationFluxInitializer extends AbstractAnnotationConfigDispatcherHandlerInitializer {
    private static final Logger LOG = LogManager.getLogger(WebApplicationFluxInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOG.info("Start MP Casino Engine [Flux]");
        super.onStartup(servletContext);
        System.out.println("Started MP Casino Engine [Flux]");

    }

    @Override
    protected String getServletMapping() {
        return "/websocket/*";
        //return "/";
    }

    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class[] {
                WebSocketRouter.class,
        };
    }
}

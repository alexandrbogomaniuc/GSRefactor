package com.betsoft.casino.mp.config;

import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * User: flsh
 * Date: 11.08.17.
 * need use org.springframework.web.reactive.support.AbstractAnnotationConfigDispatcherHandlerInitializer for flux
 **/
@Order(1)
public class WebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    private static final Logger LOG = LogManager.getLogger(WebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOG.info("Start MP Casino Engine [Servlet]");
        super.onStartup(servletContext);
        System.out.println("Started MP Casino Engine [Servlet]");

    }

    @Nullable
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {
                WebContextConfiguration.class
        };
    }

    @Nullable
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[0];
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/classic/*" };
    }
}

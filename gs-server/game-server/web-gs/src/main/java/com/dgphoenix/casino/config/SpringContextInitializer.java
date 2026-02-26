package com.abs.casino.config;

import com.abs.casino.init.Initializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.04.2021
 */
public class SpringContextInitializer implements WebApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(SpringContextInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOG.debug("Starting spring context initialization...");
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("com.abs.casino.config");
        servletContext.addListener(new ContextLoaderListener(context));

        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        servletContext.addListener(new Initializer());
        LOG.debug("Spring context initialization finished");
    }
}

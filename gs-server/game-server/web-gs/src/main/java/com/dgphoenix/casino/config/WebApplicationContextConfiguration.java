package com.abs.casino.config;

import com.abs.casino.config.ControllerContextConfiguration;
import com.abs.casino.common.config.CommonContextConfiguration;
import com.abs.casino.common.util.ExecutorUtils;
import com.abs.casino.gs.GameServerComponentsConfiguration;
import com.abs.casino.gs.PromotionContextConfiguration;
import com.abs.casino.gs.SharedGameServerComponentsConfiguration;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.GameCommandsProcessorsConfiguration;
import com.abs.casino.init.CassandraPersistenceContextConfiguration;
import com.abs.casino.kafka.config.KafkaConfiguration;
import com.abs.casino.services.GameServerServiceConfiguration;
import com.abs.casino.system.configuration.identification.ZookeeperConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 14.09.16
 */
@Configuration
@Import({
        CommonContextConfiguration.class,
        CassandraPersistenceContextConfiguration.class,
        SharedGameServerComponentsConfiguration.class,
        GameServerComponentsConfiguration.class,
        PromotionContextConfiguration.class,
        GameCommandsProcessorsConfiguration.class,
        GameServerServiceConfiguration.class,
        ControllerContextConfiguration.class,
        KafkaConfiguration.class,
        ZookeeperConfiguration.class
})
@EnableWebMvc
public class WebApplicationContextConfiguration extends WebMvcConfigurerAdapter {

    private static final Logger LOG = LogManager.getLogger(WebApplicationContextConfiguration.class);

    @PreDestroy
    private void shutdown() {
        try {
            ExecutorUtils.finalizeExecutor();
        } catch (Throwable t) {
            LOG.error("Cannot finalizeExecutor", t);
        }
        LOG.error("\nWebApplicationContext: DESTROYED\n");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
        registry.addResourceHandler("/support/css/**", "/css/**", "/testStand/**")
                .addResourceLocations("/support/css/", "/css/", "/testStand/");
        registry.addResourceHandler("/support/js/**", "/js/**", "/tools/js/**", "/DatePicker/**", "/support/metrics/flot/**")
                .addResourceLocations("/support/js/", "/js/", "/tools/js/", "/DatePicker/", "/support/metrics/flot/");
    }

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver("/", ".jsp");
        viewResolver.setOrder(2);
        return viewResolver;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver(TemplateEngine templateEngine) {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setViewNames(new String[]{"/views/*"});
        viewResolver.setOrder(1);
        return viewResolver;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        //builder.featuresToDisable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        //builder.featuresToDisable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }
}

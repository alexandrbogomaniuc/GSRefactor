package com.abs.casino.config;

import com.abs.casino.account.AccountManager;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.SubCasinoCache;
import com.abs.casino.controller.RequestContext;
import com.abs.casino.controller.mqb.MPGameSessionController;
import com.abs.casino.controller.stub.cw.CanexStubController;
import com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.gs.socket.mq.BattlegroundService;
import com.abs.casino.services.mp.MPGameSessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@ComponentScan(basePackages = "com.dgphoenix.casino.controller")
@PropertySource("classpath:settings.properties")
public class ControllerContextConfiguration {

    @Bean
    @RequestScope
    public RequestContext requestContext() {
        return new RequestContext();
    }

    @Bean(name = "MPGameSessionController")
    public MPGameSessionController gameSessionController(MPGameSessionService mpGameSessionService, BattlegroundService battlegroundService) {
        return new MPGameSessionController(mpGameSessionService, battlegroundService);
    }

    @Bean
    public CanexStubController canexStubController(AccountManager accountManager, RemoteCallHelper remoteCallHelper, RequestContext requestContext) {
        return new CanexStubController(SubCasinoCache.getInstance(), accountManager, RemoteClientStubHelper.getInstance(),
                remoteCallHelper, requestContext, BankInfoCache.getInstance());
    }
}

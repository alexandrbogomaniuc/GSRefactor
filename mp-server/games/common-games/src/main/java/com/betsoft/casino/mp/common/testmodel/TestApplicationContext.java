package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.IPendingOperationProcessor;
import com.betsoft.casino.mp.payment.PendingOperationType;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.LongIdGenerator;
import com.dgphoenix.casino.common.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public class TestApplicationContext extends DefaultResourceLoader implements ApplicationContext {
    private final StubBeanFactory beanFactory = new StubBeanFactory();

    private final String id = ObjectUtils.identityToString(this);

    private final String displayName = ObjectUtils.identityToString(this);

    private final long startupDate = System.currentTimeMillis();

    private final Environment environment = new StandardEnvironment();

    private final MessageSource messageSource = new DelegatingMessageSource();

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(this);

    public TestApplicationContext() {
    }

    @SuppressWarnings("rawtypes")
    public static TestApplicationContext createContextWithStubBeans(StubSocketService socketService, List seats) {
        TestApplicationContext ctx = new TestApplicationContext();
        GameMapStore mapStore = new GameMapStore();
        mapStore.init();
        ctx.addBean("gameMapStore", mapStore);
        ctx.addBean("playerInfoService", new StubRoomPlayerInfoService(seats));
        ctx.addBean("idGenerator", LongIdGenerator.getInstance());
        ctx.addBean("socketService", socketService);
        ctx.addBean("singleNodeRoomInfoService", new StubRoomInfoService());
        ctx.addBean("transportObjectsFactoryService", new StubTransportObjectsFactoryService());
        ctx.addBean("currencyRateService", new ICurrencyRateService() {

            @Override
            public void updateCurrencyToCache(Set<CurrencyRate> cRates) {

            }

            @Override
            public void updateOneCurrencyToCache(CurrencyRate cRate) {

            }

            @Override
            public CurrencyRate get(String sourceCode, String destCode) {
                return new CurrencyRate(sourceCode, destCode, 1.0, System.currentTimeMillis());
            }
        });
        ctx.addBean("lobbySessionService", new ILobbySessionService() {
            @Override
            public ILobbySession add(ILobbySession session) {
                return session;
            }

            @Override
            public Collection<ILobbySession> getByAccountId(long accountId) {
                return Collections.EMPTY_LIST;
            }

            @Override
            public ILobbySession get(String sessionId) {
                return null;
            }

            @Override
            public ILobbySession get(long accountId) {
                return null;
            }

            @Override
            public void remove(String sessionId) {

            }

            @Override
            public boolean closeConnection(String sessionId) {
                return false;
            }

            @Override
            public Runnable createRoundCompletedNotifyTask(String sid, long roomId, long accountId, long balance, long kills,
                                                           long treasures, int rounds, long xp, long xpPrev, long xpNext, int level) {
                return null;
            }

            @Override
            public void registerCloseLobbyConnectionListener(ILobbyConnectionClosedListener listener) {

            }

            @Override
            public void processCloseLobbyConnection(ILobbySocketClient client) throws CommonException {

            }
        });
        ctx.addBean("serverConfigService", new IServerConfigService() {

            @Override
            public int getServerId() {
                return 0;
            }

            @Override
            public IServerConfig getConfig() {
                return null;
            }

            @Override
            public IServerConfig getConfig(int id) {
                return null;
            }

            @Override
            public Iterable getConfigs() {
                return null;
            }

            @Override
            public void put(IServerConfig config) {

            }

            @Override
            public Map getConfigsMap() {
                return null;
            }

            @Override
            public boolean isThisIsAMaster() {
                return false;
            }
        });

        ctx.addBean("analyticsDBClientService", new IAnalyticsDBClientService() {
            @Override
            public List<Map<String, Object>> prepareRoomsPlayers(List<IRMSRoom> trmsRooms, int serverId) {
                return new ArrayList<>();
            }

            @Override
            public boolean saveRoomsPlayers(List<Map<String, Object>> roomsPlayersRows) {
                return false;
            }

            @Override
            public List<Map<String, Object>> prepareRoundResult(List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs, IRoom room) {
                return new ArrayList<>();
            }

            @Override
            public List<Map<String, Object>> prepareBattlegroundRoundResults(List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs, IRoom room) {
                return new ArrayList<>();
            }

            @Override
            public boolean saveRoundResults(List<Map<String, Object>> rows) {
                return true;
            }
        });

        ctx.addBean("bigQueryAsyncExecutor", new IAsyncExecutorService() {
            private final ExecutorService executor = Executors.newSingleThreadExecutor();
            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return executor.submit(task);
            }

            @Override
            public void execute(Runnable task) {
                executor.execute(task);
            }

            @Override
            public void shutdown() {
                executor.shutdown();
            }
        });

        ctx.addBean("pendingOperationService", new IPendingOperationService() {
            @Override
            public boolean isPendingOperation(IRoomPlayerInfo playerInfo) {
                return false;
            }

            @Override
            public IPendingOperationProcessor getProcessor(PendingOperationType type) {
                return null;
            }

            @Override
            public IPendingOperation get(long accountId) {
                return null;
            }

            @Override
            public void create(IPendingOperation newOperation) {

            }

            @Override
            public IPendingOperation createBuyInPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long amount, int betNumber, Long tournamentId, Long currentBalance, long gameId, long bankId) {
                return null;
            }

            @Override
            public IPendingOperation createWinPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long gameId, long bankId, long winAmount, long returnedBet, long gsRound, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
                return null;
            }

            @Override
            public void save(IPendingOperation operation) {

            }

            @Override
            public void remove(long accountId) {

            }

            @Override
            public boolean isExist(long accountId) {
                return false;
            }

            @Override
            public Collection<IPendingOperation> getAll() {
                return null;
            }

            @Override
            public Collection<Long> getAllKeys() {
                return null;
            }

            @Override
            public boolean tryLock(long accountId) {
                return false;
            }

            @Override
            public void unlock(long accountId) {

            }

            @Override
            public void lock(long accountId) {

            }
        });

        return ctx;
    }

    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return this.beanFactory;
    }

    //---------------------------------------------------------------------
    // Implementation of ApplicationContext interface
    //---------------------------------------------------------------------

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getApplicationName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public long getStartupDate() {
        return this.startupDate;
    }

    @Override
    public ApplicationContext getParent() {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    public void addBean(String name, Object bean) {
        this.beanFactory.addBean(name, bean);
    }

    public void addBeans(@Nullable List<?> beans) {
        if (beans != null) {
            for (Object bean : beans) {
                String name = bean.getClass().getName() + "#" + ObjectUtils.getIdentityHexString(bean);
                this.beanFactory.addBean(name, bean);
            }
        }
    }


    //---------------------------------------------------------------------
    // Implementation of BeanFactory interface
    //---------------------------------------------------------------------

    @Override
    public Object getBean(String name) throws BeansException {
        return this.beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException {
        return this.beanFactory.getBean(name, requiredType);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return this.beanFactory.getBean(name, args);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return this.beanFactory.getBean(requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return this.beanFactory.getBean(requiredType, args);
    }

    @Override
    public boolean containsBean(String name) {
        return this.beanFactory.containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isPrototype(name);
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isTypeMatch(name, typeToMatch);
    }

    @Override
    public boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isTypeMatch(name, typeToMatch);
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return this.beanFactory.getType(name);
    }

    @Override
    public String[] getAliases(String name) {
        return this.beanFactory.getAliases(name);
    }


    //---------------------------------------------------------------------
    // Implementation of ListableBeanFactory interface
    //---------------------------------------------------------------------

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanFactory.getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanFactory.getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(@Nullable ResolvableType type) {
        return this.beanFactory.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        return this.beanFactory.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return this.beanFactory.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return this.beanFactory.getBeansOfType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {

        return this.beanFactory.getBeansOfType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        return this.beanFactory.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
            throws BeansException {

        return this.beanFactory.getBeansWithAnnotation(annotationType);
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
            throws NoSuchBeanDefinitionException {

        return this.beanFactory.findAnnotationOnBean(beanName, annotationType);
    }


    //---------------------------------------------------------------------
    // Implementation of HierarchicalBeanFactory interface
    //---------------------------------------------------------------------

    @Override
    public BeanFactory getParentBeanFactory() {
        return null;
    }

    @Override
    public boolean containsLocalBean(String name) {
        return this.beanFactory.containsBean(name);
    }

    //---------------------------------------------------------------------
    // Implementation of MessageSource interface
    //---------------------------------------------------------------------

    @Override
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        return this.messageSource.getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        return this.messageSource.getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return this.messageSource.getMessage(resolvable, locale);
    }

    //---------------------------------------------------------------------
    // Other
    //---------------------------------------------------------------------

    @Override
    public void publishEvent(ApplicationEvent event) {
    }

    @Override
    public void publishEvent(Object event) {
    }

    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        return this.resourcePatternResolver.getResources(locationPattern);
    }

    private class StubBeanFactory extends StaticListableBeanFactory implements AutowireCapableBeanFactory {

        @Override
        public Object initializeBean(Object existingBean, String beanName) throws BeansException {
            if (existingBean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) existingBean).setApplicationContext(TestApplicationContext.this);
            }
            return existingBean;
        }

        @Override
        public <T> T createBean(Class<T> beanClass) {
            return BeanUtils.instantiateClass(beanClass);
        }

        @Override
        public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
            return BeanUtils.instantiateClass(beanClass);
        }

        @Override
        public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
            return BeanUtils.instantiateClass(beanClass);
        }

        @Override
        public void autowireBean(Object existingBean) throws BeansException {
        }

        @Override
        public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) {
        }

        @Override
        public Object configureBean(Object existingBean, String beanName) {
            return existingBean;
        }

        @Override
        public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
            throw new UnsupportedOperationException("Dependency resolution not supported");
        }

        @Override
        @Nullable
        public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) {
            throw new UnsupportedOperationException("Dependency resolution not supported");
        }

        @Override
        @Nullable
        public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
                                        @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {
            throw new UnsupportedOperationException("Dependency resolution not supported");
        }

        @Override
        public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
        }

        @Override
        public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) {
            return existingBean;
        }

        @Override
        public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {
            return existingBean;
        }

        @Override
        public void destroyBean(Object existingBean) {
        }
    }

}

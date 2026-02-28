package com.dgphoenix.casino.sm;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.system.configuration.PlayerSessionConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: plastical
 * Date: 25.02.2010
 */
public class PlayerSessionFactory {
    private static final Logger LOG = LogManager.getLogger(PlayerSessionFactory.class);
    private static final PlayerSessionFactory instance = new PlayerSessionFactory();

    private final ConcurrentHashMap<Long, IPlayerSessionManager> managers = new ConcurrentHashMap<Long, IPlayerSessionManager>();

    public static PlayerSessionFactory getInstance() {
        return instance;
    }

    private PlayerSessionFactory() {
    }

    public IPlayerSessionManager getPlayerSessionManager(long bankId) throws CommonException {
        IPlayerSessionManager psm = getPSM(bankId);
        if (psm == null) {
            psm = instantiatePSM(bankId);
        }

        return psm;
    }

    private IPlayerSessionManager getPSM(long bankId) {
        return managers.get(bankId);
    }

    private synchronized IPlayerSessionManager instantiatePSM(long bankId) throws CommonException {
        LOG.debug("PlayerSessionFactory::instantiatePSM instantiating PSM for bankId:" + bankId);
        String className = PlayerSessionConfiguration.getInstance().getPSMClass(bankId);
        try {
            if (StringUtils.isTrimmedEmpty(className)) {
                throw new CommonException("PSM is not defined");
            }

            Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(className);
            Constructor<?> psmConstructor = aClass.getConstructor(long.class);
            IPlayerSessionManager manager = (IPlayerSessionManager) psmConstructor.newInstance(bankId);
            managers.putIfAbsent(bankId, manager);
            return managers.get(bankId);
        } catch (CommonException e) {
            LOG.error("PlayerSessionFactory::instantiatePSM error: className=" + className, e);
            throw e;
        } catch (Exception e) {
            LOG.error("PlayerSessionFactory::instantiatePSM error: className:" + className, e);
            throw new CommonException(e);
        }
    }
}

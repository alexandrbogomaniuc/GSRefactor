/**
 * User: flsh
 * Date: 11.08.2009
 */
package com.abs.casino.gs.managers.payment.transfer.processor;

import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.payment.transfer.processor.IPaymentProcessor;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ReflectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class PaymentProcessorFactory {
    private static final Logger LOG = LogManager.getLogger(PaymentProcessorFactory.class);
    private static final PaymentProcessorFactory instance = new PaymentProcessorFactory();

    private static final Map<Long, IPaymentProcessor> processors = new HashMap<>();

    public static PaymentProcessorFactory getInstance() {
        return instance;
    }

    private PaymentProcessorFactory() {
    }

    public IPaymentProcessor getProcessor(BankInfo bankInfo) throws CommonException {
        IPaymentProcessor processor = processors.get(bankInfo.getId());
        return processor == null ? instantiatePP(bankInfo) : processor;
    }

    private synchronized IPaymentProcessor instantiatePP(BankInfo bankInfo) throws CommonException {
        try {
            String className = bankInfo.getPPClass();
            if (className == null) {
                throw new CommonException("PaymentProcessor not found");
            }
            Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(className);
            Constructor<?> ppConstructor = aClass.getConstructor(long.class);
            IPaymentProcessor processor = (IPaymentProcessor) ppConstructor.newInstance(bankInfo.getId());

            processors.put(bankInfo.getId(), processor);
            return processor;
        } catch (CommonException e) {
            LOG.error("PaymentProcessorFactory::instantiatePP error:", e);
            throw e;
        } catch (Exception e) {
            LOG.error("PaymentProcessorFactory::instantiatePP error:", e);
            throw new CommonException(e);
        }
    }
}

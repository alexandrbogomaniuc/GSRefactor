package com.dgphoenix.casino.common.util;

import junit.framework.Assert;
import org.apache.commons.net.ntp.TimeInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * User: van0ss
 * Date: 03.02.2017
 */
@RunWith(MockitoJUnitRunner.class)
public class NtpTimeProviderTest {

    Random random = new Random();

    @Mock
    NtpWrapper ntpWrapper;
    @Mock
    TimeInfo timeInfo;
    NtpTimeProvider timeProvider;

    @Before
    public void setUp() throws Exception {
        timeProvider = new NtpTimeProvider(ntpWrapper, 15);
        when(timeInfo.getOffset()).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (long) (2000 - random.nextInt(4000)); // +- 2 seconds
            }
        });
        when(ntpWrapper.getTime(any(InetAddress.class))).thenReturn(timeInfo);
        timeProvider.start("localhost");
    }

    @Test
    public void testGetTime() throws InterruptedException {
        getTimeTests(false);
    }

    @Test
    public void testGetTimeMicroseconds() throws InterruptedException, IOException {
        getTimeTests(true);
    }

    void getTimeTests(final boolean microseconds) throws InterruptedException {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(100);
        final AtomicLong lastTime = new AtomicLong(0);
        final ReentrantLock lock = new ReentrantLock();
        final AtomicBoolean wasError = new AtomicBoolean(false);
        for (int i = 0; i < 100; i++) {
            pool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    long providerTime = microseconds ? timeProvider.getTimeMicroseconds() : timeProvider.getTime();
                    //System.out.println(Thread.currentThread().getName() + " " + providerTime);
                    if (microseconds ? lastTime.get() >= providerTime : lastTime.get() > providerTime) {
                        wasError.set(true);
                    }
                    lastTime.set(providerTime);
                    lock.unlock();
                }
            }, 10, 10, TimeUnit.MILLISECONDS);
        }
        Thread.sleep(500);
        Assert.assertFalse("Timestamp should be more than last", wasError.get());
    }
}
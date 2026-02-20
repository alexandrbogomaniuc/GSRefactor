package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IAsyncExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class AsyncExecutorServiceTest {

    private IAsyncExecutorService executorService;

    @Before
    public void setUp() {
        // Initialize ExecutorService before each test
        executorService = new AsyncExecutorService(1, 1, 1, TimeUnit.MINUTES, 10, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @After
    public void tearDown() {
        // Cleanup ExecutorService after each test
        executorService.shutdown();
    }

    @Test
    public void testExecuteRunnable() throws InterruptedException {
        final AtomicBoolean taskExecuted = new AtomicBoolean(false);

        Runnable task = () -> {
            // Task execution logic
            taskExecuted.set(true);
        };

        // Execute the task asynchronously
        executorService.execute(task);

        // Wait briefly for the task to execute
        Thread.sleep(1000);

        // Assert that the task was executed
        assertTrue("The task was not executed as expected.", taskExecuted.get());
    }
}

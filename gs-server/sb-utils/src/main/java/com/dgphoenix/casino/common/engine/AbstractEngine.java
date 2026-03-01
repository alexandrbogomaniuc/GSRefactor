package com.abs.casino.common.engine;

import com.abs.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Date;

/**
 * Created
 * Date: 24.11.2008
 * Time: 15:36:03
 */
public abstract class AbstractEngine implements IEngine {
    private static final Logger LOG = Logger.getLogger(AbstractEngine.class);
    protected long MS_TO_SLEEP = 1000;
    protected boolean paused;
    protected boolean started;
    protected Thread thread;
    protected long interval;
    protected long lastTime;

    protected AbstractEngine() {
        interval = 0;
        lastTime = 0;
    }
    
    protected AbstractEngine(long sleepDelay) {
        interval = 0;
        lastTime = 0;
        MS_TO_SLEEP = sleepDelay;
    }
    
    public void setSleepTime(long delay){
    	MS_TO_SLEEP = delay;
    }

    public synchronized boolean startup() {
        if (isStarted()) {
            LOG.info(this + ":: already started");
            return false;
        }
        if (isRunning()) {
            LOG.info(this + ":: already running");
            return false;
        }

        LOG.debug(this + ":: startup");
        this.started = true;
        this.thread = new GameEngineThread();
        this.thread.start();
        return true;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isRunning() {
        final Thread thread = this.thread;
        return thread != null && thread.isAlive();
    }

    public synchronized boolean shutdown() {
        if (!isStarted()) {
            LOG.info(this + ":: not started");
            return false;
        }
        if (!isRunning()) {
            LOG.info(this + ":: not running");
            return false;
        }
        LOG.debug(this + ":: shutdown");
        this.started = false;
        notify();
        return true;
    }

    public synchronized void pause() {
        paused = true;
        LOG.debug(this + ":: paused");
    }

    public synchronized void resume() {
        if (lastTime != 0) {
            lastTime = new Date().getTime();
        }
        paused = false;
        LOG.debug(this + ":: resumed");
    }

    public synchronized void setSleepInterval(long interval) {
        this.interval = interval;
        this.lastTime = 0;
    }

    public void stopSleeping() {
        setSleepInterval(0);
    }

    public long getSleepInterval() {
        return interval;
    }

    public boolean isSleeping() {
        return interval > 0;
    }

    public boolean isPaused() {
        return paused;
    }

    private Runnable getNextTask() {
        if (interval > 0) {
            long time = System.currentTimeMillis();
            if (lastTime != 0) {
                interval -= time - lastTime;
                if (interval < 0) interval = 0;
            }
            lastTime = time;
            return null;
        } else {
            try {
				return nextTask();
			} catch (RemoteException e) {
				LOG.error("AbstractEngine::getNextTask error getting next task",e);
				this.started = false;
			} catch (CommonException e) {
				LOG.error("AbstractEngine::getNextTask error getting next task",e);
				this.started = false;
			}
			LOG.info("AbstractEngine::getNextTask returning null value");
			return null;
        }
    }

    abstract protected Runnable nextTask() throws RemoteException, CommonException ;

    protected long getDelay() {
        return MS_TO_SLEEP;
    }

    private void threadSleep(long delay) {
        try {
//            Thread.sleep(200);
            Thread.sleep(delay > 10 ? delay - 10 : 0);
        } catch (InterruptedException ie) {
            LOG.fatal(this + ":: were interrupted", ie);
            shutdown();
        }
    }

    class GameEngineThread extends Thread {
        public void run() {
            LOG.debug(this + ":: running ");
            try {
                while (isStarted()) {
                    while (isPaused() && isStarted()) {
                        threadSleep(getDelay());
                    }
                    if (isStarted()) {
                        Runnable task = getNextTask();
                        if (task != null) {
                            task.run();
                        } else {
                            threadSleep(getDelay());
                        }
                    }
                }
            } finally {
                LOG.debug(this + " stopped ");
            }
        }
    }

    public String toString() {
        return getClass().getName();
    }
}

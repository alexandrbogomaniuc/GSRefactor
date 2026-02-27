package com.abs.casino.common.util.hardware;

import com.abs.casino.common.util.hardware.data.CPUInfo;
import com.abs.casino.common.util.hardware.data.HardwareInfo;
import com.abs.casino.common.util.hardware.data.MemoryInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperic.sigar.*;

import java.util.Date;

public class HardwareConfigurationManager {
    private static final Logger LOG = LogManager.getLogger(HardwareConfigurationManager.class);

    private static HardwareConfigurationManager instance = new HardwareConfigurationManager();

    private Date systemUptime;
    private SigarProxy hardwareInformer;
    private final MemoryInfo memInfo = new MemoryInfo();
    private final CPUInfo cpuInfo = new CPUInfo();

    private HardwareConfigurationManager() {
        hardwareInformer = new Sigar();
    }

    public static HardwareConfigurationManager getInstance() {
        return instance;
    }

    public void startup() {
        setSystemUptime(new Date());
        CPUInformer.getInstance().startup();
    }

    public void shutdown() {
        CPUInformer.getInstance().shutdown();
    }

    public HardwareInfo getHardwareInfo() {
        HardwareInfo info = new HardwareInfo();
        info.setCpuInfo(getCPUInfo());
        info.setMemoryInfo(getMemoryInfo());
        return info;
    }

    public CPUInfo getCPUInfo() {
        cpuInfo.setCPUsCount(getCPUsCount());
        double cpuUsage = getCPUPercentFromInformer();
        if (cpuUsage <= 0) {
            cpuUsage = getCPUPercent();
        }

        cpuInfo.setCPUAveragePercent(cpuUsage);
        return cpuInfo;
    }

    public double getCPUPercent() {
        try {
            CpuPerc[] cpuList = hardwareInformer.getCpuPercList();
            double cpuUsage = 0;
            for (CpuPerc cpu : cpuList) {
                cpuUsage += cpu.getIdle();
            }
            cpuUsage = (1 - (cpuUsage / ((double) cpuList.length))) * 100;
            return cpuUsage;
        } catch (SigarException e) {
            LOG.error("getCPUPercent, exception:", e);
            return -1;
        }
    }

    public double getCPUPercentFromInformer() {
        return CPUInformer.getInstance().getAvrCpuPercent();
    }

    public int getCPUsCount() {
        try {
            return hardwareInformer.getCpuPercList().length;
        } catch (SigarException e) {
            LOG.error("getCPUsCount, exception:", e);
            return -1;
        }
    }

    public MemoryInfo getMemoryInfo() {
        memInfo.setFreeMemory(getFreeMemory());
        memInfo.setTotalMemory(getTotalMemory());
        memInfo.setUsedMemory(getUsedMemory());
        return memInfo;
    }

    public long getFreeMemory() {
        try {
            Mem memory = hardwareInformer.getMem();
            return memory.getFree();
        } catch (SigarException e) {
            LOG.error("getFreeMemory, exception:", e);
            return -1;
        }
    }

    public long getUsedMemory() {
        try {
            Mem memory = hardwareInformer.getMem();
            return memory.getActualUsed();
        } catch (SigarException e) {
            LOG.error("getUsedMemory, exception:", e);
            return -1;
        }
    }

    public long getTotalMemory() {
        try {
            Mem memory = hardwareInformer.getMem();
            return memory.getTotal();
        } catch (SigarException e) {
            LOG.error("getTotalMemory, exception:", e);
            return -1;
        }
    }

    public void setSystemUptime(Date date) {
        this.systemUptime = date;
    }

    public Date getSystemUptime() {
        return systemUptime;
    }
}

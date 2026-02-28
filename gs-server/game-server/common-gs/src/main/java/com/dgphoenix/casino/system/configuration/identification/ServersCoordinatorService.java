package com.abs.casino.system.configuration.identification;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import com.abs.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.abs.casino.common.cache.data.server.ServerInfo;

public class ServersCoordinatorService implements ServerCoordinatorInfoProvider, Closeable {
    private static final Logger LOG = LogManager.getLogger(ServersCoordinatorService.class);

    private static final int SESSION_TIMEOUT_MS = 15 * 1000;

    private static final String HOSTNAME = "HOSTNAME";

    private final ZookeeperProperties properties;
    private final CuratorFramework client;
    private final Timer heartbeatTimer = new Timer(true);
    private Integer lastLockedIndex = null;

    private Integer thisServerId;

    private LeaderSelector leaderSelector;
    private final AtomicBoolean isLeader = new AtomicBoolean(false);
    private final ExecutorService leaderExecutor = Executors.newSingleThreadExecutor();

    public ServersCoordinatorService(ZookeeperProperties properties) throws Exception {
        this.properties = properties;
        this.client = CuratorFrameworkFactory.newClient(
                properties.getConnect(),
                SESSION_TIMEOUT_MS, SESSION_TIMEOUT_MS,
                new ExponentialBackoffRetry(1000, 3)
        );
        this.client.getConnectionStateListenable().addListener((cli, state) -> {
            if (state == ConnectionState.RECONNECTED) {
                try {
                    reLockIfValid();
                } catch (Exception e) {
                    LOG.error("Failure connecting to Zookeeper to coordinate servers", e);
                }
            }
        });
        this.client.start();
        initPool();
        assignServerId();
    }

    @PostConstruct
    public void init() throws Exception {
        electLeader();
    }

    private void initPool() throws Exception {
        if (client.checkExists().forPath(properties.getPoolPath()) == null) {
            client.create().creatingParentsIfNeeded().forPath(properties.getPoolPath());
        }

        for (int i = 0; i < properties.getPoolSize(); i++) {
            String path = properties.getPoolPath() + "/" + i;
            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().forPath(path);
            }
        }
    }

    private void assignServerId() {
        String gameServerHostname = getGameServerHostname();
        try {
            thisServerId = Optional.ofNullable(assignServerId(gameServerHostname))
                    .orElseThrow(() -> new RuntimeException("CRITICAL: ! Unable to assign serverId for this server."));
        } catch (Exception e) {
            LOG.error("Error happened assigning server id: ", e);
            LOG.error("CRITICAL: ! Unable to assign serverId for this server.");
            System.exit(1);
        }
    }

    private String getGameServerHostname() {
        String gameServerHostname = System.getProperty(HOSTNAME) != null 
                ? System.getProperty(HOSTNAME) : System.getenv(HOSTNAME);
        return gameServerHostname;
    }

    private void electLeader() {
        String nodeId = Optional.ofNullable(getGameServerHostname())
                .orElseThrow(() -> new RuntimeException("CRITICAL: ! Unable to get nodeId for this server."));

        LOG.info("[LEADER ELECTION]:️ Node " + nodeId + " / " + thisServerId + " started and participating in elections");
        String leaderPath = properties.getServicePath() + "/leaders";
        this.leaderSelector = new LeaderSelector(client, leaderPath, new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) {
                isLeader.set(true);
                LOG.info("[LEADER ELECTION]:️ " + nodeId + " / " + thisServerId + " is the leader");

                // Start a background task to perform leader duties
                Future<?> task = leaderExecutor.submit(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            // perform periodic leader-only work
                            Thread.sleep(1000); // simulate doing work
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                try {
                    // Wait until the node is interrupted or shut down
                    task.get(); // this blocks takeLeadership() without blocking the calling thread
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    isLeader.set(false);
                    LOG.info("[LEADER ELECTION]:️ " + nodeId + " / " + thisServerId + " is no longer a leader");
                }
            }
        });

        leaderSelector.setId(String.valueOf(thisServerId));
        leaderSelector.autoRequeue();
        leaderSelector.start();
    }

    private synchronized Integer assignServerId(String serverIdentifier) throws Exception {
        return assignServerId(serverIdentifier, true);
    }

    private synchronized Integer assignServerId(String serverIdentifier, boolean withRetry) throws Exception {
        long now = System.currentTimeMillis();

        for (int i = 0; i < properties.getPoolSize(); i++) {
            String base = properties.getPoolPath() + "/" + i;
            String resPath = base + "/reservation";
            String lockPath = base + "/lock";

            if (client.checkExists().forPath(resPath) != null) {
                String[] data = new String(client.getData().forPath(resPath), StandardCharsets.UTF_8).split(";");
                String id = data[0];
                long lastSeen = Long.parseLong(data[1]);

                if (id.equals(serverIdentifier) && now - lastSeen <= properties.getTtlMillis()) {
                    try {
                        if (client.checkExists().forPath(lockPath) == null) {
                            client.create().withMode(CreateMode.EPHEMERAL).forPath(lockPath);
                        } else {
                            if (withRetry) {
                                LOG.warn("Unable to assign serverId as serverIdentifier '"
                                                + serverIdentifier + "' is already locked at "
                                                + lockPath + ". Will retry in " + (SESSION_TIMEOUT_MS + 5000)  + "ms ...");
                                Thread.sleep(SESSION_TIMEOUT_MS + 5000);
                                return assignServerId(serverIdentifier, false);
                            } else {
                                throw new RuntimeException(
                                        "Unable to assign serverId as serverIdentifier '"
                                                + serverIdentifier + "' is already locked at "
                                                + lockPath);
                            }
                        }
                        lastLockedIndex = i;
                        startHeartbeat(i, serverIdentifier);
                        return i + 1;
                    } catch (KeeperException.NodeExistsException ignored) {}
                }
            }
        }

        for (int i = 0; i < properties.getPoolSize(); i++) {
            String base = properties.getPoolPath() + "/" + i;
            String resPath = base + "/reservation";
            String lockPath = base + "/lock";

            boolean canUse = false;
            if (client.checkExists().forPath(resPath) == null) {
                canUse = true;
            } else {
                String[] data = new String(client.getData().forPath(resPath), StandardCharsets.UTF_8).split(";");
                long lastSeen = Long.parseLong(data[1]);
                if (now - lastSeen > properties.getTtlMillis()) {
                    canUse = true;
                }
            }

            if (canUse) {
                String data = serverIdentifier + ";" + now;
                if (client.checkExists().forPath(resPath) != null) {
                    client.setData().forPath(resPath, data.getBytes(StandardCharsets.UTF_8));
                } else {
                    client.create().forPath(resPath, data.getBytes(StandardCharsets.UTF_8));
                }

                try {
                    client.create().withMode(CreateMode.EPHEMERAL).forPath(lockPath);
                    lastLockedIndex = i;
                    startHeartbeat(i, serverIdentifier);
                    return i + 1;
                } catch (KeeperException.NodeExistsException ignored) {}
            }
        }

        LOG.error("All " + properties.getPoolSize()
                + " servers are not suitable for locking. Returning null. Critical error will happen now...");
        return null;
    }

    private void startHeartbeat(int index, String id) {
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    String path = properties.getPoolPath() + "/" + index + "/reservation";
                    String data = id + ";" + System.currentTimeMillis();
                    client.setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    System.err.println("Heartbeat failed for " + id);
                }
            }
        }, 0, properties.getHeartbeatInterval());
    }

    private void reLockIfValid() throws Exception {
        if (lastLockedIndex == null) return;

        String base = properties.getPoolPath() + "/" + lastLockedIndex;
        String resPath = base + "/reservation";
        String lockPath = base + "/lock";

        String[] data = new String(client.getData().forPath(resPath), StandardCharsets.UTF_8).split(";");
        long lastSeen = Long.parseLong(data[1]);

        if (System.currentTimeMillis() - lastSeen <= properties.getTtlMillis()) {
            try {
                client.create().withMode(CreateMode.EPHEMERAL).forPath(lockPath);
            } catch (KeeperException.NodeExistsException ignored) {}
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            close();
        } catch (IOException e) {
            // ignore
        }
    }
    @Override
    public void close() throws IOException {
        heartbeatTimer.cancel();
        leaderSelector.close();
        client.close();
    }

    @Override
    public Integer getServerId() {
        return thisServerId;
    }

    @Override
    public boolean isMaster() {
        return isLeader.get();
    }

    @Override
    public Integer getMasterServerId() {
        try {
            return Integer.parseInt(leaderSelector.getLeader().getId());
        } catch (Exception e) {
            LOG.error("Error getting leader: ", e);
            return -1;
        }
    }

    public Map<Integer, ServerInfo> getServerInfos() throws Exception {
        Map<Integer, ServerInfo> results = new HashMap<>();
        long now = System.currentTimeMillis();
        String poolPath = properties.getPoolPath();
        int poolSize = properties.getPoolSize();

        for (int i = 0; i < poolSize; i++) {
            String lockPath = poolPath + "/" + i + "/lock";
            String reservationPath = poolPath + "/" + i + "/reservation";
            int serverId = i + 1;
            ServerInfo serverInfo = new ServerInfo(serverId);
            if (thisServerId == serverId) {
                serverInfo.setThisServer(true);
                serverInfo.setOnline(true);
            }

            boolean isLocked = client.checkExists().forPath(lockPath) != null;

            if (client.checkExists().forPath(reservationPath) != null) {
                byte[] dataBytes = client.getData().forPath(reservationPath);
                String[] parts = new String(dataBytes, StandardCharsets.UTF_8).split(";");
                if (parts.length == 2) {
                    String serverIdentifier = parts[0];
                    long lastSeen = Long.parseLong(parts[1]);
                    serverInfo.setHost(serverIdentifier);

                    if (isLocked && (now - lastSeen <= properties.getTtlMillis())) {
                        serverInfo.setOnline(true); // Truly online
                    } else {
                        if (now - lastSeen <= properties.getTtlMillis()) {
                            serverInfo.setOnline(false); // Reserved and in TTL
                        } else {
                            continue; // expired, skip
                        }
                    }
                } else {
                    continue; // Malformed reservation, skip
                }
            } else {
                continue; // Offline, skip
            }

            results.put(serverId, serverInfo);
        }

        return results;
    }

    public Map<Integer, ServerInfo> getOnlineServerStates() throws Exception {
        return getServerInfos().entrySet()
                .stream()
                .filter(e -> e.getValue().isServerOnline())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}

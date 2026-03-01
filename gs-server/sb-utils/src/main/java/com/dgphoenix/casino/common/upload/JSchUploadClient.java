package com.abs.casino.common.upload;

import com.abs.casino.common.exception.CommonException;
import com.jcraft.jsch.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by vladislav on 06/10/15.
 */
public class JSchUploadClient {
    private static final Logger LOG = LogManager.getLogger(JSchUploadClient.class);

    private final JSch jSch = new JSch();
    private final Lock sessionsLocker = new ReentrantLock();
    private final Map<String, Session> sessionsByHost = new ConcurrentHashMap<String, Session>();
    private final String username;
    private final String password;
    private final Set<String> hosts;
    private final int port;
    private volatile boolean initialized = true;

    public JSchUploadClient(String username, String password, Set<String> hosts, int port) {
        this.username = username;
        this.password = password;
        this.hosts = hosts;
        this.port = port;
    }

    private Iterable<Session> getSessions() throws JSchException {
        checkArgument(initialized, "Client is closed");

        if (sessionsByHost.size() < hosts.size()) {
            sessionsLocker.lock();
            try {
                for (String host : hosts) {
                    Session session = sessionsByHost.get(host);
                    if (session == null) {
                        createSessionForHost(host);
                    }
                }
            } finally {
                sessionsLocker.unlock();
            }
        }

        for (Entry<String, Session> sessionForHost : sessionsByHost.entrySet()) {
            Session session = sessionForHost.getValue();
            if (!session.isConnected() && initialized) {
                String host = sessionForHost.getKey();
                sessionsLocker.lock();
                try {
                    if (!session.isConnected() && initialized) {
                        createSessionForHost(host);
                    }
                } finally {
                    sessionsLocker.unlock();
                }
            }
        }

        return sessionsByHost.values();
    }

    private void createSessionForHost(String host) throws JSchException {
        LOG.debug("Create session for host:" + host);
        Session newSession = null;
        try {
            newSession = jSch.getSession(username, host, port);
            newSession.setPassword(password);
            newSession.setConfig("StrictHostKeyChecking", "no");
            newSession.connect();
            sessionsByHost.put(host, newSession);
        } catch (JSchException e) {
            LOG.error("Cannot create new session for host " + host, e);
            if (newSession != null) {
                newSession.disconnect();
            }
            throw e;
        }
    }

    public void sendFile(ByteArrayInputStream fileData, String remoteFilePath) throws JSchException, SftpException {
        for (Session session : getSessions()) {
            ChannelSftp channelSftp = null;
            try {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                channelSftp.put(fileData, remoteFilePath);
            } catch (JSchException e) {
                session.disconnect();
                throw e;
            } finally {
                if (channelSftp != null) {
                    channelSftp.disconnect();
                }
                fileData.reset();
            }
        }
    }

    public void addIdentity(String pathRsaKey) throws JSchException {
        jSch.addIdentity(pathRsaKey);
    }

    public void copyRemoteFile(String sourceFilePath, String targetFileFolderPath, String targetFilePath)
            throws JSchException, CommonException {
        for (Session session : getSessions()) {
            ChannelExec execChannel = null;
            try {
                execChannel = (ChannelExec) session.openChannel("exec");
                execChannel.setCommand("mkdir " + targetFileFolderPath + "; cp " + sourceFilePath + " " + targetFilePath);
                execChannel.connect();
                while (execChannel.isConnected()) {
                    Thread.sleep(20);
                }
            } catch (JSchException e) {
                session.disconnect();
                throw e;
            } catch (InterruptedException e) {
                LOG.warn("Something went wrong", e);
            } finally {
                if (execChannel != null) {
                    execChannel.disconnect();
                }
            }

            int status = execChannel.getExitStatus();
            if (status != 0) {
                throw new CommonException("Copy failed, source file path = " + sourceFilePath +
                        ", targetFilePath = " + targetFilePath);
            }
        }
    }

    public void createDirectory(String directoryPath) throws CommonException {
        try {
            for (Session session : getSessions()) {
                ChannelExec execChannel = null;
                try {
                    execChannel = (ChannelExec) session.openChannel("exec");
                    execChannel.setCommand("mkdir -p " + directoryPath);
                    InputStream errStream = execChannel.getErrStream();
                    execChannel.connect();
                    while (!execChannel.isClosed()) {
                        Thread.sleep(20);
                    }
                    if (execChannel.getExitStatus() != 0) {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errStream));
                        StringBuilder errorBuilder = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorBuilder.append(line);
                        }
                        throw new CommonException("Error during creating directory " + directoryPath +
                                ", error: " + errorBuilder.toString());
                    }
                } catch(JSchException e) {
                    session.disconnect();
                    throw e;
                } finally {
                    if (execChannel != null) {
                        execChannel.disconnect();
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof JSchException || e instanceof IOException || e instanceof InterruptedException) {
                throw new CommonException(e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public String readFile(String remoteFilePath) throws JSchException, SftpException {
        String result = null;
        for (Session session : getSessions()) {
            ChannelSftp channelSftp = null;
            try {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                result = handleStream(channelSftp.get(remoteFilePath));
            } catch (JSchException e) {
                session.disconnect();
                throw e;
            } finally {
                if (channelSftp != null) {
                    channelSftp.disconnect();
                }
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public void readFileByLine(String remoteFilePath, FileLineCallback callback) throws JSchException, SftpException {
        boolean result;
        for (Session session : getSessions()) {
            ChannelSftp channelSftp = null;
            try {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                result = handleStreamByLine(channelSftp.get(remoteFilePath), callback);
            } catch (JSchException e) {
                session.disconnect();
                throw e;
            } finally {
                if (channelSftp != null) {
                    channelSftp.disconnect();
                }
            }
            if (result) {
                break;
            }
        }
    }

    private String handleStream(InputStream is) {
        String result = null;
        if (is != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
                result = sb.toString();
            } catch (IOException e) {
                LOG.error("Error while reading", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.error("Error while closing reader", e);
                    }
                }
            }
        }
        return result;
    }

    private boolean handleStreamByLine(InputStream is, FileLineCallback callback) {
        if (is != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String line;
                while ((line = reader.readLine()) != null) {
                    callback.handle(line);
                }
                return true;
            } catch (IOException e) {
                LOG.error("Error while reading file", e);
            } catch (CommonException e) {
                LOG.error("Error while handle file line", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.error("Error while closing reader", e);
                    }
                }
            }
        }
        return false;
    }

    public void closeClient() {
        sessionsLocker.lock();
        try {
            initialized = false;
            closeAllSessions();
        } finally {
            sessionsLocker.unlock();
        }
    }

    private void closeAllSessions() {
        for (Session session : sessionsByHost.values()) {
            session.disconnect();
        }
    }
}

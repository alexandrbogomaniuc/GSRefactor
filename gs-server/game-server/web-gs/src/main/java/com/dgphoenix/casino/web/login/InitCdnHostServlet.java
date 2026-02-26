package com.abs.casino.web.login;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraHostCdnPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by inter on 18.08.15.
 */
public class InitCdnHostServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(InitCdnHostServlet.class);
    private static final String BANK_ID_PARAM = "bankId";

    private final CassandraHostCdnPersister hostCdnPersister;

    public InitCdnHostServlet() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        hostCdnPersister = persistenceManager.getPersister(CassandraHostCdnPersister.class);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            long bankId = Long.parseLong(request.getParameter(BANK_ID_PARAM));
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                return;
            }
            Map<String, String> cdnMap = bankInfo.getCdnUrlsMap();

            String ip = request.getRemoteAddr();

            Map<String, String[]> parameters = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue()[0];
                if ((!BANK_ID_PARAM.equals(key) && cdnMap.containsValue(key)) || "DISABLED".equals(key)) {
                    hostCdnPersister.persist(ip, key, Integer.parseInt(value));
                }
            }

            response.getWriter().write("OK");
        } catch (Exception ex) {
            response.sendError(500);
            LOG.warn("Can not save cdnHost param ", ex);
        }
    }
}

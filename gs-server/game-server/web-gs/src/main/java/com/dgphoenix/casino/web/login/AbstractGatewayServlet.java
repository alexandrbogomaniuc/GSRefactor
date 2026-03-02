package com.abs.casino.web.login;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.exception.BadArgumentException;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.XMLUtils;
import com.abs.casino.common.web.AbstractLobbyRequest;
import com.abs.casino.common.web.BasicGameServerResponse;
import com.abs.casino.common.web.CommonStatus;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.abs.casino.common.web.login.LoginCommonConstants.*;

/**
 * User: plastical
 * Date: 24.02.2010
 */
public abstract class AbstractGatewayServlet<REQ extends AbstractLobbyRequest, RESP extends BasicGameServerResponse> extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(AbstractGatewayServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        final RESP serverResponse = newGameServerResponse();
        try {
            final REQ lobbyRequest;
            try {
                lobbyRequest = getLobbyRequest(req);
                LOG.debug(this.getClass().getSimpleName() + "::doPost lobby request:" + lobbyRequest);
            } catch (BadArgumentException e) {
                LOG.error(this.getClass().getSimpleName() + "::doPost error:", e);
                writeErrorResponse(writer, serverResponse, e.getMessage());
                return;
            }

            validatePrimaryRequetsParams(req, lobbyRequest, serverResponse);
            if (!VALIDATION_STATUS_SUCCESS.equals(serverResponse.getStatus())) {
                handleValidationError(req, writer, lobbyRequest, serverResponse);
                return;
            }

            validateRequestParams(req, lobbyRequest, serverResponse);
            if (!VALIDATION_STATUS_SUCCESS.equals(serverResponse.getStatus())) {
                handleValidationError(req, writer, lobbyRequest, serverResponse);
                return;
            }

            try {
                doProcess(req, resp, writer, lobbyRequest, serverResponse);
            } catch (Throwable e) {
                logError("doProcess error", "general error", e);
                writeErrorResponse(writer, serverResponse, "login error", "general login error",
                        "error.common.internalError", true);
                return;
            }
        } catch (Throwable e) {
            logError("doPost", "general error", e);
            serverResponse.setStatus(CommonStatus.FATAL_ERROR);
            serverResponse.setBundleMapping("error.common.genericError");
            writeResponse(writer, serverResponse, true);
        }
    }

    protected void doProcess(HttpServletRequest request, HttpServletResponse response, PrintWriter writer,
                             REQ lobbyRequest, RESP serverResponse) throws Exception {
    }

    protected REQ getLobbyRequest(HttpServletRequest request) throws BadArgumentException {
        String requestXML = request.getParameter(PARAM_LOBBY_REQUEST);
        if (StringUtils.isTrimmedEmpty(requestXML)) {
            throw new BadArgumentException("lobby request is empty");
        }

        try {
            XStream parser = new XStream();
            REQ lobbyRequest = (REQ) parser.fromXML(requestXML);
            return lobbyRequest;
        } catch (Exception e) {
            LOG.error(this.getClass().getSimpleName() + "::doPost parsing error:", e);
            throw new BadArgumentException(e);
        }
    }

    protected void writeResponse(PrintWriter writer, RESP serverResponse, boolean flush) {
        String xmlResponse = XMLUtils.toCompactXML(serverResponse);
        writer.write(xmlResponse);
        if (flush) {
            writer.flush();
        }
    }

    protected void writeErrorResponse(PrintWriter writer, RESP serverResponse, String errorStatus,
                                      String errorDescription, String bundleMapping, boolean flush) {
        if (!StringUtils.isTrimmedEmpty(errorStatus)) {
            serverResponse.setStatus(errorStatus);
        }

        if (!StringUtils.isTrimmedEmpty(errorDescription)) {
            serverResponse.setDescription(errorDescription);
        }

        if (!StringUtils.isTrimmedEmpty(bundleMapping)) {
            serverResponse.setBundleMapping(bundleMapping);
        }

        writeResponse(writer, serverResponse, flush);
    }

    protected void writeErrorResponse(PrintWriter writer, RESP serverResponse) {
        writeErrorResponse(writer, serverResponse, null);
    }

    protected void writeErrorResponse(PrintWriter writer, RESP serverResponse, String errorStatus) {
        writeErrorResponse(writer, serverResponse, errorStatus, null);
    }

    protected void writeErrorResponse(PrintWriter writer, RESP serverResponse, String errorStatus,
                                      String errorDescription) {
        writeErrorResponse(writer, serverResponse, errorStatus, errorDescription, null, true);
    }

    protected void logInfo(String method, String message) {
        LOG.info(this.getClass().getSimpleName() + "::" + method + " " + message);
    }

    protected void logDebug(String method, String message) {
        LOG.debug(this.getClass().getSimpleName() + "::" + method + " " + message);
    }

    protected void logError(String method, String message, Throwable cause) {
        LOG.error(this.getClass().getSimpleName() + "::" + method + " " + message, cause);
    }

    protected void logError(String method, String message) {
        LOG.error(this.getClass().getSimpleName() + "::" + method + " " + message);
    }

    protected abstract RESP newGameServerResponse();

    protected void validatePrimaryRequetsParams(HttpServletRequest request, REQ lobbyRequest, RESP serverResponse) {
        long bankId = lobbyRequest.getBankId();
        if (!BankInfoCache.getInstance().isExist(bankId)) {
            LOG.error(this.getClass().getSimpleName() + "::validatePrimaryRequetsParams bankId:" + bankId +
                    " doesn't exist");
            setValidationErrorProperties(serverResponse, VALIDATION_STATUS_ERROR, "error.login.incorrectBank",
                    "bankId is not defined");

            return;
        }

        if (lobbyRequest.getClientType() == null) {
            LOG.error("SBLoginGatewayServlet::validateRequestParams clientType info is null");
            setValidationErrorProperties(serverResponse, VALIDATION_STATUS_ERROR, "error.login.incorrectClientType",
                    "invalid client type");
            return;
        }

        serverResponse.setStatus(VALIDATION_STATUS_SUCCESS);
    }

    protected abstract void validateRequestParams(HttpServletRequest request, REQ lobbyRequest, RESP serverResponse);

    protected void setValidationErrorProperties(BasicGameServerResponse serverResponse, String status, String bundle,
                                                String description) {
        serverResponse.setStatus(status);
        serverResponse.setBundleMapping(bundle);
        serverResponse.setDescription(description);
    }

    protected void handleValidationError(HttpServletRequest req, PrintWriter writer,
                                         REQ lobbyRequest,
                                         RESP serverResponse) {
        String bundleMapping = StringUtils.isTrimmedEmpty(serverResponse.getBundleMapping()) ?
                "error.login.generalValidationError" : serverResponse.getBundleMapping();
        writeErrorResponse(writer, serverResponse, "validation error", bundleMapping);
    }
}

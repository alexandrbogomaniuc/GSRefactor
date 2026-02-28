package com.dgphoenix.casino.common.web;

import com.abs.casino.common.exception.XmlWriterException;
import com.dgphoenix.casino.common.util.string.StringBuilderWriter;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * User: flsh
 * Date: 03.09.2019.
 */
public abstract class XmlApiBaseAction<T extends ActionForm> extends BaseAction<T> {
    static final String REQUEST_NODE_NAME = "REQUEST";
    static final String TIME_NODE_NAME = "TIME";
    static final String RESPONSE_NODE_NAME = "RESPONSE";
    static final String RESULT_NODE_NAME = "RESULT";
    static final String CODE_NODE_NAME = "CODE";
    static final String DF = "dd.MM.yyyy HH:mm:ss";

    protected abstract Logger getLogger();

    protected abstract String getRootTag();

    protected boolean isExcludedNode(String nodeName) {
        return false;
    }

    protected ActionForward printErrorResponse(HttpServletRequest request, HttpServletResponse response, int errorCode)
            throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        XmlWriter xw = new XmlWriter(sw, "UTF-8");
        try {
            xw.startDocument(getRootTag());

            xw.startNode(REQUEST_NODE_NAME);
            Map<String, String[]> parameters = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                String name = entry.getKey();
                String paramValue = entry.getValue()[0];
                String nodeName = name.trim().toUpperCase();
                if (!isExcludedNode(nodeName)) {
                    xw.node(nodeName, paramValue == null ? "" : paramValue);
                }
            }

            xw.endNode(REQUEST_NODE_NAME);

            xw.node(TIME_NODE_NAME, new SimpleDateFormat(DF).format(new Date()));
            xw.startNode(RESPONSE_NODE_NAME);
            xw.node(RESULT_NODE_NAME, "FAILED");
            xw.node(CODE_NODE_NAME, String.valueOf(errorCode));
            xw.endNode(RESPONSE_NODE_NAME);

            xw.endDocument(getRootTag());
        } catch (XmlWriterException e) {
            getLogger().error("XmlWriter error", e);
        }

        String resultString = sw.toString();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("printErrorResponse: " + resultString);
        }
        response.getOutputStream().println(resultString);
        return null;
    }

    protected ActionForward printSuccessResponse(HttpServletRequest request, HttpServletResponse response,
                                                 Map<String, String> result) throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        XmlWriter xw = new XmlWriter(sw, "UTF-8");
        try {
            xw.startDocument(getRootTag());

            xw.startNode(REQUEST_NODE_NAME);
            Map<String, String[]> parameters = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                String name = entry.getKey();
                String paramValue = entry.getValue()[0];
                String nodeName = name.trim().toUpperCase();
                if (!isExcludedNode(nodeName)) {
                    xw.node(nodeName, paramValue == null ? "" : paramValue);
                }
            }
            xw.endNode(REQUEST_NODE_NAME);

            xw.node(TIME_NODE_NAME, new SimpleDateFormat(DF).format(new Date()));
            xw.startNode(RESPONSE_NODE_NAME);
            xw.node(RESULT_NODE_NAME, "OK");
            for (Map.Entry<String, String> entry : result.entrySet()) {
                xw.node(entry.getKey().toUpperCase(), entry.getValue());
            }
            xw.endNode(RESPONSE_NODE_NAME);

            xw.endDocument(getRootTag());
        } catch (XmlWriterException e) {
            getLogger().error("XmlWriter error", e);
        }
        String resultString = sw.toString();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("printSuccessResponse: " + resultString);
        }
        response.getOutputStream().println(resultString);
        return null;
    }
}

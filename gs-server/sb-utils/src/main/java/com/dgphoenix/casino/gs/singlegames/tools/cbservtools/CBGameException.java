package com.abs.casino.gs.singlegames.tools.cbservtools;

import com.abs.casino.common.util.logkit.GameLog;
import com.abs.casino.common.exception.AbstractSendAlertException;
import org.apache.log4j.Logger;


public class CBGameException extends AbstractSendAlertException {
    private static final Logger LOG = Logger.getLogger(CBGameException.class);
    public static final int UNJ_CHANGED_ERROR = 10000;

    /**
     * This string will be logged in Servlet engine's log file
     */
    private String strLog;

    /**
     * This string is the error type
     */
    private String strResult;
    private boolean sendAlert = false;
    private int errorCode = 0;

    public CBGameException(String message) {
        super(message);
    }

    public CBGameException (Exception e) {
        super (e);
        strResult = "ERROR";
        strLog = "Internal error.";
        LOG.error(e.getMessage(), e);
    }

    public CBGameException () {
        super ("ERROR");
        strResult = "ERROR";
        strLog = "Internal error.";
    }

    /**
     * Constructor of generic exception for game servlets of CBCasino.
     *
     * @param strResult contains string which will be sent to client with
     * PARAMRESULT parameter.
     * @param strLog contains string which will be written in Servlet
     * Engine's log file.
     */
    public CBGameException (String strResult, String strLog) {
        super (strResult);
        this.strResult = strResult;
        this.strLog = strLog;
        writeLog ();
    }

    public CBGameException (String strResult, String strLog, boolean sendAlert) {
        this(strResult, strLog);
        this.sendAlert = sendAlert;
    }

    /**
     * @return string which will be written in Servlet
     * Engine's log file.
     */
    public String getLogStr () {
        return strLog;
    }

    /**
     * Function for logging exception message to logfile
     */
    public void writeLog () {
        GameLog.getInstance().error(" / " + strResult + " / " + strLog);
    }

    public String getStrResult () {
        return strResult;
    }

    @Override
    public boolean isSendAlert() {
        return sendAlert;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}

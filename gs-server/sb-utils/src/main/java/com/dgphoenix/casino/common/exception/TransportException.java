package com.dgphoenix.casino.common.exception;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 7:27:51 PM
 */
public class TransportException extends CommonException {
    private static final long serialVersionUID = -1042964830910599674L;
    private int statusCode = -1;
    private String reasonPhrase;
    private String messageBody;
    
    public TransportException() {
    }

    public TransportException(String message) {
        super(message);
    }

    public TransportException(Throwable cause) {
        super(cause);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TransportException");
        sb.append("[message=").append(getMessage());
        sb.append(", statusCode=").append(statusCode);
        sb.append(", reasonPhrase='").append(reasonPhrase).append('\'');
        sb.append(", messageBody='").append(messageBody).append('\'');
        sb.append(']');
        return sb.toString();
    }
}

package com.abs.casino.common.util.test.api;

import com.abs.casino.common.util.xml.IXmlRequestResult;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 30.11.15
 */
public class TestStepResult {

    private final String stepName;
    private ResultStatus status;
    private String message;
    private String details;
    private RequestLogEntry logEntry;
    private IXmlRequestResult response;
    private long requestDuration;
    private String expectedResponse;

    public TestStepResult(String stepName) {
        this.stepName = stepName;
    }

    public String getStepName() {
        return stepName;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public boolean isPassed() {
        return status == ResultStatus.PASSED;
    }

    public boolean isFailed() {
        return status == ResultStatus.FAILED;
    }

    public void setPassed() {
        if (status != ResultStatus.FAILED) {
            status = ResultStatus.PASSED;
        }
    }

    public void setFailed(String message, String details) {
        status = ResultStatus.FAILED;
        this.message = message;
        this.details = details;
    }

    public void setFailed(String message) {
        status = ResultStatus.FAILED;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public RequestLogEntry getLogEntry() {
        return logEntry;
    }

    public void setLogEntry(RequestLogEntry logEntry) {
        this.logEntry = logEntry;
    }

    public IXmlRequestResult getResponse() {
        return response;
    }

    public void setResponse(IXmlRequestResult response) {
        this.response = response;
    }

    public long getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(long requestDuration) {
        this.requestDuration = requestDuration;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public String getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(String expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestStepResult that = (TestStepResult) o;

        return stepName.equals(that.stepName);

    }

    @Override
    public int hashCode() {
        return stepName.hashCode();
    }

    @Override
    public String toString() {
        return "TestStepResult [" +
                "stepName='" + stepName + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                ", logEntry=" + logEntry +
                ", response=" + response +
                ", requestDuration=" + requestDuration +
                ']';
    }

    public String toLog() {
        return "---------------" + stepName.toUpperCase() + "---------------\n" +
                "Request to: " + logEntry.getUrl() + "\n" +
                "with parameters: " + logEntry.getRequest() + "\n" +
                (logEntry.getResponse() != null
                        ? "response: \n" + StringEscapeUtils.escapeHtml(logEntry.getResponse())
                        : "error message: " + message
                ) + "\n" +
                "duration: " + requestDuration + " ms" + "\n" +
                "---------------" + stepName.toUpperCase() + "---------------" + "\n";
    }
}

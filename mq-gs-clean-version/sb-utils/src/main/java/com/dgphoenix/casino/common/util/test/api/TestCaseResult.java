package com.dgphoenix.casino.common.util.test.api;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 20.10.15
 */
public class TestCaseResult {

    private static final String SEPARATOR = "\n\n";
    private final String caseName;
    private ResultStatus status;
    private final Set<TestStepResult> stepResults;
    private String message;
    private String details;
    private String validationDetails;

    public TestCaseResult(String caseName) {
        this.caseName = caseName;
        this.status = ResultStatus.UNKNOWN;
        this.stepResults = new LinkedHashSet<TestStepResult>();
    }

    public String getCaseName() {
        return caseName;
    }

    public void addStepResult(TestStepResult stepResult, boolean affectingCaseResult) {
        if (affectingCaseResult) {
            addStepResult(stepResult);
        } else {
            stepResults.add(stepResult);
        }
    }

    public void addStepResult(TestStepResult stepResult) {
        boolean statusCanBeChanged = status == ResultStatus.UNKNOWN || status == ResultStatus.PASSED;
        if (statusCanBeChanged) {
            boolean added = stepResults.add(stepResult);
            if (added) {
                status = stepResult.getStatus();
                message = stepResult.getMessage();
                details = stepResult.getDetails();
            }
        }
    }

    public Set<TestStepResult> getStepResults() {
        return stepResults;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setFailed(String reason) {
        setFailed(reason, null);
    }

    public void setFailed(String reason, String details) {
        boolean canFailed = status == ResultStatus.UNKNOWN || status == ResultStatus.PASSED;
        if (canFailed) {
            this.status = ResultStatus.FAILED;
            this.message = reason;
            this.details = details;
        }
    }

    public void setSkipped(String reason) {
        setSkipped(reason, null);
    }

    public void setSkipped(String reason, String details) {
        boolean canSkipped = status == ResultStatus.UNKNOWN;
        if (canSkipped) {
            this.status = ResultStatus.SKIPPED;
            this.message = reason;
            this.details = details;
        }
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public String getValidationDetails() {
        return validationDetails;
    }

    public void setValidationDetails(String validationDetails) {
        this.validationDetails = validationDetails;
    }

    public boolean isSuccess() {
        return status == ResultStatus.PASSED;
    }

    public boolean isFail() {
        return status == ResultStatus.FAILED;
    }

    public boolean isSkipped() {
        return status == ResultStatus.SKIPPED;
    }

    public TestStepResult getFailedStep() {
        return FluentIterable.from(stepResults)
                .firstMatch(new Predicate<TestStepResult>() {
                    @Override
                    public boolean apply(TestStepResult stepResult) {
                        return stepResult.isFailed();
                    }
                })
                .orNull();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCaseResult that = (TestCaseResult) o;

        return caseName.equals(that.caseName);
    }

    @Override
    public int hashCode() {
        return caseName.hashCode();
    }

    @Override
    public String toString() {
        return "TestCaseResult [" +
                "caseName='" + caseName + '\'' +
                ", status=" + status +
                ", stepResults=" + stepResults +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                ']';
    }

    public String toLog() {
        return caseName + ": " + SEPARATOR
                + StringUtils.join(
                FluentIterable.from(stepResults)
                        .filter(new Predicate<TestStepResult>() {
                            @Override
                            public boolean apply(TestStepResult stepResult) {
                                return stepResult.getLogEntry() != null;
                            }
                        })
                        .transform(new Function<TestStepResult, String>() {
                            @Override
                            public String apply(TestStepResult stepResult) {
                                return stepResult.toLog();
                            }
                        })
                        .toList(),
                SEPARATOR);
    }
}

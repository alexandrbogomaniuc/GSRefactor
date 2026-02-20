package com.dgphoenix.casino.common.util.test.api;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.dgphoenix.casino.common.util.test.api.ResultStatus.*;
import static com.google.common.collect.FluentIterable.from;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 20.10.15
 */
public class TestScenarioResult implements Serializable {

    private static final String SEPARATOR = "\n\n\n";
    private final String scenarioName;
    private final Set<TestCaseResult> caseResults;
    private final Predicate<TestCaseResult> passed = new Predicate<TestCaseResult>() {
        @Override
        public boolean apply(TestCaseResult caseResult) {
            return caseResult.isSuccess();
        }
    };
    private final Predicate<TestCaseResult> skipped = new Predicate<TestCaseResult>() {
        @Override
        public boolean apply(TestCaseResult caseResult) {
            return caseResult.isSkipped();
        }
    };
    private final Predicate<TestCaseResult> failed = new Predicate<TestCaseResult>() {
        @Override
        public boolean apply(TestCaseResult caseResult) {
            return caseResult.isFail();
        }
    };

    public TestScenarioResult(String scenarioName) {
        this.scenarioName = scenarioName;
        this.caseResults = new LinkedHashSet<TestCaseResult>();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void addCaseResult(TestCaseResult testCaseResult) {
        caseResults.add(testCaseResult);
    }

    public void addCaseResults(List<TestCaseResult> caseResults) {
        for (TestCaseResult caseResult : caseResults) {
            addCaseResult(caseResult);
        }
    }

    public int getTotalCount() {
        return caseResults.size();
    }

    public int getPassedCount() {
        return countResult(passed);
    }

    public int getFailedCount() {
        return countResult(failed);
    }

    public int getSkippedCount() {
        return countResult(skipped);
    }

    private int countResult(Predicate<TestCaseResult> predicate) {
        return from(caseResults).filter(predicate).size();
    }

    public Set<TestCaseResult> getCaseResults() {
        return caseResults;
    }

    public Set<TestCaseResult> getFailed() {
        return getCaseResults(failed);
    }

    public Set<TestCaseResult> getSkipped() {
        return getCaseResults(skipped);
    }

    private Set<TestCaseResult> getCaseResults(Predicate<TestCaseResult> predicate) {
        return from(caseResults).filter(predicate).toSet();
    }

    public ResultStatus getStatus() {
        if (getFailedCount() > 0) {
            return FAILED;
        }
        if (getPassedCount() > 0) {
            return PASSED;
        }
        if (getSkippedCount() > 0) {
            return SKIPPED;
        }
        return UNKNOWN;
    }

    public boolean isSuccess() {
        return getStatus() == PASSED;
    }

    public boolean isFail() {
        return getStatus() == FAILED;
    }

    @Override
    public String toString() {
        return "TestScenarioResult [" +
                "scenarioName='" + scenarioName + '\'' +
                ", status=" + getStatus() +
                ", caseResults=" + caseResults +
                ']';
    }

    public String toLog() {
        return scenarioName + " test scenario: " + getStatus() + SEPARATOR
                + StringUtils.join(
                from(caseResults)
                        .filter(new Predicate<TestCaseResult>() {
                            @Override
                            public boolean apply(TestCaseResult caseResult) {
                                return !caseResult.getStepResults().isEmpty();
                            }
                        })
                        .transform(new Function<TestCaseResult, String>() {
                            int caseNumber = 0;

                            @Override
                            public String apply(TestCaseResult caseResult) {
                                return ++caseNumber + ". " + caseResult.toLog();
                            }
                        })
                        .toList(),
                SEPARATOR);
    }
}

package com.mabl.integration.jenkins.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * mabl result from deployment event creation
 */

public class ExecutionResult implements ApiResult {
    public List<ExecutionSummary> executions;
    @SerializedName("event_status") public EventStatus eventStatus;

    public ExecutionResult(
            final List<ExecutionSummary> executions,
            final EventStatus eventStatus
    ) {
        this.executions = executions;
        this.eventStatus = eventStatus;
    }

    @SuppressWarnings("WeakerAccess")
    public static class ExecutionSummary {
        public final String status;
        @SerializedName("status_code") public final String statusCause;
        public final boolean success;
        @SerializedName("start_time") public final Long startTime;
        @SerializedName("stop_time") public final Long stopTime;
        public final PlanSummary plan;
        @SerializedName("plan_execution") public final PlanExecutionResult planExecution;
        public final List<JourneySummary> journeys;
        @SerializedName("journey_executions") public final List<JourneyExecutionResult> journeyExecutions;

        public ExecutionSummary(
                final String status,
                final String statusCause,
                final boolean success,
                final Long startTime,
                final Long stopTime,
                PlanSummary plan,
                final PlanExecutionResult planExecution,
                final List<JourneySummary> journeys,
                final List<JourneyExecutionResult> journeysExecutions
        ) {
            this.status = status;
            this.statusCause = statusCause;
            this.success = success;
            this.startTime = startTime;
            this.stopTime = stopTime;
            this.plan = plan;
            this.planExecution = planExecution;
            this.journeys = journeys;
            this.journeyExecutions = journeysExecutions;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class PlanSummary {
        public final String id;
        public final String name;

        public PlanSummary(
                final String id,
                final String name
        ) {
            this.id = id;
            this.name = name;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class JourneySummary {
        public final String id;
        public final String name;
        public final String href;
        @SerializedName("app_href") public final String appHref;

        public JourneySummary(
                final String id,
                final String name,
                final String href,
                final String appHref
        ) {
            this.id = id;
            this.name = name;
            this.href = href;
            this.appHref = appHref;
        }
    }

    public static class TestCaseID {
        @SerializedName("id") public final String caseID;

        public TestCaseID(final String caseID) {
            this.caseID = caseID;
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class JourneyExecutionResult {
        @SerializedName("journey_id") public final String id;
        public final String executionId;
        public final String href;
        @SerializedName("app_href") public final String appHref;
        public final String status;
        public final String statusCause;
        public final boolean success;
        @SerializedName("start_time") public final Long startTime;
        @SerializedName("stop_time") public final Long stopTime;
        @SerializedName("test_cases") public final List<TestCaseID> testCases;

        public JourneyExecutionResult(
                final String id,
                final String executionId,
                final String href,
                final String appHref,
                final String status,
                final String statusCause,
                final boolean success,
                final Long startTime,
                final Long stopTime,
                final List<TestCaseID> testCases
                ) {
            this.id = id;
            this.executionId = executionId;
            this.href = href;
            this.appHref = appHref;
            this.status = status;
            this.statusCause = statusCause;
            this.success = success;
            this.startTime = startTime;
            this.stopTime = stopTime;
            this.testCases = testCases;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class PlanExecutionResult {
        public final String id;
        @SerializedName("is_retry") public final boolean isRetry;

        public PlanExecutionResult(
                final String id,
                final boolean isRetry
        ) {
            this.id = id;
            this.isRetry = isRetry;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static final class EventStatus {
        public Boolean succeeded;
        @SerializedName("succeeded_first_attempt") public Boolean succeededFirstAttempt;
        @SerializedName("succeeded_with_retries") public Boolean succeededWithRetries;

        public EventStatus(
        ) {
        }

        public Boolean getSucceeded() {
            return succeeded;
        }

        @SerializedName("succeeded_first_attempt")
        public Boolean getSucceededFirstAttempt() {
            return succeededFirstAttempt;
        }

        @SerializedName("succeeded_with_retries")
        public Boolean getSucceededWithRetry() {
            return succeededWithRetries;
        }

        public void setSucceeded(Boolean succeeded) {
            this.succeeded = succeeded;
        }

        @SerializedName("succeeded_first_attempt")
        public void setSucceededFirstAttempt(Boolean succeededFirstAttempt) {
            this.succeededFirstAttempt = succeededFirstAttempt;
        }

        @SerializedName("succeeded_with_retries")
        public void setSucceededWithRetry(Boolean succeededWithRetries) {
            this.succeededWithRetries = succeededWithRetries;
        }
    }

}
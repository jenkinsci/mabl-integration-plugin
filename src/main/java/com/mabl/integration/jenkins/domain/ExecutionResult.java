package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.ImmutableCollection;

import java.util.List;

/**
 * mabl result from deployment event creation
 */

public class ExecutionResult implements ApiResult {
    public List<ExecutionSummary> executions;
    public EventStatus eventStatus;

    @JsonCreator
    public ExecutionResult(
            @JsonProperty("executions") final List<ExecutionSummary> executions,
            @JsonProperty("event_status") final EventStatus eventStatus
    ) {
        this.executions = executions;
        this.eventStatus = eventStatus;
    }

    @SuppressWarnings("WeakerAccess")
    public static class ExecutionSummary {
        public final String status;
        public final String statusCause;
        public final boolean success;
        public final Long startTime;
        public final Long stopTime;
        public final PlanSummary plan;
        public final PlanExecutionResult planExecution;
        public final List<JourneySummary> journeys;
        public final List<JourneyExecutionResult> journeyExecutions;

        @JsonCreator
        public ExecutionSummary(
                @JsonProperty("status") final String status,
                @JsonProperty("status_code") final String statusCause,
                @JsonProperty("success") final boolean success,
                @JsonProperty("start_time") final Long startTime,
                @JsonProperty("stop_time") final Long stopTime,
                @JsonProperty("plan") final PlanSummary plan,
                @JsonProperty("plan_execution") final PlanExecutionResult planExecution,
                @JsonProperty("journeys") final List<JourneySummary> journeys,
                @JsonProperty("journey_executions") final List<JourneyExecutionResult> journeysExecutions
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

        @JsonCreator
        public PlanSummary(
                @JsonProperty("id") final String id,
                @JsonProperty("name") final String name
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
        public final String appHref;

        @JsonCreator
        public JourneySummary(
                @JsonProperty("id") final String id,
                @JsonProperty("name") final String name,
                @JsonProperty("href") final String href,
                @JsonProperty("app_href") final String appHref
        ) {
            this.id = id;
            this.name = name;
            this.href = href;
            this.appHref = appHref;
        }
    }

    public static class TestCaseID {
        public final String caseID;

        @JsonCreator
        public TestCaseID(@JsonProperty("id") final String caseID) {
            this.caseID = caseID;
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class JourneyExecutionResult {
        public final String id;
        public final String executionId;
        public final String href;
        public final String appHref;
        public final String status;
        public final String statusCause;
        public final boolean success;
        public final Long startTime;
        public final Long stopTime;
        public final List<TestCaseID> testCases;

        @JsonCreator
        public JourneyExecutionResult(
                @JsonProperty("journey_id") final String id,
                @JsonProperty("executionId") final String executionId,
                @JsonProperty("href") final String href,
                @JsonProperty("app_href") final String appHref,
                @JsonProperty("status") final String status,
                @JsonProperty("statusCause") final String statusCause,
                @JsonProperty("success") final boolean success,
                @JsonProperty("start_time") final Long startTime,
                @JsonProperty("stop_time") final Long stopTime,
                @JsonProperty("test_cases") final List<TestCaseID> testCases
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
        public final boolean isRetry;

        @JsonCreator
        public PlanExecutionResult(
                @JsonProperty("id") final String id,
                @JsonProperty("is_retry") final boolean isRetry
        ) {
            this.id = id;
            this.isRetry = isRetry;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static final class EventStatus {
        public Boolean succeeded;
        public Boolean succeededFirstAttempt;

        public EventStatus(
        ) {
        }

        @JsonGetter("succeeded")
        public Boolean getSucceeded() {
            return succeeded;
        }

        @JsonGetter("succeeded_first_attempt")
        public Boolean getSucceededFirstAttempt() {
            return succeededFirstAttempt;
        }

        @JsonSetter("succeeded")
        public void setSucceeded(Boolean succeeded) {
            this.succeeded = succeeded;
        }

        @JsonSetter("succeeded_first_attempt")
        public void setSucceededFirstAttempt(Boolean succeededFirstAttempt) {
            this.succeededFirstAttempt = succeededFirstAttempt;
        }
    }

}
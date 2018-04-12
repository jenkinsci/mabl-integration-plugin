package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * mabl result from deployment event creation
 */

public class ExecutionResult {
    public List<ExecutionSummary> executions;

    @JsonCreator
    public ExecutionResult(
            @JsonProperty("executions") final List<ExecutionSummary> executions
    ) {
        this.executions = executions;
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
        public final String href;
        public final String appHref;

        @JsonCreator
        public JourneySummary(
                @JsonProperty("id") final String id,
                @JsonProperty("href") final String href,
                @JsonProperty("app_href") final String appHref
        ) {
            this.id = id;
            this.href = href;
            this.appHref = appHref;
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

        @JsonCreator
        public JourneyExecutionResult(
                @JsonProperty("journey_id") final String id,
                @JsonProperty("executionId") final String executionId,
                @JsonProperty("href") final String href,
                @JsonProperty("app_href") final String appHref,
                @JsonProperty("status") final String status,
                @JsonProperty("statusCause") final String statusCause,
                @JsonProperty("success") final boolean success
        ) {
            this.id = id;
            this.executionId = executionId;
            this.href = href;
            this.appHref = appHref;
            this.status = status;
            this.statusCause = statusCause;
            this.success = success;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class PlanExecutionResult {
        public final String id;

        @JsonCreator
        public PlanExecutionResult(
                @JsonProperty("id") final String id
        ) {
            this.id = id;
        }
    }
}
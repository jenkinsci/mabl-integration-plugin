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

        @JsonCreator
        public ExecutionSummary(
                @JsonProperty("status") final String status,
                @JsonProperty("status_code") final String statusCause,
                @JsonProperty("success") final boolean success,
                @JsonProperty("start_time") final Long startTime,
                @JsonProperty("stop_time") final Long stopTime,
                @JsonProperty("plan") final PlanSummary plan,
                @JsonProperty("plan_execution") final PlanExecutionResult planExecution
        ) {
            this.status = status;
            this.statusCause = statusCause;
            this.success = success;
            this.startTime = startTime;
            this.stopTime = stopTime;
            this.plan = plan;
            this.planExecution = planExecution;
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
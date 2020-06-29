package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * mabl result from deployment event creation
 */

public class CreateDeploymentResult implements ApiResult {
    public String id;
    public String workspaceId;

    @JsonCreator
    public CreateDeploymentResult(
            @JsonProperty("id") final String id,
            @JsonProperty("workspace_id") final String workspaceId
    ) {
        this.id = id;
        this.workspaceId = workspaceId;
    }
}
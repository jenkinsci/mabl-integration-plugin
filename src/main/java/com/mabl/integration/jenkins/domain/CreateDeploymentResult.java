package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * mabl result from deployment event creation
 */

public class CreateDeploymentResult implements ApiResult {
    public String id;
    public String workspaceId;
    public String mablBranch;

    @JsonCreator
    public CreateDeploymentResult(
            @JsonProperty("id") final String id,
            @JsonProperty("workspace_id") final String workspaceId
    ) {
        this.id = id;
        this.workspaceId = workspaceId;
    }

    @JsonSetter("source_control_tag")
    public void setMablBranch(final String mablBranch) {
        this.mablBranch = mablBranch;
    }
}
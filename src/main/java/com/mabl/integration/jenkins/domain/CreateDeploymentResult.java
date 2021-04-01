package com.mabl.integration.jenkins.domain;

import com.google.gson.annotations.SerializedName;

/**
 * mabl result from deployment event creation
 */

public class CreateDeploymentResult implements ApiResult {
    public String id;
    @SerializedName("workspace_id") public String workspaceId;
    @SerializedName("source_control_tag") public String mablBranch;

    public CreateDeploymentResult(
            final String id,
            final String workspaceId
    ) {
        this.id = id;
        this.workspaceId = workspaceId;
    }

    @SerializedName("source_control_tag")
    public void setMablBranch(final String mablBranch) {
        this.mablBranch = mablBranch;
    }
}
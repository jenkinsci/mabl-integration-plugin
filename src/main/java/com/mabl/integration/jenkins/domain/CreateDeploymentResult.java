package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * mabl result from deployment event creation
 */

public class CreateDeploymentResult implements ApiResult {
    public String id;

    @JsonCreator
    public CreateDeploymentResult(
            @JsonProperty("id") String id
    ) {
        this.id = id;
    }
}
package com.mabl.integration.jenkins.domain;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;

    public CreateDeploymentPayload(String environmentId, String applicationId) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
    }
}
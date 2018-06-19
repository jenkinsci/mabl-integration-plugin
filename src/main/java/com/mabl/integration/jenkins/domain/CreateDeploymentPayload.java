package com.mabl.integration.jenkins.domain;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.properties = properties;
    }
}
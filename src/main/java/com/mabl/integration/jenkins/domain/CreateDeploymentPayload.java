package com.mabl.integration.jenkins.domain;

import java.util.List;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final List<String> planLabels;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, List<String> planLabels, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.planLabels = planLabels;
        this.properties = properties;
    }
}
package com.mabl.integration.jenkins.domain;

import java.util.Set;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final Set<String> planLabels;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, Set<String> planLabels, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.planLabels = planLabels == null || planLabels.isEmpty() ? null : planLabels;
        this.properties = properties;
    }
}

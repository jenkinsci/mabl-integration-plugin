package com.mabl.integration.jenkins.domain;

import java.util.List;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final List<String> labels;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, List<String> labels, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.labels = labels;
        this.properties = properties;
    }
}
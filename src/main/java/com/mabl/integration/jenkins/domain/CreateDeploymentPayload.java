package com.mabl.integration.jenkins.domain;

import java.util.Set;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final Set<String> planLabels;
    final String sourceControlTag;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, Set<String> planLabels, String mablBranch, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.planLabels = planLabels == null || planLabels.isEmpty() ? null : planLabels;
        this.sourceControlTag = mablBranch == null || mablBranch.length() == 0 ? null : mablBranch;
        this.properties = properties;
    }
}

package com.mabl.integration.jenkins.domain;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final Collection<String> planLabels;
    final String sourceControlTag;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, String planLabels, String mablBranch, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.planLabels = planLabels == null || planLabels.length() == 0 ? null :
                Arrays.asList(StringUtils.commaDelimitedListToStringArray(planLabels));
        this.sourceControlTag = mablBranch == null || mablBranch.length() == 0 ? null : mablBranch;
        this.properties = properties;
    }
}

package com.mabl.integration.jenkins.domain;

import java.util.Arrays;
import java.util.Collection;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.apache.commons.lang.StringUtils.isBlank;

public class CreateDeploymentPayload {
    final String environmentId;
    final String applicationId;
    final Collection<String> planLabels;
    final String sourceControlTag;
    final CreateDeploymentProperties properties;

    public CreateDeploymentPayload(String environmentId, String applicationId, String planLabels, String mablBranch, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.planLabels = isBlank(planLabels) ? null :
                Arrays.asList(commaDelimitedListToStringArray(planLabels));
        this.sourceControlTag = isBlank(mablBranch) ? null : mablBranch;
        this.properties = properties == null ? null : properties.copy();
    }
}

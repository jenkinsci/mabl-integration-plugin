package com.mabl.integration.jenkins.domain;

import com.google.gson.annotations.SerializedName;

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
    @SerializedName("plan_overrides")
    final CreateDeploymentProperties.PlanOverride plan_overrides;

    public CreateDeploymentPayload(String environmentId, String applicationId, String planLabels, String mablBranch, CreateDeploymentProperties properties) {
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.planLabels = isBlank(planLabels) ? null :
                Arrays.asList(commaDelimitedListToStringArray(planLabels));
        this.sourceControlTag = isBlank(mablBranch) ? null : mablBranch;

        if (properties != null && properties.getPlan_overrides() != null) {
            this.plan_overrides = properties.getPlan_overrides();

            CreateDeploymentProperties propsCopy = properties.copy();
            propsCopy.setPlan_overrides(null);
            this.properties = propsCopy;
        }
        else{
            this.plan_overrides = null;
            this.properties = properties == null ? null : properties.copy();
        }
    }
}

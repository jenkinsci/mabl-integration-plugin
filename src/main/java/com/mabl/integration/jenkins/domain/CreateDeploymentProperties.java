package com.mabl.integration.jenkins.domain;

import java.util.Objects;

public class CreateDeploymentProperties {
    private String deploymentOrigin;
    private String repositoryBranchName;
    private String repositoryRevisionNumber;
    private String repositoryUrl;
    private String repositoryName;
    private String repositoryPreviousRevisionNumber;
    private String repositoryCommitUsername;
    private String buildPlanId;
    private String buildPlanName;
    private String buildPlanNumber;
    private String buildPlanResultUrl;
    private PlanOverride planOverrides;

    public String getDeploymentOrigin() {
        return deploymentOrigin;
    }

    public String getRepositoryBranchName() {
        return repositoryBranchName;
    }

    public String getRepositoryRevisionNumber() {
        return repositoryRevisionNumber;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositoryPreviousRevisionNumber() {
        return repositoryPreviousRevisionNumber;
    }

    public String getRepositoryCommitUsername() {
        return repositoryCommitUsername;
    }

    public String getBuildPlanId() {
        return buildPlanId;
    }

    public String getBuildPlanName() {
        return buildPlanName;
    }

    public String getBuildPlanNumber() {
        return buildPlanNumber;
    }

    public String getBuildPlanResultUrl() {
        return buildPlanResultUrl;
    }

    public PlanOverride getPlanOverrides() {return planOverrides; }   // this it to override both api and web URL

    public void setDeploymentOrigin(String plugin) {
        this.deploymentOrigin = plugin;
    }

    public void setRepositoryBranchName(String repositoryBranchName) {
        this.repositoryBranchName = repositoryBranchName;
    }

    public void setRepositoryRevisionNumber(String repositoryRevisionNumber) {
        this.repositoryRevisionNumber = repositoryRevisionNumber;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public void setRepositoryPreviousRevisionNumber(String repositoryPreviousRevisionNumber) {
        this.repositoryPreviousRevisionNumber = repositoryPreviousRevisionNumber;
    }

    public void setRepositoryCommitUsername(String repositoryCommitUsername) {
        this.repositoryCommitUsername = repositoryCommitUsername;
    }

    public void setBuildPlanId(String buildPlanId) {
        this.buildPlanId = buildPlanId;
    }

    public void setBuildPlanName(String buildPlanName) {
        this.buildPlanName = buildPlanName;
    }

    public void setBuildPlanNumber(String buildPlanNumber) {
        this.buildPlanNumber = buildPlanNumber;
    }

    public void setBuildPlanResultUrl(String buildPlanResultUrl) {
        this.buildPlanResultUrl = buildPlanResultUrl;
    }

    public void setPlanOverrides(PlanOverride planOverrides) { this.planOverrides = planOverrides; }

    public CreateDeploymentProperties copy() {
        CreateDeploymentProperties copy = new CreateDeploymentProperties();
        copy.setDeploymentOrigin(deploymentOrigin);
        copy.setRepositoryBranchName(repositoryBranchName);
        copy.setRepositoryRevisionNumber(repositoryRevisionNumber);
        copy.setRepositoryUrl(repositoryUrl);
        copy.setRepositoryName(repositoryName);
        copy.setRepositoryPreviousRevisionNumber(repositoryPreviousRevisionNumber);
        copy.setRepositoryCommitUsername(repositoryCommitUsername);
        copy.setBuildPlanId(buildPlanId);
        copy.setBuildPlanId(buildPlanName);
        copy.setBuildPlanNumber(buildPlanNumber);
        copy.setBuildPlanResultUrl(buildPlanResultUrl);

        // To check if there is URL changes in plan and if does then update it
        if(planOverrides != null){
            PlanOverride overrideCopy = new PlanOverride();
            overrideCopy.setWebURL(planOverrides.getWebURL());
            overrideCopy.setApiUrl(planOverrides.getApiURL());
            copy.setPlanOverrides(overrideCopy);
        }
        return copy;
    }


    //InnerClass to change the new plans according to webURL Change and ApiURl Change
    public static class PlanOverride{
        private String webURL;
        private String apiURL;

        public void setWebURL(String webURL) {
            this.webURL = webURL;
        }

        public void setApiUrl(String apiURL) {
            this.apiURL = apiURL;
        }

        public String getWebURL() {
            return webURL;
        }
        public String getApiURL() {
            return apiURL;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;

            PlanOverride other = (PlanOverride) obj;
            return  Objects.equals(webURL, other.webURL) &&
                    Objects.equals(apiURL, other.apiURL);
        }

        @Override
        public int hashCode(){
            return Objects.hash(webURL, apiURL);
        }
    }
}


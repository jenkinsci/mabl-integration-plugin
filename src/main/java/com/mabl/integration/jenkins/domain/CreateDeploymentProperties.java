package com.mabl.integration.jenkins.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.List;
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
    private String revision;

    @SerializedName("plan_overrides")
    private PlanOverride plan_overrides;

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

    public PlanOverride getPlan_overrides() {return plan_overrides; }   // this it to override both api and web URL

    public String getRevision() {return revision;}

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

    public void setPlan_overrides(PlanOverride plan_overrides) { this.plan_overrides = plan_overrides; }

    public void setRevision(String revision) {
        if(revision != null && !revision.isEmpty()) {
            this.revision = revision;
        }
    }

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
        copy.setBuildPlanName(buildPlanName);
        copy.setBuildPlanNumber(buildPlanNumber);
        copy.setBuildPlanResultUrl(buildPlanResultUrl);

        /**
         * Checks for URL changes and browser type configurations within the plan. If any changes are detected,
         * the URLs and browser types in the plan are updated accordingly.
         */
        if(plan_overrides != null){
            boolean hasWebUrl = plan_overrides.getWeb_url() != null && !plan_overrides.getWeb_url().isEmpty();
            boolean hasApiUrl = plan_overrides.getApi_url() != null && !plan_overrides.getApi_url().isEmpty();
            boolean hasBrowserTypes = plan_overrides.getBrowser_types() != null && !plan_overrides.getBrowser_types().isEmpty();

            if(hasWebUrl || hasApiUrl || hasBrowserTypes){
                PlanOverride overrideCopy = new PlanOverride();
                if(hasWebUrl){
                    overrideCopy.setWeb_url(plan_overrides.getWeb_url());
                }
                if(hasApiUrl){
                    overrideCopy.setApi_url(plan_overrides.getApi_url());
                }
                if(hasBrowserTypes){
                    overrideCopy.setBrowser_types(plan_overrides.getBrowser_types());
                }
                copy.setPlan_overrides(overrideCopy);
            }
        }
        return copy;
    }


    /**
     * Inner class responsible for updating the application's plans
     * based on changes to the web URL, API URL, and browser type configurations.
     */
    public static class PlanOverride{
        @SerializedName("web_url")
        private String web_url;
        @SerializedName("api_url")
        private String api_url;
        @SerializedName("browser_types")
        private List<String> browser_types;

        public void setWeb_url(String web_url) {
            this.web_url = web_url;
        }

        public void setApi_url(String api_url) {
            this.api_url = api_url;
        }

        public void setBrowser_types(List<String> browser_types) {
            this.browser_types = browser_types;
        }

        public String getWeb_url() {
            return web_url;
        }
        public String getApi_url() {
            return api_url;
        }
        public List<String> getBrowser_types() {
            return browser_types;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;

            PlanOverride other = (PlanOverride) obj;
            return  Objects.equals(web_url, other.web_url) &&
                    Objects.equals(api_url, other.api_url) &&
                    Objects.equals(browser_types, other.browser_types);
        }

        @Override
        public int hashCode(){
            return Objects.hash(web_url, api_url, browser_types);
        }
    }
}

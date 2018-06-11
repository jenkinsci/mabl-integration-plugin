package com.mabl.integration.jenkins.domain;

public class CreateDeploymentProperties {
    public String deploymentOrigin;
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


}


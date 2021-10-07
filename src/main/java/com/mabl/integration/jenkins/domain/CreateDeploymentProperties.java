package com.mabl.integration.jenkins.domain;

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
        return copy;
    }
}


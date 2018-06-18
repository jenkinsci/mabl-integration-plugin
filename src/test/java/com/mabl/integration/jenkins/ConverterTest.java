package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import hudson.EnvVars;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConverterTest {

    @Test
    public void testJustBuildVars() {
        assertSuccessfulConversion(MablTestConstants.JUST_BUILD_PROPS, Converter.convert(new EnvVars().overrideAll(MablTestConstants.BUILD_VARS), System.out));
    }

    @Test
    public void testGitBuildVars() {
        assertSuccessfulConversion(MablTestConstants.GIT_BUILD_PROPS_SSH, Converter.convert(new EnvVars().overrideAll(MablTestConstants.GIT_VARS), System.out));
    }

    @Test
    public void testGitBuildVarsHttpsUrl() {
        EnvVars envVars = new EnvVars().overrideAll(MablTestConstants.GIT_VARS);
        envVars.override("GIT_URL", "https://github.com/fakeOrg/mabl-integration-plugin.git");
        assertSuccessfulConversion(MablTestConstants.GIT_BUILD_PROPS_HTTPS, Converter.convert(envVars, System.out));
    }

    @Test
    public void testSvnBuildVars() {
        assertSuccessfulConversion(MablTestConstants.SVN_BUILD_PROPS, Converter.convert(new EnvVars().overrideAll(MablTestConstants.SVN_VARS), System.out));
    }

    private void assertSuccessfulConversion(CreateDeploymentProperties expected, CreateDeploymentProperties actual){
        assertEquals(expected.getDeploymentOrigin(), expected.getDeploymentOrigin());
        assertEquals(expected.getBuildPlanId(), actual.getBuildPlanId());
        assertEquals(expected.getBuildPlanName(), actual.getBuildPlanName());
        assertEquals(expected.getBuildPlanNumber(), actual.getBuildPlanNumber());
        assertEquals(expected.getBuildPlanResultUrl(), actual.getBuildPlanResultUrl());
        assertEquals(expected.getRepositoryUrl(), actual.getRepositoryUrl());
        assertEquals(expected.getRepositoryName(), actual.getRepositoryName());
        assertEquals(expected.getRepositoryBranchName(), actual.getRepositoryBranchName());
        assertEquals(expected.getRepositoryRevisionNumber(), actual.getRepositoryRevisionNumber());
        assertEquals(expected.getRepositoryCommitUsername(), expected.getRepositoryCommitUsername());
        assertEquals(expected.getRepositoryPreviousRevisionNumber(), expected.getRepositoryPreviousRevisionNumber());
    }
}

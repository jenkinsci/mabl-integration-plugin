package com.mabl.integration.jenkins.validation;

import com.mabl.integration.jenkins.MablRestApiClient;
import com.mabl.integration.jenkins.MablStepBuilder;
import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.domain.GetLabelsResult;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.MockedStatic;

import java.io.IOException;

import static com.mabl.integration.jenkins.MablStepConstants.FORM_API_KEY_LABEL;
import static com.mabl.integration.jenkins.MablStepConstants.FORM_APPLICATION_ID_LABEL;
import static com.mabl.integration.jenkins.MablStepConstants.FORM_ENVIRONMENT_ID_LABEL;
import static com.mabl.integration.jenkins.validation.MablStepBuilderValidator.validateForm;
import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit test runner
 */
public class MablBuildValidatorTest {
    FreeStyleProject freeStyleProject;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setupTest() {
        freeStyleProject = jenkinsRule.jenkins.getItemByFullName("my-freestyle", FreeStyleProject.class);
    }

    @Test
    public void validateGoodAllFieldsForm() {
        try (MockedStatic<MablStepBuilder> mocked = mockStatic(MablStepBuilder.class)) {
            mockNoopRestApiClient(mocked);

            final FormValidation actual = validateForm(
                    "sample-key-id",
                    "sample-environment-id",
                    "sample-application-id",
                     freeStyleProject
            );

            assertEquals(actual.kind, OK);
        }
    }

    @Test
    public void validateGoodEnvironmentOnlyForm() {
        try (MockedStatic<MablStepBuilder> mocked = mockStatic(MablStepBuilder.class)) {
            mockNoopRestApiClient(mocked);

            final FormValidation actual = validateForm(
                    "sample-key-id",
                    "sample-environment-id",
                    null,
                     freeStyleProject
            );

            assertEquals(actual.kind, OK);
        }

    }

    @Test
    public void validateGoodApplicationOnlyForm() {
        try (MockedStatic<MablStepBuilder> mocked = mockStatic(MablStepBuilder.class)) {
            mockNoopRestApiClient(mocked);
            final FormValidation actual = validateForm(
                    "sample-key-id",
                    null,
                    "sample-application-id",
                     freeStyleProject
            );

            assertEquals(actual.kind, OK);
        }
    }

    @Test
    public void validateBadNoEnvironmentOrApplicationForm() {
        final FormValidation actual = validateForm(
                "sample-key-id",
                null,
                null,
                 freeStyleProject
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoEnvironmentIdInWrongFieldApplicationForm() {
        final FormValidation actual = validateForm(
                "sample-key-id",
                "sample-a",
                null,
                 freeStyleProject
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoApplicationIdInWrongFieldApplicationForm() {
        final FormValidation actual = validateForm(
                "sample-key-id",
                null,
                "sample-e",
                 freeStyleProject
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoEnvironmentOrApplicationWhiteSpaceForm() {
        final FormValidation actual = validateForm(
                "sample-key-id",
                "  ",
                "\t",
                 freeStyleProject
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoRestApiKeyForm() {
        final FormValidation actual = validateForm(
                null,
                null,
                null,
                 freeStyleProject
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("rest API key label expected",
                actual.getMessage().contains(FORM_API_KEY_LABEL));
    }

    @Test
    public void validateBadRestApiKey() {
        final FormValidation actual = validateForm(
                "key:invalid-key",
                null,
                null,
                 freeStyleProject
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("rest API key label expected",
                actual.getMessage().contains(FORM_API_KEY_LABEL));
    }

    private void mockNoopRestApiClient(MockedStatic<MablStepBuilder> mocked) {
        mocked.when(() -> MablStepBuilder.createMablRestApiClient(
                "sample-key-id", false, freeStyleProject)).thenReturn(
                new MablRestApiClient() {
                    @Override
                    public CreateDeploymentResult createDeploymentEvent(String environmentId, String applicationId, String labels, String mablBranch, CreateDeploymentProperties properties, String revision) throws IOException {
                        return null;
                    }

                    @Override
                    public ExecutionResult getExecutionResults(String eventId) throws IOException {
                        return null;
                    }

                    @Override
                    public GetApiKeyResult getApiKeyResult() throws IOException {
                        return null;
                    }

                    @Override
                    public GetApplicationsResult getApplicationsResult(String organizationId) throws IOException {
                        return null;
                    }

                    @Override
                    public GetEnvironmentsResult getEnvironmentsResult(String organizationId) throws IOException {
                        return null;
                    }

                    @Override
                    public GetLabelsResult getLabelsResult(String organizationId) throws IOException {
                        return null;
                    }

                    @Override
                    public String getAppBaseUrl() {
                        return null;
                    }

                    @Override
                    public void checkConnection() throws IOException {
                    }

                    @Override
                    public void close() {
                    }
                }
        );
    }

}

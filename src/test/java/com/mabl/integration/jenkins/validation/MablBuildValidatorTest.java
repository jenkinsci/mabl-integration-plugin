package com.mabl.integration.jenkins.validation;

import com.mabl.integration.jenkins.MablRestApiClient;
import com.mabl.integration.jenkins.MablStepBuilder;
import com.mabl.integration.jenkins.MablStepConstants;
import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.domain.GetLabelsResult;
import hudson.util.FormValidation;
import org.junit.Test;
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

    @Test
    public void validateGoodAllFieldsForm() {

        try (MockedStatic<MablStepBuilder> mocked = mockStatic(MablStepBuilder.class)) {
            mockNoopRestApiClient(mocked);

            final FormValidation actual = validateForm(
                    "sample-key-id",
                    "sample-environment-id",
                    "sample-application-id"
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
                    null
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
                    "sample-application-id"
            );

            assertEquals(actual.kind, OK);
        }
    }

    @Test
    public void validateBadNoEnvironmentOrApplicationForm() {

        final FormValidation actual = validateForm(
                "sample-key-id",
                null,
                null
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
                null
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
                "sample-e"
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
                "\t"
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
                null
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("rest API key label expected",
                actual.getMessage().contains(FORM_API_KEY_LABEL));
    }

    private void mockNoopRestApiClient(MockedStatic<MablStepBuilder> mocked) {
        mocked.when(() -> MablStepBuilder.createMablRestApiClient(
                "sample-key-id", false,null, null, MablStepConstants.DEFAULT_MABL_API_BASE_URL, MablStepConstants.DEFAULT_MABL_APP_BASE_URL)).thenReturn(
                new MablRestApiClient() {
                    @Override
                    public CreateDeploymentResult createDeploymentEvent(String environmentId, String applicationId, String labels, String mablBranch, CreateDeploymentProperties properties) throws IOException {
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

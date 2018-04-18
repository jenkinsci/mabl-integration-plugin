package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.mabl.integration.jenkins.MablRestApiClientImpl.DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE;
import static com.mabl.integration.jenkins.MablRestApiClientImpl.REST_API_USERNAME_PLACEHOLDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for REST API calls
 */
public class MablRestApiClientTest extends AbstractWiremockTest {

    private static final String EXPECTED_DEPLOYMENT_EVENT_ID = "d1To4-GYeZ4nl-4Ag1JyQg-v";

    @Test
    public void createDeploymentAllParametersHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = "foo-env-e";
        final String applicationId = "foo-app-a";

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"environment_id\":\"foo-env-e\",\"application_id\":\"foo-app-a\"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId);
    }

    @Test
    public void createDeploymentOnlyEnvironmentHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = "foo-env-e";
        final String applicationId = null;

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"environment_id\":\"foo-env-e\"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId);
    }

    @Test
    public void createDeploymentOnlyApplicationHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = null;
        final String applicationId = "foo-app-a";

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"application_id\":\"foo-app-a\"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId);
    }

    private void assertSuccessfulCreateDeploymentRequest(
            final String restApiKey,
            final String environmentId,
            final String applicationId
    ) throws IOException, MablSystemError {

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, restApiKey);
            CreateDeploymentResult result = client.createDeploymentEvent(environmentId, applicationId);
            assertEquals(EXPECTED_DEPLOYMENT_EVENT_ID, result.id);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }

    @Test
    public void getExecutionResultHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String eventId = "fake-event-id";

        registerGetMapping(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE,eventId),
                ok(),
                MablTestConstants.EXECUTION_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey
        );
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, fakeRestApiKey);
            ExecutionResult result = client.getExecutionResults(eventId);
            assertEquals("succeeded", result.executions.get(0).status);
            assertTrue("expected success", result.executions.get(0).success);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }

    @Test
    public void getExecutionResultNotFoundTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String eventId = "fake-event-id";

        registerGetMapping(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE,eventId),
                notFound(),
                MablTestConstants.EXECUTION_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey
        );
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, fakeRestApiKey);
            ExecutionResult result = client.getExecutionResults(eventId);
            assertNull(result);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }
}

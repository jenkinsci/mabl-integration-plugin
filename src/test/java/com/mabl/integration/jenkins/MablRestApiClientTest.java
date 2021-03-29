package com.mabl.integration.jenkins;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import hudson.util.Secret;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.mabl.integration.jenkins.MablRestApiClientImpl.DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE;
import static com.mabl.integration.jenkins.MablRestApiClientImpl.REST_API_USERNAME_PLACEHOLDER;
import static org.apache.commons.httpclient.HttpStatus.SC_CREATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for REST API calls
 */
public class MablRestApiClientTest extends AbstractWiremockTest {

    private static final String EXPECTED_DEPLOYMENT_EVENT_ID = "d1To4-GYeZ4nl-4Ag1JyQg-v";
    private static final String EXPECTED_ORGANIZATION_ID = "K8NWhtPqOyFnyvJTvCP0uw-w";
    private static final String MABL_APP_BASE_URL = "https://app.mabl.com";
    private static final String fakeProperties = "{\"deployment_origin\":\""+MablStepConstants.PLUGIN_USER_AGENT+"\"}";

    @Test
    public void createDeploymentAllParametersHappyPathTest() throws IOException {

        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String environmentId = "foo-env-e";
        final String applicationId = "foo-app-a";
        final String labels = "foo-label";

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId,
                "{\"environment_id\":\"foo-env-e\",\"application_id\":\"foo-app-a\",\"plan_labels\":[\"foo-label\"],\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKeyId, environmentId, applicationId, labels);

        // in reality, you would use the same event ID, but it is eeasier to test with separate ones
        final String eventId1 = "fake-event-id-1";
        final String eventId2 = "fake-event-id-2";
        final String eventId3 = "fake-event-id-3";
        registerGetMappingWithFile(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE, eventId1),
                ok(),
                "scheduled.json",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );
        registerGetMappingWithFile(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE, eventId2),
                ok(),
                "retrying.json",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );
        registerGetMappingWithFile(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE, eventId3),
                ok(),
                "retry-complete.json",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );


        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            ExecutionResult result = client.getExecutionResults(eventId1);
            assertEquals("scheduled", result.executions.get(0).status);
            assertNull(result.eventStatus.succeeded);
            assertNull(result.eventStatus.succeededFirstAttempt);
            assertNull(result.eventStatus.succeededWithRetries);

            result = client.getExecutionResults(eventId2);
            assertEquals("failed", result.executions.get(0).status);
            assertEquals("scheduled", result.executions.get(1).status);
            assertNull(result.eventStatus.succeeded);
            assertNull(result.eventStatus.succeededFirstAttempt);
            assertNull(result.eventStatus.succeededWithRetries);

            result = client.getExecutionResults(eventId3);
            assertEquals("failed", result.executions.get(0).status);
            assertEquals("succeeded", result.executions.get(1).status);
            assertTrue(result.eventStatus.succeeded);
            assertFalse(result.eventStatus.succeededFirstAttempt);
            assertTrue(result.eventStatus.succeededWithRetries);

        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }

    @Test
    public void createDeploymentOnlyEnvironmentHappyPathTest() throws IOException {

        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String environmentId = "foo-env-e";
        final String applicationId = null;
        final String labels = null;

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId,
                "{\"environment_id\":\"foo-env-e\",\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKeyId, environmentId, applicationId, labels);
    }

    @Test
    public void createDeploymentOnlyApplicationHappyPathTest() throws IOException {

        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String environmentId = null;
        final String applicationId = "foo-app-a";
        final String labels = null;

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId,
                "{\"application_id\":\"foo-app-a\",\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKeyId, environmentId, applicationId, labels);
    }

    @Test
    public void createDeploymentOnlyLabelsHappyPathTest() throws IOException {

        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String environmentId = null;
        final String applicationId = null;
        final String labels = "foo-label";

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId,
                "{\"plan_labels\":[\"foo-label\"],\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKeyId, environmentId, applicationId, labels);
    }

    @Test
    public void createDeploymentRequestWithBranch() throws IOException {
        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String environmentId = "my-env-e";
        final String applicationId = "my-app-a";
        final String branch = "my-test-branch";

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId,
                String.format("{\"environment_id\":\"%s\",\"application_id\":\"%s\",\"source_control_tag\":\"%s\",\"properties\":%s}",
                        environmentId, applicationId, branch, fakeProperties)
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKeyId, environmentId, applicationId, null, branch);
    }

    @Test
    public void testCheckConnection_Ok() throws IOException {
        final String fakeRestApiKeyId = "aFakeRestApiKeyId";

        registerGetMapping(
                MablRestApiClientImpl.HEALTH_LIVE_ENDPOINT,
                new ResponseDefinitionBuilder().withStatus(200),
                "",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            client.checkConnection();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(expected = IOException.class)
    public void testCheckConnection_Unauthorized() throws IOException {
        final String fakeRestApiKeyId = "aFakeRestApiKeyId";

        registerGetMapping(
                MablRestApiClientImpl.HEALTH_LIVE_ENDPOINT,
                new ResponseDefinitionBuilder().withStatus(401),
                "",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            client.checkConnection();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(expected = IOException.class)
    public void testCheckConnection_InvalidKey() throws IOException {
        final String fakeRestApiKeyId = "aFakeRestApiKeyId";

        registerGetMapping(
                MablRestApiClientImpl.HEALTH_LIVE_ENDPOINT,
                new ResponseDefinitionBuilder().withStatus(403),
                "",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            client.checkConnection();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(expected = IOException.class)
    public void testCheckConnection_ServiceDown() throws IOException {
        final String fakeRestApiKeyId = "aFakeRestApiKeyId";

        registerGetMapping(
                MablRestApiClientImpl.HEALTH_LIVE_ENDPOINT,
                new ResponseDefinitionBuilder().withStatus(500),
                "",
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            client.checkConnection();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private void assertSuccessfulCreateDeploymentRequest(
            final String restApiKey,
            final String environmentId,
            final String applicationId,
            final String labels
    ) throws IOException {
        assertSuccessfulCreateDeploymentRequest(restApiKey, environmentId, applicationId, labels, null);
    }

    private void assertSuccessfulCreateDeploymentRequest(
            final String restApiKey,
            final String environmentId,
            final String applicationId,
            final String labels,
            final String mablBranch
    ) throws IOException {
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(restApiKey), MABL_APP_BASE_URL);
            CreateDeploymentProperties properties = new CreateDeploymentProperties();
            properties.setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
            CreateDeploymentResult result =
                    client.createDeploymentEvent(environmentId, applicationId, labels, mablBranch, properties);
            assertEquals(EXPECTED_DEPLOYMENT_EVENT_ID, result.id);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }

    @Test
    public void getExecutionResultHappyPathTest() throws IOException {

        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String eventId = "fake-event-id";

        registerGetMapping(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE,eventId),
                ok(),
                MablTestConstants.EXECUTION_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
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
    public void getExecutionResultNotFoundTest() throws IOException {

        final String fakeRestApiKeyId = "aFakeRestApiKeyId";
        final String eventId = "fake-event-id";

        registerGetMapping(
                String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE,eventId),
                notFound(),
                MablTestConstants.EXECUTION_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            ExecutionResult result = client.getExecutionResults(eventId);
            assertNull(result);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }

    @Test
    public void getApiKeyObjectFromApiKey() throws IOException {

        final String fakeRestApiKeyId = "fakeApiKeyValue";

        registerGetMapping(
                String.format(MablRestApiClientImpl.GET_ORGANIZATION_ENDPOINT_TEMPLATE,fakeRestApiKeyId),
                ok(),
                MablTestConstants.APIKEY_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKeyId
        );

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            GetApiKeyResult result = client.getApiKeyResult();
            assertEquals(EXPECTED_ORGANIZATION_ID, result.organization_id);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void getApplicationsReturnsTwoResults() throws IOException {

        final String fakeRestApiKeyId = "fakeApiKeyValue";
        final String organization_id = "fakeOrganizationId";

        WireMock.stubFor(get(urlPathEqualTo("/applications"))
            .withQueryParam("organization_id", equalTo(organization_id))
                .withBasicAuth(REST_API_USERNAME_PLACEHOLDER, fakeRestApiKeyId)
                .withHeader("user-agent", new EqualToPattern(MablStepConstants.PLUGIN_USER_AGENT))
            .willReturn(ok()
                .withHeader("Content-Type", "application/json")
                .withBody(MablTestConstants.APPLICATIONS_RESULT_JSON)
            ));

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            GetApplicationsResult result = client.getApplicationsResult(organization_id);
            assertEquals(2, result.applications.size());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void getEnvironmentsReturnsOneResult() throws IOException {

        final String fakeRestApiKeyId = "fakeApiKeyValue";
        final String organization_id = "fakeOrganizationId";

        WireMock.stubFor(get(urlPathEqualTo("/environments"))
                .withQueryParam("organization_id", equalTo(organization_id))
                .withBasicAuth(REST_API_USERNAME_PLACEHOLDER, fakeRestApiKeyId)
                .withHeader("user-agent", new EqualToPattern(MablStepConstants.PLUGIN_USER_AGENT))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MablTestConstants.ENVIRONMENTS_RESULT_JSON)
                ));
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, mockSecret(fakeRestApiKeyId), MABL_APP_BASE_URL);
            GetEnvironmentsResult result = client.getEnvironmentsResult(organization_id);
            assertEquals(1, result.environments.size());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }



    @Test(expected = MablSystemException.class)
    public void apiClientDoesntRetryOn503() throws IOException {
        registerPostCreateRetryMappings("/events/deployment", "503", 503, 1);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", "foo-label");
    }

    @Test
    public void apiClientRetriesOn501() throws IOException {
        registerPostCreateRetryMappings("/events/deployment", "501", 501, 1);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", "foo-label");
    }

    @Test
    public void apiClientRetriesOn501MaxtimesSuccess() throws IOException {
        registerPostCreateRetryMappings("/events/deployment", "501", 501, 5);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", "foo-label");
    }

    @Test(expected = MablSystemException.class)
    public void apiClientRetriesOn501OverMaxtimesFailure() throws IOException {
        registerPostCreateRetryMappings("/events/deployment", "501", 501, 6);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", "foo-label");
    }

    private void registerPostCreateRetryMappings(
            final String postUrl,
            final String scenario,
            final int status,
            final int numTimes
    ) {
        String whenState = Scenario.STARTED;
        for(int i=1;i<=numTimes;i++) {
            String willState = "Requested "+i+" Times";
            stubFor(post(urlEqualTo(postUrl))
                    .inScenario(scenario)
                    .whenScenarioStateIs(whenState)
                    .willSetStateTo(willState)
                    .willReturn(aResponse()
                            .withStatus(status)
                            .withHeader("Content-Type", "application/json")
                            .withBody(""+status))
            );

            whenState = willState;
        }

        stubFor(post(urlEqualTo(postUrl))
                .inScenario(scenario)
                .whenScenarioStateIs(whenState)
                .willReturn(aResponse()
                        .withStatus(SC_CREATED)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON))
        );
    }

    private Secret mockSecret(String value) {
        try {
            final Constructor<?> c = Class.forName("hudson.util.Secret").getDeclaredConstructor(String.class);
            c.setAccessible(true);
            return (Secret)c.newInstance(value);

        } catch (Exception e) {
            fail(e.getMessage());
        }
        return null;
    }
}

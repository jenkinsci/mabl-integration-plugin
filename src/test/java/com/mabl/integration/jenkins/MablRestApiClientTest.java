package com.mabl.integration.jenkins;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for REST API calls
 */
public class MablRestApiClientTest extends AbstractWiremockTest {

    private static final String EXPECTED_DEPLOYMENT_EVENT_ID = "d1To4-GYeZ4nl-4Ag1JyQg-v";
    private static final String EXPECTED_ORGANIZATION_ID = "K8NWhtPqOyFnyvJTvCP0uw-w";
    private static final String fakeProperties = "{\"deployment_origin\":\""+MablStepConstants.PLUGIN_USER_AGENT+"\"}";

    @Test
    public void createDeploymentAllParametersHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = "foo-env-e";
        final String applicationId = "foo-app-a";
        final Set<String> labels = Collections.singleton("foo-label");

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"environment_id\":\"foo-env-e\",\"application_id\":\"foo-app-a\",\"plan_labels\":[\"foo-label\"],\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId, labels);
    }

    @Test
    public void createDeploymentOnlyEnvironmentHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = "foo-env-e";
        final String applicationId = null;
        final Set<String> labels = null;

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"environment_id\":\"foo-env-e\",\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId, labels);
    }

    @Test
    public void createDeploymentOnlyApplicationHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = null;
        final String applicationId = "foo-app-a";
        final Set<String> labels = null;

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"application_id\":\"foo-app-a\",\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId, labels);
    }

    @Test
    public void createDeploymentOnlyLabelsHappyPathTest() throws IOException, MablSystemError {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = null;
        final String applicationId = null;
        final Set<String> labels = Collections.singleton("foo-label");

        registerPostMapping(
                MablRestApiClientImpl.DEPLOYMENT_TRIGGER_ENDPOINT,
                MablTestConstants.CREATE_DEPLOYMENT_EVENT_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey,
                "{\"plan_labels\":[\"foo-label\"],\"properties\":"+fakeProperties+"}"
        );

        assertSuccessfulCreateDeploymentRequest(fakeRestApiKey, environmentId, applicationId, labels);
    }

    private void assertSuccessfulCreateDeploymentRequest(
            final String restApiKey,
            final String environmentId,
            final String applicationId,
            final Set<String> labels
    ) throws IOException, MablSystemError {

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, restApiKey);
            CreateDeploymentProperties properties = new CreateDeploymentProperties();
            properties.setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
            CreateDeploymentResult result = client.createDeploymentEvent(environmentId, applicationId, labels, properties);
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

    @Test
    public void getApiKeyObjectFromApiKey() throws IOException, MablSystemError {

        final String fakeRestApiKey = "fakeApiKeyValue";

        registerGetMapping(
                String.format(MablRestApiClientImpl.GET_ORGANIZATION_ENDPOINT_TEMPLATE,fakeRestApiKey),
                ok(),
                MablTestConstants.APIKEY_RESULT_JSON,
                REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey
        );

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, fakeRestApiKey);
            GetApiKeyResult result = client.getApiKeyResult(fakeRestApiKey);
            assertEquals(EXPECTED_ORGANIZATION_ID, result.organization_id);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void getApplicationsReturnsTwoResults() throws IOException, MablSystemError {

        final String fakeRestApiKey = "fakeApiKeyValue";
        final String organization_id = "fakeOrganizationId";

        WireMock.stubFor(get(urlPathEqualTo("/applications"))
            .withQueryParam("organization_id", equalTo(organization_id))
                .withBasicAuth(REST_API_USERNAME_PLACEHOLDER, fakeRestApiKey)
                .withHeader("user-agent", new EqualToPattern(MablStepConstants.PLUGIN_USER_AGENT))
            .willReturn(ok()
                .withHeader("Content-Type", "application/json")
                .withBody(MablTestConstants.APPLICATIONS_RESULT_JSON)
            ));

        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, fakeRestApiKey);
            GetApplicationsResult result = client.getApplicationsResult(organization_id);
            assertEquals(2, result.applications.size());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void getEnvironmentsReturnsOneResult() throws IOException, MablSystemError {

        final String fakeRestApiKey = "fakeApiKeyValue";
        final String organization_id = "fakeOrganizationId";

        WireMock.stubFor(get(urlPathEqualTo("/environments"))
                .withQueryParam("organization_id", equalTo(organization_id))
                .withBasicAuth(REST_API_USERNAME_PLACEHOLDER, fakeRestApiKey)
                .withHeader("user-agent", new EqualToPattern(MablStepConstants.PLUGIN_USER_AGENT))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MablTestConstants.ENVIRONMENTS_RESULT_JSON)
                ));
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClientImpl(baseUrl, fakeRestApiKey);
            GetEnvironmentsResult result = client.getEnvironmentsResult(organization_id);
            assertEquals(1, result.environments.size());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }



    @Test(expected = MablSystemError.class)
    public void apiClientDoesntRetryOn503() throws IOException, MablSystemError {
        registerPostCreateRetryMappings("/events/deployment", "503", 503, 1);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", Collections.singleton("foo-label"));
    }

    @Test
    public void apiClientRetriesOn501() throws IOException, MablSystemError {
        registerPostCreateRetryMappings("/events/deployment", "501", 501, 1);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", Collections.singleton("foo-label"));
    }

    @Test
    public void apiClientRetriesOn501MaxtimesSuccess() throws IOException, MablSystemError {
        registerPostCreateRetryMappings("/events/deployment", "501", 501, 5);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", Collections.singleton("foo-label"));
    }

    @Test(expected = MablSystemError.class)
    public void apiClientRetriesOn501OverMaxtimesFailure() throws IOException, MablSystemError {
        registerPostCreateRetryMappings("/events/deployment", "501", 501, 6);
        assertSuccessfulCreateDeploymentRequest("pa$$\\/\\/orD", "foo-env-e", "foo-app-a", Collections.singleton("foo-label"));
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
}

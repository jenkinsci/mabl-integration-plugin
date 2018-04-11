package com.mabl.integration.jenkins;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_USER_AGENT;
import static org.apache.commons.httpclient.HttpStatus.SC_CREATED;

/**
 * Unit test for REST API calls
 */
// TODO pull out into abstract test
public class MablRestApiClientTest {

    // Annotation used only for static rules, so we only startup a single Wiremock instance
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule();

    // Pass the above instance to all concrete implementations
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private Map<String, String> expectedUrls = new HashMap<String, String>();

    @Test
    public void createDeploymentHappyPathTest() throws IOException, ExecutionException, InterruptedException {

        final String fakeRestApiKey = "pa$$\\/\\/orD";
        final String environmentId = "foo-env-e";
        final String applicationId = "foo-app-a";

        registerPostMapping(
                MablRestApiClient.DEPLOYMENT_TRIGGER_ENDPOINT,
                "{\"id\":\"foo-id-v\",\"unknown\":\"dont-break\"}",
                MablRestApiClient.REST_API_USERNAME_PLACEHOLDER,
                fakeRestApiKey
        );
        final String baseUrl = getBaseUrl();

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClient(baseUrl, fakeRestApiKey);
            CloseableHttpResponse response = client.createDeploymentEvent(environmentId, applicationId);

            Assert.assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());

            MablRestApiClient.CreateDeploymentResult result = client.parseCreateDeploymentEventResponse(response);
            Assert.assertEquals("foo-id-v", result.id);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        verifyExpectedUrls();
    }

    /**
     * Register the local file to a mapping and provide full URL path
     *
     * @param path             mapped relative path
     * @param jsonResponse     return json body on hit
     * @param expectedUsername required username
     * @param expectedPassword required password
     * @return mapped URL (full URL)
     */
    private String registerPostMapping(
            final String path,
            final String jsonResponse,
            final String expectedUsername,
            final String expectedPassword
    ) {

        final String mappedUrl = generatePageUrl(path);
        expectedUrls.put(path, "POST");

        final MappingBuilder mappingBuilder = post(urlPathEqualTo(path))
                .willReturn(created()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse));

        mappingBuilder.withBasicAuth(expectedUsername, expectedPassword);
        mappingBuilder.withHeader("user-agent", new EqualToPattern(PLUGIN_USER_AGENT));
        mappingBuilder.withHeader("Content-Type", new EqualToPattern("application/json"));
        mappingBuilder.withRequestBody(
                new EqualToPattern(
                        "{\"environment_id\":\"foo-env-e\",\"application_id\":\"foo-app-a\"}"));

        stubFor(mappingBuilder);

        return mappedUrl;
    }

    /**
     * Register the local file to a mapping and provide full URL path
     *
     * @param path             mapped relative path
     * @param jsonResponse     return json body on hit
     * @param expectedUsername required username
     * @param expectedPassword required password
     * @return mapped URL (full URL)
     */
    private String registerGetMapping(
            final String path,
            final String jsonResponse,
            final String expectedUsername,
            final String expectedPassword
    ) {

        final String mappedUrl = generatePageUrl(path);
        expectedUrls.put(path, "GET");

        final MappingBuilder mappingBuilder = get(urlPathEqualTo(path))
                .willReturn(created()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse));

        mappingBuilder.withBasicAuth(expectedUsername, expectedPassword);
        mappingBuilder.withHeader("user-agent", new EqualToPattern(PLUGIN_USER_AGENT));

        stubFor(mappingBuilder);

        return mappedUrl;
    }

    protected void verifyExpectedUrls() {
        for (final Map.Entry<String, String> expectedUrlEntry : this.expectedUrls.entrySet()) {

            final String method = expectedUrlEntry.getValue();
            final String url = expectedUrlEntry.getKey();

            RequestPatternBuilder builder = null;

            if ("GET".equals(method)) {
                builder = getRequestedFor(urlPathEqualTo(url));

            } else if ("POST".equals(method)) {
                builder = postRequestedFor(urlPathEqualTo(url));
            }

            verify(builder);
        }
    }

    private String generatePageUrl(final String path) {
        return getBaseUrl() + path;
    }

    private String getBaseUrl() {
        final int portNumber = wireMockRule.getOptions().portNumber();
        final String address = wireMockRule.getOptions().bindAddress();
        return String.format("http://%s:%d", address, portNumber);
    }
}

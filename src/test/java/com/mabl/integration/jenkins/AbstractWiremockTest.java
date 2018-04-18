package com.mabl.integration.jenkins;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.mabl.integration.jenkins.MablRestApiClientImpl.DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE;
import static com.mabl.integration.jenkins.MablRestApiClientImpl.REST_API_USERNAME_PLACEHOLDER;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_USER_AGENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Common Wiremock testing harness
 */
public abstract class AbstractWiremockTest {

    // Annotation used only for static rules, so we only startup a single Wiremock instance
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule();

    // Pass the above instance to all concrete implementations
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    private Map<String, String> expectedUrls = new HashMap<String, String>();

    /**
     * Register the local file to a mapping and provide full URL path
     *
     * @param path             mapped relative path
     * @param jsonResponse     return json body on hit
     * @param expectedUsername required username
     * @param expectedPassword required password
     * @return mapped URL (full URL)
     */
    protected String registerPostMapping(
            final String path,
            final String jsonResponse,
            final String expectedUsername,
            final String expectedPassword,
            final String expectedBody
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
        mappingBuilder.withRequestBody(new EqualToPattern(expectedBody));

        stubFor(mappingBuilder);

        return mappedUrl;
    }

    /**
     * Register the local file to a mapping and provide full URL path
     *
     * @param path             mapped relative path
     * @param responseBuilder  target response
     * @param jsonResponse     return json body on hit
     * @param expectedUsername required username
     * @param expectedPassword required password
     * @return mapped URL (full URL)
     */
    protected String registerGetMapping(
            final String path,
            final ResponseDefinitionBuilder responseBuilder,
            final String jsonResponse,
            final String expectedUsername,
            final String expectedPassword
    ) {

        final String mappedUrl = generatePageUrl(path);
        expectedUrls.put(path, "GET");

        final MappingBuilder mappingBuilder = get(urlPathEqualTo(path))
                .willReturn(responseBuilder
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

    protected String getBaseUrl() {
        final int portNumber = wireMockRule.getOptions().portNumber();
        final String address = wireMockRule.getOptions().bindAddress();
        return String.format("http://%s:%d", address, portNumber);
    }
}

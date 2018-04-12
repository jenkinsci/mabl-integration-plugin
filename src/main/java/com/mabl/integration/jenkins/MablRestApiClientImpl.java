package com.mabl.integration.jenkins;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mabl.integration.jenkins.domain.CreateDeploymentPayload;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import hudson.remoting.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_USER_AGENT;
import static org.apache.commons.httpclient.HttpStatus.SC_CREATED;
import static org.apache.commons.httpclient.HttpStatus.SC_NOT_FOUND;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;

/**
 * mabl runner to launch all plans for a given
 * environment and application
 */
public class MablRestApiClientImpl implements MablRestApiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    protected static final String REST_API_USERNAME_PLACEHOLDER = "key";
    protected static final String DEPLOYMENT_TRIGGER_ENDPOINT = "/events/deployment";
    protected static final String DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE = "/execution/result/event/%s";

    private static final Header JSON_TYPE_HEADER = new BasicHeader("Content-Type", "application/json");

    private final CloseableHttpClient httpClient;
    private final String restApiBaseUrl;
    private final String restApiKey;

    public MablRestApiClientImpl(
            final String restApiBaseUrl,
            final String restApiKey
    ) {

        this.restApiKey = restApiKey;
        this.restApiBaseUrl = restApiBaseUrl;

        httpClient = HttpClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy())
//                .setRetryHandler() // TODO retry on 50[123] status
                // TODO why isn't this setting the required Basic auth headers? Hardcoded as work around.
                .setDefaultCredentialsProvider(getApiCredentialsProvider(restApiKey))
                .setUserAgent(PLUGIN_USER_AGENT) // track calls @ API level
                .build();
    }

    private CredentialsProvider getApiCredentialsProvider(
            final String restApiKey
    ) {
        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials creds =
                new UsernamePasswordCredentials(REST_API_USERNAME_PLACEHOLDER, restApiKey);

        provider.setCredentials(AuthScope.ANY, creds);

        return provider;
    }

    private Header getBasicAuthHeader(
            final String restApiKey
    ) {
        final String encoded = Base64.encode((REST_API_USERNAME_PLACEHOLDER + ":" + restApiKey).getBytes());
        return new BasicHeader("Authorization", "Basic " + encoded);
    }

    @Override
    public CreateDeploymentResult createDeploymentEvent(
            final String environmentId,
            final String applicationId
    ) throws IOException, MablSystemError {

        final String url = restApiBaseUrl + DEPLOYMENT_TRIGGER_ENDPOINT; // TODO validate inputs so we can't have illegal urls

        // TODO do sanity check of parameters, so we can catch the encoding exception
        final String jsonPayload = objectMapper.writeValueAsString(new CreateDeploymentPayload(environmentId, applicationId));
        final AbstractHttpEntity payloadEntity = new ByteArrayEntity(jsonPayload.getBytes("UTF-8"));

        final HttpPost request = new HttpPost(url);

        request.setEntity(payloadEntity);
        request.addHeader(getBasicAuthHeader(restApiKey));
        request.addHeader(JSON_TYPE_HEADER);

        return parseCreateDeploymentEventResponse(httpClient.execute(request));
    }

    @Override
    public ExecutionResult getExecutionResults(String eventId) throws IOException, MablSystemError {

        final String url = restApiBaseUrl + String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE, eventId);
        final HttpGet request = new HttpGet(url);

        request.addHeader(getBasicAuthHeader(restApiKey));

        return parseExecutionResultResponse(httpClient.execute(request));
    }

    private ExecutionResult parseExecutionResultResponse(final HttpResponse response) throws IOException, MablSystemError {

        // TODO handle key error
        // TODO handle not found

        final int statusCode = response.getStatusLine().getStatusCode();

        switch (statusCode) {
            case SC_OK:
                return objectMapper.reader(ExecutionResult.class).readValue(response.getEntity().getContent());
            case SC_NOT_FOUND:
                return null;
            default:

                final String message = String.format(
                        "Unexpected status from mabl API on execution result fetch: %d\n" +
                                "body: [%s]\n", statusCode, EntityUtils.toString((response.getEntity())));

                throw new MablSystemError(message);
        }
    }

    private CreateDeploymentResult parseCreateDeploymentEventResponse(final HttpResponse response) throws IOException, MablSystemError {

        final int statusCode = response.getStatusLine().getStatusCode();
        if (SC_CREATED != response.getStatusLine().getStatusCode()) {
            final String message = String.format(
                    "Unexpected status from mabl API on deployment event creation: %d\n" +
                            "body: [%s]\n", statusCode, EntityUtils.toString((response.getEntity())));

            throw new MablSystemError(message);
        }

        return objectMapper.reader(CreateDeploymentResult.class).readValue(response.getEntity().getContent());
    }

    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                // TODO cleaner exception handling
                e.printStackTrace();
            }
        }
    }

}
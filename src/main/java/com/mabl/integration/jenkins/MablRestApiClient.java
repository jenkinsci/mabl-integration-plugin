package com.mabl.integration.jenkins;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.remoting.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.IOException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_USER_AGENT;

/**
 * mabl runner to launch all plans for a given
 * environment and application
 */
public class MablRestApiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected static final String REST_API_USERNAME_PLACEHOLDER = "key";
    protected static final String DEPLOYMENT_TRIGGER_ENDPOINT = "/events/deployment";
    protected static final String DEPLOYMENT_RESULT_ENDPOINT = "/execution/result/event/%s";

    private static final Header JSON_TYPE_HEADER = new BasicHeader("Content-Type", "application/json");

    private final CloseableHttpClient httpClient;
    private final HttpClientContext localContext;
    private final String restApiBaseUrl;
    private final String restApiKey;

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    private static class CreateDeploymentPayload {
        @JsonProperty("environment_id")
        final String environmentId;
        @JsonProperty("application_id")
        final String applicationId;

        public CreateDeploymentPayload(String environmentId, String applicationId) {
            this.environmentId = environmentId;
            this.applicationId = applicationId;
        }
    }

    public static class CreateDeploymentResult {
        public String id;

        @JsonCreator
        public CreateDeploymentResult(@JsonProperty("id") String id) {
            this.id = id;
        }
    }

    public MablRestApiClient(
            final String restApiBaseUrl,
            final String restApiKey
    ) {

        this.restApiKey = restApiKey;
        this.restApiBaseUrl = restApiBaseUrl;

        httpClient = HttpClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy())
//                .setRetryHandler()
                // TODO why isn't this setting the required Basic auth headers?
                .setDefaultCredentialsProvider(getApiCredentialsProvider(restApiKey))
                .setUserAgent(PLUGIN_USER_AGENT) // track calls @ API level
                .build();

        localContext = HttpClientContext.create();
        localContext.setCredentialsProvider(getApiCredentialsProvider(restApiKey));
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

    public CloseableHttpResponse createDeploymentEvent(
            final String environmentId,
            final String applicationId
    ) throws IOException {

        final String url = restApiBaseUrl + DEPLOYMENT_TRIGGER_ENDPOINT; // TODO validate inputs so we can't have illegal urls

        // TODO do sanity check of parameters, so we can catch the encoding exception
        final String jsonPayload = objectMapper.writeValueAsString(new CreateDeploymentPayload(environmentId, applicationId));
        final AbstractHttpEntity payloadEntity = new ByteArrayEntity(jsonPayload.getBytes("UTF-8"));

        final HttpPost request = new HttpPost(url);

        request.setEntity(payloadEntity);
        request.addHeader(getBasicAuthHeader(restApiKey));
        request.addHeader(JSON_TYPE_HEADER);

        return httpClient.execute(request);
    }

    public CreateDeploymentResult parseCreateDeploymentEventResponse(final HttpResponse response) throws IOException {
        return objectMapper.reader(CreateDeploymentResult.class).readValue(response.getEntity().getContent());
    }

    // TODO create request to poll for results until complete

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
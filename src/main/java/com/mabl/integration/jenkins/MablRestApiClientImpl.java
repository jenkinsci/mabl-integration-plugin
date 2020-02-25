package com.mabl.integration.jenkins;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mabl.integration.jenkins.domain.CreateDeploymentPayload;
import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.domain.GetLabelsResult;
import hudson.remoting.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_USER_AGENT;
import static com.mabl.integration.jenkins.MablStepConstants.REQUEST_TIMEOUT_MILLISECONDS;
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
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    static final String REST_API_USERNAME_PLACEHOLDER = "key";
    static final String DEPLOYMENT_TRIGGER_ENDPOINT = "/events/deployment";
    static final String DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE = "/execution/result/event/%s";
    static final String GET_ORGANIZATION_ENDPOINT_TEMPLATE = "/apiKeys/%s";
    static final String GET_APPLICATIONS_ENDPOINT_TEMPLATE = "/applications?organization_id=%s";
    static final String GET_ENVIRONMENTS_ENDPOINT_TEMPLATE = "/environments?organization_id=%s";
    static final String GET_LABELS_ENDPOINT_TEMPLATE = "/schedule/runPolicy/labels?organization_id=%s";

    private static final Header JSON_TYPE_HEADER = new BasicHeader("Content-Type", "application/json");

    private final CloseableHttpClient httpClient;
    private final String restApiBaseUrl;
    private final String restApiKey;

    MablRestApiClientImpl(
            final String restApiBaseUrl,
            final String restApiKey
    ) {
        this(restApiBaseUrl, restApiKey, false);
    }
    
    MablRestApiClientImpl(
            final String restApiBaseUrl,
            final String restApiKey,
            final boolean disableSslVerification
    ) {

        this.restApiKey = restApiKey;
        this.restApiBaseUrl = restApiBaseUrl;

        final HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy())
                .setServiceUnavailableRetryStrategy(getRetryHandler())
                // TODO why isn't this setting the required Basic auth headers? Hardcoded as work around.
                .setDefaultCredentialsProvider(getApiCredentialsProvider(restApiKey))
                .setUserAgent(PLUGIN_USER_AGENT) // track calls @ API level
                .setConnectionTimeToLive(30, TimeUnit.SECONDS) // use keep alive in SSL API connections
                .setDefaultRequestConfig(getDefaultRequestConfig());
        
        if (disableSslVerification) {
            final SSLContext sslContext;
            try {
                sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, new TrustAllStrategy())
                    .build();
            } catch (Exception e) {
                throw new RuntimeException("Error initializing SSL", e);
            }
            httpClientBuilder.setSSLContext(sslContext);
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();
            httpClientBuilder.setConnectionManager(new PoolingHttpClientConnectionManager(socketFactoryRegistry));
        }
        
        httpClient = httpClientBuilder.build();
    }

    private MablRestApiClientRetryHandler getRetryHandler() {
        Injector injector = Guice.createInjector(new JenkinsModule());
        return injector.getInstance(MablRestApiClientRetryHandler.class);
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
        final String encoded = Base64.encode((REST_API_USERNAME_PLACEHOLDER + ":" + restApiKey)
                .getBytes(Charset.forName("UTF-8")));
        return new BasicHeader("Authorization", "Basic " + encoded);
    }

    @Override
    public CreateDeploymentResult createDeploymentEvent(
            final String environmentId,
            final String applicationId,
            final Set<String> labels,
            final CreateDeploymentProperties properties
            ) throws IOException, MablSystemError {
        final String url = restApiBaseUrl + DEPLOYMENT_TRIGGER_ENDPOINT; // TODO validate inputs so we can't have illegal urls

        // TODO do sanity check of parameters, so we can catch the encoding exception
        final String jsonPayload = objectMapper.writeValueAsString(new CreateDeploymentPayload(environmentId, applicationId, labels, properties));
        final AbstractHttpEntity payloadEntity = new ByteArrayEntity(jsonPayload.getBytes("UTF-8"));

        final HttpPost request = new HttpPost(url);

        request.setEntity(payloadEntity);
        request.addHeader(getBasicAuthHeader(restApiKey));
        request.addHeader(JSON_TYPE_HEADER);

        return parseApiResult(httpClient.execute(request), CreateDeploymentResult.class);
    }

    @Override
    public ExecutionResult getExecutionResults(String eventId) throws IOException, MablSystemError {
        final String url = restApiBaseUrl + String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE, eventId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), ExecutionResult.class);
    }

    @Override
    public GetApiKeyResult getApiKeyResult(String formApiKey) throws IOException, MablSystemError {
        final String url = restApiBaseUrl + String.format(GET_ORGANIZATION_ENDPOINT_TEMPLATE, formApiKey);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetApiKeyResult.class);
    }

    @Override
    public GetApplicationsResult getApplicationsResult(String organizationId) throws IOException, MablSystemError {
        final String url = restApiBaseUrl + String.format(GET_APPLICATIONS_ENDPOINT_TEMPLATE, organizationId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetApplicationsResult.class);
    }

    @Override
    public GetEnvironmentsResult getEnvironmentsResult(String organizationId) throws IOException, MablSystemError {
        final String url = restApiBaseUrl + String.format(GET_ENVIRONMENTS_ENDPOINT_TEMPLATE, organizationId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetEnvironmentsResult.class);
    }

    @Override
    public GetLabelsResult getLabelsResult(String organizationId) throws IOException, MablSystemError {
        final String url = restApiBaseUrl + String.format(GET_LABELS_ENDPOINT_TEMPLATE, organizationId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetLabelsResult.class);
    }

    private HttpGet buildGetRequest(String url) throws MablSystemError {
        try {
            final HttpGet request = new HttpGet(url);
            request.addHeader(getBasicAuthHeader(restApiKey));
            return request;
        } catch (IllegalArgumentException e) {
            throw new MablSystemError(String.format("Unexpected status from mabl trying to build API url: %s", url));
        }
    }

    private <ApiResult> ApiResult parseApiResult(
            final HttpResponse response,
            Class<ApiResult> resultClass
    ) throws IOException, MablSystemError {

        final int statusCode = response.getStatusLine().getStatusCode();

        switch (statusCode) {
            case SC_OK: // fall through case
            case SC_CREATED:
                return resultClass.cast(objectMapper.reader(resultClass).readValue(response.getEntity().getContent()));
            case SC_NOT_FOUND:
                return null;
            default:

                final String message = String.format(
                        "Unexpected status from mabl API on result fetch: %d%n" +
                                "body: [%s]%n", statusCode, EntityUtils.toString((response.getEntity())));

                throw new MablSystemError(message);
        }
    }

    private RequestConfig getDefaultRequestConfig() {
        // TODO we should retry connection timeouts
        return RequestConfig.custom()
                .setConnectTimeout(REQUEST_TIMEOUT_MILLISECONDS)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT_MILLISECONDS)
                .setSocketTimeout(REQUEST_TIMEOUT_MILLISECONDS)
                .build();
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
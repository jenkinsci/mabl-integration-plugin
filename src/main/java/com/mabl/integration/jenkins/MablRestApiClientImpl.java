package com.mabl.integration.jenkins;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
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
import hudson.ProxyConfiguration;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
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
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

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

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    static final String REST_API_USERNAME_PLACEHOLDER = "key";
    static final String DEPLOYMENT_TRIGGER_ENDPOINT = "/events/deployment";
    static final String DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE = "/execution/result/event/%s";
    static final String GET_ORGANIZATION_ENDPOINT_TEMPLATE = "/apiKeys/self";
    static final String GET_APPLICATIONS_ENDPOINT_TEMPLATE = "/applications?organization_id=%s";
    static final String GET_ENVIRONMENTS_ENDPOINT_TEMPLATE = "/environments?organization_id=%s";
    static final String GET_LABELS_ENDPOINT_TEMPLATE = "/schedule/runPolicy/labels?organization_id=%s";
    static final String HEALTH_LIVE_ENDPOINT = "/health/live";

    private static final Header JSON_TYPE_HEADER = new BasicHeader("Content-Type", "application/json");

    private final CloseableHttpClient httpClient;
    private final String restApiBaseUrl;
    private final Secret restApiKey;
    private final String appBaseUrl;
    private ProxyConfiguration proxy;

    MablRestApiClientImpl(
            final String restApiBaseUrl,
            final Secret restApiKey,
            final String appBaseUrl
    ) {
        this(restApiBaseUrl, restApiKey, appBaseUrl, false);
    }

    MablRestApiClientImpl(
            final String restApiBaseUrl,
            final Secret restApiKey,
            final String appBaseUrl,
            final boolean disableSslVerification
    ) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            proxy = jenkins.proxy;
        }

        this.restApiKey = restApiKey;
        this.restApiBaseUrl = restApiBaseUrl;
        this.appBaseUrl = appBaseUrl;

        final HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy())
                .setServiceUnavailableRetryStrategy(getRetryHandler())
                // TODO why isn't this setting the required Basic auth headers? Hardcoded as work around.
                .setDefaultCredentialsProvider(getApiCredentialsProvider(restApiKey))
                .setUserAgent(PLUGIN_USER_AGENT) // track calls @ API level
                .setConnectionTimeToLive(10, TimeUnit.MINUTES) // use keep alive in SSL API connections
                .setRetryHandler(new StandardHttpRequestRetryHandler(3, true))
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
            final Secret restApiKey
    ) {
        final CredentialsProvider provider = new BasicCredentialsProvider();

        // Adding check for null or empty restApiKey
        final String keyTest = restApiKey.getPlainText();
        if (keyTest == null || keyTest.trim().isEmpty()) {
            throw new IllegalArgumentException ("API key not found");
        }

        final UsernamePasswordCredentials creds =
                new UsernamePasswordCredentials(REST_API_USERNAME_PLACEHOLDER, keyTest);

        provider.setCredentials(AuthScope.ANY, creds);

        // Set proxy credentials if provided
        if (proxy != null && (!StringUtils.isBlank(proxy.getUserName()) || !StringUtils.isBlank(proxy.getPassword()))) {
            final Credentials credentials = new UsernamePasswordCredentials(
                    proxy.getUserName() == null ? "" : proxy.getUserName(),
                    proxy.getPassword()
            );
            provider.setCredentials(new AuthScope(new HttpHost(proxy.name, proxy.port)), credentials);
        }

        return provider;
    }

    private Header getBasicAuthHeader(
            final Secret restApiKey
    ) {
        final String encoded = Base64.getMimeEncoder().encodeToString(
                (REST_API_USERNAME_PLACEHOLDER + ":" + restApiKey.getPlainText()).getBytes(StandardCharsets.UTF_8));
        return new BasicHeader("Authorization", "Basic " + encoded);
    }

    @Override
    public CreateDeploymentResult createDeploymentEvent(
            final String environmentId,
            final String applicationId,
            final String labels,
            final String mablBranch,
            final CreateDeploymentProperties properties
            ) throws IOException {
        final String url = restApiBaseUrl + DEPLOYMENT_TRIGGER_ENDPOINT; // TODO validate inputs so we can't have illegal urls

        // TODO do sanity check of parameters, so we can catch the encoding exception
        final String jsonPayload = gson.toJson(
                new CreateDeploymentPayload(environmentId, applicationId, labels, mablBranch, properties));
        final AbstractHttpEntity payloadEntity = new ByteArrayEntity(jsonPayload.getBytes(StandardCharsets.UTF_8));

        final HttpPost request = new HttpPost(url);

        request.setEntity(payloadEntity);
        request.addHeader(getBasicAuthHeader(restApiKey));
        request.addHeader(JSON_TYPE_HEADER);

        return parseApiResult(httpClient.execute(request), CreateDeploymentResult.class);
    }

    @Override
    public ExecutionResult getExecutionResults(String eventId) throws IOException {
        final String url = restApiBaseUrl + String.format(DEPLOYMENT_RESULT_ENDPOINT_TEMPLATE, eventId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), ExecutionResult.class);
    }

    @Override
    public GetApiKeyResult getApiKeyResult() throws IOException {
        final String url = restApiBaseUrl + GET_ORGANIZATION_ENDPOINT_TEMPLATE;
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetApiKeyResult.class);
    }

    @Override
    public GetApplicationsResult getApplicationsResult(String organizationId) throws IOException {
        final String url = restApiBaseUrl + String.format(GET_APPLICATIONS_ENDPOINT_TEMPLATE, organizationId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetApplicationsResult.class);
    }

    @Override
    public GetEnvironmentsResult getEnvironmentsResult(String organizationId) throws IOException {
        final String url = restApiBaseUrl + String.format(GET_ENVIRONMENTS_ENDPOINT_TEMPLATE, organizationId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetEnvironmentsResult.class);
    }

    @Override
    public GetLabelsResult getLabelsResult(String organizationId) throws IOException {
        final String url = restApiBaseUrl + String.format(GET_LABELS_ENDPOINT_TEMPLATE, organizationId);
        return parseApiResult(httpClient.execute(buildGetRequest(url)), GetLabelsResult.class);
    }

    @Override
    public String getAppBaseUrl() {
        return appBaseUrl;
    }

    @Override
    public void checkConnection() throws IOException {
        final String url = restApiBaseUrl + HEALTH_LIVE_ENDPOINT;
        final HttpResponse response = httpClient.execute(buildGetRequest(url));
        final int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case 200:
                break;
            case 401:
                throw new IOException("Invalid API key provided or the proxy connection requires authentication");
            case 403:
                throw new IOException("Invalid API key type. You must use a 'CI/CD Integration' key type");
            default:
                throw new IOException(response.getStatusLine().toString());
        }
    }

    private HttpGet buildGetRequest(String url) {
        try {
            final HttpGet request = new HttpGet(url);
            request.addHeader(getBasicAuthHeader(restApiKey));
            return request;
        } catch (IllegalArgumentException e) {
            throw new MablSystemException("Unexpected status from mabl trying to build API url: %s", url);
        }
    }

    private <ApiResult> ApiResult parseApiResult(
            final HttpResponse response,
            Class<ApiResult> resultClass
    ) throws IOException {

        final int statusCode = response.getStatusLine().getStatusCode();

        switch (statusCode) {
            case SC_OK: // fall through case
            case SC_CREATED:
                ApiResult result = null;
                try (InputStream responseStream = response.getEntity().getContent()) {
                    result = gson.fromJson(
                            new JsonReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8)), resultClass);
                }
                return result;
            case SC_NOT_FOUND:
                return null;
            default:
                throw new MablSystemException(
                        "Unexpected status from mabl API on result fetch: %d%n" +
                        "body: [%s]%n", statusCode, EntityUtils.toString((response.getEntity())));
        }
    }

    private RequestConfig getDefaultRequestConfig() {
        // TODO we should retry connection timeouts
        return RequestConfig.custom()
                .setConnectTimeout(REQUEST_TIMEOUT_MILLISECONDS)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT_MILLISECONDS)
                .setSocketTimeout(REQUEST_TIMEOUT_MILLISECONDS)
                .setProxy(proxy != null ? new HttpHost(proxy.name, proxy.port) : null)
                .setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC))
                .setTargetPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC))
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

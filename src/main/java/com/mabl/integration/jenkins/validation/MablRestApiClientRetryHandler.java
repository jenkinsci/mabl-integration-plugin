package com.mabl.integration.jenkins.validation;

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.commons.httpclient.HttpStatus.SC_NOT_IMPLEMENTED; // 501
import static org.apache.commons.httpclient.HttpStatus.SC_BAD_GATEWAY; // 502

public class MablRestApiClientRetryHandler implements ServiceUnavailableRetryStrategy {
    private static final ArrayList<Integer> retryStatusCodes = new ArrayList<Integer>(Arrays.asList(
            SC_NOT_IMPLEMENTED,
            SC_BAD_GATEWAY
    ));
    private static final int maxRetries = 5;
    private static final long retryIntervalMillis = 6000L;

    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        return isRetryStatusCode(response.getStatusLine().getStatusCode())
                && executionCount <= this.maxRetries;
    }

    @Override
    public long getRetryInterval() {
        return this.retryIntervalMillis;
    }

    private boolean isRetryStatusCode(int statusCode) {
        return this.retryStatusCodes.contains(statusCode);
    }

}

package com.mabl.integration.jenkins;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class JenkinsModule extends AbstractModule {
    private static final String MAX_RETRIES = "com.mabl.http.retryer.max.retries";
    private static final String RETRY_INTERVAL = "com.mabl.http.retryer.retry.interval.milliseconds";

    @Override
    protected void configure() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(getClass().getResource("/config.properties").getFile()));
            Names.bindProperties(binder(), properties);
        } catch (IOException e) {
            System.out.println("ERROR: Could not load properties");
            throw new RuntimeException(e);
        }
    }

    @Provides
    MablRestApiClientRetryHandler providesMablRestApiClientRetryHandler(
            @Named(MAX_RETRIES) int maxRetires,
            @Named(RETRY_INTERVAL) long retryInterval
    ) {
        return new MablRestApiClientRetryHandler(maxRetires, retryInterval);
    }
}

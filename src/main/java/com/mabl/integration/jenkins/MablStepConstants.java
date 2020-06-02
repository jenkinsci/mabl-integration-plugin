package com.mabl.integration.jenkins;

import hudson.model.Hudson;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.jar.Manifest;

/**
 * mabl custom build step
 */
public class MablStepConstants {
    static final String PLUGIN_SYMBOL = "mabl";
    static final String PLUGIN_NAME = "mabl-integration-plugin";
    private static final String PLUGIN_ARTIFACT_NAME = "mabl-integration";
    static final String PLUGIN_VERSION = getPluginVersion();
    static final String PLUGIN_VERSION_UNKNOWN = "unknown";
    static final String PLUGIN_USER_AGENT =
            "mabl-jenkins-plugin/" + PLUGIN_VERSION + " (JVM: " +
            System.getProperty("java.version") + ", Jenkins: " +
                    Hudson.getVersion() + ")";
    static final String TEST_OUTPUT_XML_FILENAME = "report.xml";
    public static final String TEST_OUTPUT_XML_XLINK = "http://www.w3.org/1999/xlink";

    // Label for build steps drop down list
    static final String BUILD_STEP_DISPLAY_NAME = "Run mabl tests";
    static final String MABL_REST_API_BASE_URL = "https://api.mabl.com";
    static final int EXECUTION_TIMEOUT_SECONDS = 3600;
    static final int REQUEST_TIMEOUT_MILLISECONDS = 60000;
    static final long EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS = 10000;

    // Form labels
    public static final String FORM_API_KEY_LABEL = "API Key";
    public static final String FORM_ENVIRONMENT_ID_LABEL = "Application ID";
    public static final String FORM_APPLICATION_ID_LABEL = "Environment ID";

    /**
     * Dynamically grab the plugin version, so we can't forget to update it on release.
     *
     * @return plugin version, or {@link #PLUGIN_VERSION_UNKNOWN} on error/missing.
     */
    static String getPluginVersion() {
        try {
            final Enumeration<URL> resources = MablStepConstants.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                final Manifest manifest = new Manifest(resources.nextElement().openStream());

                String title = manifest.getMainAttributes().getValue("Implementation-title");
                if (PLUGIN_ARTIFACT_NAME.equalsIgnoreCase(title)) {
                    final String version = manifest.getMainAttributes().getValue("Implementation-Version");
                    return version != null && !version.isEmpty() ? version : PLUGIN_VERSION_UNKNOWN;
                }
            }
        } catch (IOException ignored) {}
        return PLUGIN_VERSION_UNKNOWN;
    }
}
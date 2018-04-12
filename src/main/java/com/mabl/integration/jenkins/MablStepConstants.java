package com.mabl.integration.jenkins;

/**
 * mabl custom build step
 *
 */
class MablStepConstants {
    static final String PLUGIN_SYMBOL = "mabl";
    static final String MABL_PLUGIN_NAME = "mabl-integration-plugin";
    private static final String PLUGIN_VERSION = "0.4.0"; // TODO automatically set this from Maven
    static final String PLUGIN_USER_AGENT = "mabl-jenkins-plugin/"+PLUGIN_VERSION;

    // Label for build steps drop down list
    static final String BUILD_STEP_DISPLAY_NAME = "Run mabl journeys";
    static final String MABL_REST_API_BASE_URL = "https://api.mabl.com";
    static final String MABL_WEBAPP_BASE_URL = "https://app.mabl.com";
    static final int EXECUTION_TIMEOUT_SECONDS = 3600;
    static final int REQUEST_TIMEOUT_MILLISECONDS = 60000;
    static final long EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS = 10000;
}
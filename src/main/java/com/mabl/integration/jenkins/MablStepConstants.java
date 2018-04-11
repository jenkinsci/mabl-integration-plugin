package com.mabl.integration.jenkins;

/**
 * mabl custom build step
 *
 */
class MablStepConstants {
    static final String PLUGIN_SYMBOL = "mabl";
    private static final String PLUGIN_VERSION = "0.1.0";
    static final String PLUGIN_USER_AGENT = "mabl-jenkins-plugin/"+PLUGIN_VERSION;

    // Label for build steps drop down list
    static final String BUILD_STEP_DISPLAY_NAME = "Run mabl journeys";
    static final String MABL_REST_API_BASE_URL = "https://api.mabl.com";
    static final String MABL_WEBAPP_BASE_URL = "https://app.mabl.com";
    static final long EXECUTION_TIMEOUT_SECONDS = 600;
    static final long REQUEST_TIMEOUT_SECONDS = 60;
}
package com.mabl.integration.jenkins.validation;

import com.mabl.integration.jenkins.MablRestApiClient;
import com.mabl.integration.jenkins.MablRestApiClientImpl;
import com.mabl.integration.jenkins.MablStepBuilder;
import com.mabl.integration.jenkins.MablStepConstants;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;

import java.io.IOException;

import static com.mabl.integration.jenkins.MablStepConstants.FORM_API_KEY_LABEL;
import static com.mabl.integration.jenkins.MablStepConstants.FORM_APPLICATION_ID_LABEL;
import static com.mabl.integration.jenkins.MablStepConstants.FORM_ENVIRONMENT_ID_LABEL;
import static hudson.util.FormValidation.error;
import static hudson.util.FormValidation.okWithMarkup;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * mabl result from deployment event creation
 */

public class MablStepBuilderValidator {

    /**
     * Validate form
     *
     * @param restApiKeyName prospective key name
     * @param environmentId prospective environment identifier
     * @param applicationId prospective application identifier
     * @return validation result
     */
    public static FormValidation validateForm(
            String restApiKeyName,
            String environmentId,
            String applicationId
    ) {
        return validateForm(restApiKeyName, environmentId, applicationId,false, null, null,
                MablStepConstants.DEFAULT_MABL_API_BASE_URL, MablStepConstants.DEFAULT_MABL_APP_BASE_URL);
    }

    /**
     * Validate form
     *
     * @param restApiKeyId prospective key id
     * @param environmentId prospective environment identifier
     * @param applicationId prospective application identifier
     * @param disableSslVerification prospective flag to indicate if SSL verification should be disabled
     * @param proxyUrl prospective proxy URL
     * @param proxyCredentialsId prospective proxy credentials id
     * @param apiBaseUrl base URL for API (not user-visible)
     * @param appBaseUrl base URL for the ap (not user-visible)
     * @return validation result
     */
    public static FormValidation validateForm(
            String restApiKeyId,
            String environmentId,
            String applicationId,
            boolean disableSslVerification,
            String proxyUrl,
            String proxyCredentialsId,
            String apiBaseUrl,
            String appBaseUrl
    ) {
        try {

            // TODO MOVE into validator class when we add remote validation
            final String restApiKeyClean = trimToNull(restApiKeyId);
            final String applicationIdClean = trimToNull(applicationId);
            final String environmentIdClean = trimToNull(environmentId);

            if (restApiKeyClean == null) {
                return error("Non-empty %s required", FORM_API_KEY_LABEL);
            }
            // User copied wrong text from API settings page
            if (restApiKeyClean.contains("key:")) {
                return error("Invalid %s", FORM_API_KEY_LABEL);
            }
            // applicationId pasted into environmentId field
            if (environmentIdClean != null && environmentIdClean.endsWith("-a")) {
                return error("Invalid %s value. Contains an %s", FORM_ENVIRONMENT_ID_LABEL, FORM_APPLICATION_ID_LABEL);
            }
            // environmentId pasted into applicationId field
            if (applicationIdClean != null && applicationIdClean.endsWith("-e")) {
                return error("Invalid %s value. Contains an %s", FORM_APPLICATION_ID_LABEL, FORM_ENVIRONMENT_ID_LABEL);
            }
            // We need one of these
            if (environmentIdClean == null && applicationIdClean == null) {
                return error("Non-empty %s or %s required",
                        FORM_APPLICATION_ID_LABEL, FORM_ENVIRONMENT_ID_LABEL);
            }

            if (!StringUtils.isBlank(proxyUrl)) {
                try {
                    HttpHost.create(proxyUrl);
                } catch (IllegalArgumentException e) {
                    return error("Invalid proxy URL provided: %s", proxyUrl);
                }
            }

            try {
                MablRestApiClient client = MablStepBuilder.createMablRestApiClient(
                        restApiKeyClean,
                        disableSslVerification,
                        trimToNull(proxyUrl),
                        trimToNull(proxyCredentialsId),
                        apiBaseUrl,
                        appBaseUrl
                );
                client.checkConnection();
            } catch (IOException e) {
                return error("Failed to connect to mabl API: " + e.getMessage());
            }

            return okWithMarkup("<span style='color:green'>✓ Everything looks good</span>");
        } catch (Exception e) {
            return error("Client error : " + e.getMessage());
        }
    }
}
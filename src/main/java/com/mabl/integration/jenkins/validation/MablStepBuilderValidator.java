package com.mabl.integration.jenkins.validation;

import hudson.util.FormValidation;

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
     * @param restApiKey    prospective key
     * @param environmentId prospective environment identifier
     * @param applicationId prospective application identifier
     * @return validation result
     */
    public static FormValidation validateForm(
            String restApiKey,
            String environmentId,
            String applicationId
    ) {
        try {

            // TODO MOVE into validator class when we add remote validation
            final String restApiKeyClean = trimToNull(restApiKey);
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
                return error("Invalid %s value. Contains an  %s", FORM_ENVIRONMENT_ID_LABEL, FORM_APPLICATION_ID_LABEL);
            }
            // environmentId pasted into applicationId field
            if (applicationIdClean != null && applicationIdClean.endsWith("-e")) {
                return error("Invalid %s value. Contains an  %s", FORM_APPLICATION_ID_LABEL, FORM_ENVIRONMENT_ID_LABEL);
            }
            // We need one of these
            if (environmentIdClean == null && applicationIdClean == null) {
                return error("Non-empty %s or %s required",
                        FORM_APPLICATION_ID_LABEL, FORM_ENVIRONMENT_ID_LABEL);
            }

            // Future validations
            // TODO contact API an ensure key exists
            // TODO ensure workspace contains (1) environment (2) application
            return okWithMarkup("<span style='color:green'>✓ Everything looks good</span>");
        } catch (Exception e) {
            return error("Client error : " + e.getMessage());
        }
    }
}
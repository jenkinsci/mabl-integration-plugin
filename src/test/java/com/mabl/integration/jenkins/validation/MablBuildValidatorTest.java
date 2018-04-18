package com.mabl.integration.jenkins.validation;

import hudson.util.FormValidation;
import org.junit.Test;

import static com.mabl.integration.jenkins.MablStepConstants.FORM_API_KEY_LABEL;
import static com.mabl.integration.jenkins.MablStepConstants.FORM_APPLICATION_ID_LABEL;
import static com.mabl.integration.jenkins.MablStepConstants.FORM_ENVIRONMENT_ID_LABEL;
import static com.mabl.integration.jenkins.validation.MablStepBuilderValidator.validateForm;
import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test runner
 */
public class MablBuildValidatorTest {

    @Test
    public void validateGoodAllFieldsForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                "sample-environment-id",
                "sample-application-id"
        );

        assertEquals(actual.kind, OK);
    }

    @Test
    public void validateGoodEnvironmentOnlyForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                "sample-environment-id",
                null
        );

        assertEquals(actual.kind, OK);
    }

    @Test
    public void validateGoodApplicationOnlyForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                null,
                "sample-application-id"
        );

        assertEquals(actual.kind, OK);
    }

    @Test
    public void validateBadNoEnvironmentOrApplicationForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                null,
                null
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoEnvironmentIdInWrongFieldApplicationForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                "sample-a",
                null
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoApplicationIdInWrongFieldApplicationForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                null,
                "sample-e"
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoEnvironmentOrApplicationWhiteSpaceForm() {

        final FormValidation actual = validateForm(
                "sample-key",
                "  ",
                "\t"
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("application label expected",
                actual.getMessage().contains(FORM_APPLICATION_ID_LABEL));
        assertTrue("environment label expected",
                actual.getMessage().contains(FORM_ENVIRONMENT_ID_LABEL));
    }

    @Test
    public void validateBadNoRestApiKeyForm() {

        final FormValidation actual = validateForm(
                null,
                null,
                null
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("rest API key label expected",
                actual.getMessage().contains(FORM_API_KEY_LABEL));
    }

    @Test
    public void validateBadNoRestApiKeyWithWhitespaceForm() {

        final FormValidation actual = validateForm(
                "\t\n ",
                null,
                null
        );

        assertEquals(ERROR, actual.kind);
        assertTrue("rest API key label expected",
                actual.getMessage().contains(FORM_API_KEY_LABEL));
    }
}

package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.validation.MablStepBuilderValidator;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.mabl.integration.jenkins.MablStepConstants.BUILD_STEP_DISPLAY_NAME;
import static com.mabl.integration.jenkins.MablStepConstants.EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS;
import static com.mabl.integration.jenkins.MablStepConstants.EXECUTION_TIMEOUT_SECONDS;
import static com.mabl.integration.jenkins.MablStepConstants.MABL_REST_API_BASE_URL;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_SYMBOL;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * mabl custom build step
 */
@SuppressWarnings("unused") // automatically discovered by Jenkins
public class MablStepBuilder extends Builder {

    private final String restApiKey;
    private final String environmentId;
    private final String applicationId;
    private final boolean continueOnPlanFailure;
    private final boolean continueOnMablError;

    @DataBoundConstructor
    public MablStepBuilder(
            final String restApiKey,
            final String environmentId,
            final String applicationId,
            final boolean continueOnPlanFailure,
            final boolean continueOnMablError

    ) {
        this.restApiKey = trimToNull(restApiKey);
        this.environmentId = trimToNull(environmentId);
        this.applicationId = trimToNull(applicationId);
        this.continueOnPlanFailure = continueOnPlanFailure;
        this.continueOnMablError = continueOnMablError;
    }

    // Accessors to be used by Jelly UI templates
    public String getRestApiKey() {
        return restApiKey;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public boolean perform(
            final AbstractBuild<?, ?> build,
            final Launcher launcher,
            final BuildListener listener
    ) throws InterruptedException {

        final PrintStream outputStream = listener.getLogger();
        final MablRestApiClient client = new MablRestApiClientImpl(MABL_REST_API_BASE_URL, restApiKey);

        final MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS,
                environmentId,
                applicationId,
                continueOnPlanFailure,
                continueOnMablError
        );

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> runnerFuture = executorService.submit(runner);
        try {
            return runnerFuture.get(EXECUTION_TIMEOUT_SECONDS, SECONDS);

        } catch (ExecutionException e) {
            outputStream.println("There was an execution error trying to run your journeys in mabl");
            e.printStackTrace(outputStream);
            return continueOnMablError;

        } catch (TimeoutException e) {
            outputStream.printf("Oh dear. Your journeys exceeded the max plugin runtime limit of %d seconds.%n" +
                    "We've aborted this Jenkins step, but your journeys may still be running in mabl.", EXECUTION_TIMEOUT_SECONDS);
            return continueOnMablError;
        }
    }

    @Override
    public MablStepDescriptor getDescriptor() {
        return (MablStepDescriptor) super.getDescriptor();
    }

    /**
     * Descriptor used in views. Centralized metadata store for all {@link MablStepBuilder} instances.
     */
    @Extension
    @Symbol(PLUGIN_SYMBOL)
    public static class MablStepDescriptor extends BuildStepDescriptor<Builder> {

        public MablStepDescriptor() {
            super.load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
            return true; // Plugin may be used by all project types
        }

        @Override
        public String getDisplayName() {
            return BUILD_STEP_DISPLAY_NAME;
        }


        public FormValidation doValidateForm(
                @QueryParameter("restApiKey") final String restApiKey,
                @QueryParameter("environmentId") final String environmentId,
                @QueryParameter("applicationId") final String applicationId
        ) {
            return MablStepBuilderValidator.validateForm(restApiKey, environmentId, applicationId);
        }

        public ListBoxModel doFillApplicationIdItems(@QueryParameter String restApiKey) {
            if(restApiKey == null || restApiKey.isEmpty()) {
                ListBoxModel items = new ListBoxModel();
                items.add("Input an Api Key", "");

                return items;
            }

            return getApplicationIdItems(restApiKey);
        }

        private ListBoxModel getApplicationIdItems(String formApiKey) {
            final MablRestApiClient client = new MablRestApiClientImpl(MABL_REST_API_BASE_URL, formApiKey);
            ListBoxModel items = new ListBoxModel();
            try {
                GetApiKeyResult apiKeyResult = ((MablRestApiClientImpl) client).getApiKeyResult(formApiKey);
                String organizationId = apiKeyResult.organization_id;
                GetApplicationsResult applicationsResult = ((MablRestApiClientImpl) client)
                        .getApplicationsResult(organizationId);

                items.add("","");
                for(GetApplicationsResult.Application application : applicationsResult.applications) {
                    items.add(application.name, application.id);
                }

                return items;
            } catch (IOException e) {
            } catch (MablSystemError e) {
            }

            items.add("Input a valid ApiKey", "");
            return items;
        }

        public ListBoxModel doFillEnvironmentIdItems(@QueryParameter String restApiKey) {
            if(restApiKey == null || restApiKey.isEmpty()) {
                ListBoxModel items = new ListBoxModel();
                items.add("Input an Api Key", "");

                return items;
            }

            return getEnvironmentIdItems(restApiKey);
        }

        private ListBoxModel getEnvironmentIdItems(String formApiKey) {
            final MablRestApiClient client = new MablRestApiClientImpl(MABL_REST_API_BASE_URL, formApiKey);
            ListBoxModel items = new ListBoxModel();
            try {
                GetApiKeyResult apiKeyResult = ((MablRestApiClientImpl) client).getApiKeyResult(formApiKey);
                String organizationId = apiKeyResult.organization_id;
                GetEnvironmentsResult environmentsResult = ((MablRestApiClientImpl) client)
                        .getEnvironmentsResult(organizationId);

                items.add("","");
                for(GetEnvironmentsResult.Environment environment : environmentsResult.environments) {
                    items.add(environment.name, environment.id);
                }

                return items;
            } catch (IOException e) {
            } catch (MablSystemError e) {
            }

            items.add("Input a valid ApiKey", "");
            return items;
        }
    }

}
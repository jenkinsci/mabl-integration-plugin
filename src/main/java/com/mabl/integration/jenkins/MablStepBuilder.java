package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.validation.MablStepBuilderValidator;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
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
import static com.mabl.integration.jenkins.MablStepConstants.TEST_OUTPUT_XML_FILENAME;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * mabl custom build step
 */
@SuppressWarnings("unused") // automatically discovered by Jenkins
public class MablStepBuilder extends Builder implements SimpleBuildStep {

    private final String restApiKey;
    private final String environmentId;
    private final String applicationId;
    private boolean continueOnPlanFailure;
    private boolean continueOnMablError;

    @DataBoundConstructor
    public MablStepBuilder(
            final String restApiKey,
            final String environmentId,
            final String applicationId
    ) {
        this.restApiKey = trimToNull(restApiKey);
        this.environmentId = trimToNull(environmentId);
        this.applicationId = trimToNull(applicationId);
    }

    @DataBoundSetter
    public void setContinueOnPlanFailure(boolean continueOnPlanFailure) {
        this.continueOnPlanFailure = continueOnPlanFailure;
    }

    @DataBoundSetter
    public void setContinueOnMablError(boolean continueOnMablError) {
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

    public boolean isContinueOnPlanFailure() {
        return this.continueOnPlanFailure;
    }

    public boolean isContinueOnMablError() {
        return this.continueOnMablError;
    }

    @Override
    public void perform(
            final Run<?, ?> run,
            FilePath workspace,
            final Launcher launcher,
            final TaskListener listener
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
                continueOnMablError,
                getOutputFileLocation(workspace)
        );

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> runnerFuture = executorService.submit(runner);
        try {
            if(runnerFuture.get(EXECUTION_TIMEOUT_SECONDS, SECONDS)) {
                run.setResult(Result.SUCCESS);
            } else {
                run.setResult(Result.FAILURE);
            }
        } catch (ExecutionException e) {
            outputStream.println("There was an execution error trying to run your journeys in mabl");
            e.printStackTrace(outputStream);
            run.setResult(Result.FAILURE);
        } catch (TimeoutException e) {
            outputStream.printf("Oh dear. Your journeys exceeded the max plugin runtime limit of %d seconds.%n" +
                    "We've aborted this Jenkins step, but your journeys may still be running in mabl.", EXECUTION_TIMEOUT_SECONDS);
            run.setResult(Result.FAILURE);
        }
    }

    private FilePath getOutputFileLocation(FilePath workspace) {
        if (workspace == null) {
            return new FilePath(new File(TEST_OUTPUT_XML_FILENAME));
        }
        if(workspace.isRemote()) {
            workspace = new FilePath(workspace.getChannel(), workspace.toString() + File.separator + TEST_OUTPUT_XML_FILENAME);
        } else {
            workspace = new FilePath(new File(workspace.toString() + File.separator + TEST_OUTPUT_XML_FILENAME));
        }

        return workspace;
    }

    /**
     * Descriptor used in views. Centralized metadata store for all {@link MablStepBuilder} instances.
     */
    @Extension
    @Symbol(PLUGIN_SYMBOL)
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

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
                GetApiKeyResult apiKeyResult = client.getApiKeyResult(formApiKey);
                String organizationId = apiKeyResult.organization_id;
                GetApplicationsResult applicationsResult = client.getApplicationsResult(organizationId);

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
                GetApiKeyResult apiKeyResult = client.getApiKeyResult(formApiKey);
                String organizationId = apiKeyResult.organization_id;
                GetEnvironmentsResult environmentsResult = client.getEnvironmentsResult(organizationId);

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
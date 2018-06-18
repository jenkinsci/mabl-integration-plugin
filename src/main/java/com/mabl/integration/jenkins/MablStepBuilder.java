package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.validation.MablStepBuilderValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

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

    public boolean isCollectVars() {
        return getDescriptor().collectVars;
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
                continueOnMablError,
                isCollectVars(),
                getOutputFileLocation(build),
                getEnvironmentVars(build, listener)
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

    private FilePath getOutputFileLocation(AbstractBuild<?, ?> build) {
        FilePath fp = build.getWorkspace();
        if (fp == null) {
            return new FilePath(new File(TEST_OUTPUT_XML_FILENAME));
        }
        if(fp.isRemote()) {
            fp = new FilePath(fp.getChannel(), fp.toString() + File.separator + TEST_OUTPUT_XML_FILENAME);
        } else {
            fp = new FilePath(new File(fp.toString() + File.separator + TEST_OUTPUT_XML_FILENAME));
        }

        return fp;
    }

    private EnvVars getEnvironmentVars(AbstractBuild<?, ?> build, BuildListener listener) {
        final PrintStream outputStream = listener.getLogger();
        EnvVars environmentVars = new EnvVars();
        try {
            environmentVars = build.getEnvironment(listener);
        } catch (IOException e) {
            outputStream.println("There was an error trying to read environment variables.");
            e.printStackTrace(outputStream);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            outputStream.println("There was an interruption during read of environment variables.");
            e.printStackTrace(outputStream);
        }

        return environmentVars;
    }

    /**
     * Descriptor used in views. Centralized metadata store for all {@link MablStepBuilder} instances.
     */
    @Extension
    @Symbol(PLUGIN_SYMBOL)
    public static class MablStepDescriptor extends BuildStepDescriptor<Builder> {
        private boolean collectVars;

        public MablStepDescriptor() {
            super.load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            JSONObject json = formData.getJSONObject("mabl");
            collectVars = json.getBoolean("collectVars");
            save();
            return super.configure(req, formData);
        }

        public boolean isCollectVars() {
            return collectVars;
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
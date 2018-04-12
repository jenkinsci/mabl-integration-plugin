package com.mabl.integration.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.mabl.integration.jenkins.MablStepConstants.BUILD_STEP_DISPLAY_NAME;
import static com.mabl.integration.jenkins.MablStepConstants.EXECUTION_TIMEOUT_SECONDS;
import static com.mabl.integration.jenkins.MablStepConstants.MABL_REST_API_BASE_URL;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_SYMBOL;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        this.restApiKey = restApiKey;
        this.environmentId = environmentId;
        this.applicationId = applicationId;
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
                environmentId,
                applicationId,
                continueOnPlanFailure,
                continueOnMablError
        );

        // TODO crop and cleanup if someone entered string with "key:XXXX"

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> runnerFuture = executorService.submit(runner);
        try {
            return runnerFuture.get(EXECUTION_TIMEOUT_SECONDS, SECONDS);

        } catch (ExecutionException e) {
            outputStream.println("Execution error during mabl deployment step");
            e.printStackTrace(outputStream);

        } catch (TimeoutException e) {
            outputStream.printf("Execution time limit of %d seconds exceeded by mabl deployment step\n", EXECUTION_TIMEOUT_SECONDS);
        }

        return true;
    }

    @Override
    public MablStepDescriptor getDescriptor() {
        return (MablStepDescriptor) super.getDescriptor();
    }

    /**
     * Descriptor used in views. Centralized metadata store for all {@link MablStepBuilder} instances.
     */
    @Extension @Symbol(PLUGIN_SYMBOL)
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
    }
}
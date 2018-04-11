package com.mabl.integration.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.mabl.integration.jenkins.MablStepConstants.BUILD_STEP_DISPLAY_NAME;
import static com.mabl.integration.jenkins.MablStepConstants.EXECUTION_TIMEOUT_SECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * mabl custom build step
 */
@SuppressWarnings("unused") // automatically discovered by Jenkins
public class MablStepBuilder extends Builder {

    private final String restApiKey;
    private final String environmentId;
    private final String applicationId;

    @DataBoundConstructor
    public MablStepBuilder(
            final String restApiKey,
            final String environmentId,
            final String applicationId
    ) {
        this.restApiKey = restApiKey;
        this.environmentId = environmentId;
        this.applicationId = applicationId;
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

        PrintStream outputStream = listener.getLogger();

        final MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                outputStream,
                restApiKey,
                environmentId,
                applicationId
        );

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
    @Extension
    public static class MablStepDescriptor extends BuildStepDescriptor<Builder> {

        public MablStepDescriptor() {
            super.load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
//            return FreeStyleProject.class.isAssignableFrom(clazz);
            return true; // Plugin may be used by all project types
        }

        @Override
        public String getDisplayName() {
            return BUILD_STEP_DISPLAY_NAME;
        }
    }
}
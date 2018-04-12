package com.mabl.integration.jenkins;

import com.google.common.collect.ImmutableSet;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * mabl runner to launch all plans for a given
 * environment and application
 *
 * NOTE: Runner will attempt to run until all tests are completion.
 * It is the responsibility of the Step to terminate at max time.
 */
public class MablStepDeploymentRunner implements Callable<Boolean> {

    private static final long RESULT_POLLING_INTERVAL_MILLISECONDS = 1000;
    private static final Set<String> COMPLETE_STATUSES = ImmutableSet.of(
      "succeeded",
      "failed",
      "cancelled",
      "completed",
      "terminated",
      "post_execution" // ensure bug is fixed
    );

    private final MablRestApiClient client;
    private final PrintStream outputStream;

    private final String environmentId;
    private final String applicationId;
    private final boolean continueOnPlanFailure;
    private final boolean continueOnMablError;

    @SuppressWarnings("WeakerAccess") // required public for DataBound
    @DataBoundConstructor
    public MablStepDeploymentRunner(
            final MablRestApiClient client,
            final PrintStream outputStream,
            final String environmentId,
            final String applicationId,
            final boolean continueOnPlanFailure,
            final boolean continueOnMablError
    ) {
        this.outputStream = outputStream;
        this.client = client;
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.continueOnPlanFailure = continueOnPlanFailure;
        this.continueOnMablError = continueOnMablError;
    }

    @Override
    public Boolean call() {
        try {
            execute();
            return true;

        } catch (MablSystemError error) {
            printException(error);
            return continueOnMablError;

        } catch (MablPlanExecutionFailure failure) {
            printException(failure);
            return continueOnPlanFailure;
        }

        //        catch (Exception e) {
//            outputStream.println("Unexpected exception");
//            e.printStackTrace(outputStream);
//        }
    }

    private void printException(final Exception exception) {
        outputStream.print(exception.getMessage());

        if (exception.getCause() != null) {
            exception.getCause().printStackTrace(outputStream);
        }
    }

    private void execute() throws MablSystemError, MablPlanExecutionFailure {
        // TODO descriptive error messages on 401/403
        // TODO retry on 50x errors (proxy, redeploy)
        outputStream.printf("Creating Deployment API event:\n  environment_id: %s \n  application_id: %s\n",
                environmentId,
                applicationId
        );

        try {
            final CreateDeploymentResult deployment = client.createDeploymentEvent(environmentId, applicationId);

            try {

                // Poll until we are successful or failed - note execution service is responsible for timeout
                ExecutionResult executionResult;
                do {
                    Thread.sleep(RESULT_POLLING_INTERVAL_MILLISECONDS);
                    executionResult = client.getExecutionResults(deployment.id);

                    if(executionResult==null ) {
                        // No such id - this shouldn't happen
                        throw new MablSystemError(String.format("No deployment event for id [%s] in mabl API", deployment.id));
                    }

                } while(!allPlansComplete(executionResult));


                if(!allPlansSuccess(executionResult)) {
                    // TODO better logging specifics about which plan
                    throw new MablPlanExecutionFailure("Plans experienced failure");
                }


            } catch (InterruptedException e) {
                // TODO better error handling/logging
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }

            outputStream.printf("Deployment event created with id [%s]\n", deployment.id);
            outputStream.println("mabl deployment step complete.");
            return;
        } catch (IOException e) {
            outputStream.println("Request error creating deployment event via mabl API.");
        }

        finally {
            if (client != null) {
                client.close();
            }
        }

        throw new MablSystemError("Unable to properly execute plans");
    }

    private boolean allPlansComplete(final ExecutionResult result) {

        boolean isComplete = true;

        for(ExecutionResult.ExecutionSummary summary : result.executions) {
            isComplete &= COMPLETE_STATUSES.contains(summary.status.toLowerCase());
        }

        return isComplete;
    }

    private boolean allPlansSuccess(final ExecutionResult result) {

        boolean isSuccess = true;

        for(ExecutionResult.ExecutionSummary summary : result.executions) {
            isSuccess &= summary.success;
        }

        return isSuccess;
    }
}
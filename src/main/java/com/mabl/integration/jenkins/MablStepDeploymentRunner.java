package com.mabl.integration.jenkins;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;

import static com.mabl.integration.jenkins.MablStepConstants.MABL_REST_API_BASE_URL;
import static org.apache.commons.httpclient.HttpStatus.SC_CREATED;

/**
 * mabl runner to launch all plans for a given
 * environment and application
 */
public class MablStepDeploymentRunner implements Callable<Boolean> {

    private final String restApiKey;
    private final String environmentId;
    private final String applicationId;
    private final boolean continueOnPlanFailure;
    private final boolean continueOnMablError;
    private final PrintStream outputStream;

    @SuppressWarnings("WeakerAccess") // required public for DataBound
    @DataBoundConstructor
    public MablStepDeploymentRunner(
            final PrintStream outputStream,
            final String restApiKey,
            final String environmentId,
            final String applicationId,
            final boolean continueOnPlanFailure,
            final boolean continueOnMablError
    ) {
        this.outputStream = outputStream;
        this.restApiKey = restApiKey;
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
            // TODO consolidate printing error messages
            return continueOnMablError;
        } catch (MablPlanExecutionFailure failure) {
            // TODO consolidate printing error messages
            return continueOnPlanFailure;
        }
    }

    private void execute() throws MablSystemError, MablPlanExecutionFailure {
        // TODO descriptive error messages on 401/403
        // TODO retry on 50x errors (proxy, redeploy)
        outputStream.printf("Creating Deployment API event:\n  environment_id: %s \n  application_id: %s\n",
                environmentId,
                applicationId
        );

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClient(MABL_REST_API_BASE_URL, restApiKey);
            CloseableHttpResponse response = client.createDeploymentEvent(environmentId, applicationId);

            final int statusCode = response.getStatusLine().getStatusCode();
            if(SC_CREATED != response.getStatusLine().getStatusCode()) {
                outputStream.printf("Unexpected status from mabl API on deployment event creation: %d\n"+
                        "body: [%s]", statusCode, EntityUtils.toString((response.getEntity())));

                throw new MablSystemError(String.format(
                        "Unexpected status from mabl API on deployment event creation: %d", statusCode
                ));
            }

            // TODO capture test failure
            if(false) {
                throw new MablPlanExecutionFailure("Plan XXX failed");
            }

            MablRestApiClient.CreateDeploymentResult result = client.parseCreateDeploymentEventResponse(response);
            final String deploymentEventId = result.id;

            outputStream.printf("Deployment event created with id [%s]\n", deploymentEventId);
            outputStream.println("mabl deployment step complete.");
            return;
        }
        catch (IOException e) {
            outputStream.println("Request error creating deployment event via mabl API.");
        }
//        catch (Exception e) {
//            outputStream.println("Unexpected exception");
//            e.printStackTrace(outputStream);
//        }
        finally {
            if (client != null) {
                client.close();
            }
        }

        throw new MablSystemError("Unable to properly execute plans");
    }
}
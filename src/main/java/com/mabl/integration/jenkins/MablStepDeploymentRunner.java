package com.mabl.integration.jenkins;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.mabl.integration.jenkins.MablStepConstants.MABL_REST_API_BASE_URL;
import static com.mabl.integration.jenkins.MablStepConstants.REQUEST_TIMEOUT_SECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.httpclient.HttpStatus.SC_CREATED;

/**
 * mabl runner to launch all plans for a given
 * environment and application
 */
public class MablStepDeploymentRunner implements Callable<Boolean> {

    private final String restApiKey;
    private final String environmentId;
    private final String applicationId;
    private final PrintStream outputStream;

    @SuppressWarnings("WeakerAccess") // required public for DataBound
    @DataBoundConstructor
    public MablStepDeploymentRunner(
            final PrintStream outputStream,
            final String restApiKey,
            final String environmentId,
            final String applicationId
    ) {
        this.restApiKey = restApiKey;
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.outputStream = outputStream;
    }

    @Override
    public Boolean call() {

        // TODO descriptive error messages on 401/403
        // TODO retry on 50x errors (proxy, redeploy)
        outputStream.printf("Creating Deployment API event:\n  environment_id: %s \n  application_id: %s\n",
                environmentId,
                applicationId
        );

        MablRestApiClient client = null;
        try {
            client = new MablRestApiClient(MABL_REST_API_BASE_URL, restApiKey);
            Future<HttpResponse> responseFuture = client.createDeploymentEvent(environmentId, applicationId);
            HttpResponse response = responseFuture.get(REQUEST_TIMEOUT_SECONDS, SECONDS);

            final int statusCode = response.getStatusLine().getStatusCode();
            if(SC_CREATED != response.getStatusLine().getStatusCode()) {
                outputStream.printf("Unexpected status from mabl API on deployment event creation: %d\n"+
                        "body: [%s]", statusCode, EntityUtils.toString((response.getEntity())));
                return false;
            }

            MablRestApiClient.CreateDeploymentResult result = client.parseCreateDeploymentEventReponse(response);
            final String deploymentEventId = result.id;

            outputStream.printf("Deployment event created with id [%s]\n", deploymentEventId);
            outputStream.println("mabl deployment step complete.");
            return true;
        }
        catch (TimeoutException e) {
            outputStream.printf("Request timeout creating deployment event via mabl API.\n Default timeout %d seconds\n", REQUEST_TIMEOUT_SECONDS);
        }
        catch (Exception e) {
            outputStream.println("Unexpected exception");
            e.printStackTrace(outputStream);
        }
        finally {
            if (client != null) {
                client.close();
            }
        }

        return false;
    }
}
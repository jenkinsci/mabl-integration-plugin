package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.domain.GetLabelsResult;

import java.io.IOException;

public interface MablRestApiClient {

    CreateDeploymentResult createDeploymentEvent(
            String environmentId,
            String applicationId,
            String labels,
            String mablBranch,
            CreateDeploymentProperties properties
    ) throws IOException;

    /**
     * Attempt to fetch results for given deployment event
     *
     * @param eventId deployment event identifier
     * @return partially parsed download, or null on 404
     * @throws IOException on parsing error
     */
    ExecutionResult getExecutionResults(
            String eventId
    ) throws IOException;

    /**
     * Attempt to fetch full ApiKey Obj
     *
     * @return parsed ApiKey, or null on 404
     * @throws IOException on parsing error
     */
    GetApiKeyResult getApiKeyResult(
    ) throws IOException;

    /**
     * Attempt to fetch List of Applications for the given organization
     *
     * @param organizationId The organizationId for the given ApiKey
     * @return parsed List<Application>, or null on 404
     * @throws IOException on parsing error
     */
    GetApplicationsResult getApplicationsResult(
            String organizationId
    ) throws IOException;

    /**
     * Attempt to fetch List of Environments for the given organization
     *
     * @param organizationId The organizationId for the given ApiKey
     * @return parsed List<Environment>, or null on 404
     * @throws IOException on parsing error
     */
    GetEnvironmentsResult getEnvironmentsResult(
            String organizationId
    ) throws IOException;

    /**
     * Attempt to fetch List of Labels for the given organization
     *
     * @param organizationId The organizationId for the given ApiKey
     * @return parsed List<String>, or null on 404
     * @throws IOException on parsing error
     */
    GetLabelsResult getLabelsResult(
            String organizationId
    ) throws IOException;

    /**
     * Returns the base URL of the mabl app.
     *
     * @return the base URL of the mabl app.
     */
    String getAppBaseUrl();

    /**
     * Performs testing the health endpoint to ensure that connection is working.
     *
     * @throws IOException on connection failure
     */
    void checkConnection() throws IOException;

    void close();
}

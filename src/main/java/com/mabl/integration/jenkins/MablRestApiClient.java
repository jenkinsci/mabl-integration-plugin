package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;

import java.io.IOException;

public interface MablRestApiClient {

    CreateDeploymentResult createDeploymentEvent(
            String environmentId,
            String applicationId,
            CreateDeploymentProperties properties
    ) throws IOException, MablSystemError;

    /**
     * Attempt to fetch results for given deployment event
     *
     * @param eventId deployment event identifier
     * @return partially parsed download, or null on 404
     * @throws IOException on parsing error
     * @throws MablSystemError on non 200 or 404 response
     */
    ExecutionResult getExecutionResults(
            String eventId
    ) throws IOException, MablSystemError;

    /**
     * Attempt to fetch full ApiKey Obj from ApiKey String
     *
     * @param restApiKey restApiKey
     * @return parsed ApiKey, or null on 404
     * @throws IOException on parsing error
     * @throws MablSystemError on non 200 or 404 response
     */
    GetApiKeyResult getApiKeyResult(
            String restApiKey
    ) throws IOException, MablSystemError;

    /**
     * Attempt to fetch List of Applications for the given organization
     *
     * @param organizationId The organizationId for the given ApiKey
     * @return parsed List<Application>, or null on 404
     * @throws IOException on parsing error
     * @throws MablSystemError on non 200 or 404 response
     */
    GetApplicationsResult getApplicationsResult(
            String organizationId
    ) throws IOException, MablSystemError;

    /**
     * Attempt to fetch List of Environments for the given organization
     *
     * @param organizationId The organizationId for the given ApiKey
     * @return parsed List<Environment>, or null on 404
     * @throws IOException on parsing error
     * @throws MablSystemError on non 200 or 404 response
     */
    GetEnvironmentsResult getEnvironmentsResult(
            String organizationId
    ) throws IOException, MablSystemError;

    void close();
}

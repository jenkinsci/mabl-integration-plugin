package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;

import java.io.IOException;

public interface MablRestApiClient {

    CreateDeploymentResult createDeploymentEvent(
            String environmentId,
            String applicationId
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

    void close();
}

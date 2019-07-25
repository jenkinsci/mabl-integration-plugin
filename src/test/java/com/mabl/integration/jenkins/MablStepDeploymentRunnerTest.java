package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import hudson.EnvVars;
import hudson.FilePath;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test runner
 */
public class MablStepDeploymentRunnerTest {

    private static final long TEST_TIMEOUT_SECONDS = 2;
    private static final long TEST_POLLING_INTERVAL_MILLISECONDS = 50;

    private final String environmentId = "foo-env-e";
    private final String applicationId = "foo-app-a";
    private final List<String> labels = Collections.singletonList("foo-label");
    private final String eventId = "foo-event-id";
    private final FilePath buildPath = new FilePath(new File("/dev/null"));
    private final EnvVars envVars = new EnvVars();

    private MablStepDeploymentRunner runner;
    private MablRestApiClient client;
    private PrintStream outputStream;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(TEST_TIMEOUT_SECONDS);

    @Before
    public void setup() {
        client = mock(MablRestApiClient.class);
        outputStream = mock(PrintStream.class);
        runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                false,
                false,
                true,
                buildPath,
                envVars
        );
    }

    @Test
    public void runTestsHappyPath() throws IOException, MablSystemError {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("succeeded", true));

        assertTrue("successful outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsHappyPathManyPollings() throws IOException, MablSystemError {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("queued", true))
                .thenReturn(createExecutionResult("pre-execution", true))
                .thenReturn(createExecutionResult("scheduling", true))
                .thenReturn(createExecutionResult("scheduled", true))
                .thenReturn(createExecutionResult("running", true))
                .thenReturn(createExecutionResult("post-execution", true))
                .thenReturn(createExecutionResult("completed", true));

        assertTrue("successful outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsMablErrorOnCreateDeployment() throws IOException, MablSystemError {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenThrow(new MablSystemError("mabl error"));

        assertFalse("failure outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsMablErrorDeploymentResultsNotFound() throws IOException, MablSystemError {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenThrow(new MablSystemError("mabl error"));

        when(client.getExecutionResults(eventId)).thenReturn(null);

        assertFalse("failure outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsPlanFailure() throws IOException, MablSystemError {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("failed", false));

        assertFalse("failure outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void continueOnMablError() throws IOException, MablSystemError {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                false,
                true,
                true,
                buildPath,
                envVars
        );

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenThrow(new MablSystemError("mabl error"));

        assertTrue("failure override expected", runner.call());

        verify(client).close();
    }

    @Test
    public void continueOnPlanFailure() throws IOException, MablSystemError {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                true,
                false,
                true,
                buildPath,
                envVars
        );

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("queued", true))
                .thenReturn(createExecutionResult("terminated", false));

        assertTrue("failure override expected", runner.call());

        verify(client).close();
    }

    private ExecutionResult createExecutionResult(
            final String status,
            final boolean success
    ) {
        return new ExecutionResult(
                singletonList(
                        new ExecutionResult.ExecutionSummary
                                (status, "all is well",
                                        success, 0L, 0L,
                                        null,
                                        null,
                                        new ArrayList<ExecutionResult.JourneySummary>(),
                                        new ArrayList<ExecutionResult.JourneyExecutionResult>()
                                )));
    }
}

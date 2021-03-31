package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.test.output.Properties;
import com.mabl.integration.jenkins.test.output.Property;
import com.mabl.integration.jenkins.test.output.TestCase;
import com.mabl.integration.jenkins.test.output.TestSuite;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private final String mablBranch = "my-development-branch";
    private final String labels = "foo-label";
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
                null,
                false,
                false,
                true,
                buildPath,
                envVars
        );
    }

    @Test(timeout = 6000000)
    public void runTestsHappyPath() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("succeeded", true));

        assertTrue("successful outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsHappyPathManyPollings() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

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
    public void runTestsMablErrorOnCreateDeployment() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenThrow(new MablSystemException("mabl error"));

        assertFalse("failure outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsMablErrorDeploymentResultsNotFound() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenThrow(new MablSystemException("mabl error"));

        when(client.getExecutionResults(eventId)).thenReturn(null);

        assertFalse("failure outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void runTestsPlanFailure() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("failed", false));

        assertFalse("failure outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void continueOnMablError() throws IOException {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                null,
                false,
                true,
                true,
                buildPath,
                envVars
        );

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenThrow(new MablSystemException("mabl error"));

        assertTrue("failure override expected", runner.call());

        verify(client).close();
    }

    @Test
    public void continueOnPlanFailure() throws IOException {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                null,
                true,
                false,
                true,
                buildPath,
                envVars
        );

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("queued", true))
                .thenReturn(createExecutionResult("terminated", false));

        assertTrue("failure override expected", runner.call());

        verify(client).close();
    }

    @Test
    public void planWithRetrySuccess() throws IOException {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                null,
                false,
                false,
                true,
                buildPath,
                envVars
        );

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResultWithRetry(true));

        assertTrue("success expected on successful retry", runner.call());

        verify(client).close();
    }

    @Test
    public void planWithRetryFailure() throws IOException {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                null,
                false,
                false,
                true,
                buildPath,
                envVars
        );

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResultWithRetry(false));

        assertFalse("failure expected", runner.call());

        verify(client).close();
    }

    @Test
    public void planWithMablBranch() throws IOException {
        MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                mablBranch,
                false,
                false,
                true,
                buildPath,
                envVars
        );

        final CreateDeploymentResult createDeploymentResult = new CreateDeploymentResult(eventId, "workspace-w");
        createDeploymentResult.setMablBranch(mablBranch);

        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), eq(mablBranch), any(CreateDeploymentProperties.class)))
                .thenReturn(createDeploymentResult);

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("completed", true));

        assertTrue("successful outcome expected", runner.call());

        verify(client).close();
    }

    @Test
    public void planWithFailedEventCreation() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(null);

        assertFalse("failure expected as continue on error is false", runner.call());

        verify(client).close();
    }

    @Test
    public void executionResultToString_undefinedStatus_isWaiting() {
        ExecutionResult.JourneyExecutionResult result = new ExecutionResult.JourneyExecutionResult(
            null,
            null,
            null,
            null,
            null, // <-- status is null
            null,
            false,
            0L,
            0L,
                Collections.emptyList());

        assertEquals("[waiting]", MablStepDeploymentRunner.executionResultToString(result));
    }

    @Test
    public void executionResultToString_failed_showsURL() {
        ExecutionResult.JourneyExecutionResult result = new ExecutionResult.JourneyExecutionResult(
            null,
            null,
            null,
            "http://appUrl",
            "failed",
            null,
            false,
            0L,
            0L,
                Collections.emptyList());

        assertEquals("[failed] at [http://appUrl]", MablStepDeploymentRunner.executionResultToString(result));
    }

    @Test
    public void executionResultToString_completed_showsCompleted() {
        ExecutionResult.JourneyExecutionResult result = new ExecutionResult.JourneyExecutionResult(
            null,
            null,
            null,
            null,
            "completed",
            null,
            false,
            0L,
            0L,
                Collections.emptyList());

        assertEquals("[completed]", MablStepDeploymentRunner.executionResultToString(result));
    }

    @Test
    public void executionSummary_times() {
        ExecutionResult executionResult = createExecutionResultWithTimes();
        for (ExecutionResult.ExecutionSummary summary : executionResult.executions) {
            TestSuite suite = runner.createTestSuite(summary);
            assertEquals(suite.getTime(), (summary.stopTime - summary.startTime) / 1000);
            ExecutionResult.JourneyExecutionResult firstTest = summary.journeyExecutions.get(0);
            assertEquals(suite.getTestCases().get(0).getDuration(),
                    (firstTest.stopTime - firstTest.startTime) / 1000);
        }
    }

    @Test
    public void executionSummary_testCaseIdsAndSkipped() {
        ExecutionResult executionResult = createExecutionResultWithTestCaseIds();
        for (ExecutionResult.ExecutionSummary summary : executionResult.executions) {
            TestSuite suite = runner.createTestSuite(summary);
            Properties props = suite.getProperties();
            assertNotNull(props);
            Collection<Property> propertyCollection = props.getProperties();
            assertNotNull(propertyCollection);
            boolean foundFailed = false;
            boolean foundCompleted = false;
            boolean foundSkipped = false;
            for (Property property : propertyCollection) {
                switch (property.getName()) {
                    case "failed-test-cases":
                        assertEquals("FAILED-1,FAILED-91", property.getValue());
                        foundFailed = true;
                        break;
                    case "completed-test-cases":
                        assertEquals("COMPLETED-2", property.getValue());
                        foundCompleted = true;
                        break;
                    case "skipped-test-cases":
                        assertEquals("SKIPPED-3,SKIPPED-33,SKIPPED-333", property.getValue());
                        foundSkipped = true;
                        break;
                    default:
                        fail();
                }
            }
            assertTrue(foundCompleted);
            assertTrue(foundFailed);
            assertTrue(foundSkipped);
            assertEquals(1, suite.getFailures());
            assertEquals(1, suite.getSkipped());
            assertEquals(3, suite.getTests());

            for (TestCase testCase : suite.getTestCases()) {
                Properties caseProperties = testCase.getProperties();
                assertNotNull(caseProperties);
                Collection<Property> casePropertyCollection = caseProperties.getProperties();
                assertNotNull(casePropertyCollection);
                Property caseProperty = casePropertyCollection.iterator().next();
                assertEquals("requirement", caseProperty.getName());
                if (testCase.getFailure() != null) {
                    assertEquals("FAILED-1,FAILED-91", caseProperty.getValue());
                    assertNull(testCase.getSkipped());
                } else if (testCase.getSkipped() != null) {
                    assertEquals("SKIPPED-3,SKIPPED-33,SKIPPED-333", caseProperty.getValue());
                    assertNull(testCase.getFailure());
                } else {
                    assertEquals("COMPLETED-2", caseProperty.getValue());
                    assertNull(testCase.getSkipped());
                    assertNull(testCase.getFailure());
                }
            }
        }
    }


    private ExecutionResult createExecutionResultWithTestCaseIds() {
        ExecutionResult.EventStatus eventStatus = new ExecutionResult.EventStatus();
        eventStatus.setSucceeded(false);
        eventStatus.setSucceededFirstAttempt(false);
        return new ExecutionResult(
                singletonList(
                        new ExecutionResult.ExecutionSummary
                                ("failed", "some tests failed",
                                        true, 1596323475000L, 1596323575000L,
                                        null,
                                        null,
                                        new ArrayList<>(),
                                        Arrays.asList(
                                                new ExecutionResult.JourneyExecutionResult(
                                                "failingTestRun-jr",
                                                "executionId1",
                                                "http://www.example.com",
                                                "http://app.example.com",
                                                "failed",
                                                "failed because ",
                                                false,
                                                1596323475000L,
                                                1596323565000L,
                                                 Arrays.asList(
                                                         new ExecutionResult.TestCaseID("FAILED-1"),
                                                         new ExecutionResult.TestCaseID("FAILED-91"))),
                                                new ExecutionResult.JourneyExecutionResult(
                                                        "skippedTestRun-jr",
                                                        "executionId2",
                                                        "http://www.example.com",
                                                        "http://app.example.com",
                                                        "skipped",
                                                        "skipped because a dependent test failed",
                                                        false,
                                                        1596323475001L,
                                                        1596323565001L,
                                                        Arrays.asList(
                                                                new ExecutionResult.TestCaseID("SKIPPED-3"),
                                                                new ExecutionResult.TestCaseID("SKIPPED-33"),
                                                                new ExecutionResult.TestCaseID("SKIPPED-333"))),
                                                new ExecutionResult.JourneyExecutionResult(
                                                        "completedTestRun-jr",
                                                        "executionId3",
                                                        "http://www.example.com",
                                                        "http://app.example.com",
                                                        "completed",
                                                        "success",
                                                        false,
                                                        1596323475002L,
                                                        1596323565002L,
                                                        singletonList(
                                                                new ExecutionResult.TestCaseID("COMPLETED-2")))
                                        )
                                )
                ),
                eventStatus
        );
    }

    private ExecutionResult createExecutionResultWithTimes() {
        ExecutionResult.EventStatus eventStatus = new ExecutionResult.EventStatus();
        eventStatus.setSucceeded(true);
        eventStatus.setSucceededFirstAttempt(false);
        return new ExecutionResult(
                Arrays.asList(
                        new ExecutionResult.ExecutionSummary
                                ("failed", "first attempt failed",
                                        true, 1596323475000L, 1596323575000L,
                                        null,
                                        null,
                                        new ArrayList<>(),
                                        singletonList(new ExecutionResult.JourneyExecutionResult(
                                                "firstJourneyRun-jr",
                                                "executionId1",
                                                "http://www.example.com",
                                                "http://app.example.com",
                                                "failed",
                                                "failed on the first run",
                                                false,
                                                1596323475000L,
                                                1596323565000L,
                                                Collections.emptyList())
                                        ))
                        ,
                        new ExecutionResult.ExecutionSummary
                                ("completed", "retry succeeded", true
                                        , 1596323575000L, 1596323775000L,
                                        null,
                                        null,
                                        new ArrayList<>(),
                                        singletonList(new ExecutionResult.JourneyExecutionResult(
                                                "secondJourneyRun-jr",
                                                "executionId2",
                                                "http://www.example.com",
                                                "http://app.example.com",
                                                "success",
                                                "succeeded",
                                                false,
                                                1596323575000L,
                                                1596323755000L,
                                                Collections.emptyList())
                                        ))
                ),
                eventStatus
        );
    }

    private ExecutionResult createExecutionResult(
            final String status,
            final boolean success
    )
    {
        ExecutionResult.EventStatus eventStatus = new ExecutionResult.EventStatus();
        eventStatus.setSucceeded(success);
        return new ExecutionResult(
                singletonList(
                        new ExecutionResult.ExecutionSummary
                                (status, "all is well",
                                        success, 0L, 0L,
                                        new ExecutionResult.PlanSummary("plan-id", "Plan name"),
                                        new ExecutionResult.PlanExecutionResult("plan-id", true),
                                        new ArrayList<>(),
                                        new ArrayList<>()
                                )),
                eventStatus);
    }

    private ExecutionResult createExecutionResultWithRetry(
            final boolean success
    )
    {
        ExecutionResult.EventStatus eventStatus = new ExecutionResult.EventStatus();
        eventStatus.setSucceeded(success);
        eventStatus.setSucceededFirstAttempt(false);
        return new ExecutionResult(
                Arrays.asList(
                        new ExecutionResult.ExecutionSummary
                                ("failed", "first attempt failed",
                                        success, 0L, 0L,
                                        new ExecutionResult.PlanSummary("plan-id", "Plan name"),
                                        null,
                                        new ArrayList<>(),
                                        new ArrayList<>()
                                ),
                        new ExecutionResult.ExecutionSummary
                                ("completed", "retry succeeded",
                                        success, 0L, 0L,
                                        new ExecutionResult.PlanSummary("plan-id", "Plan name"),
                                        null,
                                        new ArrayList<>(),
                                        new ArrayList<>()
                                )),
                eventStatus);
    }
}

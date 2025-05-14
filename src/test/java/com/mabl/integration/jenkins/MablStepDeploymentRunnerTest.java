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
import java.util.*;
import java.io.ByteArrayOutputStream;

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
    private final String webUrlOverride = "https://test-web-override.example.com";
    private final String apiUrlOverride = "https://test-api-override.example.com";
    private final List<String> browser = Arrays.asList("firefox", "Chrome", "safari", "Edge");
    private final String revision = "Plan successful";

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
                envVars,
                null,
                null,
                browser,
                revision
        );
    }

    @Test
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
                envVars,
                null,
                null,
                browser,
                revision
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
                envVars,
                null,
                null,
                browser,
                revision
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
                envVars,
                null,
                null,
                browser,
                revision
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
                envVars,
                null,
                null,
                browser,
                revision
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
                envVars,
                null,
                null,
                browser,
                revision
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
                        assertEquals("FAILED-1,FAILED-11,FAILED-91", property.getValue());
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
            assertEquals(2, suite.getFailures());
            assertEquals(1, suite.getSkipped());
            assertEquals(4, suite.getTests());

            for (TestCase testCase : suite.getTestCases()) {
                Collection<Property> casePropertyCollection = testCase.getProperties();
                assertNotNull(casePropertyCollection);
                Property caseProperty = casePropertyCollection.iterator().next();
                assertEquals("requirement", caseProperty.getName());
                if (testCase.getFailure() != null) {
                    switch (testCase.getJourney()) {
                        case "Failing Test 1":
                            assertEquals("FAILED-1,FAILED-91", caseProperty.getValue());
                            break;
                        case "Failing Test 2":
                            assertEquals("FAILED-11", caseProperty.getValue());
                            break;
                        default:
                            System.err.println("journey:" + testCase.getJourney());
                            fail();
                    }
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

    @Test
    public void testInterruptedExecution() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class))).
                thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        when(client.getExecutionResults(eventId))
                .thenAnswer(invocation ->{
                    Thread.currentThread().interrupt();
                    return createExecutionResult("running",true);
                });

        assertTrue("successful outcome expected due to interruption handling", runner.call());

        assertFalse("Thread Interrupted status should be clear",Thread.currentThread().isInterrupted());

        verify(client).close();
    }

    @Test
    public void testSafePlanNameWithNull() throws IOException {
        when(client.createDeploymentEvent(eq(environmentId), eq(applicationId), eq(labels), isNull(), any(CreateDeploymentProperties.class)))
                .thenReturn(new CreateDeploymentResult(eventId, "workspace-w"));

        ExecutionResult.EventStatus eventStatus = new ExecutionResult.EventStatus();
        eventStatus.setSucceeded(true);
        ExecutionResult executionResult = new ExecutionResult(
                singletonList(
                        new ExecutionResult.ExecutionSummary(
                                "completed", "success", true, 0L, 0L,
                                new ExecutionResult.PlanSummary("plan-id", null),
                                null,
                                List.of(
                                        new ExecutionResult.JourneySummary("journey-id", "Test Journey", "href", "appHref")
                                ),
                                List.of(
                                        new ExecutionResult.JourneyExecutionResult(
                                                "journey-id", "execution-id", "href", "appHref",
                                                "completed", null, true, 0L, 0L, Collections.emptyList()
                                        )
                                )
                        )
                ),
                eventStatus);

        when(client.getExecutionResults(eventId))
                .thenReturn(executionResult);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream capturedOutputStream = new PrintStream(byteArrayOutputStream);
        MablStepDeploymentRunner runnerWithCapture = new MablStepDeploymentRunner(
                client,
                capturedOutputStream,
                TEST_POLLING_INTERVAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                null,
                false,
                false,
                true,
                buildPath,
                envVars,
                null,
                null,
                browser,
                revision
        );

        assertTrue("successful outcome expected", runnerWithCapture.call());

        String output = byteArrayOutputStream.toString();
        assertTrue("Output should contain unnamed plan fallback", output.contains("<Unnamed Plan>"));

        TestSuite testSuite = runnerWithCapture.createTestSuite(executionResult.executions.get(0));
        assertEquals("<Unnamed Plan>", testSuite.getName());
    }

    @Test
    public void testDeploymentCreateWithUrlOverrides() throws IOException {
        MablStepDeploymentRunner runnerWithOverrides = new MablStepDeploymentRunner(
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
                envVars,
                webUrlOverride,
                apiUrlOverride,
                browser,
                revision
        );

        // Capture the properties that would be sent to mabl
        when(client.createDeploymentEvent(
                eq(environmentId),
                eq(applicationId),
                eq(labels),
                isNull(),
                any(CreateDeploymentProperties.class)))
                .thenAnswer(invocation -> {
                    CreateDeploymentProperties props = invocation.getArgument(4);
                    assertNotNull("Properties should not be null", props);
                    assertNotNull("Plan overrides should not be null", props.getPlan_overrides());
                    assertEquals("Web URL override should match", webUrlOverride, props.getPlan_overrides().getWeb_url());
                    assertEquals("API URL override should match", apiUrlOverride, props.getPlan_overrides().getApi_url());
                    return new CreateDeploymentResult(eventId, "workspace-w");
                });

        when(client.getExecutionResults(eventId))
                .thenReturn(createExecutionResult("succeeded", true));

        assertTrue("successful outcome expected", runnerWithOverrides.call());
    }

    @Test
    public void testExecutionSummaryWithTestCaseStatus() {
        ExecutionResult.EventStatus eventStatus = new ExecutionResult.EventStatus();
        eventStatus.setSucceeded(false);

        ExecutionResult.ExecutionSummary summary = new ExecutionResult.ExecutionSummary(
                "failed", "Some tests failed", false, 1000L, 5000L,
                new ExecutionResult.PlanSummary("plan-id", "Plan name"),
                null,
                Arrays.asList(
                        new ExecutionResult.JourneySummary("j1", "Journey Failed", "href1", "appHref1"),
                        new ExecutionResult.JourneySummary("j2", "Journey Skipped", "href2", "appHref2")
                ),
                Arrays.asList(
                        new ExecutionResult.JourneyExecutionResult(
                                "j1", "exec1", "href1", "appHref1", "failed", "Failed due to assertion", false, 1000L, 3000L,
                                Arrays.asList(
                                        new ExecutionResult.TestCaseID("TEST-123"),
                                        new ExecutionResult.TestCaseID("TEST-456")
                                )
                        ),
                        new ExecutionResult.JourneyExecutionResult(
                                "j2", "exec2", "href2", "appHref2", "skipped", "Skipped due to dependency", false, 1000L, 1000L,
                                List.of(
                                        new ExecutionResult.TestCaseID("TEST-789")
                                )
                        )
                )
        );

        TestSuite testSuite = runner.createTestSuite(summary);

        assertEquals(2, testSuite.getTests());
        assertEquals(1, testSuite.getFailures());
        assertEquals(1, testSuite.getSkipped());

        boolean foundFailedTestCases = false;
        boolean foundSkippedTestCases = false;

        for (Property property : testSuite.getProperties().getProperties()) {
            if (property.getName().equals("failed-test-cases")) {
                assertEquals("TEST-123,TEST-456", property.getValue());
                foundFailedTestCases = true;
            } else if (property.getName().equals("skipped-test-cases")) {
                assertEquals("TEST-789", property.getValue());
                foundSkippedTestCases = true;
            }
        }

        assertTrue("Failed test cases should be recorded in properties", foundFailedTestCases);
        assertTrue("Skipped test cases should be recorded in properties", foundSkippedTestCases);

        // Verify test cases have correct requirement property
        for (TestCase testCase : testSuite.getTestCases()) {
            if (testCase.getJourney().equals("Journey Failed")) {
                Property requirement = testCase.getProperties().iterator().next();
                assertEquals("requirement", requirement.getName());
                assertEquals("TEST-123,TEST-456", requirement.getValue());
            } else if (testCase.getJourney().equals("Journey Skipped")) {
                Property requirement = testCase.getProperties().iterator().next();
                assertEquals("requirement", requirement.getName());
                assertEquals("TEST-789", requirement.getValue());
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
                                        Arrays.asList(
                                                new ExecutionResult.JourneySummary(
                                                        "failingTestId1-j",
                                                        "Failing Test 1",
                                                        "https://app.example.com",
                                                        "https://app.example.com"),
                                                new ExecutionResult.JourneySummary(
                                                        "skippedTestId-j",
                                                        "Skipped Test",
                                                        "https://app.example.com",
                                                        "https://app.example.com"),
                                                new ExecutionResult.JourneySummary(
                                                        "completedTestId-j",
                                                        "Completed Test",
                                                        "https://app.example.com",
                                                        "https://app.example.com"),
                                                new ExecutionResult.JourneySummary(
                                                        "failingTestId2-j",
                                                        "Failing Test 2",
                                                        "https://app.example.com",
                                                        "https://app.example.com")
                                        ),
                                        Arrays.asList(
                                                new ExecutionResult.JourneyExecutionResult(
                                                "failingTestId1-j",
                                                "failingTestId1-jr",
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
                                                        "skippedTestId-j",
                                                        "skippedTestId-jr",
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
                                                        "completedTestId-j",
                                                        "completedTestId-jr",
                                                        "http://www.example.com",
                                                        "http://app.example.com",
                                                        "completed",
                                                        "success",
                                                        false,
                                                        1596323475002L,
                                                        1596323565002L,
                                                        singletonList(
                                                                new ExecutionResult.TestCaseID("COMPLETED-2"))),
                                                new ExecutionResult.JourneyExecutionResult(
                                                        "failingTestId2-j",
                                                        "failingTestId2-jr",
                                                        "http://www.example.com",
                                                        "http://app.example.com",
                                                        "failed",
                                                        "failed because ",
                                                        false,
                                                        1596323475003L,
                                                        1596323565003L,
                                                        singletonList(
                                                                new ExecutionResult.TestCaseID("FAILED-11")))
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

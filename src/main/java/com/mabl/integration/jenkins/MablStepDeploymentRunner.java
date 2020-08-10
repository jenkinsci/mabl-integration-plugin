package com.mabl.integration.jenkins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;
import com.mabl.integration.jenkins.domain.CreateDeploymentResult;
import com.mabl.integration.jenkins.domain.ExecutionResult;
import com.mabl.integration.jenkins.test.output.Failure;
import com.mabl.integration.jenkins.test.output.TestCase;
import com.mabl.integration.jenkins.test.output.TestSuite;
import com.mabl.integration.jenkins.test.output.TestSuites;
import hudson.EnvVars;
import hudson.FilePath;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_NAME;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_VERSION;

/**
 * mabl runner to launch all plans for a given
 * environment and application
 * <p>
 * NOTE: Runner will attempt to run until all tests are completion.
 * It is the responsibility of the Step to terminate at max time.
 */
public class MablStepDeploymentRunner implements Callable<Boolean> {

    private static final Set<String> COMPLETE_STATUSES = ImmutableSet.of(
            "succeeded",
            "failed",
            "cancelled",
            "completed",
            "terminated"
    );

    private final MablRestApiClient client;
    private final PrintStream outputStream;
    private final long pollingIntervalMilliseconds;

    private final String environmentId;
    private final String applicationId;
    private final String labels;
    private final String mablBranch;
    private final boolean continueOnPlanFailure;
    private final boolean continueOnMablError;
    private final boolean collectVars;
    private final FilePath buildPath;
    private final EnvVars environmentVars;

    @SuppressWarnings("WeakerAccess") // required public for DataBound
    @DataBoundConstructor
    public MablStepDeploymentRunner(
            final MablRestApiClient client,
            final PrintStream outputStream,
            final long pollingIntervalMilliseconds,
            final String environmentId,
            final String applicationId,
            final String labels,
            final String mablBranch,
            final boolean continueOnPlanFailure,
            final boolean continueOnMablError,
            final boolean collectVars,
            final FilePath buildPath,
            final EnvVars environmentVars

    ) {
        this.outputStream = outputStream;
        this.client = client;
        this.pollingIntervalMilliseconds = pollingIntervalMilliseconds;
        this.environmentId = environmentId;
        this.applicationId = applicationId;
        this.labels = labels;
        this.mablBranch = mablBranch;
        this.continueOnPlanFailure = continueOnPlanFailure;
        this.continueOnMablError = continueOnMablError;
        this.collectVars = collectVars;
        this.buildPath = buildPath;
        this.environmentVars = environmentVars;
    }

    @Override
    public Boolean call() {
        try {
            outputStream.printf("%nmabl Jenkins plugin v%s running...%n", PLUGIN_VERSION);
            execute();
            return true;

        } catch (MablSystemException error) {
            printException(error);
            return continueOnMablError;

        } catch (MablPlanExecutionFailure failure) {
            printException(failure);
            return continueOnPlanFailure;

        } catch (Exception e) {
            outputStream.printf("Unexpected %s exception%n", PLUGIN_NAME);
            e.printStackTrace();
            return continueOnMablError;
        }
        finally {
            outputStream.print("mabl test execution step complete.\n\n");
        }
    }

    private void execute() throws MablPlanExecutionFailure {
        // TODO descriptive error messages on 401/403
        // TODO retry on 50x errors (proxy, redeploy)
        outputStream.printf("mabl is creating a deployment event:%n  environment_id: [%s]%n  application_id: [%s]%n  labels: [%s]  branch: [%s]%n",
                environmentId == null ? "empty" : environmentId,
                applicationId == null ? "empty" : applicationId,
                labels == null ? "empty" : labels,
                mablBranch == null ? "master" : mablBranch
        );

        try {
            final CreateDeploymentProperties properties = getDeploymentProperties();
            final CreateDeploymentResult deployment =
                    client.createDeploymentEvent(environmentId, applicationId, labels, mablBranch, properties);
            outputStream.printf("Deployment event was created in mabl at [%s/workspaces/%s/events/%s]%n",
                    client.getAppBaseUrl(), deployment.workspaceId, deployment.id);

            try {

                // Poll until we are successful or failed - note execution service is responsible for timeout
                ExecutionResult executionResult;
                do {
                    Thread.sleep(pollingIntervalMilliseconds);
                    executionResult = client.getExecutionResults(deployment.id);

                    if (executionResult == null) {
                        // No such id - this shouldn't happen
                        throw new MablSystemException("No deployment event found for id [%s] in mabl.", deployment.id);
                    }

                    printAllJourneyExecutionStatuses(executionResult);

                } while (!allPlansComplete(executionResult));

                printFinalStatuses(executionResult);

                if (!allPlansSuccess(executionResult)) {
                    throw new MablPlanExecutionFailure("One or more plans were unsuccessful running in mabl.");
                }

            } catch (InterruptedException e) {
                // TODO better error handling/logging
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new MablSystemException("There was an API error trying to run tests in mabl.", e);

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private CreateDeploymentProperties getDeploymentProperties() {
        CreateDeploymentProperties properties = Converter.convert(new EnvVars(), outputStream);
        if(collectVars) {
            outputStream.println("Send build environment variables is set. Collecting the following information:");
            properties = Converter.convert(this.environmentVars, outputStream);
        } else {
            outputStream.println("Send build environment variables is unset. Not collecting any environment information:");
        }

        properties.setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
        return properties;
    }

    private boolean allPlansComplete(final ExecutionResult result) {

        boolean isComplete = true;

        for (ExecutionResult.ExecutionSummary summary : result.executions) {
            isComplete &= COMPLETE_STATUSES.contains(summary.status.toLowerCase(Locale.US));
        }
        return isComplete;
    }

    private boolean allPlansSuccess(final ExecutionResult result) {
        Boolean success = result.eventStatus.getSucceeded();
        return Boolean.TRUE.equals(success);
    }

    private void printFinalStatuses(final ExecutionResult result) {
        final List<TestSuite> suites = new ArrayList<>();

        outputStream.println("The final plan states in mabl:");
        for (ExecutionResult.ExecutionSummary summary : result.executions) {
            final String successState = summary.success ? "SUCCESSFUL" : "FAILED";
            outputStream.printf("  %sPlan [%s] is %s in state [%s]%n",
                    (summary.planExecution != null && summary.planExecution.isRetry) ? "RETRY: " : "",
                    safePlanName(summary), successState, summary.status);
            suites.add(createTestSuite(summary));
        }

        outputTestSuiteXml(new TestSuites(ImmutableList.copyOf(suites)));
    }

    private void printAllJourneyExecutionStatuses(final ExecutionResult result) {

        outputStream.println("Running mabl test(s) status update:");
        Map<String, ExecutionResult.ExecutionSummary> summariesToPrint = new TreeMap<>();

        // Filter out out statuses for plans that already failed and will be retried
        for (ExecutionResult.ExecutionSummary summary : result.executions) {
            ExecutionResult.ExecutionSummary existing = summariesToPrint.get(summary.plan.id);
            if (existing == null || existing.planExecution == null || !existing.planExecution.isRetry) {
                summariesToPrint.put(summary.plan.id, summary);
            }
        }

        for (ExecutionResult.ExecutionSummary summary : summariesToPrint.values()) {
            outputStream.printf("  %sPlan [%s] is [%s]%n",
                    (summary.planExecution != null && summary.planExecution.isRetry) ? "RETRY: " : "",
                    safePlanName(summary), summary.status);
            for (ExecutionResult.JourneyExecutionResult journeyResult : summary.journeyExecutions) {
                outputStream.printf("    Test [%s] is %s%n",
                    safeJourneyName(summary, journeyResult.id),
                    executionResultToString(journeyResult));
            }
        }
    }

    static String executionResultToString(ExecutionResult.JourneyExecutionResult journeyResult) {
        final String cleanStatus = journeyResult.status != null ? journeyResult.status : "waiting";
        final String journeyFormat = String.format("[%s]", cleanStatus);
        if (cleanStatus.equalsIgnoreCase("failed")) {
            return String.format("%s at [%s]", journeyFormat, journeyResult.appHref);
        } else {
            return journeyFormat;
        }
    }

    private void outputTestSuiteXml(TestSuites testSuites) {
        try {
            JAXBContext context = JAXBContext.newInstance(TestSuites.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(testSuites, buildPath.write());
        } catch (JAXBException e) {
            throw new MablSystemException("There was an error trying to output test results in mabl.", e);
        } catch (IOException e) {
            throw new MablSystemException("There was an error trying to write test results in mabl.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MablSystemException("There was an interruption trying to write test results in mabl.", e);
        }
    }

    TestSuite createTestSuite(ExecutionResult.ExecutionSummary summary) {
        final Date startDate = new Date(summary.startTime);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String timestamp = format.format(startDate);
        final TestSuite testSuite = new TestSuite(safePlanName(summary), getDuration(summary), timestamp);

        final Map<String, SortedSet<String>> testCaseIDs = new HashMap<>();

        for (ExecutionResult.JourneyExecutionResult journeyResult : summary.journeyExecutions) {

            TestCase testCase = new TestCase(
                    safePlanName(summary),
                    safeJourneyName(summary, journeyResult.id),
                    getDuration(journeyResult),
                    journeyResult.appHref
            );

            if (journeyResult.testCases != null && !journeyResult.testCases.isEmpty()) {
                switch (journeyResult.status) {
                    case "failed":
                    case "completed":
                    case "skipped":
                        final SortedSet<String> ids =
                                testCaseIDs.computeIfAbsent(journeyResult.status + "-test-cases", k -> new TreeSet<>());
                        for (ExecutionResult.TestCaseID id : journeyResult.testCases) {
                            ids.add(id.caseID);
                        }

                        // XRay - report extension
                        // https://docs.getxray.app/display/XRAYCLOUD/Taking+advantage+of+JUnit+XML+reports
                        testCase.setTestCaseIDs(ids);
                        break;
                    default:
                        // ignore, only the above statuses are captured
                }
            }

            testSuite.addToTestCases(testCase).incrementTests();

            if (!journeyResult.success && null != journeyResult.status) {
                switch (journeyResult.status) {
                    case "failed":
                        // fall through
                    case "terminated":
                        final Failure failure = new Failure(journeyResult.status, journeyResult.statusCause);
                        testCase.setFailure(failure);
                        testSuite.incrementFailures();
                        break;
                    case "skipped":
                        testCase.setSkipped();
                        testSuite.incrementSkipped();
                        break;
                    default:
                        outputStream.printf("WARNING: unexpected status '%s' found for test '%s' in plan '%s'%n",
                                journeyResult.status,
                                safePlanName(summary),
                                safeJourneyName(summary, journeyResult.id)
                        );
                }

            }

        }

        if (!testCaseIDs.isEmpty()) {
            for (Map.Entry<String,SortedSet<String>> e : testCaseIDs.entrySet()) {
                testSuite.addProperty(e.getKey(), String.join(",", e.getValue()));
            }
        }
        return testSuite;
    }

    private static long getDuration(ExecutionResult.ExecutionSummary summary) {
        return summary.stopTime != null ?
                TimeUnit.SECONDS.convert( (summary.stopTime - summary.startTime), TimeUnit.MILLISECONDS) : 0;
    }

    private static long getDuration(ExecutionResult.JourneyExecutionResult summary) {
        return summary.stopTime != null ?
                TimeUnit.SECONDS.convert( (summary.stopTime - summary.startTime), TimeUnit.MILLISECONDS) : 0;
    }

    private void printException(final Exception exception) {
        outputStream.print(exception.getMessage());

        if (exception.getCause() != null) {
            exception.getCause().printStackTrace(outputStream);
        }
    }

    private static String safePlanName(final ExecutionResult.ExecutionSummary summary) {
        // Defensive treatment of possibly malformed future payloads
        return summary.plan != null &&
                summary.plan.name != null &&
                !summary.plan.name.isEmpty()
                    ? summary.plan.name :
                    "<Unnamed Plan>";
    }

    private static String safeJourneyName(
            final ExecutionResult.ExecutionSummary summary,
            final String journeyId
    ) {
        // Defensive treatment of possibly malformed future payloads
        String journeyName = "<Unnamed Journey>";
        for (ExecutionResult.JourneySummary journeySummary: summary.journeys) {
            if (journeySummary.id.equals(journeyId) && !journeySummary.name.isEmpty()) {
                journeyName = journeySummary.name;
                break;
            }
        }

        return journeyName;
    }
}
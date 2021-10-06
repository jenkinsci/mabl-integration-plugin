package com.mabl.integration.jenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import com.mabl.integration.jenkins.domain.GetApiKeyResult;
import com.mabl.integration.jenkins.domain.GetApplicationsResult;
import com.mabl.integration.jenkins.domain.GetEnvironmentsResult;
import com.mabl.integration.jenkins.validation.MablStepBuilderValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsProvider.*;
import static com.mabl.integration.jenkins.MablStepConstants.BUILD_STEP_DISPLAY_NAME;
import static com.mabl.integration.jenkins.MablStepConstants.EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS;
import static com.mabl.integration.jenkins.MablStepConstants.EXECUTION_TIMEOUT_SECONDS;
import static com.mabl.integration.jenkins.MablStepConstants.DEFAULT_MABL_APP_BASE_URL;
import static com.mabl.integration.jenkins.MablStepConstants.DEFAULT_MABL_API_BASE_URL;
import static com.mabl.integration.jenkins.MablStepConstants.PLUGIN_SYMBOL;
import static com.mabl.integration.jenkins.MablStepConstants.TEST_OUTPUT_XML_FILENAME;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * mabl custom build step
 */
@SuppressWarnings("unused") // automatically discovered by Jenkins
public class MablStepBuilder extends Builder implements SimpleBuildStep {
    @Override
    public String toString() {
        return "MABL Step Builder " + this.hashCode();
    }

    private final String restApiKeyId;
    private final String environmentId;
    private final String applicationId;
    private String labels;
    private String mablBranch;
    private boolean continueOnPlanFailure;
    private boolean continueOnMablError;
    private boolean disableSslVerification;
    private String apiBaseUrl;
    private String appBaseUrl;

    @DataBoundConstructor
    public MablStepBuilder(
            final String restApiKeyId,
            final String environmentId,
            final String applicationId
    ) {
        this.restApiKeyId = restApiKeyId;
        this.environmentId = trimToNull(environmentId);
        this.applicationId = trimToNull(applicationId);
    }

    @DataBoundSetter
    public void setLabels(String labels) {
        String value = labels;
        if (!StringUtils.isEmpty(labels)) {
            value = labels.trim().replaceAll(",[\\s]+", ",");
        }
        this.labels = value;
    }

    @DataBoundSetter
    public void setMablBranch(String mablBranch) {
        this.mablBranch = mablBranch;
    }

    @DataBoundSetter
    public void setContinueOnPlanFailure(boolean continueOnPlanFailure) {
        this.continueOnPlanFailure = continueOnPlanFailure;
    }

    @DataBoundSetter
    public void setContinueOnMablError(boolean continueOnMablError) {
        this.continueOnMablError = continueOnMablError;
    }
    
    @DataBoundSetter
    public void setDisableSslVerification(boolean disableSslVerification) {
        this.disableSslVerification = disableSslVerification;
    }

    @DataBoundSetter
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    @DataBoundSetter
    public void setAppBaseUrl(String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
    }

    // Accessors to be used by Jelly UI templates
    public String getRestApiKeyId() {
        return restApiKeyId;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getLabels() {
        return labels;
    }

    public String getMablBranch() { return mablBranch; }

    public boolean isCollectVars() {
        return getDescriptor().isCollectVars();
    }

    public boolean isContinueOnPlanFailure() {
        return this.continueOnPlanFailure;
    }

    public boolean isContinueOnMablError() {
        return this.continueOnMablError;
    }

    public boolean isDisableSslVerification() {
        return this.disableSslVerification;
    }

    public String getApiBaseUrl() { return this.apiBaseUrl; }

    public String getAppBaseUrl() { return this.appBaseUrl; }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener
    ) throws InterruptedException {

        final PrintStream outputStream = listener.getLogger();
        final MablRestApiClient client = new MablRestApiClientImpl(
                StringUtils.isBlank(apiBaseUrl) ? DEFAULT_MABL_API_BASE_URL : apiBaseUrl,
                getRestApiSecret(getRestApiKeyId()),
                StringUtils.isBlank(appBaseUrl) ? DEFAULT_MABL_APP_BASE_URL : appBaseUrl,
                disableSslVerification
        );

        final MablStepDeploymentRunner runner = new MablStepDeploymentRunner(
                client,
                outputStream,
                EXECUTION_STATUS_POLLING_INTERNAL_MILLISECONDS,
                environmentId,
                applicationId,
                labels,
                mablBranch,
                continueOnPlanFailure,
                continueOnMablError,
                isCollectVars(),
                getOutputFileLocation(workspace),
                getEnvironmentVars(run, listener)
        );

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> runnerFuture = executorService.submit(runner);
        try {
            if (runnerFuture.get(EXECUTION_TIMEOUT_SECONDS, SECONDS)) {
                run.setResult(Result.SUCCESS);
            } else {
                run.setResult(Result.FAILURE);
            }
        } catch (ExecutionException e) {
            outputStream.println("There was an execution error trying to run your tests in mabl");
            e.printStackTrace(outputStream);
            if (continueOnMablError) {
                run.setResult(Result.FAILURE);
            } else {
                run.setResult(Result.SUCCESS);
            }
        } catch (TimeoutException e) {
            outputStream.printf("Your tests exceeded the maximum plugin runtime limit of %d seconds.%n" +
                    "We've aborted this Jenkins step, but your tests may still be running in mabl.", EXECUTION_TIMEOUT_SECONDS);
            if (continueOnMablError) {
                run.setResult(Result.FAILURE);
            } else {
                run.setResult(Result.SUCCESS);
            }
        }
    }

    @Override
    public MablStepDescriptor getDescriptor() {
        return (MablStepDescriptor) super.getDescriptor();
    }

    private FilePath getOutputFileLocation(FilePath workspace) {
        if (workspace == null) {
            return new FilePath(new File(TEST_OUTPUT_XML_FILENAME));
        }
        if (workspace.isRemote()) {
            workspace = new FilePath(workspace.getChannel(), workspace + File.separator + TEST_OUTPUT_XML_FILENAME);
        } else {
            workspace = new FilePath(new File(workspace + File.separator + TEST_OUTPUT_XML_FILENAME));
        }

        return workspace;
    }

    private EnvVars getEnvironmentVars(Run<?, ?> build, TaskListener listener) {
        final PrintStream outputStream = listener.getLogger();
        EnvVars environmentVars = new EnvVars();
        try {
            environmentVars = build.getEnvironment(listener);
        } catch (IOException e) {
            outputStream.println("There was an error trying to read environment variables.");
            e.printStackTrace(outputStream);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            outputStream.println("There was an interruption while reading environment variables.");
            e.printStackTrace(outputStream);
        }

        return environmentVars;
    }

    static Secret getRestApiSecret(final String restApiKeyId) {
        if (restApiKeyId == null) {
            return null;
        }

        Secret secretKey = null;
        List<StringCredentials> stringCredentials =
                CredentialsProvider.lookupCredentials(StringCredentials.class, (Item) null, ACL.SYSTEM, Collections.emptyList());
        for (StringCredentials cred : stringCredentials) {
            if (restApiKeyId.equals(cred.getId())) {
                secretKey = cred.getSecret();
                break;
            }
        }
        return secretKey;
    }

    /**
     * Descriptor used in views. Centralized metadata store for all {@link MablStepBuilder} instances.
     */
    @Extension
    @Symbol(PLUGIN_SYMBOL)
    public static class MablStepDescriptor extends BuildStepDescriptor<Builder> {
        private static final Logger LOGGER = Logger.getLogger(MablStepDescriptor.class.getName());
        private boolean collectVars;

        public MablStepDescriptor() {
            super.load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            JSONObject json = formData.getJSONObject("mabl");
            collectVars = json.getBoolean("collectVars");
            save();
            return super.configure(req, formData);
        }

        public boolean isCollectVars() {
            return collectVars;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
            return true; // Plugin may be used by all project types
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return BUILD_STEP_DISPLAY_NAME;
        }


        public FormValidation doValidateForm(
                @QueryParameter("restApiKeyId") final String restApiKeyId,
                @QueryParameter("environmentId") final String environmentId,
                @QueryParameter("applicationId") final String applicationId,
                @QueryParameter("disableSslVerification") final boolean disableSslVerification,
                @QueryParameter("apiBaseUrl") final String apiBaseUrl,
                @QueryParameter("appBaseUrl") final String appBaseUrl
        ) {
            return MablStepBuilderValidator.validateForm(
                    restApiKeyId, environmentId, applicationId, disableSslVerification, apiBaseUrl, appBaseUrl);
        }

        public ListBoxModel doFillRestApiKeyIdItems(
                @AncestorInPath Item item,
                @QueryParameter String restApiKeyId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (restApiKeyId != null) {
                result.add(restApiKeyId);
            }
            List<StringCredentials> creds =
                    lookupCredentials(StringCredentials.class, item, ACL.SYSTEM,
                            Collections.emptyList());
            for (StringCredentials cred : creds) {
                result.add(cred.getId());
            }
            return result.includeEmptyValue();
        }

        public FormValidation doCheckRestApiKeyIds(
                @AncestorInPath Item item,
                @QueryParameter String value
        ) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.warning("Provide a credentials ID");
            } else if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(USE_ITEM)) {
                return FormValidation.warning("Insufficient permissions");
            }

            if (value.startsWith("${") && value.endsWith("}")) {
                return FormValidation.warning("Cannot validate expression based credentials");
            }

            return FormValidation.ok();
        }

        public ListBoxModel doFillApplicationIdItems(
                @QueryParameter String restApiKeyId,
                @QueryParameter boolean disableSslVerification,
                @QueryParameter String apiBaseUrl,
                @QueryParameter String appBaseUrl
        ) {
            if (StringUtils.isBlank(restApiKeyId)) {
                return getSelectValidApiKeyListBoxModel();
            }
            Secret secretKey = getRestApiSecret(restApiKeyId);
            if (secretKey != null) {
                final MablRestApiClient client =
                        createMablRestApiClient(
                                secretKey,
                                disableSslVerification,
                                apiBaseUrl,
                                appBaseUrl
                );
                return getApplicationIdItems(client);
            }
            return new ListBoxModel();
        }

        private ListBoxModel getApplicationIdItems(
                final MablRestApiClient client) {
            try {
                GetApiKeyResult apiKeyResult = client.getApiKeyResult();
                if (apiKeyResult == null) {
                    return getSelectValidApiKeyListBoxModel();
                }

                ListBoxModel items = new ListBoxModel();
                String organizationId = apiKeyResult.organization_id;
                GetApplicationsResult applicationsResult = client.getApplicationsResult(organizationId);

                items.add("", "");
                for (GetApplicationsResult.Application application : applicationsResult.applications) {
                    items.add(application.name, application.id);
                }

                return items;
            } catch (IOException | MablSystemException e) {
                LOGGER.warning("Failed to retrieve application IDs: " + e.getLocalizedMessage());
            }

            return getSelectValidApiKeyListBoxModel();
        }

        public ListBoxModel doFillEnvironmentIdItems(
                @QueryParameter String restApiKeyId,
                @QueryParameter boolean disableSslVerification,
                @QueryParameter String apiBaseUrl,
                @QueryParameter String appBaseUrl
        ) {
            if (StringUtils.isBlank(restApiKeyId)) {
                return getSelectValidApiKeyListBoxModel();
            }

            final Secret secretKey = getRestApiSecret(restApiKeyId);
            if (secretKey != null) {
                final MablRestApiClient client = createMablRestApiClient(
                        secretKey, disableSslVerification, apiBaseUrl, appBaseUrl);
                return getEnvironmentIdItems(client);
            }
            return new ListBoxModel();
        }

        private ListBoxModel getEnvironmentIdItems(final MablRestApiClient client) {
            try {
                GetApiKeyResult apiKeyResult = client.getApiKeyResult();
                if (apiKeyResult == null) {
                    return getSelectValidApiKeyListBoxModel();
                }

                ListBoxModel items = new ListBoxModel();
                String organizationId = apiKeyResult.organization_id;
                GetEnvironmentsResult environmentsResult = client.getEnvironmentsResult(organizationId);

                items.add("", "");
                for (GetEnvironmentsResult.Environment environment : environmentsResult.environments) {
                    items.add(environment.name, environment.id);
                }

                return items;
            } catch (IOException | MablSystemException e) {
                LOGGER.warning("Failed to retrieve environment IDs: " + e.getLocalizedMessage());
            }

            return getSelectValidApiKeyListBoxModel();
        }

        public FormValidation doCheckMablBranch(
                @QueryParameter String value
        ) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok();
            }

            if (value.matches("[A-Za-z0-9_-]+")) {
                return FormValidation.ok();
            }

            return FormValidation.error("The branch name field may only contain alpha-numeric characters, " +
                    "as well as dashes and underscores.");
        }

        private static ListBoxModel getSelectValidApiKeyListBoxModel() {
            final ListBoxModel listBoxModel = new ListBoxModel();
            listBoxModel.add("Select a valid API key");
            return listBoxModel;
        }

        private static MablRestApiClient createMablRestApiClient(
                final Secret key,
                final boolean disableSslVerification,
                final String apiBaseUrl,
                final String appBaseUrl
        ) {
            return new MablRestApiClientImpl(
                    StringUtils.isEmpty(apiBaseUrl) ? DEFAULT_MABL_API_BASE_URL : apiBaseUrl,
                    key,
                    StringUtils.isEmpty(appBaseUrl) ? DEFAULT_MABL_APP_BASE_URL : appBaseUrl,
                    disableSslVerification);
        }
    }

    public @Nonnull static MablRestApiClient createMablRestApiClient(
            final String restApiKeyId,
            final boolean disableSslVerification,
            final String apiBaseUrl,
            final String appBaseUrl
    ) {
        return new MablRestApiClientImpl(
                StringUtils.isEmpty(apiBaseUrl) ? DEFAULT_MABL_API_BASE_URL : apiBaseUrl,
                getRestApiSecret(restApiKeyId),
                StringUtils.isEmpty(appBaseUrl) ? DEFAULT_MABL_APP_BASE_URL : appBaseUrl,
                disableSslVerification);
    }

}
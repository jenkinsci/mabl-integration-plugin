package com.mabl.integration.jenkins;

import com.mabl.integration.jenkins.domain.CreateDeploymentProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * mabl custom build step
 *
 */
class MablTestConstants {
    static final String CREATE_DEPLOYMENT_EVENT_RESULT_JSON =
            "{" +
            "  \"id\": \"d1To4-GYeZ4nl-4Ag1JyQg-v\"," +
            "  \"environment_id\": \"xolMgRp4CwvHQjQUX_MOoA-e\"," +
            "  \"application_id\": \"smoTxTR8B9oh73qstERNyg-a\"," +
            "  \"received_time\": 1523541704649" +
            "}";

    static final String EXECUTION_RESULT_JSON =
            "{" +
            "  \"plan_execution_metrics\": {" +
            "    \"total\": 1," +
            "    \"passed\": 1," +
            "    \"failed\": 0" +
            "  }," +
            "  \"journey_execution_metrics\": {" +
            "    \"total\": 1," +
            "    \"passed\": 1," +
            "    \"failed\": 0" +
            "  }," +
            "  \"executions\": [" +
            "    {" +
            "      \"status\": \"succeeded\"," +
            "      \"success\": true," +
            "      \"plan\": {" +
            "        \"id\": \"xRdJlCbtG5kAbOI9KkwLdA-p\"," +
            "        \"name\": \"Trigger Happy Plan generated plan\"," +
            "        \"href\": \"https://api.mabl.com/schedule/runPolicy/xRdJlCbtG5kAbOI9KkwLdA-p\"," +
            "        \"app_href\": \"https://app.mabl.com/workspaces/rFp7Wo2M_L-6hRgqO9H8Gw-w/test/plans/xRdJlCbtG5kAbOI9KkwLdA-p\"" +
            "      }," +
            "      \"plan_execution\": {" +
            "        \"id\": \"eJ8vgNbPL6RbyZJTIWhQ6Q-pe\"," +
            "        \"status\": \"succeeded\"," +
            "        \"href\": \"https://api.mabl.com/execution/runPolicyExecution/xRdJlCbtG5kAbOI9KkwLdA-p\"" +
            "      }," +
            "      \"journeys\": [" +
            "        {" +
            "          \"id\": \"RhlpLTirVR5QH1nfEm3SfA-j:0\"," +
            "          \"name\": \"Trigger Happy Plan\"," +
            "          \"href\": \"https://api.mabl.com/execution/runPolicyExecution/eJ8vgNbPL6RbyZJTIWhQ6Q-pe/testScriptExecution/RhlpLTirVR5QH1nfEm3SfA-j:0\"," +
            "          \"app_href\": \"https://app.mabl.com/workspaces/rFp7Wo2M_L-6hRgqO9H8Gw-w/test/plan-executions/eJ8vgNbPL6RbyZJTIWhQ6Q-pe/journeys/RhlpLTirVR5QH1nfEm3SfA-j:0\"" +
            "        }" +
            "      ]," +
            "      \"journey_executions\": [" +
            "        {" +
            "          \"journey_id\": \"RhlpLTirVR5QH1nfEm3SfA-j:0\"," +
            "          \"journey_execution_id\": \"RhlpLTirVR5QH1nfEm3SfA-j:0\"," +
            "          \"status\": \"completed\"," +
            "          \"success\": true," +
            "          \"href\": \"https://api.mabl.com/test/journey/RhlpLTirVR5QH1nfEm3SfA-j:0\"," +
            "          \"app_href\": \"https://app.mabl.com/workspaces/rFp7Wo2M_L-6hRgqO9H8Gw-w/train/journeys/RhlpLTirVR5QH1nfEm3SfA-j:0\"" +
            "        }" +
            "      ]," +
            "      \"start_time\": 1523541069783," +
            "      \"stop_time\": 1523541117700" +
            "    }" +
            "  ]" +
            "}";

    static final String APIKEY_RESULT_JSON = "" +
            "{" +
            "   \"id\":\"XjO5GsxvWRi_zwbK3-h2PB\"," +
            "   \"created_time\":1526412082062," +
            "   \"created_by_id\":\"FYuRFw9hMzqhjI5xnFYH3A\"," +
            "   \"last_updated_time\":1526412082062," +
            "   \"last_updated_by_id\":\"FYuRFw9hMzqhjI5xnFYH3A\"," +
            "   \"organization_id\":\"K8NWhtPqOyFnyvJTvCP0uw-w\"," +
            "   \"name\":\"Default API Key\"," +
            "   \"scopes\":[" +
            "     {" +
            "       \"permission\":\"write\"," +
            "       \"target\":\"events\"" +
            "     }," +
            "     {" +
            "       \"permission\":\"read\"," +
            "       \"target\":\"execution_results\"" +
            "     }," +
            "     {" +
            "       \"permission\":\"read\"," +
            "       \"target\":\"api_keys\"" +
            "     }," +
            "     {" +
            "       \"permission\":\"read\"," +
            "       \"target\":\"environments\"" +
            "     }," +
            "     {" +
            "       \"permission\":\"read\"," +
            "       \"target\":\"applications\"" +
            "     }" +
            "   ]," +
            "   \"tags\":[" +
            "     {" +
            "       \"name\":\"default\"" +
            "     }" +
            "   ]" +
            "}";

    static final String APPLICATIONS_RESULT_JSON = "" +
            "{" +
            "   \"applications\":[" +
            "     {" +
            "       \"id\":\"zFgoXbl__YHLbezTsBEHig-a\"," +
            "       \"created_time\":1526412127291," +
            "       \"created_by_id\":\"FYuRFw9hMzqhjI5xnFYH3A\"," +
            "       \"last_updated_time\":1526412127291," +
            "       \"last_updated_by_id\":\"FYuRFw9hMzqhjI5xnFYH3A\"," +
            "       \"organization_id\":\"K8NWhtPqOyFnyvJTvCP0uw-w\"," +
            "       \"name\":\"Wikipedia\"" +
            "     }," +
            "     {" +
            "       \"id\":\"dEfaUiL__HYBeztssIOhNK-a\"," +
            "       \"created_time\":15264121277623," +
            "       \"created_by_id\":\"GuDwew9hMqhjI5xnF8H3A\"," +
            "       \"last_updated_time\":1426412127761," +
            "       \"last_updated_by_id\":\"GuDwew9hMqhjI5xnF8H3A\"," +
            "       \"organization_id\":\"K8NWhtPqOyFnyvJTvCP0uw-w\"," +
            "       \"name\":\"Yahoo\"" +
            "     }" +
            "   ]," +
            "   \"cursor\":\"Cj0SN2oKc35tYWJsLWRldnIpCxILQXBwbGljYXRpb24iGHpGZ29YYmxfX1lITGJlelRzQkVIaWctYQwYACAA\"" +
            "}";

    static final String ENVIRONMENTS_RESULT_JSON = "" +
            "{" +
            "   \"environments\":[" +
            "     {" +
            "       \"id\":\"7xNT3ADgflTI2yN9Ihjk_Q-e\"," +
            "       \"created_time\":1526412127288," +
            "       \"created_by_id\":\"FYuRFw9hMzqhjI5xnFYH3A\"," +
            "       \"last_updated_time\":1526412127288," +
            "       \"last_updated_by_id\":\"FYuRFw9hMzqhjI5xnFYH3A\"," +
            "       \"organization_id\":\"K8NWhtPqOyFnyvJTvCP0uw-w\"," +
            "       \"name\":\"Wikipedia\"" +
            "     }" +
            "   ]," +
            "   \"cursor\":\"Cj0SN2oKc35tYWJsLWRldnIpCxILRW52aXJvbm1lbnQiGDd4TlQzQURnZmxUSTJ5TjlJaGprX1EtZQwYACAA\"" +
            "}";

    static final String TEST_CASE_XML = "" +
            "<testcase classname=\"My Plan Name\" name=\"My Test Name\" time=\"23\" xlink:type=\"simple\" xlink:href=\"http://myapphref.com\"/>";

    static final String TEST_CASE_XML_WITH_FAILURE = "" +
            "<testcase classname=\"My Plan Name\" name=\"My Test Name\" time=\"23\" xlink:type=\"simple\" xlink:href=\"http://myapphref.com\">\n" +
            "  <failure message=\"My Message\">My Reason</failure>\n" +
            "</testcase>";

    static final String TEST_SUITES_XML = "" +
            "<testsuites xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
            "  <testsuite name=\"Empty Test Suite\" tests=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" time=\"0\" timestamp=\"2013-05-24T10:23:58\">\n" +
            "    <properties/>\n" +
            "  </testsuite>\n" +
            "  <testsuite name=\"Full Test Suite\" tests=\"2\" errors=\"0\" failures=\"1\" skipped=\"0\" time=\"33\" timestamp=\"2013-05-24T10:23:58\">\n" +
            "    <properties>\n" +
            "      <property name=\"environment\" value=\"my env-e\"/>\n" +
            "      <property name=\"application\" value=\"my app-a\"/>\n" +
            "    </properties>\n" +
            "    <testcase classname=\"My Plan Name 1\" name=\"My Test Name 1\" time=\"11\" xlink:type=\"simple\" xlink:href=\"http://myapphref.com\"/>\n" +
            "    <testcase classname=\"My Plan Name 2\" name=\"My Test Name 2\" time=\"22\" xlink:type=\"simple\" xlink:href=\"http://myapphref.com\">\n" +
            "      <failure message=\"My Message\">My Reason</failure>\n" +
            "    </testcase>\n" +
            "  </testsuite>\n" +
            "</testsuites>";

    static final Map<String, String> BUILD_VARS = new HashMap<String, String> () {
        {
            put("JOB_NAME", "MyFakeJobName");
            put("BUILD_NUMBER", "6");
            put("RUN_DISPLAY_URL", "http://server/job/FakeJobName/6/display/redirect");
        }
    };

    static final Map<String, String> GIT_VARS = new HashMap<String, String>() {
        {
            put("GIT_BRANCH", "main");
            put("GIT_COMMIT", "1234");
            put("GIT_URL", "git@github.com:fakeOrg/mabl-integration-plugin.git");
            put("GIT_PREVIOUS_COMMIT", "1233");
            putAll(BUILD_VARS);
        }
    };

    static final Map<String, String> SVN_VARS = new HashMap<String, String>() {
        {
            put("SVN_REVISION", "12");
            put("SVN_URL", "https://svn.fakeDomain.com/int_test_svn");
            putAll(BUILD_VARS);
        }
    };

    static final CreateDeploymentProperties JUST_BUILD_PROPS = new CreateDeploymentProperties() {
        {
            setBuildPlanId("MyFakeJobName");
            setBuildPlanName("MyFakeJobName");
            setBuildPlanNumber("6");
            setBuildPlanResultUrl("http://server/job/FakeJobName/6/display/redirect");
            setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
        }
    };

    static final CreateDeploymentProperties GIT_BUILD_PROPS_SSH = new CreateDeploymentProperties() {
        {
            setBuildPlanId("MyFakeJobName");
            setBuildPlanName("MyFakeJobName");
            setBuildPlanNumber("6");
            setBuildPlanResultUrl("http://server/job/FakeJobName/6/display/redirect");
            setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
            setRepositoryBranchName("main");
            setRepositoryName("mabl-integration-plugin");
            setRepositoryRevisionNumber("1234");
            setRepositoryPreviousRevisionNumber("1233");
            setRepositoryUrl("git@github.com:fakeOrg/mabl-integration-plugin.git");
        }
    };

    static final CreateDeploymentProperties GIT_BUILD_PROPS_HTTPS = new CreateDeploymentProperties() {
        {
            setBuildPlanId("MyFakeJobName");
            setBuildPlanName("MyFakeJobName");
            setBuildPlanNumber("6");
            setBuildPlanResultUrl("http://server/job/FakeJobName/6/display/redirect");
            setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
            setRepositoryBranchName("main");
            setRepositoryName("mabl-integration-plugin");
            setRepositoryRevisionNumber("1234");
            setRepositoryPreviousRevisionNumber("1233");
            setRepositoryUrl("https://github.com/fakeOrg/mabl-integration-plugin.git");
        }
    };

    static final CreateDeploymentProperties SVN_BUILD_PROPS = new CreateDeploymentProperties() {
        {
            setBuildPlanId("MyFakeJobName");
            setBuildPlanName("MyFakeJobName");
            setBuildPlanNumber("6");
            setBuildPlanResultUrl("http://server/job/FakeJobName/6/display/redirect");
            setDeploymentOrigin(MablStepConstants.PLUGIN_USER_AGENT);
            setRepositoryName("int_test_svn");
            setRepositoryRevisionNumber("12");
            setRepositoryUrl("https://svn.fakeDomain.com/int_test_svn");
        }
    };
}
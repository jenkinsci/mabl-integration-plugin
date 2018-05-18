package com.mabl.integration.jenkins;

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
}
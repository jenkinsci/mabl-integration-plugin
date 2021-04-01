package com.mabl.integration.jenkins.domain;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static com.mabl.integration.jenkins.domain.JsonUtil.deserialize;

public class GetApplicationsResultTest {

    @Test
    public void testGetApplicationsResultToJson() throws FileNotFoundException {
        GetApplicationsResult getApplicationsResult = new GetApplicationsResult(
                Arrays.asList(
                        new GetApplicationsResult.Application(
                                "fake-id-1",
                                "fake-name-1",
                                1617293557L,
                                "creator-id-1",
                                1617294567L,
                                "updated-id-1",
                                "workspace-id-1"
                        ),
                        new GetApplicationsResult.Application(
                                "fake-id-2",
                                "fake-name-2",
                                1617294567L,
                                "creator-id-2",
                                1617295678L,
                                "updated-id-2",
                                "workspace-id-2"
                        )
                )
        );

        GetApplicationsResult expected = deserialize("getapplicationsresult.json", GetApplicationsResult.class);
        for (int i=0; i<2; i++) {
            assertEquals(expected.applications.get(i).id, getApplicationsResult.applications.get(i).id);
            assertEquals(expected.applications.get(i).name, getApplicationsResult.applications.get(i).name);
            assertEquals(expected.applications.get(i).createdTime, getApplicationsResult.applications.get(i).createdTime);
            assertEquals(expected.applications.get(i).createdById, getApplicationsResult.applications.get(i).createdById);
            assertEquals(expected.applications.get(i).lastUpdatedTime, getApplicationsResult.applications.get(i).lastUpdatedTime);
            assertEquals(expected.applications.get(i).lastUpdatedById, getApplicationsResult.applications.get(i).lastUpdatedById);
            assertEquals(expected.applications.get(i).organizationId, getApplicationsResult.applications.get(i).organizationId);
        }
    }
}

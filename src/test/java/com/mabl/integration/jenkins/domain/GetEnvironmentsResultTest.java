package com.mabl.integration.jenkins.domain;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static com.mabl.integration.jenkins.domain.JsonUtil.deserialize;

public class GetEnvironmentsResultTest {

    @Test
    public void testGetEnvironmentsResultToJson() throws FileNotFoundException {
        GetEnvironmentsResult getEnvironmentsResult = new GetEnvironmentsResult(
                Arrays.asList(
                        new GetEnvironmentsResult.Environment(
                                "fake-id-1",
                                "fake-name-1",
                                1617296634L,
                                "fake-creator-1",
                                1617296789L,
                                "fake-updater-1",
                                "fake-workspace-id-1"
                        ),
                        new GetEnvironmentsResult.Environment(
                                "fake-id-2",
                                "fake-name-2",
                                1617297634L,
                                "fake-creator-2",
                                1617297890L,
                                "fake-updater-2",
                                "fake-workspace-id-2"
                        )
                )
        );

        GetEnvironmentsResult expected = deserialize("getenvironmentsresult.json", GetEnvironmentsResult.class);
        
        for (int i=0; i<2; i++) {
            assertEquals(expected.environments.get(i).id, getEnvironmentsResult.environments.get(i).id);
            assertEquals(expected.environments.get(i).name, getEnvironmentsResult.environments.get(i).name);
            assertEquals(expected.environments.get(i).createdTime, getEnvironmentsResult.environments.get(i).createdTime);
            assertEquals(expected.environments.get(i).createdById, getEnvironmentsResult.environments.get(i).createdById);
            assertEquals(expected.environments.get(i).lastUpdatedTime, getEnvironmentsResult.environments.get(i).lastUpdatedTime);
            assertEquals(expected.environments.get(i).lastUpdatedById, getEnvironmentsResult.environments.get(i).lastUpdatedById);
            assertEquals(expected.environments.get(i).organizationId, getEnvironmentsResult.environments.get(i).organizationId);
        }
    }
}

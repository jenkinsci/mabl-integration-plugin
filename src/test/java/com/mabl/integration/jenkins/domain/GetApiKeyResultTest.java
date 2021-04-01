package com.mabl.integration.jenkins.domain;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static com.mabl.integration.jenkins.domain.JsonUtil.deserialize;

public class GetApiKeyResultTest {


    @Test
    public void testGetApiKeyResultToJson() throws FileNotFoundException {
        GetApiKeyResult getApiKeyResult = new GetApiKeyResult("fake-id");
        GetApiKeyResult expected = deserialize("getapikeyresult.json", GetApiKeyResult.class);

        assertEquals(expected.organization_id, getApiKeyResult.organization_id);
    }


    @Test
    public void testGetApiKeyToJson() throws FileNotFoundException {
        GetApiKeyResult.ApiKey apiKey = new GetApiKeyResult.ApiKey(
                "fake-id",
                "fake-name",
                1617238221L,
                "fake-creator-id",
                1617239876L,
                "fake-updater-id",
                "fake-organization-id",
                Arrays.asList(
                        new GetApiKeyResult.Scope("fake-permission", "fake-target"),
                        new GetApiKeyResult.Scope("fake-permisssion-2", "fake-target-2")),
                Arrays.asList(
                        new GetApiKeyResult.Tag("fake-tag-1"),
                        new GetApiKeyResult.Tag("fake-tag-2")
                )
        );

        GetApiKeyResult.ApiKey expected = deserialize("apikey.json", GetApiKeyResult.ApiKey.class);

        assertEquals(expected.createdById, apiKey.createdById);
        assertEquals(expected.createdTime, apiKey.createdTime);
        assertEquals(expected.id, apiKey.id);
        assertEquals(expected.lastUpdatedById, apiKey.lastUpdatedById);
        assertEquals(expected.lastUpdatedTime, apiKey.lastUpdatedTime);
        assertEquals(expected.name, apiKey.name);
        assertEquals(expected.organizationId, apiKey.organizationId);
        for (int i=0; i<2; i++) {
            assertEquals(expected.scopes.get(i).permission, apiKey.scopes.get(i).permission);
            assertEquals(expected.scopes.get(i).target, apiKey.scopes.get(i).target);
        }
        assertEquals(expected.tags.get(0).name, apiKey.tags.get(0).name);
        assertEquals(expected.tags.get(1).name, apiKey.tags.get(1).name);
    }

}

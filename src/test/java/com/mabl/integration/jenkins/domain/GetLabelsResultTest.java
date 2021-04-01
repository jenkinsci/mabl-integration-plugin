package com.mabl.integration.jenkins.domain;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static com.mabl.integration.jenkins.domain.JsonUtil.deserialize;

public class GetLabelsResultTest {

    @Test
    public void testGetLabelsResultToJson() throws FileNotFoundException {
        GetLabelsResult getLabelsResult = new GetLabelsResult(
                Arrays.asList(
                        new GetLabelsResult.Label("regression", "red"),
                        new GetLabelsResult.Label("smoke", "amber")
                )
        );

        GetLabelsResult expected = deserialize("getlabelsresult.json", GetLabelsResult.class);
        for (int i=0; i<2; i++) {
            assertEquals(expected.labels.get(i).color, getLabelsResult.labels.get(i).color);
            assertEquals(expected.labels.get(i).name, getLabelsResult.labels.get(i).name);
        }
    }
}

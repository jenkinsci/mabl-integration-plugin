package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetLabelsResult implements ApiResult {
    public List<Label> labels;

    @JsonCreator
    public GetLabelsResult(
            @JsonProperty("labels") final List<Label> labels
    ) {
        this.labels = labels;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Label {
        public final String name;
        public final String color;

        @JsonCreator
        public Label(
                @JsonProperty("name") final String name,
                @JsonProperty("color") final String color
        ) {
            this.name = name;
            this.color = color;
        }
    }

}

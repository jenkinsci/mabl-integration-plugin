package com.mabl.integration.jenkins.domain;

import java.util.List;

public class GetLabelsResult implements ApiResult {
    public List<Label> labels;

    public GetLabelsResult(
            final List<Label> labels
    ) {
        this.labels = labels;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Label {
        public final String name;
        public final String color;

        public Label(
                final String name,
                final String color
        ) {
            this.name = name;
            this.color = color;
        }
    }

}

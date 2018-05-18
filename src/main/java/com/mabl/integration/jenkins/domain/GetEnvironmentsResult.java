package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetEnvironmentsResult implements ApiResult {
    public List<Environment> environments;

    @JsonCreator
    public GetEnvironmentsResult(
            @JsonProperty("environments") final List<Environment> applications
    ) {
        this.environments = applications;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Environment {
        public final String id;
        public final String name;
        public final Long createdTime;
        public final String createdById;
        public final Long lastUpdatedTime;
        public final String lastUpdatedById;
        public final String organizationId;

        @JsonCreator
        public Environment(
                @JsonProperty("id") final String id,
                @JsonProperty("name") final String name,
                @JsonProperty("created_time") final Long created_time,
                @JsonProperty("created_by_id") final String created_by_id,
                @JsonProperty("last_updated_time") final Long last_updated_time,
                @JsonProperty("last_updated_by_id") final String last_updated_by_id,
                @JsonProperty("organization_id") final String organization_id
        ) {
            this.id = id;
            this.name = name;
            this.createdTime = created_time;
            this.createdById = created_by_id;
            this.lastUpdatedTime = last_updated_time;
            this.lastUpdatedById = last_updated_by_id;
            this.organizationId = organization_id;
        }
    }

}

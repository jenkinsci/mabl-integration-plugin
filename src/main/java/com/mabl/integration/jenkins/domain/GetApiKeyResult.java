package com.mabl.integration.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetApiKeyResult implements ApiResult {
    public String organization_id;

    @JsonCreator
    public GetApiKeyResult(
            @JsonProperty("organization_id") String organization_id
    ) {
        this.organization_id = organization_id;
    }

    @SuppressWarnings("WeakerAccess")
    public static class ApiKey {
        public final String id;
        public final String name;
        public final Long createdTime;
        public final String createdById;
        public final Long lastUpdatedTime;
        public final String lastUpdatedById;
        public final String organizationId;
        public final List<Scope> scopes;
        public final List<Tag> tags;

        @JsonCreator
        public ApiKey(
                @JsonProperty("id") final String id,
                @JsonProperty("name") final String name,
                @JsonProperty("created_time") final Long created_time,
                @JsonProperty("created_by_id") final String created_by_id,
                @JsonProperty("last_updated_time") final Long last_updated_time,
                @JsonProperty("last_updated_by_id") final String last_updated_by_id,
                @JsonProperty("organization_id") final String organization_id,
                @JsonProperty("scopes") final List<Scope> scopes,
                @JsonProperty("scopes") final List<Tag> tags
        ) {
            this.id = id;
            this.name = name;
            this.createdTime = created_time;
            this.createdById = created_by_id;
            this.lastUpdatedTime = last_updated_time;
            this.lastUpdatedById = last_updated_by_id;
            this.organizationId = organization_id;
            this.scopes = scopes;
            this.tags = tags;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class Scope {
        public final String permission;
        public final String target;

        @JsonCreator
        public Scope(
                @JsonProperty("id") final String permission,
                @JsonProperty("name") final String target
        ) {
            this.permission = permission;
            this.target = target;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class Tag {
        public final String name;

        @JsonCreator
        public Tag(
                @JsonProperty("name") final String name
        ) {
            this.name = name;
        }
    }
}

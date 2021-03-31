package com.mabl.integration.jenkins.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetApiKeyResult implements ApiResult {
    public String organization_id;

    public GetApiKeyResult(
            String organization_id
    ) {
        this.organization_id = organization_id;
    }

    @SuppressWarnings("WeakerAccess")
    public static class ApiKey {
        public final String id;
        public final String name;
        @SerializedName("created_time") public final Long createdTime;
        @SerializedName("created_by_id") public final String createdById;
        @SerializedName("last_updated_time") public final Long lastUpdatedTime;
        @SerializedName("last_updated_by_id")public final String lastUpdatedById;
        @SerializedName("organization_id") public final String organizationId;
        public final List<Scope> scopes;
        public final List<Tag> tags;

        public ApiKey(
                final String id,
                final String name,
                final Long created_time,
                final String created_by_id,
                final Long last_updated_time,
                final String last_updated_by_id,
                final String organization_id,
                final List<Scope> scopes,
                final List<Tag> tags
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
        @SerializedName("id") public final String permission;
        @SerializedName("name") public final String target;

        public Scope(
                final String permission,
                final String target
        ) {
            this.permission = permission;
            this.target = target;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class Tag {
        public final String name;

        public Tag(
                final String name
        ) {
            this.name = name;
        }
    }
}

package com.mabl.integration.jenkins.domain;

import java.util.List;

public class GetApplicationsResult implements ApiResult {
    public List<Application> applications;

    public GetApplicationsResult(
            final List<Application> applications
    ) {
        this.applications = applications;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Application {
        public final String id;
        public final String name;
        public final Long createdTime;
        public final String createdById;
        public final Long lastUpdatedTime;
        public final String lastUpdatedById;
        public final String organizationId;

        public Application(
                final String id,
                final String name,
                final Long created_time,
                final String created_by_id,
                final Long last_updated_time,
                final String last_updated_by_id,
                final String organization_id
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

package com.walmartlabs.concord.server.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.common.validation.ConcordKey;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public class ProjectEntry implements Serializable {

    private final UUID id;

    @NotNull
    @ConcordKey
    private final String name;

    @Size(max = 1024)
    private final String description;

    private final UUID teamId;

    @ConcordKey
    private final String teamName;

    private final Map<String, UpdateRepositoryRequest> repositories;

    private final Map<String, Object> cfg;

    @JsonCreator
    public ProjectEntry(@JsonProperty("id") UUID id,
                        @JsonProperty("name") String name,
                        @JsonProperty("description") String description,
                        @JsonProperty("teamId") UUID teamId,
                        @JsonProperty("teamName") String teamName,
                        @JsonProperty("repositories") Map<String, UpdateRepositoryRequest> repositories,
                        @JsonProperty("cfg") Map<String, Object> cfg) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.teamId = teamId;
        this.teamName = teamName;
        this.repositories = repositories;
        this.cfg = cfg;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public Map<String, UpdateRepositoryRequest> getRepositories() {
        return repositories;
    }

    public Map<String, Object> getCfg() {
        return cfg;
    }

    @Override
    public String toString() {
        return "ProjectEntry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", repositories=" + repositories +
                ", cfg=" + cfg +
                '}';
    }
}
